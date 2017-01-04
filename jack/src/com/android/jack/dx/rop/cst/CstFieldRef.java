/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.jack.dx.rop.cst;

import com.android.jack.dx.dex.file.ValueEncoder.ValueType;
import com.android.jack.dx.rop.type.Type;

import javax.annotation.Nonnull;

/**
 * Constants of type {@code CONSTANT_Fieldref_info}.
 */
public final class CstFieldRef extends CstMemberRef {

  /**
   * Constructs an instance.
   *
   * @param name {@code non-null;} the member reference name
   * @param descriptor {@code non-null;} the member reference descriptor
   */
  public CstFieldRef(@Nonnull CstType definingClass, @Nonnull CstString name,
      @Nonnull CstString descriptor) {
    super(definingClass, name, descriptor);
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String typeName() {
    return "field";
  }

  /**
   * Returns the type of this field.
   *
   * @return {@code non-null;} the field's type
   */
  @Override
  @Nonnull
  public Type getType() {
    return Type.intern(getDescriptor().getString());
  }

  /** {@inheritDoc} */
  @Override
  protected int compareTo0(Constant other) {
    int cmp = super.compareTo0(other);

    if (cmp != 0) {
      return cmp;
    }

    CstFieldRef otherField = (CstFieldRef) other;
    CstString thisDescriptor = getDescriptor();
    CstString otherDescriptor = otherField.getDescriptor();
    return thisDescriptor.compareTo(otherDescriptor);
  }

  @Override
  @Nonnull
  public ValueType getEncodedValueType() {
    return ValueType.VALUE_FIELD;
  }
}
