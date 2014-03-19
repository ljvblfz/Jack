/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.backend.dex.rop;

import com.android.jack.dx.rop.code.BasicBlock;
import com.android.jack.dx.rop.code.BasicBlockList;
import com.android.jack.dx.rop.code.InsnList;
import com.android.jack.dx.util.IntList;

import java.util.ArrayList;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

class RopBasicBlockManager {

  /** label offset for the parameter assignment block */
  static final int PARAM_ASSIGNMENT = -1;
  /** label offset for the return block */
  static final int RETURN = -2;
  /** number of special label offsets */
  static final int SPECIAL_LABEL_COUNT = 7;
  /** max label (exclusive) of any original code block */
  @Nonnegative
  private final int maxLabel;
  /** output block list in-progress */
  @Nonnull
  private final ArrayList<BasicBlock> basicBlocks;

  RopBasicBlockManager(@Nonnegative int maxLabel) {
    this.maxLabel = maxLabel;

    /*
     * The "* 2 + 10" below is to conservatively believe that every block is an exception handler
     * target and should also take care of enough other possible extra overhead such that the
     * underlying array is unlikely to need resizing.
     */
    basicBlocks = new ArrayList<BasicBlock>(maxLabel * 2 + 10);
  }

  @Nonnull
  BasicBlock createBasicBlock(@Nonnegative int label, @Nonnull InsnList insns,
      @Nonnull IntList successors, int primarySuccessor) {
    BasicBlock bb = new BasicBlock(label, insns, successors, primarySuccessor);
    basicBlocks.add(bb);
    return bb;
  }

  @Nonnull
  BasicBlockList getBasicBlockList() {
    int sz = basicBlocks.size();
    BasicBlockList bbl = new BasicBlockList(sz);
    for (int i = 0; i < sz; i++) {
      bbl.set(i, basicBlocks.get(i));
    }
    bbl.setImmutable();
    return bbl;
  }

  /**
   * Gets the minimum label for unreserved use.
   *
   * @return the minimum label
   */
  @Nonnegative
  int getMinimumUnreservedLabel() {

    /*
     * The labels below ((maxLabel * 2) + SPECIAL_LABEL_COUNT) are reserved for particular uses.
     */
    return (maxLabel * 2) + RopBasicBlockManager.SPECIAL_LABEL_COUNT;
  }

  /**
   * Gets an arbitrary unreserved and available label.
   *
   * @return the label
   */
  @Nonnegative
  int getAvailableLabel() {
    int candidate = getMinimumUnreservedLabel();

    for (BasicBlock bb : basicBlocks) {
      int label = bb.getLabel();
      if (label >= candidate) {
        candidate = label + 1;
      }
    }

    return candidate;
  }

  /**
   * Gets the label for the given special-purpose block. The given label should be one of the static
   * constants defined by this class.
   *
   * @param label {@code < 0;} the special label constant
   * @return the actual label value to use
   */
  @Nonnegative
  int getSpecialLabel(int label) {
    assert label < 0 : "label is supposed to be negative";
    /*
     * The label is bitwise-complemented so that mistakes where LABEL is used instead of
     * getSpecialLabel(LABEL) cause a failure at block construction time, since negative labels are
     * illegal. We multiply maxLabel by 2 since 0..maxLabel (exclusive) are the original blocks and
     * maxLabel..(maxLabel*2) are reserved for exception handler setup blocks (see
     * getExceptionSetupLabel(), above).
     */
    return (maxLabel * 2) + ~label;
  }
}
