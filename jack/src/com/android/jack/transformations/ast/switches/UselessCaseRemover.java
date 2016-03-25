/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.transformations.ast.switches;

import com.android.jack.Options;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JStatementList;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.ControlFlowHelper;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that removes useless cases from switch statements.
 */
@Description("Removes useless cases from switch statements.")
@Constraint(need = {JSwitchStatement.class})
@Transform(add = UselessSwitches.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class UselessCaseRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  private static class Remover extends JVisitor {

    @CheckForNull
    private TransformationRequest tr;

    @Override
    public boolean visit(@Nonnull JMethod method) {
      tr = new TransformationRequest(method);
      return super.visit(method);
    }

    @Override
    public void endVisit(@Nonnull JMethod method) {
      assert tr != null;
      tr.commit();
      super.endVisit(method);
    }

    @Override
    public boolean visit(@Nonnull JCaseStatement caseStmt) {
      JStatement nextStmt = caseStmt;
      do {
        assert nextStmt != null;
        nextStmt = ControlFlowHelper.getNextStatement(nextStmt);
        while (nextStmt instanceof JStatementList) {
          nextStmt = ControlFlowHelper.getConcreteStatement((JStatementList) nextStmt);
        }
      } while (isCaseStmt(nextStmt));

      if (isDefaultCaseStmt(nextStmt)) {
        assert tr != null;
        tr.append(new Remove(caseStmt));
        caseStmt.getParent(JSwitchStatement.class).removeCase(caseStmt);
      }

      return super.visit(caseStmt);
    }

    private boolean isCaseStmt(@CheckForNull JStatement stmt) {
      return (stmt instanceof JCaseStatement && ((JCaseStatement) stmt).getExpr() != null);
    }

    private boolean isDefaultCaseStmt(@CheckForNull JStatement stmt) {
      return (stmt instanceof JCaseStatement && ((JCaseStatement) stmt).getExpr() == null);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    new Remover().accept(method);
  }

}
