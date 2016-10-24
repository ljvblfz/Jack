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
import com.android.jack.ir.ast.JVisitor;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;
import com.android.sched.util.findbugs.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/** Represents switch block. */
public final class JSwitchBasicBlock extends JRegularBasicBlock {
  @Nonnull
  private List<JBasicBlock> cases = new ArrayList<>();

  @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
  public JSwitchBasicBlock(@Nonnull JControlFlowGraph cfg, @Nonnull JBasicBlock defaultCase) {
    super(defaultCase);
    defaultCase.addPredecessor(this);
    updateParents(cfg);
  }

  @Override
  public boolean hasPrimarySuccessor() {
    return true;
  }

  @Override
  void collectSuccessors(@Nonnull ArrayList<JBasicBlock> successors) {
    super.collectSuccessors(successors);
    successors.addAll(cases);
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
}
