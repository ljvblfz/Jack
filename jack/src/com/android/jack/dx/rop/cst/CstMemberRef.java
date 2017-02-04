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

import com.android.jack.dx.rop.type.Type;

import javax.annotation.Nonnull;

/**
 * Constants of type {@code CONSTANT_*ref_info}.
 */
public abstract class CstMemberRef extends TypedConstant {
  /** {@code non-null;} the type of the defining class */

  private final Type definingClass;

  /** {@code non-null;} the name */
  @Nonnull
  private final CstString name;

  /**
   * Constructs an instance.
   *
   * @param definingClass {@code non-null;} the type of the defining class
   * @param name {@code non-null;} the member reference name
   */
  /* package */ CstMemberRef(@Nonnull Type definingClass, @Nonnull CstString name) {
    assert definingClass != null;
    assert name != null;
    this.definingClass = definingClass;
    this.name = name;
  }

  /**
   * {@inheritDoc}
   *
   * <p><b>Note:</b> This implementation just compares the defining
   * class and name, and it is up to subclasses to compare the rest
   * after calling {@code super.compareTo0()}.</p>
   */
  @Override
  protected int compareTo0(Constant other) {
    CstMemberRef otherMember = (CstMemberRef) other;
    int cmp = definingClass.compareTo(otherMember.definingClass);

    if (cmp != 0) {
      return cmp;
    }

    return name.compareTo(otherMember.name);
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public final String toString() {
    return typeName() + '{' + toHuman() + '}';
  }

  /** {@inheritDoc} */
  @Override
  public final boolean isCategory2() {
    return false;
  }

  /**
   * Gets the type of the defining class.
   *
   * @return {@code non-null;} the type of defining class
   */
  @Nonnull
  public final Type getDefiningClass() {
    return definingClass;
  }

  @Nonnull
  public CstString getName() {
    return name;
  }
}
