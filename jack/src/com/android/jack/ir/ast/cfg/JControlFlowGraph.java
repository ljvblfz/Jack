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
import java.util.List;
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
  public final JEntryBasicBlock entry() {
    return entry;
  }

  @Nonnull
  public final JExitBasicBlock exit() {
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

  @Override
  public void traverse(@Nonnull final JVisitor visitor) {
    // NOTE: forward depth-first basic blocks traversal
    if (visitor.visit(this)) {
      for (JBasicBlock block : getBlocksDepthFirst(true)) {
        visitor.accept(block);
      }
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(
      @Nonnull final ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    // NOTE: forward depth-first basic blocks traversal
    for (JBasicBlock block : getBlocksDepthFirst(true)) {
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
