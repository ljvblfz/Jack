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

package com.android.jack.transformations.exceptions;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JLabel;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.transformations.ast.NoImplicitBlock;
import com.android.jack.transformations.finallyblock.InlinedFinallyMarker;
import com.android.jack.transformations.request.AppendStatement;
import com.android.jack.transformations.request.PrependAfter;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.ControlFlowHelper;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Replaces try/catch statement by lower exception support.
 */
@Description("Replaces try/catch statement by lower exception support.")
@Name("TryCatchRemover")
@Constraint(need = {NoImplicitBlock.class, JTryStatement.class, InlinedFinallyMarker.class},
    no = {JTryStatement.FinallyBlock.class, TryStatementSchedulingSeparator.SeparatorTag.class})
@Transform(
    add = {JLabel.class, JBlock.class, JLabeledStatement.class, JGoto.class},
    remove = {JTryStatement.class})
public class TryCatchRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    /**
     * Represent all try statement and specify if it is a starting point or not. Starting point
     * means that {@code tryStmt} represent the first try that will catch exceptions, others before
     * this try must be ignore.
     */
    private static class TryStmtCatchingExceptions {
      @CheckForNull
      private final JTryStatement tryStmt;
      private final boolean isStartingPoint;

      public TryStmtCatchingExceptions(@CheckForNull JTryStatement tryStmt,
          boolean isStartingPoint) {
        this.tryStmt = tryStmt;
        this.isStartingPoint = isStartingPoint;
      }
    }

    @Nonnull
    private final Stack<TryStmtCatchingExceptions> tries = new Stack<TryStmtCatchingExceptions>();

    @Nonnull
    private final TransformationRequest tr;

    public Visitor(@Nonnull TransformationRequest tr) {
      this.tr = tr;
    }

    @Override
    public boolean visit(@Nonnull JStatement stmt) {
      addCatchesToStmt(stmt);
      return super.visit(stmt);
    }

    @Override
    public boolean visit(@Nonnull JBlock jBlock) {

      InlinedFinallyMarker marker = jBlock.getMarker(InlinedFinallyMarker.class);
      if (marker != null) {
        tries.push(new TryStmtCatchingExceptions(marker.getTryStmt(), true));
      }

      return super.visit(jBlock);
    }

    @Override
    public void endVisit(@Nonnull JBlock jBlock) {

      InlinedFinallyMarker marker = jBlock.getMarker(InlinedFinallyMarker.class);
      if (marker != null) {
        tries.pop();
      }

      super.endVisit(jBlock);
    }

    @Override
    public boolean visit(@Nonnull JTryStatement jTry) {
      tries.push(new TryStmtCatchingExceptions(jTry, false));

      accept(jTry.getTryBlock());

      tries.pop();

      accept(jTry.getCatchBlocks());

      return false;
    }

    @Override
    public void endVisit(@Nonnull JTryStatement jTry) {
      JStatement nextStatement = ControlFlowHelper.getNextStatement(jTry);
      List<JStatement> stmtsInTry = jTry.getTryBlock().getStatements();
      JStatement lastStmtInTry =
          stmtsInTry.size() > 0 ? stmtsInTry.get(stmtsInTry.size() - 1) : null;

      if (nextStatement != null &&
          (!(lastStmtInTry instanceof JReturnStatement) &&
           !(lastStmtInTry instanceof JThrowStatement) &&
           !(lastStmtInTry instanceof JGoto)
          || stmtsInTry.size() == 0)) {
        JLabel label =
            new JLabel(nextStatement.getSourceInfo(), "L"
                + nextStatement.getSourceInfo().getStartLine());

        JBlock labeledBlock = new JBlock(nextStatement.getSourceInfo());
        labeledBlock.addStmt(nextStatement);

        JLabeledStatement labeledStmt =
            new JLabeledStatement(nextStatement.getSourceInfo(), label, labeledBlock);

        JGoto branchOnNextStatement = new JGoto(SourceInfo.UNKNOWN, labeledStmt);

        tr.append(new Replace(nextStatement, labeledStmt));
        tr.append(new AppendStatement(jTry.getTryBlock(), branchOnNextStatement));
      }

      for (JCatchBlock bb : jTry.getCatchBlocks()) {
        tr.append(new PrependAfter(jTry, bb));
      }

      tr.append(new Replace(jTry, jTry.getTryBlock()));
      super.endVisit(jTry);
    }

    private void addCatchesToStmt(@Nonnull JStatement stmt) {

      List<JType> catchTypes = new ArrayList<JType>();
      ListIterator<TryStmtCatchingExceptions> tryStmtIt = tries.listIterator(tries.size());

      loop: while (tryStmtIt.hasPrevious()) {
        TryStmtCatchingExceptions tryStmtCatchingException = tryStmtIt.previous();
        JTryStatement tryUsedToCatchException = tryStmtCatchingException.tryStmt;

        if (tryUsedToCatchException == null) {
          // Not catch stop here
          break;
        }

        if (tryStmtCatchingException.isStartingPoint) {
          // Go to the starting point and forget others try/catch
          JTryStatement tryStatementToFound = tryUsedToCatchException;
          assert tryStmtIt.hasPrevious();
          tryStmtCatchingException = tryStmtIt.previous();
          while (tryStmtIt.hasPrevious() &&
              tryStmtCatchingException.tryStmt != tryStatementToFound) {
            tryStmtCatchingException = tryStmtIt.previous();
            tryUsedToCatchException = tryStmtCatchingException.tryStmt;
          }
          assert tryStmtCatchingException.tryStmt == tryStatementToFound;
        }

        assert tryUsedToCatchException != null;
        for (JCatchBlock bb : tryUsedToCatchException.getCatchBlocks()) {
          int catchTypesCount = catchTypes.size();

          for (JClass catchedType : bb.getCatchTypes()) {
            if (catchedType.equals(Jack.getSession().getPhantomLookup()
                .getClass(CommonTypes.JAVA_LANG_OBJECT))) {
              assert bb.getCatchTypes().size() == 1;
              stmt.appendCatchBlock(bb);
              // means any, thus could not be catch again
              break loop;
            } else {
              if (catchTypes.contains(catchedType)) {
                // Type already catched by a nearest try statement
                continue;
              }
              catchTypes.add(catchedType);
            }
          }

          if (catchTypesCount != catchTypes.size()) {
            stmt.appendCatchBlock(bb);
          }
        }
      }
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.getEnclosingType().isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    Visitor visitor = new Visitor(tr);
    visitor.accept(method);
    tr.commit();
  }
}