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
 * Constants of type {@code CONSTANT_NameAndType_info}.
 */
public final class CstNat extends Constant {

  /** {@code non-null;} the descriptor (type) */
  private final CstString descriptor;

  /**
   * Constructs an instance.
   *
   * @param descriptor {@code non-null;} the descriptor
   */
  public CstNat(CstString descriptor) {
    assert descriptor != null;
    this.descriptor = descriptor;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof CstNat)) {
      return false;
    }

    CstNat otherNat = (CstNat) other;
    return descriptor.equals(otherNat.descriptor);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return descriptor.hashCode();
  }

  /** {@inheritDoc} */
  @Override
  protected int compareTo0(Constant other) {
    CstNat otherNat = (CstNat) other;
    return descriptor.compareTo(otherNat.descriptor);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "nat{" + toHuman() + '}';
  }

  /** {@inheritDoc} */
  @Override
  public String typeName() {
    return "nat";
  }

  /** {@inheritDoc} */
  @Override
  public boolean isCategory2() {
    return false;
  }

  /**
   * Gets the descriptor.
   *
   * @return {@code non-null;} the descriptor
   */
  public CstString getDescriptor() {
    return descriptor;
  }

  /**
   * Returns an unadorned but human-readable version of the name-and-type
   * value.
   *
   * @return {@code non-null;} the human form
   */
  @Override
  public String toHuman() {
    return descriptor.toHuman();
  }

  /**
   * Gets the field type corresponding to this instance's descriptor.
   * This method is only valid to call if the descriptor in fact describes
   * a field (and not a method).
   *
   * @return {@code non-null;} the field type
   */
  public Type getFieldType() {
    return Type.intern(descriptor.getString());
  }

  @Override
  @Nonnull
  public ValueType getEncodedValueType() {
    throw new UnsupportedOperationException();
  }
}
