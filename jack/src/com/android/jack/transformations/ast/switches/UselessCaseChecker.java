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
import com.android.jack.transformations.SanityChecks;
import com.android.jack.util.ControlFlowHelper;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * This visitor checks that there is no useless cases into switch statement.
 */
@Description("Checks that there is no useless cases into switch statement.")
@Constraint(need = {JSwitchStatement.class})
@Support(SanityChecks.class)
public class UselessCaseChecker implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  private final Checker checker = new Checker();

  private static class Checker extends JVisitor {

    @Override
    public boolean visit(@Nonnull JCaseStatement caseStmt) {
      JStatement nextStmt = caseStmt;
      do {
        nextStmt = ControlFlowHelper.getNextStatement(nextStmt);
        while (nextStmt instanceof JStatementList) {
          nextStmt = ControlFlowHelper.getConcreteStatement((JStatementList) nextStmt);
        }
      } while (nextStmt instanceof JCaseStatement && ((JCaseStatement) nextStmt).getExpr() != null);

      if (nextStmt instanceof JCaseStatement && ((JCaseStatement) nextStmt).getExpr() == null) {
        throw new AssertionError("Useless cases into switch exist.");
      }

      return super.visit(caseStmt);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    checker.accept(method);
  }

}
