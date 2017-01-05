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

  /** {@code non-null;} the field type */
  @Nonnull
  private final Type type;

  /**
   * Constructs an instance.
   *
   * @param definingClass {@code non-null;} the type of the defining class
   * @param name {@code non-null;} the member reference name
   * @param type {@code non-null;} the member reference type
   */
  public CstFieldRef(@Nonnull Type definingClass, @Nonnull CstString name,
      @Nonnull Type type) {
    super(definingClass, name);
    this.type = type;

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
    return type;
  }

  /** {@inheritDoc} */
  @Override
  protected int compareTo0(Constant other) {
    int cmp = super.compareTo0(other);

    if (cmp != 0) {
      return cmp;
    }

    CstFieldRef otherField = (CstFieldRef) other;
    return type.compareTo(otherField.type);
  }

  @Override
  @Nonnull
  public ValueType getEncodedValueType() {
    return ValueType.VALUE_FIELD;
  }

  /** {@inheritDoc} */
  @Override
  public final boolean equals(Object other) {
    if ((other == null) || (getClass() != other.getClass())) {
      return false;
    }

    CstFieldRef otherRef = (CstFieldRef) other;
    return getDefiningClass().equals(otherRef.getDefiningClass())
        && getName().equals(otherRef.getName()) && type.equals(otherRef.type);
  }

  /** {@inheritDoc} */
  @Override
  public final int hashCode() {
    return ((getDefiningClass().hashCode() * 31) + getName().hashCode() * 31)
        + getType().hashCode();
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public final String toHuman() {
    return getDefiningClass().toHuman() + '.' + getName().toHuman() + ':' + type.toHuman();
  }
}
