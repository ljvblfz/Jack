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
import com.android.jack.ir.ast.JVisitor;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/** Represents switch block. */
public final class JSwitchBasicBlock extends JRegularBasicBlock {
  @Nonnull
  private final List<JBasicBlock> cases = new ArrayList<>();

  public JSwitchBasicBlock(@Nonnull JControlFlowGraph cfg, @Nonnull JBasicBlock defaultCase) {
    super(defaultCase);
    defaultCase.addPredecessor(this);
    updateParents(cfg);
  }

  @Override
  public boolean hasPrimarySuccessor() {
    return true;
  }

  @Nonnull
  @Override
  public List<JBasicBlock> getSuccessors() {
    ArrayList<JBasicBlock> successors = new ArrayList<>();
    successors.add(getPrimarySuccessor());
    successors.addAll(cases);
    return successors;
  }

  /** Add a new case block */
  public void addCase(@Nonnull JBasicBlock block) {
    cases.add(block);
    block.addPredecessor(this);
  }

  @Nonnull
  public List<JBasicBlock> getCases() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(cases);
  }

  @Nonnull
  public JBasicBlock getDefaultCase() {
    return getPrimarySuccessor();
  }

  @Override
  public void replaceAllSuccessors(@Nonnull JBasicBlock what, @Nonnull JBasicBlock with) {
    super.replaceAllSuccessors(what, with);
    for (int i = 0; i < cases.size(); i++) {
      if (cases.get(i) == what) {
        cases.set(i, resetSuccessor(what, with));
      }
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

  @Override
  public void checkValidity() {
    super.checkValidity();

    if (!(getLastElement() instanceof JSwitchBlockElement)) {
      throw new JNodeInternalError(this,
          "The last element of the block must be switch element");
    }
  }
}
