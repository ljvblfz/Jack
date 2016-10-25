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

import com.android.jack.ir.ast.JVisitor;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Simplifies iterations through basic blocks of possibly *mutating* CFG. Since the CFG
 * may be mutated during iteration, does not guarantee any particular iteration order.
 *
 * If new basic blocks are added while mutating CFG, it is possible to add these
 * newly created blocks with `enqueue(...)`.
 */
public abstract class BasicBlockLiveProcessor extends JVisitor {
  @Nonnull
  private final Queue<JBasicBlock> queue = new LinkedList<>();
  @Nonnull
  private final Set<JBasicBlock> everQueued = new HashSet<>();
  private final boolean stepIntoElements;

  public BasicBlockLiveProcessor(boolean stepIntoElements) {
    this.stepIntoElements = stepIntoElements;
    new BasicBlockIterator() {
      @Override
      public boolean process(@Nonnull JBasicBlock block) {
        enqueue(block);
        return true;
      }

      @Nonnull
      @Override
      public JControlFlowGraph getCfg() {
        return BasicBlockLiveProcessor.this.getCfg();
      }
    }.iterateDepthFirst(true);
  }

  /** Enqueues the element if it was not queued before */
  protected void enqueue(@Nonnull JBasicBlock block) {
    if (!everQueued.contains(block)) {
      everQueued.add(block);
      queue.add(block);
    }
  }

  @Override
  public boolean visit(@Nonnull JBasicBlock basicBlock) {
    return stepIntoElements;
  }

  @Override
  public boolean visit(@Nonnull JBasicBlockElement element) {
    return false;
  }

  /** Get control flow graph to work on */
  @Nonnull
  public abstract JControlFlowGraph getCfg();

  /** Process the blocks */
  public final void process() {
    while (!queue.isEmpty()) {
      this.accept(queue.remove());
    }
  }
}
