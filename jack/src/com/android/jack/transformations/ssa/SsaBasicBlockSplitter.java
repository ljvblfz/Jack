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

package com.android.jack.transformations.ssa;

import com.google.common.collect.Lists;

import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JBasicBlockElement;
import com.android.jack.ir.ast.cfg.JCaseBasicBlock;
import com.android.jack.ir.ast.cfg.JCatchBasicBlock;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JEntryBasicBlock;
import com.android.jack.ir.ast.cfg.JExitBasicBlock;
import com.android.jack.ir.ast.cfg.JSimpleBasicBlock;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * Split basic blocks for Phi element insertion.
 */
@Description("Split basic blocks for Phi element insertion.")
@Name("SsaBasicBlockSplitter")
@Transform(add = {SsaBasicBlockSplitterMarker.class},
    modify = {JControlFlowGraph.class})
@Filter(TypeWithoutPrebuiltFilter.class)
public class SsaBasicBlockSplitter implements RunnableSchedulable<JControlFlowGraph> {

  @Override
  public void run(@Nonnull JControlFlowGraph cfg) {
    assert cfg.getMarker(SsaBasicBlockSplitterMarker.class) == null;
    edgeSplit(cfg);
    cfg.addMarker(SsaBasicBlockSplitterMarker.INSTANCE);
  }

  /**
   * Extracts control flow graph from method and perform edge split in neccessary.
   */
  private void edgeSplit(@Nonnull JControlFlowGraph cfg) {
    edgeSplitPredecessors(cfg);
    edgeSplitMoveExceptionsAndResults(cfg);
    edgeSplitSuccessors(cfg);
  }

  /**
   * Inserts Z nodes as new predecessors for every node that has multiple successors and multiple
   * predecessors.
   */
  private void edgeSplitPredecessors(JControlFlowGraph cfg) {
    for (JBasicBlock block : cfg.getInternalBlocksUnordered()) {
      if (nodeNeedsUniquePredecessor(block)) {
        block.split(0);
      }
    }
  }

  /**
   * @param block {@code non-null;} block in question
   * @return {@code true} if this node needs to have a unique predecessor created for it
   */
  private static boolean nodeNeedsUniquePredecessor(JBasicBlock block) {
    if (block instanceof JExitBasicBlock) {
      throw new RuntimeException("exit bblock doesn't need unique pred");
    }
    /*
     * Any block with that has both multiple successors and multiple predecessors needs a new
     * predecessor node.
     */
    int countPredecessors = block.getPredecessorCount();
    int countSuccessors = block.getSuccessors().size();
    boolean needsUniquePredecessor = countPredecessors > 1 && countSuccessors > 1;
    assert !needsUniquePredecessor
        || !(block instanceof JCaseBasicBlock || block instanceof JCatchBasicBlock);
    return needsUniquePredecessor;
  }

  private static void edgeSplitMoveExceptionsAndResults(JControlFlowGraph cfg) {
    /*
     * New blocks are added to the end of the block list during this iteration.
     */
    for (JBasicBlock block : cfg.getInternalBlocksUnordered()) {
      /*
       * Any block that starts with a move-exception and has more than one predecessor...
       */
      if (!(block instanceof JExitBasicBlock) && block.getPredecessorCount() > 1
          && (block instanceof JCatchBasicBlock)) {
        for (JBasicBlock predecessor : Lists.newArrayList(block.getPredecessors())) {
          insertNewSimpleSuccessor(predecessor, block);
        }
      }
    }
  }

  /**
   * Inserts Z nodes for every node that needs a new successor.
   *
   */
  private static void edgeSplitSuccessors(JControlFlowGraph cfg) {
    /*
     * New blocks are added to the end of the block list during this iteration.
     */
    for (JBasicBlock block : cfg.getInternalBlocksUnordered()) {
      // Successors list is modified in loop below.
      for (JBasicBlock succ : block.getSuccessors()) {
        if (needsNewSuccessor(block, succ)) {
          // These two type of basic block requires special case handling. Otherwise, we might
          // up with an IR that is very difficult to understand. Please refer to the design for
          // detail information.
          if (succ instanceof JCatchBasicBlock || succ instanceof JCaseBasicBlock) {
            block.split(-1);
          } else {
            insertNewSimpleSuccessor(block, succ);
          }
        }
      }
    }
  }

  /**
   * Returns {@code true} if block and successor need a Z-node between them. Presently, this is
   * {@code true} if the final instruction has any sources or results and the current successor
   * block has more than one predecessor.
   *
   * @param block predecessor node
   * @param succ successor node
   * @return {@code true} if a Z node is needed
   */
  private static boolean needsNewSuccessor(JBasicBlock block, JBasicBlock succ) {
    if (block instanceof JEntryBasicBlock) {
      return false;
    }
    if (block.getElementCount() == 0) {
      return false;
    }
    if (succ instanceof JExitBasicBlock) {
      return false;
    }
    JBasicBlockElement lastInsn = block.getLastElement();
    int uvCount = SsaUtil.getUsedVariables(lastInsn).size();
    int dvCount = SsaUtil.getDefinedVariable(lastInsn) == null ? 0 : 1;
    return (uvCount + dvCount > 0) && succ.getPredecessorCount() > 1;
  }

  /**
   * Insert a new successor between a block and one of its successor.
   */
  private static void insertNewSimpleSuccessor(JBasicBlock block, JBasicBlock other) {
    JSimpleBasicBlock newSucc = new JSimpleBasicBlock(block.getCfg(), other);
    block.replaceAllSuccessors(other, newSucc);
  }
}
