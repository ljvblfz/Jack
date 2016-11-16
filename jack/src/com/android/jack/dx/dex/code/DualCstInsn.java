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

package com.android.jack.dx.dex.code;

import com.android.jack.dx.rop.code.RegisterSpecList;
import com.android.jack.dx.rop.code.SourcePosition;
import com.android.jack.dx.rop.cst.Constant;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Instruction which has two constant arguments in addition to all the normal instruction
 * information.
 */
public final class DualCstInsn extends FixedSizeInsn {
  /** {@code non-null;} the first constant argument for this instruction */
  @Nonnull
  private final Constant firstConstant;

  /** {@code non-null;} the second constant argument for this instruction */
  @Nonnull
  private final Constant secondConstant;

  /**
   * {@code >= -1;} the constant pool index for {@link #firstConstant}, or {@code -1} if not yet set
   */
  private int firstIndex;

  /**
   * {@code >= -1;} the constant pool index for {@link #secondConstant}, or {@code -1} if not yet
   * set
   */
  private int secondIndex;

  /**
   * Constructs an instance. The output address of this instance is initially unknown ({@code -1})
   * as is the constant pool index.
   *
   * @param opcode the opcode; one of the constants from {@link Dops}
   * @param position {@code non-null;} source position
   * @param registers {@code non-null;} register list, including a result register if appropriate
   *        (that is, registers may be either ins or outs)
   * @param firstConstant {@code non-null;} the first constant argument
   * @param secondConstant {@code non-null;} the second constant argument
   */
  public DualCstInsn(@Nonnull Dop opcode, @Nonnull SourcePosition position,
      @Nonnull RegisterSpecList registers, @Nonnull Constant firstConstant,
      @Nonnull Constant secondConstant) {
    super(opcode, position, registers);

    assert firstConstant != null;
    assert secondConstant != null;

    this.firstConstant = firstConstant;
    this.secondConstant = secondConstant;
    this.firstIndex = -1;
    this.secondIndex = -1;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public DalvInsn withOpcode(@Nonnull Dop opcode) {
    DualCstInsn result =
        new DualCstInsn(opcode, getPosition(), getRegisters(), firstConstant, secondConstant);

    if (firstIndex >= 0) {
      result.setFirstIndex(firstIndex);
    }

    if (secondIndex >= 0) {
      result.setSecondIndex(secondIndex);
    }

    return result;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public DalvInsn withRegisters(@Nonnull RegisterSpecList registers) {
    DualCstInsn result =
        new DualCstInsn(getOpcode(), getPosition(), registers, firstConstant, secondConstant);

    if (firstIndex >= 0) {
      result.setFirstIndex(firstIndex);
    }

    if (secondIndex >= 0) {
      result.setSecondIndex(secondIndex);
    }

    return result;
  }

  /**
   * Gets the first constant argument.
   *
   * @return {@code non-null;} the first constant argument
   */
  @Nonnull
  public Constant getFirstConstant() {
    return firstConstant;
  }

  /**
   * Gets the second constant argument.
   *
   * @return {@code non-null;} the second constant argument
   */
  @Nonnull
  public Constant getSecondConstant() {
    return secondConstant;
  }

  /**
   * Sets the first constant's index. It is only valid to call this method once per instance.
   *
   * @param index {@code >= 0;} the first constant pool index
   */
  public void setFirstIndex(@Nonnegative int index) {
    if (index < 0) {
      throw new IllegalArgumentException("index < 0");
    }

    if (this.firstIndex >= 0) {
      throw new RuntimeException("firstIndex already set");
    }

    this.firstIndex = index;
  }

  /**
   * Sets the second constant's index. It is only valid to call this method once per instance.
   *
   * @param index {@code >= 0;} the second constant pool index
   */
  public void setSecondIndex(@Nonnegative int index) {
    if (index < 0) {
      throw new IllegalArgumentException("index < 0");
    }

    if (this.secondIndex >= 0) {
      throw new RuntimeException("secondIndex already set");
    }

    this.secondIndex = index;
  }

  /**
   * Gets the first constant's index. It is only valid to call this after {@link #setFirstIndex} has
   * been called.
   *
   * @return {@code >= 0;} the first constant pool index
   */
  @Nonnegative
  public int getFirstIndex() {
    if (firstIndex < 0) {
      throw new RuntimeException("index not yet set for " + firstConstant);
    }

    return firstIndex;
  }

  /**
   * Gets the second constant's index. It is only valid to call this after {@link #setSecondIndex}
   * has been called.
   *
   * @return {@code >= 0;} the second constant pool index
   */
  @Nonnegative
  public int getSecondIndex() {
    if (secondIndex < 0) {
      throw new RuntimeException("index not yet set for " + secondConstant);
    }

    return secondIndex;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  protected String argString() {
    return firstConstant.toHuman() + ", " + secondConstant.toHuman();
  }
}
