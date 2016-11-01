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

package com.android.jack.optimizations.cfg;

import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JCatchBasicBlock;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JEntryBasicBlock;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import javax.annotation.Nonnull;

/** Remove all unreachable basic blocks */
@Description("Remove all unreachable basic blocks")
@Transform(modify = JControlFlowGraph.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class RemoveUnreachableBasicBlocks
    implements RunnableSchedulable<JControlFlowGraph> {

  /** Returns `true` if the block should be removed */
  private boolean canRemoveBlock(@Nonnull JBasicBlock block) {
    return block.getPredecessors().size() == 0   // Block must have 0 predecessors
        && !(block instanceof JEntryBasicBlock)  // Never remove entry block
        && !(block instanceof JCatchBasicBlock); // Never remove catch block
  }

  @Override
  public void run(@Nonnull final JControlFlowGraph cfg) {
    // Collect all basic blocks without predecessors
    Queue<JBasicBlock> blocks = new LinkedList<>();
    for (JBasicBlock block : cfg.getBlocksDepthFirst(/* forward = */ false)) {
      if (canRemoveBlock(block)) {
        blocks.offer(block);
      }
    }

    while (!blocks.isEmpty()) {
      JBasicBlock block = blocks.remove();
      Set<JBasicBlock> successors = new HashSet<>(block.getSuccessors());
      block.detach();
      for (JBasicBlock successor : successors) {
        if (canRemoveBlock(successor)) {
          blocks.offer(successor);
        }
      }
    }
  }
}
