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
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.ArrayList;
import javax.annotation.Nonnull;

/** Represents blocks ending with conditional branching. */
public final class JConditionalBasicBlock extends JRegularBasicBlock {
  @Nonnull
  private JBasicBlock ifFalse;

  /**
   * If true, ensures that the primary successor is the one specified as isFalse,
   * and the alternative successor if the one specified as ifTrue.
   */
  private boolean inverted = false;

  public JConditionalBasicBlock(@Nonnull JControlFlowGraph cfg,
      @Nonnull JBasicBlock ifTrue, @Nonnull JBasicBlock ifFalse) {
    super(ifTrue);
    this.ifFalse = ifFalse;
    this.ifFalse.addPredecessor(this);
    updateParents(cfg);
  }

  @Override
  public boolean hasPrimarySuccessor() {
    return true;
  }

  @Override
  @Nonnull
  public JBasicBlock getPrimarySuccessor() {
    return inverted ? getIfFalse() : getIfTrue();
  }

  @Nonnull
  public JBasicBlock getAlternativeSuccessor() {
    return inverted ? getIfTrue() : getIfFalse();
  }

  @Nonnull
  public final JBasicBlock getIfTrue() {
    return super.getPrimarySuccessor();
  }

  @Nonnull
  public final JBasicBlock getIfFalse() {
    return ifFalse;
  }

  public boolean isInverted() {
    return inverted;
  }

  public void setInverted(boolean inverted) {
    this.inverted = inverted;
  }

  @Override
  void collectSuccessors(@Nonnull ArrayList<JBasicBlock> successors) {
    super.collectSuccessors(successors);
    successors.add(ifFalse);
  }

  @Override
  public void replaceAllSuccessors(@Nonnull JBasicBlock what, @Nonnull JBasicBlock with) {
    super.replaceAllSuccessors(what, with);
    if (this.ifFalse == what) {
      this.ifFalse = resetSuccessor(what, with);
    }
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
}
