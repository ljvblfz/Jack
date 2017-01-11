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
import com.android.jack.annotations.DisableFieldValuePropagationOptimization;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.cfg.BasicBlockLiveProcessor;
import com.android.jack.ir.ast.cfg.JBasicBlockElement;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JGotoBlockElement;
import com.android.jack.ir.ast.cfg.JSimpleBasicBlock;
import com.android.jack.ir.ast.cfg.JThrowingExpressionBasicBlock;
import com.android.jack.ir.ast.cfg.JVariableAsgBlockElement;
import com.android.jack.ir.ast.cfg.mutations.BasicBlockBuilder;
import com.android.jack.ir.ast.cfg.mutations.CfgFragment;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.optimizations.Optimizations;
import com.android.jack.optimizations.cfg.CfgJlsNullabilityChecker;
import com.android.jack.optimizations.common.OptimizerUtils;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import javax.annotation.Nonnull;

/**
 * Field value propagation, final phase: for all single-valued fields replace reads in
 * methods and constructors with the literal value.
 */
@Description("Field value propagation, field reads substitution")
@Constraint(need = { FieldSingleValueMarker.class,
                     ThreeAddressCodeForm.class })
@Transform(add = JValueLiteral.class)
@Use(CfgJlsNullabilityChecker.class)
@Name("FieldValuePropagation: PropagateFieldValues")
public class FvpPropagateFieldValues extends FvpSchedulable
    implements RunnableSchedulable<JControlFlowGraph> {

  @Nonnull
  public final JAnnotationType disablingAnnotationType =
      Jack.getSession().getPhantomLookup().getAnnotationType(
          NamingTools.getTypeSignatureName(
              DisableFieldValuePropagationOptimization.class.getName()));

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();
  @Nonnull
  private final JPhantomLookup phantomLookup = Jack.getSession().getPhantomLookup();

  private final boolean preserveNullChecks = ThreadConfig.get(
      Optimizations.FieldValuePropagation.PRESERVE_NULL_CHECKS).booleanValue();
  private final boolean ensureTypeInitializers = ThreadConfig.get(
      Optimizations.FieldValuePropagation.ENSURE_TYPE_INITIALIZERS).booleanValue();

  @Override
  public void run(@Nonnull final JControlFlowGraph cfg) {
    final JMethod method = cfg.getMethod();
    final boolean insideConstructor = OptimizerUtils.isConstructor(method);
    final TransformationRequest request = new TransformationRequest(method);
    final CfgJlsNullabilityChecker nullChecker = preserveNullChecks ?
        new CfgJlsNullabilityChecker(cfg,
            new LocalVarCreator(method, "fvp"), phantomLookup) : null;

    new BasicBlockLiveProcessor(cfg, /* stepIntoElements: */ false) {
      @Override
      public boolean visit(@Nonnull JThrowingExpressionBasicBlock block) {
        JBasicBlockElement element = block.getLastElement();
        if (element instanceof JVariableAsgBlockElement &&
            ((JVariableAsgBlockElement) element).isFieldLoad()) {
          handle((JVariableAsgBlockElement) element);
        }
        return false;
      }

      private void handle(@Nonnull JVariableAsgBlockElement element) {
        JFieldRef ref = (JFieldRef) element.getValue();
        if (OptimizerUtils.isAssigned(ref)) {
          return;
        }

        JField field = ref.getFieldId().getField();
        if (field == null ||
            !field.getAnnotations(disablingAnnotationType).isEmpty() ||
            !field.getEnclosingType().getAnnotations(disablingAnnotationType).isEmpty()) {
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

        // #1: Split the block to move all but the last element
        //     into a separate simple block.
        JThrowingExpressionBasicBlock secondBlock =
            (JThrowingExpressionBasicBlock) element.getBasicBlock();
        JSimpleBasicBlock firstBlock = secondBlock.split(-1);

        // #2: Insert null-checks if needed.
        if (nullChecker != null && !field.isStatic()) {
          // The null-check is needed, we insert it in between the two blocks
          JExpression instance = ref.getInstance();
          assert instance != null;
          assert !instance.canThrow();

          CfgFragment nullCheckFragment =
              nullChecker.createNullCheck(element.getEHContext(), instance, request);

          nullCheckFragment.insert(firstBlock, secondBlock);
        }

        // #3: Schedule value replace request
        request.append(new Replace(ref, value));
        tracer.getStatistic(FIELD_VALUES_PROPAGATED).incValue();

        // #4: Turn the second block into a simple block in case it does not throw
        if (!value.canThrow()) {
          JSimpleBasicBlock newSecondBlock =
              new BasicBlockBuilder(cfg)
                  .append(secondBlock)
                  .append(new JGotoBlockElement(
                      SourceInfo.UNKNOWN, secondBlock.getLastElement().getEHContext()))
                  .createSimpleBlock(secondBlock.getPrimarySuccessor());
          secondBlock.detach(newSecondBlock);
        }

        // #5: Merge the first block into its primary successor
        firstBlock.mergeIntoSuccessor();
      }
    }.process();

    request.commit();
  }
}
