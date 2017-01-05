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
import com.android.jack.dx.rop.type.Prototype;

import javax.annotation.Nonnull;

/**
 * Constants of type {@code CONSTANT_Methodref_info}.
 */
public final class CstMethodRef extends CstBaseMethodRef {

  /**
   * Constructs an instance.
   *
   * @param definingClass {@code non-null;} the type of the defining class
   * @param name {@code non-null;} the member reference name
   * @param prototype {@code non-null;} the member reference prototype
   */
  public CstMethodRef(@Nonnull CstType definingClass, @Nonnull CstString name,
      @Nonnull Prototype prototype) {
    super(definingClass, name, prototype);
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String typeName() {
    return "method";
  }

  @Override
  @Nonnull
  public ValueType getEncodedValueType() {
    return ValueType.VALUE_METHOD;
  }
}
