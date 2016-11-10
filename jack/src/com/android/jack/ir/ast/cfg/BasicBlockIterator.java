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

  /** Iterates basic blocks depth first */
  public final void iterateDepthFirst(boolean forward) {
    Stack<JBasicBlock> stack = new Stack<>();
    Set<JBasicBlock> stacked = new HashSet<>();

    JBasicBlock start = forward ? cfg.getEntryBlock() : cfg.getExitBlock();
    stack.push(start);
    stacked.add(start);

    while (!stack.isEmpty()) {
      JBasicBlock block = stack.pop();
      assert stacked.contains(block);

      // Since we use stack to hold the list of the blocks to be processed, we
      // need to reverse successors lists to visit them not in reversed order
      List<JBasicBlock> blocks =
          forward ? block.getSuccessors() : block.getPredecessors();
      for (int i = blocks.size() - 1; i >= 0; i--) {
        JBasicBlock next = blocks.get(i);
        if (!stacked.contains(next)) {
          stack.push(next);
          stacked.add(next);
        }
      }

      if (!process(block)) {
        break;
      }
    }
  }
}
