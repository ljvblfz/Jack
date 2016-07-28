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

package com.android.jack.dx.ssa.back;

import com.android.jack.dx.rop.code.PlainInsn;
import com.android.jack.dx.rop.code.RegisterSpecList;
import com.android.jack.dx.rop.code.Rop;
import com.android.jack.dx.rop.code.Rops;
import com.android.jack.dx.ssa.NormalSsaInsn;
import com.android.jack.dx.ssa.SsaBasicBlock;
import com.android.jack.dx.ssa.SsaInsn;
import com.android.jack.dx.ssa.SsaMethod;
import com.android.jack.dx.util.BitIntSet;
import com.android.jack.dx.util.IntList;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Change a conditional branch to an unconditional branch if both successors eventually go to the
 * same target.
 *
 * <p>
 * Example in pseudo code:
 *
 * <pre>
 * if (a) { goto X; } else { goto Y; }
 * Y: goto X
 * </pre>
 *
 * Would turn into:
 *
 * <pre>
 * goto Y:
 * goto X:
 * </pre>
 *
 * <p>
 * Which will be further optimized by {@link IdenticalBlockCombiner}.
 */
class RedundantConditionalBranchRemover {

  @Nonnull
  private final SsaMethod method;

  @Nonnull
  private final List<SsaBasicBlock> blocks;

  RedundantConditionalBranchRemover(@Nonnull SsaMethod method) {
    this.method = method;
    this.blocks = method.getBlocks();
  }

  void process() {
    for (SsaBasicBlock block : blocks) {
      pruneRedundantConditionalBranch(block);
    }
  }

  private void pruneRedundantConditionalBranch(@Nonnull SsaBasicBlock block) {
    SsaInsn slast = block.getLastInsns();

    if (slast == null) {
      return;
    }

    int branchingness = slast.getOpcode().getBranchingness();
    if (branchingness != Rop.BRANCH_IF && branchingness != Rop.BRANCH_SWITCH) {
      return;
    }

    IntList successors = block.getSuccessorList().mutableCopy();
    if (successors.size() <= 1) {
      return;
    }

    // Step #1: Check the first branch. Follow its successor chain as long as they are a single
    // GOTO.
    int target = traceEmptyGoto(blocks.get(successors.get(0)));

    // Step #2: Find the transitive closure of single GOTO for all the other branches.
    for (int i = 1; i < successors.size(); i++) {
      SsaBasicBlock successor = blocks.get(successors.get(i));
      if (traceEmptyGoto(successor) != target) {
        // At least a branch does not go to the target compute previously in #1. We cannot switch
        // the conditional branch to unconditional.
        return;
      }
    }

    // Step #3: If we didn't bail out in #2, we know that all branches eventually jumps to the
    // target computed in #1. We update all of its successor to target.
    for (int i = 0, size = successors.size(); i < size; i++) {
      int successorIdx = successors.get(i);
      if (successorIdx != target) {
        block.replaceSuccessor(successorIdx, target);
      }
    }

    // Step #4: Finally change it to a single GOTO.
    NormalSsaInsn nLast = (NormalSsaInsn) slast;
    block.replaceLastInsn(new PlainInsn(Rops.GOTO, nLast.getOriginalRopInsn().getPosition(), null,
        RegisterSpecList.EMPTY));
  }

  /**
   * Trace, from a single starting block, to first successor that is not a block with a single GOTO
   * statement.
   *
   * @return The basic block index of such block. This could be the index of the parameter block if
   *         itself is not a single GOTO statement.
   */
  private int traceEmptyGoto(@Nonnull SsaBasicBlock block) {
    BitIntSet worklist = new BitIntSet(method.getBlocks().size());
    while (!worklist.has(block.getIndex())) {
      worklist.add(block.getIndex());
      if (!block.isSingleGoto()) {
        return block.getIndex();
      }
      block = block.getPrimarySuccessor();
    }
    return block.getIndex();
  }
}
