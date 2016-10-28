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

/** Represents case basic block. */
public final class JCaseBasicBlock extends JRegularBasicBlock {
  public JCaseBasicBlock(@Nonnull JControlFlowGraph cfg, @Nonnull JBasicBlock primary) {
    super(primary);
    updateParents(cfg);
  }

  @Override
  public boolean hasPrimarySuccessor() {
    return true;
  }

  @Nonnull
  @Override
  public JSimpleBasicBlock split(int at) {
    // Case blocks are referenced directly by JSwitchBasicBlock and cannot
    // be split so that there is another block in between switch block and
    // case block. Consider splitting the only successor of the case block.
    throw new UnsupportedOperationException();
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

    if (getElementCount() != 1) {
      throw new JNodeInternalError(this, "Block must always have one single element");
    }
    if (!(getLastElement() instanceof JCaseBlockElement)) {
      throw new JNodeInternalError(this, "The only element of the block must be case element");
    }
  }
}
