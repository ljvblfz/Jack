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
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.jack.transformations.request.PrependAfter;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Add implicit block to avoid specific management of stand alone statement.
 */
@Description("Add implicit block to avoid specific management of stand alone statement.")
@Name("ImplicitBlocks")
@Transform(add = {JBlock.class, NoImplicitBlock.class})
@Filter(SourceTypeFilter.class)
public class ImplicitBlocks implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  private static class ImplicitBlocksVisitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    public ImplicitBlocksVisitor(@Nonnull TransformationRequest tr) {
      this.tr = tr;
    }

    @Override
    public boolean visit(@Nonnull JIfStatement ifStmt) {
      JStatement thenStmt = ifStmt.getThenStmt();
      if (thenStmt != null && !(thenStmt instanceof JBlock)) {
        moveIntoBLock(thenStmt);
      }

      JStatement elseStmt = ifStmt.getElseStmt();
      if (elseStmt != null && !(elseStmt instanceof JBlock)) {
        moveIntoBLock(elseStmt);
      }

      return super.visit(ifStmt);
    }

    @Override
    public boolean visit(@Nonnull JLabeledStatement labeledStmt) {
      JStatement labelStmtBody = labeledStmt.getBody();

      if (!(labelStmtBody instanceof JBlock)) {
        moveIntoBLock(labelStmtBody);
      }

      return super.visit(labeledStmt);
    }

    @Override
    public boolean visit(@Nonnull JForStatement forStmt) {
      JStatement forBody = forStmt.getBody();

      if (!(forBody instanceof JBlock)) {
        moveIntoBLock(forBody);
      }

      JNode parent = forStmt.getParent();
      if (parent instanceof JBlock) {
        JBlock parentBlock = (JBlock) parent;
        if (parentBlock.getStatements().size() != 1) {
          moveIntoBLock(forStmt);
        }
      }

      return super.visit(forStmt);
    }

    @Override
    public boolean visit(@Nonnull JWhileStatement whileStmt) {
      JStatement whileBody = whileStmt.getBody();

      if (!(whileBody instanceof JBlock)) {
        moveIntoBLock(whileBody);
      }

      return super.visit(whileStmt);
    }


    @Override
    public boolean visit(@Nonnull JDoStatement doStmt) {
      JStatement doBody = doStmt.getBody();

      if (!(doBody instanceof JBlock)) {
        moveIntoBLock(doBody);
      }

      return super.visit(doStmt);
    }

    @Override
    public boolean visit(@Nonnull JCaseStatement caseStmt) {
      List<JStatement> statementsToMove = getFollowingStatements(caseStmt);

      JBlock newBlock = new JBlock(caseStmt.getSourceInfo());
      newBlock.addStmts(statementsToMove);

      // TODO(mikaelpeltier) Think about an API that allows directly to remove a list ?
      for (JStatement stmt : statementsToMove) {
        tr.append(new Remove(stmt));
      }

      tr.append(new PrependAfter(caseStmt, newBlock));

      return super.visit(caseStmt);
    }

    private void moveIntoBLock(@Nonnull JStatement stmt) {
      JBlock newBlock = new JBlock(stmt.getSourceInfo());
      newBlock.addStmt(stmt);
      tr.append(new Replace(stmt, newBlock));
    }

    @Nonnull
    private List<JStatement> getFollowingStatements(@Nonnull JStatement stmt) {
      JNode parent = stmt.getParent();
      assert parent instanceof JBlock;

      JBlock switchBlock = (JBlock) parent;
      List<JStatement> switchStmts = switchBlock.getStatements();
      // +1 means next statement of labeled statement.
      List<JStatement> statementsToMove = switchBlock.getStatements().subList(
          switchStmts.indexOf(stmt) + 1, switchStmts.size());

      return statementsToMove;
    }

  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    ImplicitBlocksVisitor ibv = new ImplicitBlocksVisitor(tr);
    ibv.accept(method);
    tr.commit();
  }
}
