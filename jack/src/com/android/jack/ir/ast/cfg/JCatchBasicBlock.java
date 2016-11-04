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

import com.android.jack.Jack;
import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JVisitor;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Represents catch basic block.
 *
 * Catch basic block is intended to carry catch specific information and
 * always has one catch variable assignment element.
 */
public final class JCatchBasicBlock extends JRegularBasicBlock {
  @Nonnull
  private final List<JClass> catchTypes;

  public JCatchBasicBlock(@Nonnull JControlFlowGraph cfg,
      @Nonnull JBasicBlock primary, @Nonnull List<JClass> catchTypes) {
    super(primary);
    this.catchTypes = catchTypes;
    updateParents(cfg);
  }

  @Override
  public boolean hasPrimarySuccessor() {
    return true;
  }

  @Nonnull
  @Override
  public JSimpleBasicBlock split(int at) {
    // Catch blocks are referenced directly by JThrowingBasicBlock and cannot
    // be split so that there is another block in between throwing block and
    // catch block. Consider splitting the only successor of the catch block.
    throw new UnsupportedOperationException();
  }

  @Nonnull
  public List<JClass> getCatchTypes() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(catchTypes);
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
  public void checkValidity() {
    super.checkValidity();

    // TODO(acleung): I am removing this check for phi nodes for now.
    /*
    if (getElementCount() != 1) {
      throw new JNodeInternalError(this, "Block must always have one single element");
    }
    */
    if (!(getLastElement() instanceof JVariableAsgBlockElement) ||
        !((JVariableAsgBlockElement) getLastElement()).isCatchVariableAssignment()) {
      throw new JNodeInternalError(this,
          "The only element of the block must be catch variable assignment element");
    }
  }
}
