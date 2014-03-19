/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.cfg;

import com.android.jack.ir.SourceOrigin;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JVisitor;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Resolve forward branch that appears during control flow graph building.
 */
class ForwardBranchResolver {

  enum ForwardBranchKind {
    IF_THEN,
    IF_ELSE,
    BRANCH,
    SWITCH_CASE,
    SWITCH_DEFAULT,
    EXCEPTION
  }

  @Description("Statement to represent dead code.")
  static class JDeadCodeStatement extends JStatement {

    private static final long serialVersionUID = 1L;

    public JDeadCodeStatement() {
      super(SourceOrigin.UNKNOWN);
    }

    @Override
    public void traverse(@Nonnull JVisitor visitor) {
      throw new AssertionError("JDeadCodeStatement is not usable by JVisitor");
    }

    @Override
    public void traverse(@Nonnull ScheduleInstance<? super Component> instance) throws Exception {
      throw new AssertionError("JDeadCodeStatement is not usable by JVisitor");
    }

    @Override
    public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
        throws Exception {
      visitor.visit(this, transformRequest);
    }
  }

  @Nonnull
  final JDeadCodeStatement deadCodeStatement = new JDeadCodeStatement();

  @Nonnull
  private final Map<BasicBlock, Map<ForwardBranchKind, List<JStatement>>> bbSuccessorsToResolve =
      new HashMap<BasicBlock, Map<ForwardBranchKind, List<JStatement>>>();

  /**
   * Add forward branch to resolve.
   *
   * @param bb A basic block targeting {@code targetStatement}.
   * @param targetStatement Target statement of a the fordward branch.
   */
  void addForwardBranch(@Nonnull ForwardBranchKind brKind, @Nonnull BasicBlock bb,
      @Nonnull JStatement targetStatement) {
    Map<ForwardBranchKind, List<JStatement>> brKindTotargetStatements =
        bbSuccessorsToResolve.get(bb);

    if (brKindTotargetStatements == null) {
      brKindTotargetStatements = new HashMap<ForwardBranchKind, List<JStatement>>();
      bbSuccessorsToResolve.put(bb, brKindTotargetStatements);
    }

    List<JStatement> targetStatements = brKindTotargetStatements.get(brKind);

    if (targetStatements == null) {
      targetStatements = new LinkedList<JStatement>();
      brKindTotargetStatements.put(brKind, targetStatements);
    }

    assert !targetStatements.contains(targetStatement);

    if (brKind == ForwardBranchKind.EXCEPTION) {
      // Target branch must be save in reverse order for try statement, the nearest a the end of
      // list. By construction, the list will be scanned during resolve and resolved branches will
      // be reversed. Consequently after resolution, the nearest target for try statement will
      // be at index 0 of the successor list.
      targetStatements.add(0, targetStatement);
    } else {
      targetStatements.add(targetStatement);
    }
  }

  /**
   * Resolve forward branches by updating basic bloc successors.
   */
  void resolve() {
    for (BasicBlock bbToResolve : bbSuccessorsToResolve.keySet()) {
      for (ForwardBranchKind brKind : bbSuccessorsToResolve.get(bbToResolve).keySet()) {
        for (JStatement targetStatement : bbSuccessorsToResolve.get(bbToResolve).get(brKind)) {
          if (targetStatement == deadCodeStatement){
            continue;
          }

          BasicBlockMarker bbm = targetStatement.getMarker(BasicBlockMarker.class);
          assert bbm != null;
          BasicBlock targetBb = bbm.getBasicBlock();
          assert targetBb != null;

          switch (brKind) {
            case IF_THEN:
              ((ConditionalBasicBlock) bbToResolve).setThenBlock(targetBb);
              break;
            case IF_ELSE:
              ((ConditionalBasicBlock) bbToResolve).setElseBlock(targetBb);
              break;
            case BRANCH:
              ((NormalBasicBlock) bbToResolve).setTarget(targetBb);
              break;
            case SWITCH_CASE:
              ((SwitchBasicBlock) bbToResolve).addCaseBlock(targetBb);
              break;
            case SWITCH_DEFAULT:
              ((SwitchBasicBlock) bbToResolve).setDefault(targetBb);
              break;
            case EXCEPTION:
              ((PeiBasicBlock) bbToResolve).addExceptionBlock((CatchBasicBlock) targetBb);
              break;
          }
        }
      }
    }
  }
}
