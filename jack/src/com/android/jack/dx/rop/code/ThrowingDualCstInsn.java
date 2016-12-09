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
import com.android.jack.dx.rop.type.Type;
import com.android.jack.dx.rop.type.TypeList;

import javax.annotation.Nonnull;

/**
 * Instruction which contains two explicit references to constants
 * and which might throw an exception.
 */
public final class ThrowingDualCstInsn extends DualCstInsn {
  /** {@code non-null;} list of exceptions caught */
  @Nonnull
  private final TypeList catches;

  /**
   * Constructs an instance.
   *
   * @param opcode {@code non-null;} the opcode
   * @param position {@code non-null;} source position
   * @param sources {@code non-null;} specs for all the sources
   * @param catches {@code non-null;} list of exceptions caught
   * @param firstConstant {@code non-null;} the first constant
   * @param secondConstant {@code non-null;} the second constant
   */
  public ThrowingDualCstInsn(@Nonnull Rop opcode, @Nonnull SourcePosition position,
      @Nonnull RegisterSpecList sources, @Nonnull TypeList catches,
      @Nonnull Constant firstConstant, @Nonnull Constant secondConstant) {
    super(opcode, position, /* result = */ null, sources, firstConstant, secondConstant);

    assert opcode.getBranchingness() == Rop.BRANCH_THROW;
    assert catches != null;

    this.catches = catches;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public TypeList getCatches() {
    return catches;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String getInlineString() {
    return super.getInlineString() + " " + ThrowingInsn.toCatchString(getCatches());
  }

  /** {@inheritDoc} */
  @Override
  public void accept(@Nonnull Visitor visitor) {
    visitor.visitThrowingDualCstInsn(this);
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Insn withAddedCatch(Type type) {
    return new ThrowingDualCstInsn(getOpcode(), getPosition(), getSources(),
        getCatches().withAddedType(type), getFirstConstant(), getSecondConstant());
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Insn withRegisterOffset(int delta) {
    return new ThrowingDualCstInsn(getOpcode(), getPosition(), getSources().withOffset(delta),
        getCatches(), getFirstConstant(), getSecondConstant());
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Insn withNewRegisters(RegisterSpec result, RegisterSpecList sources) {
    return new ThrowingDualCstInsn(getOpcode(), getPosition(), sources, getCatches(),
        getFirstConstant(), getSecondConstant());
  }
}
