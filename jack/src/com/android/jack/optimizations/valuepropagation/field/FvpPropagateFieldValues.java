/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.optimizations.valuepropagation.field;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.optimizations.Optimizations;
import com.android.jack.optimizations.common.ExpressionReplaceHelper;
import com.android.jack.optimizations.common.JlsNullabilityChecker;
import com.android.jack.optimizations.common.OptimizerUtils;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.AppendBefore;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Field value propagation, final phase: for all single-valued fields replace reads in
 * methods and constructors with the literal value.
 */
@Description("Field value propagation, field reads substitution")
@Constraint(need = { FieldSingleValueMarker.class,
                     ThreeAddressCodeForm.class })
@Transform(add = JValueLiteral.class)
@Use({ ExpressionReplaceHelper.class,
       JlsNullabilityChecker.class })
@Name("FieldValuePropagation: PropagateFieldValues")
public class FvpPropagateFieldValues extends FvpSchedulable
    implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();
  @Nonnull
  private final JPhantomLookup phantomLookup = Jack.getSession().getPhantomLookup();

  private final boolean removeNullChecks = ThreadConfig.get(
      Optimizations.FieldValuePropagation.REMOVE_NULL_CHECKS).booleanValue();

  private final boolean ensureTypeInitializers = ThreadConfig.get(
      Optimizations.FieldValuePropagation.ENSURE_TYPE_INITIALIZERS).booleanValue();

  private class Visitor extends JVisitor {
    @Nonnull
    private final JMethod method;
    private final boolean insideConstructor;

    @CheckForNull
    private final JlsNullabilityChecker jlsNullabilityHelper;
    @Nonnull
    private final ExpressionReplaceHelper replaceHelper;
    @Nonnull
    public final TransformationRequest request;

    Visitor(@Nonnull JMethod method, boolean addNullChecks) {
      this.method = method;
      this.insideConstructor = OptimizerUtils.isConstructor(method);
      this.request = new TransformationRequest(method);

      LocalVarCreator fvp = new LocalVarCreator(method, "fvp");
      this.replaceHelper = new ExpressionReplaceHelper(fvp);
      this.jlsNullabilityHelper = addNullChecks ?
          new JlsNullabilityChecker(fvp, phantomLookup) : null;
    }

    @Override
    public void endVisit(@Nonnull JFieldRef ref) {
      replaceFieldWithValue(ref);
      super.endVisit(ref);
    }

    private void replaceFieldWithValue(@Nonnull JFieldRef ref) {
      if (OptimizerUtils.isAssigned(ref)) {
        return;
      }

      JField field = ref.getFieldId().getField();
      if (field == null) {
        return;
      }

      // Only process fields of types to be emitted
      JDefinedClassOrInterface type = field.getEnclosingType();
      if (!type.isToEmit()) {
        return;
      }

      // Only process tracked fields. Note: there may be valid reasons why a tracked field
      // might not have a marker yet, for example not initialized static fields if there
      // is no any assignment to this field and type static initializer is also missing.
      FieldSingleValueMarker marker = FieldSingleValueMarker.getOrCreate(field);
      if (marker == null || marker.isMultipleOrNonLiteralValue()) {
        return;
      }

      // Do not substitute field reads inside correspondent type
      // initializers where this field is being initialized
      if (insideConstructor &&
          type == method.getEnclosingType() &&
          method.isStatic() == field.isStatic()) {
        if (method.isStatic() || ref.getInstance() instanceof JThisRef) {
          // Field either is static or is instance and implicitly
          // or explicitly referenced via 'this' reference
          return;
        }
      }

      // In case this is a static field and the field is accessed not from the same
      // type, we don't propagate value if 'ensure-type-initializers' is true.
      if (field.isStatic() && ensureTypeInitializers &&
          field.getEnclosingType() != method.getEnclosingType()) {
        return;
      }

      JValueLiteral value = marker.getConsolidatedValue();
      if (value == null) {
        value = createDefaultValue(field); // Assume default value.
      } else {
        value = OptimizerUtils.cloneExpression(value);
        value.setSourceInfo(ref.getSourceInfo());
      }

      replaceHelper.replace(ref, value, request);
      tracer.getStatistic(FIELD_VALUES_PROPAGATED).incValue();

      if (jlsNullabilityHelper != null) {
        if (!field.isStatic()) {
          JExpression instance = ref.getInstance();
          assert instance != null;
          JStatement nullCheck = jlsNullabilityHelper
              .createNullCheckIfNeeded(instance, request);
          if (nullCheck != null) {
            JStatement stmt = instance.getParent(JStatement.class);
            request.append(new AppendBefore(stmt, nullCheck));
          }
        }
      }
    }
  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (preserveJls || method.isNative() || method.isAbstract()) {
      return;
    }

    Visitor visitor = new Visitor(method, !removeNullChecks);
    visitor.accept(method);
    visitor.request.commit();
  }
}
