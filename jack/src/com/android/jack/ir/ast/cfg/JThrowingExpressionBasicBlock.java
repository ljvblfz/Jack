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

import javax.annotation.Nonnull;

/** Represents blocks which potentially may trigger exceptions. */
public final class JThrowingExpressionBasicBlock extends JThrowingBasicBlock {
  public JThrowingExpressionBasicBlock(
      @Nonnull JControlFlowGraph cfg, @Nonnull JBasicBlock primary) {
    super(primary, cfg.getExitBlock());
    updateParents(cfg);
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

  @Override
  public boolean hasPrimarySuccessor() {
    return true;
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

    JBasicBlockElement lastElement = getLastElement();
    boolean lastElementCanThrow = lastElement instanceof JMethodCallBlockElement
        || lastElement instanceof JPolymorphicMethodCallBlockElement
        || lastElement instanceof JLockBlockElement
        || lastElement instanceof JUnlockBlockElement
        || lastElement instanceof JStoreBlockElement
        || (lastElement instanceof JVariableAsgBlockElement &&
        ((JVariableAsgBlockElement) lastElement).getAssignment().getRhs().canThrow());

    if (!lastElementCanThrow) {
      throw new JNodeInternalError(this,
          "The last element of the block must be an element that may throw");
    }
  }
}
