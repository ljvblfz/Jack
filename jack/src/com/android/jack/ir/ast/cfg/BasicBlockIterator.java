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

package com.android.jack.ir.ast.cfg;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import javax.annotation.Nonnull;

/** Simplifies iterations through basic blocks */
public abstract class BasicBlockIterator {
  @Nonnull
  private final JControlFlowGraph cfg;

  protected BasicBlockIterator(@Nonnull JControlFlowGraph cfg) {
    this.cfg = cfg;
  }

  /** Processes basic block, returns `false` if the iterations should be stopped */
  public abstract boolean process(@Nonnull JBasicBlock block);

  /**
   * Iterates basic blocks depth first with weak references. Weakly referenced
   * blocks are iterated after regular referenced blocks.
   */
  public final void iterateDepthFirst() {
    Stack<JBasicBlock> blocksStack = new Stack<>();
    Set<JBasicBlock> blocksEverStacked = new HashSet<>();

    Stack<ExceptionHandlingContext> ehcStack = new Stack<>();
    Set<ExceptionHandlingContext> ehcEverStacked = Sets.newIdentityHashSet();

    JBasicBlock start = cfg.getEntryBlock();
    blocksStack.push(start);
    blocksEverStacked.add(start);

    while (true) {
      if (!blocksStack.isEmpty()) {
        // Block queue is not empty, process the next block
        JBasicBlock block = blocksStack.pop();
        assert blocksEverStacked.contains(block);
        enqueueBlocks(block.getSuccessors(), blocksStack, blocksEverStacked);

        // Compute the list of exception handling contexts
        for (JBasicBlockElement element : block.getElements(/* forward: */ false)) {
          ExceptionHandlingContext ehc = element.getEHContext();
          if (!ehcEverStacked.contains(ehc)) {
            ehcStack.push(ehc);
            ehcEverStacked.add(ehc);
          }
        }

        if (!process(block)) {
          break;
        }

      } else if (!ehcStack.isEmpty()) {
        // Otherwise, if the block queue is empty fetch check blocks from
        // the next exception handling context
        ExceptionHandlingContext ehc = ehcStack.pop();
        assert ehcEverStacked.contains(ehc);
        enqueueBlocks(ehc.getCatchBlocks(), blocksStack, blocksEverStacked);

      } else {
        // Both stacks are empty
        break;
      }
    }
  }

  private void enqueueBlocks(@Nonnull List<? extends JBasicBlock> blocks,
      @Nonnull Stack<JBasicBlock> blocksStack, @Nonnull Set<JBasicBlock> blocksEverStacked) {
    // Since we use stack to hold the list of the blocks to be processed, we
    // need to reverse successors lists to visit them not in reversed order
    for (int i = blocks.size() - 1; i >= 0; i--) {
      JBasicBlock next = blocks.get(i);
      if (!blocksEverStacked.contains(next)) {
        blocksStack.push(next);
        blocksEverStacked.add(next);
      }
    }
  }
}
