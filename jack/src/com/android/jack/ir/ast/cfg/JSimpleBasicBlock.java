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

import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.ast.JVisitor;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.List;
import javax.annotation.Nonnull;

/** Represents a simple blocks with just one successor ending with GOTO. */
public final class JSimpleBasicBlock extends JRegularBasicBlock {
  public JSimpleBasicBlock(@Nonnull JControlFlowGraph cfg, @Nonnull JBasicBlock primary) {
    super(primary);
    updateParents(cfg);
  }

  @Override
  public boolean hasPrimarySuccessor() {
    return true;
  }

  /** Merges the block into its primary successor, returns the successor */
  @Nonnull
  public JBasicBlock mergeIntoSuccessor() {
    JBasicBlock successor = getPrimarySuccessor();
    if (successor.getPredecessorCount() != 1) {
      // Make sure we never merge into the block with multiple predecessors
      throw new AssertionError();
    }

    // Move children
    List<JBasicBlockElement> elements = this.getElements(true);
    int count = elements.size() - 1; // Ignore trailing GOTO element
    for (int i = 0; i < count; i++) {
      successor.insertElement(i, elements.get(i));
    }

    this.detach(successor);
    return successor;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      acceptElements(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    traverseElements(schedule);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest request) throws Exception {
    visitor.visit(this, request);
  }

  /**
   * Removes the basic block by redirecting all the predecessors to point to the
   * primary successor of this block.
   */
  public void delete() {
    this.detach(getPrimarySuccessor());
  }

  @Override
  public void checkValidity() {
    super.checkValidity();

    if (!(getLastElement() instanceof JGotoBlockElement)) {
      throw new JNodeInternalError(this, "The last element of the block must be goto element");
    }
  }
}
