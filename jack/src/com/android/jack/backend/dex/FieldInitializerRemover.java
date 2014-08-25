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

package com.android.jack.backend.dex;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReferenceType;
import com.android.jack.ir.ast.JType;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;

import javax.annotation.Nonnull;

/**
 * Removes {@link JFieldInitializer} and replace them by assign or initial value.
 */
@HasKeyId
@Description("Removes JFieldInitializer")
@Synchronized
@Constraint(need = JFieldInitializer.class)
@Transform(add = {JAsgOperation.NonReusedAsg.class, JExpressionStatement.class},
    remove = {JFieldInitializer.class, ThreeAddressCodeForm.class})
public class FieldInitializerRemover implements RunnableSchedulable<JField> {

  @Nonnull
  public static final BooleanPropertyId CLASS_AS_INITIALVALUE = BooleanPropertyId.create(
      "jack.legacy.dx.initialvalue.class",
      "Emit class literal as initial value of field").addDefaultValue(Boolean.TRUE);

  @Nonnull
  public static final BooleanPropertyId STRING_AS_INITIALVALUE_OF_OBJECT = BooleanPropertyId.create(
      "jack.legacy.runtime.initialvalue.string",
      "Emit string literal as initial value of field").addDefaultValue(Boolean.TRUE);

  private final boolean allowClassInInitialValue =
      ThreadConfig.get(CLASS_AS_INITIALVALUE).booleanValue();
  private final boolean allowStringAsObjectInit =
      ThreadConfig.get(STRING_AS_INITIALVALUE_OF_OBJECT).booleanValue();

  @Nonnull
  private final JClass stringType =
    Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_STRING);

  @Override
  public synchronized void run(@Nonnull JField field) throws Exception {
    JFieldInitializer declaration = field.getFieldInitializer();
    if (declaration != null) {
      JExpression initialValue = declaration.getInitializer();
      TransformationRequest tr = new TransformationRequest(declaration.getParent());
      if (/* Field is static final and initialized by a literal */
          field.isStatic() && field.isFinal() && initialValue instanceof JLiteral
          /* Object field initialized by a String literal: don't remove unless allowed */
          && (allowStringAsObjectInit
              || field.getType().isSameType(stringType)
              || !(initialValue instanceof JAbstractStringLiteral))
          /* Field initialized by a class literal: don't remove unless allowed */
          && (allowClassInInitialValue || !(initialValue instanceof JClassLiteral))
          /* Auto boxing: don't remove */
          && !hasBoxing(field.getType(), (JLiteral) initialValue)) {
        field.setInitialValue((JLiteral) initialValue);
        field.setFieldInitializer(null);
        tr.append(new Remove(declaration));
      } else {
        JBinaryOperation assign = new JAsgOperation(declaration.getSourceInfo(),
            declaration.getFieldRef(), initialValue);
        tr.append(new Replace(declaration, assign.makeStatement()));
      }
      tr.commit();
    }
  }

  private boolean hasBoxing(@Nonnull JType fieldType, @Nonnull JLiteral initialValue) {
    JType valueType = initialValue.getType();

    if (valueType instanceof JPrimitiveType && fieldType instanceof JReferenceType) {
      assert isCompatible(fieldType, initialValue, valueType);
      return true;
    }

    return false;
  }

  private boolean isCompatible(
      @Nonnull JType fieldType, @Nonnull JLiteral initialValue, @Nonnull JType valueType) {
    boolean requiredBoxing = ((JPrimitiveType) valueType).isEquivalent(fieldType);

    if (requiredBoxing == false && initialValue instanceof JIntLiteral) {
      int value = ((JIntLiteral) initialValue).getValue();
      requiredBoxing |= (JPrimitiveTypeEnum.BYTE.getType().isEquivalent(fieldType)
          && value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE);
      requiredBoxing |= (JPrimitiveTypeEnum.SHORT.getType().isEquivalent(fieldType)
          && value >= Short.MIN_VALUE && value <= Short.MAX_VALUE);
      requiredBoxing |= (JPrimitiveTypeEnum.CHAR.getType().isEquivalent(fieldType)
          && value >= Character.MIN_VALUE && value <= Character.MAX_VALUE);
    }

    return requiredBoxing;
  }
}
