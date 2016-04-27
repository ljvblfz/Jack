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

package com.android.jack.transformations.threeaddresscode;

import com.android.jack.Options;
import com.android.jack.analysis.DefinitionMarker;
import com.android.jack.analysis.SubTreeDefinitionMarkers;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JAssertStatement;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JCastOperation;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JConditionalOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JLoop;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMultiExpression;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JUnaryOperation;
import com.android.jack.ir.ast.JUnaryOperator;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.scheduling.marker.collector.SubTreeMarkersCollector;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.ast.BooleanTestOutsideIf;
import com.android.jack.transformations.ast.InitInNewArray;
import com.android.jack.transformations.ast.NoImplicitBlock;
import com.android.jack.transformations.ast.RefAsStatement;
import com.android.jack.transformations.booleanoperators.FallThroughMarker;
import com.android.jack.transformations.cast.SourceCast;
import com.android.jack.transformations.request.AppendBefore;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.AnnotationSkipperVisitor;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Transform body of a {@code JMethod} to into a three address form.
 */
@Description("Transform body of a JMethod to into a three address form.")
@Name("ThreeAddressCodeBuilder")
@Constraint(need = {NoImplicitBlock.class, SourceCast.class}, no = {BooleanTestOutsideIf.class,
    InitInNewArray.class, JAssertStatement.class, JAsgOperation.class, JFieldInitializer.class,
    JConditionalOperation.class, JLoop.class, JCastOperation.WithIntersectionType.class})
@Transform(add = {ThreeAddressCodeForm.class,
    RefAsStatement.class,
    JLocalRef.class,
    JAsgOperation.NonReusedAsg.class,
    JBlock.class,
    JIfStatement.class,
    JExpressionStatement.class}, remove = {JConditionalExpression.class, JMultiExpression.class})
