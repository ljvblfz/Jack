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

package com.android.jack.dx.rop.cst;

import com.android.jack.dx.dex.file.ValueEncoder.ValueType;
import com.android.jack.dx.rop.type.Prototype;
import com.android.jack.dx.rop.type.Type;

import javax.annotation.Nonnull;

/**
 * Constants of type reference to a {@Link Prototype}.
 */
public final class CstPrototypeRef extends TypedConstant {

  /** {@code non-null;} the reference prototype */
  @Nonnull
  private final Prototype prototype;

  /**
   * Constructs an instance from a {@link CstPrototypeRef}.
   *
   * @param prototype {@code non-null;} the referenced prototype
   */
  public CstPrototypeRef(@Nonnull Prototype prototype) {
    assert prototype != null;
    this.prototype = prototype;
  }

  /** {@inheritDoc} */
  @Override
  public final boolean equals(@Nonnull Object other) {
    if (!(other instanceof CstPrototypeRef)) {
      return false;
    }

    return prototype.equals(((CstPrototypeRef) other).prototype);
  }

  /** {@inheritDoc} */
  @Override
  public final int hashCode() {
    return prototype.hashCode();
  }

  /** {@inheritDoc} */
  @Override
  protected int compareTo0(@Nonnull Constant other) {
    return prototype.compareTo(((CstPrototypeRef) other).prototype);
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String toString() {
    return typeName() + '{' + toHuman() + '}';
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String typeName() {
    return "prototype";
  }

  /** {@inheritDoc} */
  @Override
  public boolean isCategory2() {
    return false;
  }

  /**
   * Gets the referenced prototype. This doesn't include a {@code this} argument.
   *
   * @return {@code non-null;} the referenced prototype
   */
  @Nonnull
  public final Prototype getPrototype() {
    return prototype;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Type getType() {
    return prototype.getReturnType();
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String toHuman() {
    return prototype.toString();
  }

  @Override
  @Nonnull
  public ValueType getEncodedValueType() {
    return ValueType.VALUE_METHOD_TYPE;
  }
}
