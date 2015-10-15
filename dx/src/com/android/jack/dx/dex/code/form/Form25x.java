/*
 * Copyright (C) 2015 The Android Open Source Project
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
import com.android.jack.dx.dex.code.InsnFormat;
import com.android.jack.dx.dex.code.SimpleInsn;
import com.android.jack.dx.rop.code.RegisterSpec;
import com.android.jack.dx.rop.code.RegisterSpecList;
import com.android.jack.dx.util.AnnotatedOutput;

import java.util.BitSet;

/**
 * Instruction format {@code 25x}. See the instruction format spec
 * for details.
 */
public final class Form25x extends Form35c {
  /** {@code non-null;} unique instance of this class */
  @SuppressWarnings("hiding")
  public static final InsnFormat THE_ONE = new Form25x();

  /**
   * Constructs an instance. This class is not publicly
   * instantiable. Use {@link #THE_ONE}.
   */
  private Form25x() {
    super();
    // This space intentionally left blank.
  }

  @Override
  public String insnArgString(DalvInsn insn) {
    RegisterSpecList regs = explicitize(insn.getRegisters());
    return regListString(regs);
  }

  @Override
  public String insnCommentString(DalvInsn insn, boolean noteIndices) {
    // This format has no comment.
    return "";
  }

  @Override
  public int codeSize() {
    return 2;
  }

  @Override
  public boolean isCompatible(DalvInsn insn) {
    if (!(insn instanceof SimpleInsn)) {
      return false;
    }

    RegisterSpecList regs = insn.getRegisters();
    return (wordCount(regs) >= 0);
  }

  @Override
  public BitSet compatibleRegs(DalvInsn insn) {
    RegisterSpecList regs = insn.getRegisters();
    int sz = regs.size();
    BitSet bits = new BitSet(sz);

    for (int i = 0; i < sz; i++) {
      RegisterSpec reg = regs.get(i);
      /*
       * The check below adds (category - 1) to the register, to
       * account for the fact that the second half of a
       * category-2 register has to be represented explicitly in
       * the result.
       */
      bits.set(i, unsignedFitsInNibble(reg.getReg() + reg.getCategory() - 1));
    }

    return bits;
  }

  @Override
  public void writeTo(AnnotatedOutput out, DalvInsn insn) {
    RegisterSpecList regs = explicitize(insn.getRegisters());
    int sz = regs.size();
    int r0 = (sz > 0) ? regs.get(0).getReg() : 0;
    int r1 = (sz > 1) ? regs.get(1).getReg() : 0;
    int r2 = (sz > 2) ? regs.get(2).getReg() : 0;
    int r3 = (sz > 3) ? regs.get(3).getReg() : 0;
    int r4 = (sz > 4) ? regs.get(4).getReg() : 0;

    // -1 because closure as first register should be managed in a specific way and not count into
    // registerCount
    write(out, opcodeUnit(insn, makeByte(r4, sz - 1)), // encode the fifth operand here
        codeUnit(r0, r1, r2, r3));
  }
}
