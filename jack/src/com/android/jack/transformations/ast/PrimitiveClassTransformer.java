/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.transformations.ast;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.ast.FieldKind;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.AnnotationSkipperVisitor;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.With;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * Replaces {@link JPrimitiveType}.class by an access field to TYPE of the corresponding class.
 */
@Description("Replaces {@link JPrimitiveType}.class by an access field to " +
    "TYPE of the corresponding class.")
@Name("PrimitiveClassTransformer")
@Constraint(need = {JClassLiteral.class, OriginalNames.class})
@Transform(
    add = {JFieldRef.class}, remove = {JPrimitiveClassLiteral.class, ThreeAddressCodeForm.class})
@Protect(add = JClassLiteral.class, unprotect = @With(add = JPrimitiveClassLiteral.class))
public class PrimitiveClassTransformer implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends AnnotationSkipperVisitor {

    private static final String FIELD_TYPE_NAME = "TYPE";
    @Nonnull
    private final TransformationRequest tr;

    public Visitor(TransformationRequest tr) {
      this.tr = tr;
    }

    @Override
    public boolean visit(@Nonnull JClassLiteral classLiteral) {
      if (classLiteral.getRefType() instanceof JPrimitiveType) {
        JClass receiverType = getType((JPrimitiveType) classLiteral.getRefType());
        JFieldRef fieldAccess = new JFieldRef(classLiteral.getSourceInfo(),
            null, receiverType.getFieldId(FIELD_TYPE_NAME,
                Jack.getProgram().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_CLASS),
                FieldKind.STATIC), receiverType);
        tr.append(new Replace(classLiteral, fieldAccess));
      }
      return super.visit(classLiteral);
    }

    // TODO(mikaelpeltier): Remove getType when getWrapperType method into JPrimitiveType will be
    // available.
    @Nonnull
    private JClass getType(@Nonnull JPrimitiveType primType) {
      JPhantomLookup lookup = Jack.getProgram().getPhantomLookup();
      switch (primType.getPrimitiveTypeEnum()) {
        case BOOLEAN:
          return lookup.getClass(CommonTypes.JAVA_LANG_BOOLEAN);
        case BYTE:
          return lookup.getClass(CommonTypes.JAVA_LANG_BYTE);
        case CHAR:
          return lookup.getClass(CommonTypes.JAVA_LANG_CHAR);
        case SHORT:
          return lookup.getClass(CommonTypes.JAVA_LANG_SHORT);
        case INT:
          return lookup.getClass(CommonTypes.JAVA_LANG_INTEGER);
        case FLOAT:
          return lookup.getClass(CommonTypes.JAVA_LANG_FLOAT);
        case DOUBLE:
          return lookup.getClass(CommonTypes.JAVA_LANG_DOUBLE);
        case LONG:
          return lookup.getClass(CommonTypes.JAVA_LANG_LONG);
        case VOID:
          return lookup.getClass(CommonTypes.JAVA_LANG_VOID);
      }

      throw new AssertionError();
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    JDefinedClassOrInterface enclosingType = method.getEnclosingType();
    if (enclosingType.isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    Visitor visitor = new Visitor(tr);
    visitor.accept(method);
    tr.commit();
  }
}