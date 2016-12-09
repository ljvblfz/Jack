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

package com.android.jack.dx.dex.code.form;

import com.android.jack.dx.dex.code.DalvInsn;
import com.android.jack.dx.dex.code.DualCstInsn;
import com.android.jack.dx.dex.code.InsnFormat;
import com.android.jack.dx.rop.code.RegisterSpecList;
import com.android.jack.dx.rop.cst.CstMethodRef;
import com.android.jack.dx.rop.cst.CstPrototypeRef;
import com.android.jack.dx.util.AnnotatedOutput;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Instruction format {@code 4rcc}. See the instruction format spec for details.
 */
public final class Form4rcc extends InsnFormat {
  /** {@code non-null;} unique instance of this class */
  @Nonnull
  public static final InsnFormat THE_ONE = new Form4rcc();

  /** The size is a number of 16-bit code units **/
  @Nonnegative
  private static final int CODE_SIZE = 4;

  /**
   * Constructs an instance. This class is not publicly instantiable. Use {@link #THE_ONE}.
   */
  private Form4rcc() {
    // This space intentionally left blank.
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String insnArgString(@Nonnull DalvInsn insn) {
    return regRangeString(insn.getRegisters()) + ", " + cstString((DualCstInsn) insn);
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String insnCommentString(@Nonnull DalvInsn insn, boolean noteIndices) {
    if (noteIndices) {
      return cstComment((DualCstInsn) insn);
    } else {
      return "";
    }
  }

  /** {@inheritDoc} */
  @Override
  @Nonnegative
  public int codeSize() {
    return CODE_SIZE;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isCompatible(@Nonnull DalvInsn insn) {
    if (!(insn instanceof DualCstInsn)) {
      return false;
    }

    DualCstInsn dci = (DualCstInsn) insn;

    if (!unsignedFitsInShort(dci.getFirstIndex())) {
      return false;
    }

    if (!(dci.getFirstConstant() instanceof CstMethodRef)) {
      return false;
    }

    if (!unsignedFitsInShort(dci.getSecondIndex())) {
      return false;
    }

    if (!(dci.getSecondConstant() instanceof CstPrototypeRef)) {
      return false;
    }

    RegisterSpecList regs = dci.getRegisters();

    return (regs.size() == 0) || (isRegListSequential(regs)
        && unsignedFitsInShort(regs.get(0).getReg()) && unsignedFitsInByte(regs.getWordCount()));
  }

  /** {@inheritDoc} */
  @Override
  public void writeTo(@Nonnull AnnotatedOutput out, @Nonnull DalvInsn insn) {
    RegisterSpecList regs = insn.getRegisters();
    int firstReg = (regs.size() == 0) ? 0 : regs.get(0).getReg();
    int count = regs.getWordCount();
    DualCstInsn dci = (DualCstInsn) insn;

    write(out, opcodeUnit(insn, count), (short) dci.getFirstIndex(), (short) firstReg,
        (short) dci.getSecondIndex());
  }
}
