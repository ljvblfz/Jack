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

package com.android.jack.transformations.ast;

import com.android.jack.Options;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JDoStatement;
import com.android.jack.ir.ast.JForStatement;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.JWhileStatement;
import com.android.jack.transformations.SanityChecks;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Check that all implicit blocks are created.
 */
@Description("Check that all implicit blocks are created.")
@Name("ImplicitBlocksChecker")
@Constraint(need = NoImplicitBlock.class)
@Support(SanityChecks.class)
public class ImplicitBlocksChecker implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class BlockStatisticsVisitor extends JVisitor {

    @Override
    public boolean visit(@Nonnull JIfStatement ifStmt) {
      JStatement thenStmt = ifStmt.getThenStmt();
      JStatement elseStmt = ifStmt.getElseStmt();

      if ((thenStmt != null && !(thenStmt instanceof JBlock)) ||
          (elseStmt != null && !(elseStmt instanceof JBlock))) {
        throw new AssertionError("If statement with stand-alone statement.");
      }

      return super.visit(ifStmt);
    }

    @Override
    public boolean visit(@Nonnull JLabeledStatement labeledStmt) {
      if (!(labeledStmt.getBody() instanceof JBlock)) {
        throw new AssertionError("Labeled statement with stand-alone statement.");
      }

      return super.visit(labeledStmt);
    }

    @Override
    public boolean visit(@Nonnull JForStatement forStmt) {
      if (!(forStmt.getBody() instanceof JBlock)) {
        throw new AssertionError("For statement with stand-alone statement.");
      }

      JNode parent = forStmt.getParent();
      if (parent instanceof JBlock) {
        JBlock parentBlock = (JBlock) parent;
        if (parentBlock.getStatements().size() != 1) {
          throw new AssertionError("Implicit block surrounded for statement does not exist.");
        }
      }

      return super.visit(forStmt);
    }

    @Override
    public boolean visit(@Nonnull JWhileStatement whileStmt) {
      if (!(whileStmt.getBody() instanceof JBlock)) {
        throw new AssertionError("For statement with stand-alone statement.");
      }

      return super.visit(whileStmt);
    }

    @Override
    public void endVisit(@Nonnull JDoStatement doWhileStatement) {
      if (!(doWhileStatement.getBody() instanceof JBlock)) {
        throw new AssertionError("Do while statement with stand-alone statement.");
      }
      super.endVisit(doWhileStatement);
    }


    @Override
    public boolean visit(@Nonnull JCaseStatement caseStmt) {
      JNode parent = caseStmt.getParent();

      if (!(parent instanceof JBlock)) {
        throw new AssertionError("Case statement must be start a block.");
      }

      List<JStatement> stmts = ((JBlock) parent).getStatements();
      int indexOfCaseStmt = stmts.indexOf(caseStmt);
      // +1 means statement that follows the case statement.
      int statementIndexAfterCaseStmt = indexOfCaseStmt + 1;
      if (!(stmts.get(statementIndexAfterCaseStmt) instanceof JBlock)
          || (statementIndexAfterCaseStmt) != (stmts.size() - 1)) {
        throw new AssertionError("Case statement must be only follows by one block.");
      }

      return super.visit(caseStmt);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    BlockStatisticsVisitor statistics = new BlockStatisticsVisitor();
    statistics.accept(method);
  }
}
