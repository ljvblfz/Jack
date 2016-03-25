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

import com.android.jack.backend.dex.rop.RopHelper;
import com.android.jack.dx.dex.file.ClassDefItem;
import com.android.jack.dx.dex.file.EncodedField;
import com.android.jack.dx.rop.code.AccessFlags;
import com.android.jack.dx.rop.cst.CstFieldRef;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.scheduling.marker.ClassDefItemMarker;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * Builds an {@code EncodedField} instance from a {@code JDefinedField} and adds it to
 * the {@code ClassDefItem} of its enclosing {@code JDeclaredType}.
 */
@Description("Builds EncodedField from JField")
@Name("EncodedFieldBuilder")
@Synchronized
@Constraint(need = ClassDefItemMarker.class)
@Transform(add = ClassDefItemMarker.Field.class, modify = ClassDefItemMarker.class)
@Protect(add = JField.class, modify = JField.class, remove = JField.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class EncodedFieldBuilder implements RunnableSchedulable<JField> {

  /**
   * Creates an {@code EncodedField} for the given {@code JField} and adds it
   * to the {@code ClassDefItem} of its {@code JDeclaredType}.
   *
   * <p>This {@code EncodedField} is added to one of the field set of {@code ClassDefItem} depending
   * on if it's static or not.
   *
   * <p>If this field belongs to an external type, it is ignored. In this case,
   * no {@code EncodedField} is created.
   */
  @Override
  public synchronized void run(@Nonnull JField field) throws Exception {
    JDefinedClassOrInterface declaringClass = field.getEnclosingType();

    ClassDefItemMarker classDefItemMarker =
        declaringClass.getMarker(ClassDefItemMarker.class);
    assert classDefItemMarker != null;

    ClassDefItem classDefItem = classDefItemMarker.getClassDefItem();
    assert classDefItem != null;
    EncodedField encodedField = createEncodedField(field);

    if (field.isStatic()) {
      JLiteral initialValue = field.getInitialValue();
      classDefItem.addStaticField(encodedField,
          initialValue != null ? new ConstantBuilder().parseLiteral(initialValue) : null);
    } else {
      assert field.getInitialValue() == null;
      classDefItem.addInstanceField(encodedField);
    }
  }

  @Nonnull
  private EncodedField createEncodedField(@Nonnull JField field) {
    CstFieldRef fieldRef = RopHelper.createFieldRef(field, field.getEnclosingType());

    return new EncodedField(fieldRef, getDxAccessFlags(field));
  }

  private static int getDxAccessFlags(@Nonnull JField field) {
    return field.getModifier() & AccessFlags.FIELD_FLAGS;
  }

}
