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

import com.google.common.collect.Lists;

import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBodyCfg;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import javax.annotation.Nonnull;

/** Represents method control flow graph */
@Description("Control Flow Graph")
public final class JControlFlowGraph extends JNode {
  @Nonnull
  private final JEntryBasicBlock entry;
  @Nonnull
  private final JExitBasicBlock exit;

  public JControlFlowGraph(@Nonnull SourceInfo info) {
    super(info);
    this.exit = new JExitBasicBlock(this);
    this.entry = new JEntryBasicBlock(this, exit);
  }

  @Nonnull
  public final JEntryBasicBlock getEntryBlock() {
    return entry;
  }

  @Nonnull
  public final JExitBasicBlock getExitBlock() {
    return exit;
  }

  @Nonnull
  public JMethod getMethod() {
    return getMethodBody().getMethod();
  }

  @Nonnull
  public JMethodBodyCfg getMethodBody() {
    JNode parent = getParent();
    assert parent instanceof JMethodBodyCfg;
    return (JMethodBodyCfg) parent;
  }

  @Nonnull
  public List<JBasicBlock> getBlocksDepthFirst(boolean forward) {
    final List<JBasicBlock> blocks = new ArrayList<>();
    new BasicBlockIterator() {
      @Override public boolean process(@Nonnull JBasicBlock block) {
        blocks.add(block);
        return true;
      }

      @Nonnull @Override public JControlFlowGraph getCfg() {
        return JControlFlowGraph.this;
      }
    }.iterateDepthFirst(forward);
    return blocks;
  }

  /**
   * Returns all basic blocks reachable from entry or exit block via
   * successor/predecessor edges. Note that some of the blocks may not be
   * returned by getBlocksDepthFirst(true) or getBlocksDepthFirst(false).
   *
   * The blocks are returned in stable order, i.e. two calls to this method
   * will return the same sequence of the blocks.
   **/
  @Nonnull
  public List<JBasicBlock> getAllBlocksUnordered() {
    Set<JBasicBlock> blocks = new LinkedHashSet<>();
    Queue<JBasicBlock> queue = new LinkedList<>(); // Contains duplicates

    queue.offer(this.getEntryBlock());
    queue.offer(this.getExitBlock());

    while (!queue.isEmpty()) {
      JBasicBlock block = queue.remove();
      if (!blocks.contains(block)) {
        blocks.add(block);
        for (JBasicBlock successor : block.getSuccessors()) {
          if (!blocks.contains(successor)) {
            queue.offer(successor);
          }
        }
        for (JBasicBlock predecessor : block.getPredecessors()) {
          if (!blocks.contains(predecessor)) {
            queue.offer(predecessor);
          }
        }
      }
    }

    return Lists.newArrayList(blocks);
  }

  /** Traverses the the blocks in the order provided by `getAllBlocksUnordered()` */
  @Override
  public void traverse(@Nonnull final JVisitor visitor) {
    if (visitor.visit(this)) {
      for (JBasicBlock block : getAllBlocksUnordered()) {
        visitor.accept(block);
      }
    }
    visitor.endVisit(this);
  }

  /** Traverses the the blocks in the order provided by `getAllBlocksUnordered()` */
  @Override
  public void traverse(
      @Nonnull final ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    for (JBasicBlock block : getAllBlocksUnordered()) {
      block.traverse(schedule);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest request) throws Exception {
    visitor.visit(this, request);
  }

  @Override
  public void checkValidity() {
  }
}
