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

package com.android.jack.transformations.flow;

import com.android.jack.Options;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JBreakStatement;
import com.android.jack.ir.ast.JContinueStatement;
import com.android.jack.ir.ast.JDoStatement;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JForStatement;
import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JLabel;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JLoop;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JStatementList;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.JWhileStatement;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.jack.transformations.ast.NoImplicitBlock;
import com.android.jack.transformations.request.AppendBefore;
import com.android.jack.transformations.request.PrependAfter;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * The {@link FlowNormalizer} replaces loops by if/goto instructions and continue and break
 * instructions by goto instructions.
 */
@Description("Replaces loops by if/goto. Replaces continue and break into loops by goto.")
@Name("FlowNormalizer")
@Constraint(need = NoImplicitBlock.class)
@Transform(
    add = {JGoto.class, JIfStatement.class, JBlock.class, JLabel.class, JLabeledStatement.class},
    remove = {
        JLoop.class, JBreakStatement.class, JContinueStatement.class, ThreeAddressCodeForm.class})
@Filter(SourceTypeFilter.class)
public class FlowNormalizer implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Nonnegative
    private int labelId = 0;

    @Nonnull
    private final Stack<JStatement> stmts = new Stack<JStatement>();

    @Nonnull
    private final HashMap<JStatement, JLabeledStatement> continueTargets =
      new HashMap<JStatement, JLabeledStatement>();

    @Nonnull
    private final HashMap<JStatement, JLabeledStatement> breakTargets =
      new HashMap<JStatement, JLabeledStatement>();

    @Nonnull
    private final TransformationRequest trRequest;

    private Visitor(@Nonnull JMethod method) {
      trRequest = new TransformationRequest(method);
    }

    @Override
    public void endVisit(@Nonnull JMethod x) {
      super.endVisit(x);
      trRequest.commit();
      continueTargets.clear();
      breakTargets.clear();
    }

    @Override
    public boolean visit(@Nonnull JStatement statement) {
      stmts.push(statement);
      return super.visit(statement);
    }

    @Override
    public void endVisit(@Nonnull JStatement statement) {
      assert statement == stmts.peek();
      stmts.pop();
      super.endVisit(statement);
    }

    @Override
    public boolean visit(@Nonnull JBreakStatement breakStmt) {
      JLabeledStatement target =
          findTarget(breakStmt.getLabel(), breakTargets);
      trRequest.append(new Replace(breakStmt, new JGoto(breakStmt.getSourceInfo(), target)));
      return super.visit(breakStmt);
    }

    @Override
    public boolean visit(@Nonnull JContinueStatement continueStmt) {
      JLabeledStatement target =
          findTarget(continueStmt.getLabel(), continueTargets);
      trRequest.append(new Replace(continueStmt, new JGoto(continueStmt.getSourceInfo(), target)));
      return super.visit(continueStmt);
    }

    @Override
    public boolean visit(@Nonnull JForStatement forStmt) {
      SourceInfo loopSrcInfo = forStmt.getSourceInfo();

      registerBreakTarget(forStmt, splitBlockOnStatement("for.break", forStmt));

      // Initializers
      for (JStatement initializer : forStmt.getInitializers()) {
        trRequest.append(new AppendBefore(forStmt, initializer));
      }

      // Condition & body
      JLabeledStatement condLabeledStmt = createLabeledBlock("for.cond", loopSrcInfo);
      JBlock condLabeledBlock = (JBlock) condLabeledStmt.getBody();
      JBlock loopBody;

      JExpression condExpr = forStmt.getTestExpr();
      if (condExpr instanceof JBooleanLiteral) {
        assert ((JBooleanLiteral) condExpr).getValue();
        loopBody = condLabeledBlock;
      } else {
        loopBody = new JBlock(loopSrcInfo);
        JIfStatement ifStmt = new JIfStatement(loopSrcInfo, forStmt.getTestExpr(), loopBody, null);
        condLabeledBlock.addStmt(ifStmt);
      }

      loopBody.addStmt(forStmt.getBody());

      trRequest.append(new AppendBefore(forStmt, condLabeledStmt));

      // Increment
      JLabeledStatement incLabeledBlock = createLabeledBlock("for.inc", loopSrcInfo);
      JBlock incBlock = (JBlock) incLabeledBlock.getBody();
      for (JExpressionStatement increment : forStmt.getIncrements()) {
        incBlock.addStmt(increment);
      }

      incBlock.addStmt(new JGoto(SourceInfo.UNKNOWN, condLabeledStmt));
      loopBody.addStmt(incLabeledBlock);

      trRequest.append(new Remove(forStmt));

      registerContinueTarget(forStmt, incLabeledBlock);

      return super.visit(forStmt);
    }

    @Override
    public boolean visit(@Nonnull JDoStatement doStmt) {
      registerBreakTarget(doStmt, splitBlockOnStatement("do.break", doStmt));

      // Body
      JStatement body = doStmt.getBody();
      assert body instanceof JBlock;
      SourceInfo bodyInfo = body.getSourceInfo();
      JLabel bodyLabel = new JLabel(bodyInfo, "do.body.label." + labelId);
      labelId++;
      JLabeledStatement labeledBody = new JLabeledStatement(bodyInfo, bodyLabel, body);

      // Condition
      JExpression cond = doStmt.getTestExpr();
      SourceInfo condInfo = cond.getSourceInfo();
      JBlock branchBlock = new JBlock(condInfo);
      JLabeledStatement labeledCond = createLabeledBlock("do.cond", condInfo);
      JGoto gotoStmt = new JGoto(SourceInfo.UNKNOWN, labeledBody);

      if (cond instanceof JBooleanLiteral) {
        if (((JBooleanLiteral) cond).getValue()) {
          ((JBlock) labeledCond.getBody()).addStmt(gotoStmt);
        }
      } else {
        branchBlock.addStmt(gotoStmt);
        ((JBlock) labeledCond.getBody())
            .addStmt(new JIfStatement(condInfo, cond, branchBlock, null));
      }

      trRequest.append(new PrependAfter(doStmt, labeledCond));
      trRequest.append(new Replace(doStmt, labeledBody));

      registerContinueTarget(doStmt, labeledCond);

      return super.visit(doStmt);
    }

    @Override
    public boolean visit(@Nonnull JWhileStatement whileStmt) {
      registerBreakTarget(whileStmt, splitBlockOnStatement("while.break", whileStmt));

      JExpression cond = whileStmt.getTestExpr();
      SourceInfo loopInfo = whileStmt.getSourceInfo();
      SourceInfo condInfo = cond.getSourceInfo();
      JLabeledStatement condLabeledStmt = createLabeledBlock("while.cond", condInfo);
      JBlock condLabeledBlock = (JBlock) condLabeledStmt.getBody();
      JBlock loopBody = (JBlock) whileStmt.getBody();
      JBlock newBody;

      if (cond instanceof JBooleanLiteral) {
        assert ((JBooleanLiteral) cond).getValue();
        newBody = condLabeledBlock;
        newBody.addStmt(loopBody);
      } else {
        newBody = loopBody;
        JIfStatement ifStmt =
            new JIfStatement(loopInfo, cond, newBody, null);
        condLabeledBlock.addStmt(ifStmt);
      }

      newBody.addStmt(new JGoto(SourceInfo.UNKNOWN, condLabeledStmt));

      trRequest.append(new Replace(whileStmt, condLabeledStmt));

      registerContinueTarget(whileStmt, condLabeledStmt);

      return super.visit(whileStmt);
    }

    @Override
    public boolean visit(@Nonnull JSwitchStatement switchStmt) {
      registerBreakTarget(switchStmt, splitBlockOnStatement("switch.break", switchStmt));
      return super.visit(switchStmt);

    }

    @Override
    public boolean visit(@Nonnull JLabeledStatement labelStmt) {
      registerBreakTarget(labelStmt, splitBlockOnStatement("label.break",
            labelStmt));
      return super.visit(labelStmt);
    }

    private void registerContinueTarget(JStatement stmt, JLabeledStatement target) {
      continueTargets.put(stmt, target);
    }

    private void registerBreakTarget(JStatement stmt, JLabeledStatement target) {
      breakTargets.put(stmt, target);
    }

    @Nonnull
    private JLabeledStatement findTarget(@CheckForNull JLabel label,
        @Nonnull HashMap<JStatement, JLabeledStatement> targetsMap) {
      if (label == null) {
        return findTarget(targetsMap);
      } else {
        return findTargetWithLabel(label, targetsMap);
      }
    }

    private JLabeledStatement findTargetWithLabel(
         @Nonnull JLabel label, @Nonnull HashMap<JStatement, JLabeledStatement> targetsMap)
             throws AssertionError {

      /* rewind statement stack to find label*/
      ListIterator<JStatement> listIterator = stmts.listIterator(stmts.size());
      while (listIterator.hasPrevious()) {
        JStatement currentStatement = listIterator.previous();
        if (currentStatement instanceof JLabeledStatement
            && ((JLabeledStatement) currentStatement).getLabel().getName().equals(
                label.getName())) {
          /* Label found, now search for the target of this label. Break targets are
           * registered directly on the JLabeledStatement, continue targets are registered on
           * loops that have to be found up in the stack. So lets search forward starting at the
           * JLabeledStatement. */
          while (true) {
            JLabeledStatement target = targetsMap.get(currentStatement);
            if (target != null) {
              return target;
            }
            if (listIterator.hasNext()) {
              currentStatement = listIterator.next();
            } else {
              throw new AssertionError("Break or continue to invalid label " + label.getName());
            }
          }
        }
      }
      throw new AssertionError("Break or continue to invalid label " + label.getName());
    }

    private JLabeledStatement findTarget(@Nonnull HashMap<JStatement, JLabeledStatement> targetsMap)
        throws AssertionError {

      /* rewind statement stack to find first available target */
      ListIterator<JStatement> listIterator = stmts.listIterator(stmts.size());
      while (listIterator.hasPrevious()) {
        JLabeledStatement target = targetsMap.get(listIterator.previous());
        if (target != null) {
          return target;
        }
      }
      throw new AssertionError("Break or continue in invalid location");
    }

    @Nonnull
    private JLabeledStatement splitBlockOnStatement(@Nonnull String labelPrefix,
        @Nonnull JStatement targetStmt) {
      JLabeledStatement target;
      List<JStatement> statementsToMove = getFollowingStatements(targetStmt);

      target = createLabeledBlock(labelPrefix, targetStmt.getSourceInfo());

      ((JBlock) target.getBody()).addStmts(statementsToMove);

      for (JStatement stmt : statementsToMove) {
        trRequest.append(new Remove(stmt));
      }

      trRequest.append(new PrependAfter(targetStmt, target));

      return target;
    }

    @Nonnull
    private JLabeledStatement createLabeledBlock(@Nonnull String labelPrefix,
        @Nonnull SourceInfo srcInfo) {
      JLabel label = new JLabel(srcInfo, labelPrefix + ".label." + labelId);
      JBlock labledBlock = new JBlock(srcInfo);
      labelId++;
      return (new JLabeledStatement(srcInfo, label, labledBlock));
    }

    @Nonnull
    private List<JStatement> getFollowingStatements(@Nonnull JStatement stmt) {
      JNode parent = stmt.getParent();
      assert parent instanceof JStatementList;

      JStatementList parentBlock = (JStatementList) parent;
      List<JStatement> stmts = parentBlock.getStatements();
      // +1 means next statement of labeled statement.
      List<JStatement> statementsToMove = parentBlock.getStatements().subList(
          stmts.indexOf(stmt) + 1, stmts.size());

      return statementsToMove;
    }
  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    Visitor visitor = new Visitor(method);
    visitor.accept(method);
  }

}
