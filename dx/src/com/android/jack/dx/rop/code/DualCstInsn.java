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

package com.android.jack.dx.rop.code;

import com.android.jack.dx.rop.cst.Constant;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Instruction which contains two explicit references to constants.
 */
public abstract class DualCstInsn extends Insn {

  /** {@code non-null;} the first constant */
  @Nonnull
  private final Constant firstConstant;

  /** {@code non-null;} the second constant */
  @Nonnull
  private final Constant secondConstant;

  /**
   * Constructs an instance.
   *
   * @param opcode {@code non-null;} the opcode
   * @param position {@code non-null;} source position
   * @param result {@code null-ok;} spec for the result, if any
   * @param sources {@code non-null;} specs for all the sources
   * @param firstConstant {@code non-null;} the first constant
   * @param secondConstant {@code non-null;} the second constant
   */
  public DualCstInsn(@Nonnull Rop opcode, @Nonnull SourcePosition position,
      @CheckForNull RegisterSpec result, @Nonnull RegisterSpecList sources,
      @Nonnull Constant firstConstant, @Nonnull Constant secondConstant) {
    super(opcode, position, result, sources);

    assert firstConstant != null;
    assert secondConstant != null;

    this.firstConstant = firstConstant;
    this.secondConstant = secondConstant;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String getInlineString() {
    return firstConstant.toHuman() + ", " + secondConstant.toHuman();
  }

  /**
   * Gets the first constant.
   *
   * @return {@code non-null;} the first constant
   */
  @Nonnull
  public Constant getFirstConstant() {
    return firstConstant;
  }

  /**
   * Gets the second constant.
   *
   * @return {@code non-null;} the second constant
   */
  @Nonnull
  public Constant getSecondConstant() {
    return secondConstant;
  }

  /** {@inheritDoc} */
  @Override
  public boolean contentEquals(Insn b) {
    /*
     * The cast (CstInsn)b below should always succeed since
     * Insn.contentEquals compares classes of this and b.
     */
    return super.contentEquals(b) && firstConstant.equals(((DualCstInsn) b).getFirstConstant())
        && secondConstant.equals(((DualCstInsn) b).getSecondConstant());
  }
}