@Use(LocalVarCreator.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class ThreeAddressCodeBuilder implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  /**
   * {@code ThreeAddressCode} is the visitor building the three address form.
   */
  private static class ThreeAddressCode extends AnnotationSkipperVisitor {

    @Nonnull
    private static final SubTreeMarkersCollector<DefinitionMarker> defMarkerCollector =
        new SubTreeMarkersCollector<DefinitionMarker>(SubTreeDefinitionMarkers.class);

    @CheckForNull
    private JStatement insertStatement;

    @Nonnull
    private final List<JStatement> newStmtToVisit = new LinkedList<JStatement>();

    @Nonnull
    private final JMethod method;

    @Nonnull
    private final LocalVarCreator localVarCreator;

    @Nonnull
    private List<JCatchBlock> currentCatchBlocks = new LinkedList<JCatchBlock>();

    public ThreeAddressCode(@Nonnull JMethod method) {
      this.method = method;
      localVarCreator = new LocalVarCreator(method, "tac");
    }

    @Override
    public boolean visit(@Nonnull JNewArray newArray) {
      // Do not visit initializers, visit only dimensions.
      accept(newArray.getDims());

      return false;
    }

    private boolean isRedefineVariable(@Nonnull List<DefinitionMarker> defs,
        @Nonnull JVariable var) {
      for (DefinitionMarker def : defs) {
        if (def.getDefinedVariable() == var) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void endVisit(@Nonnull JExpression expr) {
      JNode parent = expr.getParent();
      assert insertStatement != null;

      // Nothing to do on:
      // - JConditional, they are taken into account in descending order by replacing them by if
      // statements
      // - JLiteral that can not throw exception since they are manage by the back-end
      // - JVariableRef, no need to put variables into temporary variables except if they are
      // redefined by the following expressions in the statement.
      // - No need to move variable if it is the left part of an assignment.
      if (expr instanceof JConditionalExpression
          || (expr instanceof JValueLiteral && !expr.canThrow())
          || (expr instanceof JVariableRef && !isRedefineVariable(
              expr.getMarkersOnNodesRightToPath(defMarkerCollector, insertStatement),
              ((JVariableRef) expr).getTarget()))
          || (parent instanceof JAsgOperation &&
              ((JAsgOperation) parent).getLhs() == expr)) {
        return;
      }

      if (((expr instanceof JFieldRef) || (expr instanceof JArrayRef))
          && (parent instanceof JBinaryOperation)) {
        JBinaryOperation binaryOperation = (JBinaryOperation) parent;
        if (binaryOperation.getLhs() == expr && binaryOperation.isAssignment()) {
          /* This a write field or array, it can not be removed from the assign.*/
          return;
        }
      }

      // Do not split comparison operations and 'not' operation contained in IfStatement
      // (it will simplify the Ropper, since there is
      // no link between variable and content of variable)
      if (parent instanceof JIfStatement) {
        if ((expr instanceof JBinaryOperation &&
            ((JBinaryOperation) expr).getOp().isComparison())) {
          return;
        }
        if ((expr instanceof JUnaryOperation &&
                ((JUnaryOperation) expr).getOp() == JUnaryOperator.NOT)) {
          return;
        }
      }

      TransformationRequest transformationRequest = new TransformationRequest(method);

      if (expr instanceof JMultiExpression) {
        List<JExpression> exprs = ((JMultiExpression) expr).getExprs();
        if (exprs.size() > 0) {
          if (parent instanceof JExpressionStatement) {
            transformationRequest.append(new Remove(parent));
          } else {
            transformationRequest.append(new Replace(expr, exprs.get(exprs.size() - 1)));
          }
        } else {
          transformationRequest.append(new Remove(parent));
        }
      } else {

        if ((expr.getType() == JPrimitiveTypeEnum.VOID.getType()
            && !(parent instanceof JMultiExpression)) || parent instanceof JExpressionStatement) {
          return;
        } else if (parent instanceof JAsgOperation &&
            ((JAsgOperation) parent).getLhs() instanceof JLocalRef) {
          return;
        }

        if (expr instanceof JAsgOperation) {
          if (expr.isResultUsed()) {
            throw new AssertionError("Uses result of assign is not supported.");
          } else {
            JStatement stmt = expr.makeStatement();
            stmt.setCatchBlocks(currentCatchBlocks);
            transformationRequest.append(new Remove(expr));
            assert insertStatement != null;
            transformationRequest.append(new AppendBefore(insertStatement, stmt));
          }
        } else if (expr.getType() == JPrimitiveTypeEnum.VOID.getType()) {
          // Splits expressions contained into another expression or into a statement
          JStatement stmt = expr.makeStatement();
          stmt.setCatchBlocks(currentCatchBlocks);
          transformationRequest.append(new Remove(expr));
          assert insertStatement != null;
          transformationRequest.append(new AppendBefore(insertStatement, stmt));
        } else {
          SourceInfo sourceInfo = expr.getSourceInfo();
          JType type = expr.getType();
          JLocal tempLocal =
              localVarCreator.createTempLocal(type, sourceInfo, transformationRequest);

          JLocalRef localRef = tempLocal.makeRef(sourceInfo);
          transformationRequest.append(new Replace(expr, localRef));
          assert !(expr instanceof JAsgOperation);
          JBinaryOperation newBin =
              new JAsgOperation(sourceInfo, tempLocal.makeRef(sourceInfo), expr);

          JStatement stmt = newBin.makeStatement();
          stmt.setCatchBlocks(currentCatchBlocks);
          assert insertStatement != null;
          transformationRequest.append(new AppendBefore(insertStatement, stmt));
        }
      }

      transformationRequest.commit();
    }

    @Override
    public boolean visit(@Nonnull JStatement stmt) {
      insertStatement = stmt;
      currentCatchBlocks = new ArrayList<JCatchBlock>(stmt.getJCatchBlocks());
      super.visit(stmt);
      return true;
    }

    @Override
    public void endVisit(@Nonnull JStatement stmt) {
      insertStatement = null;
      currentCatchBlocks.clear();
      List<JStatement> copyOfStmt = new LinkedList<JStatement>(newStmtToVisit);
      newStmtToVisit.clear();
      // Apply 3@ code builder on code generated to support JConditional to JIfStatement
      // transformation
      for (JStatement stmtToVisit : copyOfStmt) {
        accept(stmtToVisit);
      }
      super.endVisit(stmt);
    }

    @Override
    public boolean visit(@Nonnull JConditionalExpression conditional) {
      TransformationRequest transformationRequest = new TransformationRequest(method);

      SourceInfo srcInfo = conditional.getSourceInfo();
      SourceInfo thenSrcInfo = conditional.getThenExpr().getSourceInfo();
      SourceInfo elseSourceInfo = conditional.getElseExpr().getSourceInfo();
      JType exprType = conditional.getType();

      JLocal tempLocal = localVarCreator.createTempLocal(exprType, srcInfo, transformationRequest);

      // If
      JBlock thenBlock = new JBlock(thenSrcInfo);
      JBlock elseBlock = new JBlock(elseSourceInfo);
      JIfStatement ifStmt =
          new JIfStatement(conditional.getIfTest().getSourceInfo(), conditional.getIfTest(),
              thenBlock, elseBlock);
      newStmtToVisit.add(ifStmt);

      FallThroughMarker ftm = conditional.getMarker(FallThroughMarker.class);
      if (ftm != null) {
        ifStmt.addMarker(ftm);
      }

      // Then
      JBinaryOperation assign =
          new JAsgOperation(thenSrcInfo, tempLocal.makeRef(thenSrcInfo), conditional.getThenExpr());
      JStatement assignStmt = assign.makeStatement();
      thenBlock.addStmt(assignStmt);

      // Else
      assign = new JAsgOperation(elseSourceInfo, tempLocal.makeRef(elseSourceInfo),
          conditional.getElseExpr());
      assignStmt = assign.makeStatement();
      elseBlock.addStmt(assignStmt);

      assert insertStatement != null;
      transformationRequest.append(new AppendBefore(insertStatement, ifStmt));
      transformationRequest.append(new Replace(conditional, tempLocal.makeRef(srcInfo)));

      transformationRequest.commit();

      return false;
    }

    @Override
    public boolean visit(@Nonnull JTryStatement tryStmt) {
      accept(tryStmt.getTryBlock());
      // Skip visit of catch args, nothing to do here.
      accept(tryStmt.getCatchBlocks());
      JBlock finallyBlock = tryStmt.getFinallyBlock();
      if (finallyBlock != null) {
        accept(finallyBlock);
      }
      return false;
    }

  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    ThreeAddressCode tcaBuilder = new ThreeAddressCode(method);
    tcaBuilder.accept(method);
    new SubTreeDefinitionMarkersRemover().accept(method);
  }

  private static class SubTreeDefinitionMarkersRemover extends JVisitor {
    @Override
    public boolean visit(@Nonnull JNode node) {
      node.removeMarker(SubTreeDefinitionMarkers.class);
      return super.visit(node);
    }
  }
}
