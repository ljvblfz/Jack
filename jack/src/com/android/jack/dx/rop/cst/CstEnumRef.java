/*
 * Copyright (C) 2008 The Android Open Source Project
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
 * Constant type to represent a reference to a particular constant
 * value of an enumerated type.
 */
public final class CstEnumRef extends CstMemberRef {
  /** {@code null-ok;} the corresponding field ref, lazily initialized */
  private CstFieldRef fieldRef;

  /**
   * Constructs an instance.
   *
   * @param name {@code non-null;} the member reference name
   * @param type {@code non-null;} the member reference type
   */
  public CstEnumRef(@Nonnull CstString name, @Nonnull Type type) {
    super(type, name);

    fieldRef = null;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String typeName() {
    return "enum";
  }

  /**
   * {@inheritDoc}
   *
   * <b>Note:</b> This returns the enumerated type.
   */
  @Override
  @Nonnull
  public Type getType() {
    return getDefiningClass();
  }

  /**
   * Get a {@link CstFieldRef} that corresponds with this instance.
   *
   * @return {@code non-null;} the corresponding field reference
   */
  @Nonnull
  public CstFieldRef getFieldRef() {
    if (fieldRef == null) {
      fieldRef = new CstFieldRef(getDefiningClass(), getName(), getDefiningClass());
    }

    return fieldRef;
  }

  @Override
  @Nonnull
  public ValueType getEncodedValueType() {
    return ValueType.VALUE_ENUM;
  }

  /** {@inheritDoc} */
  @Override
  public final boolean equals(Object other) {
    if ((other == null) || (getClass() != other.getClass())) {
      return false;
    }

    CstEnumRef otherRef = (CstEnumRef) other;
    return getDefiningClass().equals(otherRef.getDefiningClass())
        && getName().equals(otherRef.getName()) && getType().equals(otherRef.getType());
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
    return getDefiningClass().toHuman() + '.' + getName().toHuman() + ':' + getType().toHuman();
  }
}
