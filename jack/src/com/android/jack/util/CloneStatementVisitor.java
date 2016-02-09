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

package com.android.jack.util;

import com.android.jack.ir.ast.JAssertStatement;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JBreakStatement;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JContinueStatement;
import com.android.jack.ir.ast.JDoStatement;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JForStatement;
import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JLabel;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JLock;
import com.android.jack.ir.ast.JLoop;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JUnlock;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.JWhileStatement;
import com.android.jack.transformations.finallyblock.InlinedFinallyMarker;
import com.android.jack.transformations.request.AddJLocalInMethodBody;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.marker.Marker;
import com.android.sched.schedulable.Constraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A general purpose statement cloner.
 * <p>
 * When cloning a block, the target of a {@code JGoto} or a {@code JLocalRef} is set to the cloned
 * version of the target if the target belongs to the group of statements.
 *<p>
 * Warning: The cloning of {@link JParameterRef} whose enclosing method is different than the target
 * method is not supported.
 */
@Constraint(no = {JLoop.class, JBreakStatement.class, JContinueStatement.class,
    JFieldInitializer.class})
public class CloneStatementVisitor extends CloneExpressionVisitor {
  @CheckForNull
  private JStatement statement;

  @Nonnull
  private Map<JLabeledStatement, JLabeledStatement> clonedLabeledStmts = Collections.emptyMap();

  @Nonnull
  private Map<JLocal, JLocal> clonedLocals = Collections.emptyMap();

  @Nonnull
  private Map<JCatchBlock, JCatchBlock> clonedCatchBlocks = Collections.emptyMap();

  @Nonnull
  private List<JGoto> clonedGotos = Collections.emptyList();

  @Nonnull
  private final TransformationRequest trRequest;

  @Nonnull
  private Map<JStatement, JStatement> clonedStmts = Collections.emptyMap();

  @Nonnull
  private List<Marker> clonedMarkers = Collections.emptyList();

  @Nonnull
  private final JMethod targetMethod;

  /**
   * Build a {@code CloneStatementVisitor}.
   * @param trRequest a request for the modifications.
   * @param targetMethod the method whose body will contain the cloned statement.
   */
  public CloneStatementVisitor(@Nonnull TransformationRequest trRequest,
      @Nonnull JMethod targetMethod) {
    this.trRequest = trRequest;
    this.targetMethod = targetMethod;
  }

  public  List<Marker> getClonedMarkers() {
    return clonedMarkers;
  }

  @Nonnull
  public <T extends JStatement> T cloneStatement(@Nonnull T stmt) {

    clonedLabeledStmts = new HashMap<JLabeledStatement, JLabeledStatement>();
    clonedLocals = new HashMap<JLocal, JLocal>();
    clonedGotos = new ArrayList<JGoto>();
    clonedStmts = new HashMap<JStatement, JStatement>();
    clonedMarkers = new ArrayList<Marker>();
    clonedCatchBlocks = new HashMap<JCatchBlock, JCatchBlock>();

    T statement = internalCloneStatement(stmt);

    fixGotos();

    // TODO(mikaelpeltier) Think how to modify marker to reflect cloning
    for (Marker m : clonedMarkers) {
      if (m instanceof InlinedFinallyMarker) {
        InlinedFinallyMarker newMarker = (InlinedFinallyMarker) m;
        JStatement newStmt = clonedStmts.get(newMarker.getTryStmt());
        if (newStmt != null) {
          newMarker.setTryStmt((JTryStatement) newStmt);
        }
      }
    }

    return statement;
  }

  private void fixGotos() {
    for (JGoto clonedGoto : clonedGotos) {
      JLabeledStatement target = clonedGoto.getTargetBlock();
      JLabeledStatement clonedTarget = clonedLabeledStmts.get(target);
      if (clonedTarget != null) {
        clonedGoto.replace(target, clonedTarget);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  private <T extends JStatement> T internalCloneStatement(@Nonnull T stmt) {

    JStatement alreadyCloned = clonedStmts.get(stmt);
    if (alreadyCloned != null) {
      return (T) alreadyCloned;
    }

    // double check that the expression is successfully cloned
    statement = null;

    this.accept(stmt);

    JStatement clonedStatement = statement;

    if (clonedStatement == null) {
      throw new AssertionError("Unable to clone statement " + stmt.toString());
    }

    for (Marker m : stmt.getAllMarkers()) {
      Marker newMarker = m.cloneIfNeeded();
      clonedMarkers.add(newMarker);
      clonedStatement.addMarker(newMarker);
    }

    clonedStmts.put(stmt, statement);

    return (T) clonedStatement;
  }

  @Override
  public boolean visit(@Nonnull JAssertStatement assertStatement) {
    JExpression clonedArg = cloneExpression(assertStatement.getArg());
    JExpression clonedTestExpr = cloneExpression(assertStatement.getTestExpr());
    statement =
        updateCatchBlockList(new JAssertStatement(assertStatement.getSourceInfo(), clonedTestExpr,
            clonedArg), assertStatement);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JCatchBlock catchBlock) {
    statement = updateCatchBlockList(cloneCatchBlock(catchBlock), catchBlock);
    return false;
  }

  @Nonnull
  private JCatchBlock cloneCatchBlock(@Nonnull JCatchBlock catchBlock) {
    JCatchBlock newBlock = clonedCatchBlocks.get(catchBlock);

    if (newBlock == null) {
      JLocal clonedLocal = clonedLocals.get(catchBlock.getCatchVar());
      if (clonedLocal == null) {
        clonedLocal = cloneLocal(catchBlock.getCatchVar());
      }
      newBlock =
          new JCatchBlock(catchBlock.getSourceInfo(), catchBlock.getCatchTypes(),
              clonedLocal);
      clonedCatchBlocks.put(catchBlock, newBlock);
      for (JStatement stmt : catchBlock.getStatements()) {
        newBlock.addStmt(internalCloneStatement(stmt));
      }
    }

    return newBlock;
  }

  @Override
  public boolean visit(@Nonnull JBlock block) {
    JBlock newBlock = new JBlock(block.getSourceInfo());
    for (JStatement stmt : block.getStatements()) {
      newBlock.addStmt(internalCloneStatement(stmt));
    }
    statement = updateCatchBlockList(newBlock, block);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JBreakStatement breakStatement) {
    assert false : "Not supported";
    return false;
  }

  @Override
  public boolean visit(@Nonnull JCaseStatement caseStatement) {
    JLiteral caseExpr = caseStatement.getExpr();
    JLiteral clonedCaseExpr = caseExpr != null ? cloneExpression(caseExpr) : null;

    statement =
        updateCatchBlockList(new JCaseStatement(caseStatement.getSourceInfo(), clonedCaseExpr),
            caseStatement);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JContinueStatement continueStatement) {
    assert false : "Not supported";
    return false;
  }

  @Override
  public boolean visit(@Nonnull JFieldInitializer init) {
    throw new AssertionError();
  }

  @Nonnull
  private JLocal cloneLocal(@Nonnull JLocal var) {
    JMethodBody methodBody = (JMethodBody) targetMethod.getBody();
    assert methodBody != null;
    if (methodBody.getLocals().contains(var)) {
      return var;
    }

    JLocal clonedVar =
        new JLocal(var.getSourceInfo(), var.getName(), var.getType(), var.getModifier(),
            methodBody);

    // If parent is JCatchBLock, cloned variable will be add into rather than into method body
    if (!(var.getParent() instanceof JCatchBlock)) {
      trRequest.append(new AddJLocalInMethodBody(clonedVar, methodBody));
    }

    clonedLocals.put(var, clonedVar);
    return clonedVar;
  }

  @Override
  public boolean visit(@Nonnull JDoStatement doStatement) {
    assert false : "Not supported";
    return false;
  }

  @Override
  public boolean visit(@Nonnull JExpressionStatement expressionStatement) {
    statement =
        updateCatchBlockList(cloneExpression(expressionStatement.getExpr()).makeStatement(),
            expressionStatement);
    return false;
  }

  /**
   * @return The cloned statement with the catch block list updated
   */
  @Nonnull
  private JStatement updateCatchBlockList(@Nonnull JStatement clonedStmt,
      @Nonnull JStatement orignalStmt) {
    for (JCatchBlock catchBlocks : orignalStmt.getJCatchBlocks()) {
      clonedStmt.appendCatchBlock(cloneCatchBlock(catchBlocks));
    }
    return clonedStmt;
  }

  @Override
  public boolean visit(@Nonnull JForStatement forStatement) {
    assert false : "Not supported";
    return false;
  }

  @Override
  public boolean visit(@Nonnull JGoto gotoStatement) {
    JGoto newGoto = new JGoto(gotoStatement.getSourceInfo(), gotoStatement.getTargetBlock());
    clonedGotos.add(newGoto);
    statement = updateCatchBlockList(newGoto, gotoStatement);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JIfStatement ifStatement) {
    JExpression clonedCond = cloneExpression(ifStatement.getIfExpr());
    JStatement clonedThen = internalCloneStatement(ifStatement.getThenStmt());
    JStatement elseStmt = ifStatement.getElseStmt();
    JStatement clonedElse = null;

    if (elseStmt != null) {
      clonedElse = internalCloneStatement(elseStmt);
    }

    statement =
        updateCatchBlockList(new JIfStatement(ifStatement.getSourceInfo(), clonedCond, clonedThen,
            clonedElse), ifStatement);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JLabeledStatement labeledStatement) {
    JStatement clonedBody = internalCloneStatement(labeledStatement.getBody());
    JLabel label = labeledStatement.getLabel();
    JLabel newLabel = new JLabel(label.getSourceInfo(), label.getName());
    JLabeledStatement newLabeledStatement = new JLabeledStatement(labeledStatement.getSourceInfo(),
        newLabel, clonedBody);
    clonedLabeledStmts.put(labeledStatement, newLabeledStatement);
    statement = updateCatchBlockList(newLabeledStatement, labeledStatement);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JLock lockStatement) {
    JExpression clonedExpr = cloneExpression(lockStatement.getLockExpr());
    statement =
        updateCatchBlockList(new JLock(lockStatement.getSourceInfo(), clonedExpr), lockStatement);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JUnlock unlockStatement) {
    JExpression clonedExpr = cloneExpression(unlockStatement.getLockExpr());
    statement =
        updateCatchBlockList(new JUnlock(unlockStatement.getSourceInfo(), clonedExpr),
            unlockStatement);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JReturnStatement returnStatement) {
    JExpression clonedExpr = null;
    JExpression expr = returnStatement.getExpr();
    if (expr != null) {
      clonedExpr = cloneExpression(expr);
    }
    statement =
        updateCatchBlockList(new JReturnStatement(returnStatement.getSourceInfo(), clonedExpr),
            returnStatement);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JSwitchStatement switchStatement) {
    JExpression clonedExpr = cloneExpression(switchStatement.getExpr());
    JBlock clonedBody = internalCloneStatement(switchStatement.getBody());
    List<JCaseStatement> cases = switchStatement.getCases();
    List<JCaseStatement> clonedCases = new ArrayList<JCaseStatement>();
    for (JCaseStatement currentCase : cases) {
      clonedCases.add(internalCloneStatement(currentCase));
    }
    JCaseStatement clonedDefaultCase = null;
    JCaseStatement defaultCase = switchStatement.getDefaultCase();
    if (defaultCase != null) {
      clonedDefaultCase = internalCloneStatement(defaultCase);
    }
    statement =
        updateCatchBlockList(new JSwitchStatement(switchStatement.getSourceInfo(), clonedExpr,
            clonedBody, clonedCases, clonedDefaultCase), switchStatement);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JThrowStatement throwStatement) {
    JExpression clonedExpr = cloneExpression(throwStatement.getExpr());
    statement =
        updateCatchBlockList(new JThrowStatement(throwStatement.getSourceInfo(), clonedExpr),
            throwStatement);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JTryStatement tryStatement) {
    JBlock clonedTryBlock = internalCloneStatement(tryStatement.getTryBlock());
    assert clonedTryBlock != null;

    List<JCatchBlock> catchBlocks = tryStatement.getCatchBlocks();
    List<JCatchBlock> clonedCatchBlocks = new ArrayList<JCatchBlock>(catchBlocks.size());
    for (JCatchBlock catchBlock : catchBlocks) {
      clonedCatchBlocks.add(internalCloneStatement(catchBlock));
    }

    JBlock clonedFinallyBlock = null;
    JBlock finallyBlock = tryStatement.getFinallyBlock();
    if (finallyBlock != null) {
      clonedFinallyBlock = internalCloneStatement(finallyBlock);
    }

    List<JStatement> resourcesDeclarations = tryStatement.getResourcesDeclarations();
    List<JStatement> clonedResourcesDeclarations =
        new ArrayList<JStatement>(resourcesDeclarations.size());
    for (JStatement stmt : resourcesDeclarations) {
      clonedResourcesDeclarations.add(internalCloneStatement(stmt));
    }

    statement = updateCatchBlockList(new JTryStatement(tryStatement.getSourceInfo(),
        clonedResourcesDeclarations,
        clonedTryBlock,
        clonedCatchBlocks,
        clonedFinallyBlock), tryStatement);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JWhileStatement whileStatement) {
    assert false : "Not supported";
    return false;
  }

  @Override
  public boolean visit(@Nonnull JThisRef jThisRef) {
    JThis jThis = targetMethod.getThis();
    assert jThis != null;
    assert jThis.getType().isSameType(jThisRef.getType());
    expression = jThis.makeRef(jThisRef.getSourceInfo());
    return false;

  }

  @Override
  public boolean visit(@Nonnull JLocalRef localRef) {
    JLocal clonedLocal = clonedLocals.get(localRef.getLocal());
    if (clonedLocal == null) {
      clonedLocal = cloneLocal(localRef.getLocal());
    }
    expression = clonedLocal.makeRef(localRef.getSourceInfo());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JParameterRef parameterRef) {
    JParameter parameter = parameterRef.getParameter();
    assert parameter.getEnclosingMethod() == targetMethod;
    expression = parameter.makeRef(parameterRef.getSourceInfo());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JLambda lambda) {
    JLambda clonedLambda = new JLambda(lambda.getSourceInfo(), lambda.getMethod(), lambda.getType(),
        lambda.needToCaptureInstance(), lambda.getInterfaceBounds());

    for (JVariableRef capturedVarRef : lambda.getCapturedVariables()) {
      JVariable capturedVar = capturedVarRef.getTarget();
      JVariableRef clonedVarRef = null;
      if (capturedVar instanceof JLocal) {
        JLocal clonedVar = clonedLocals.get(capturedVar);
        if (clonedVar == null) {
          clonedVar = cloneLocal((JLocal) capturedVar);
        }
        clonedVarRef = clonedVar.makeRef(capturedVarRef.getSourceInfo());
      } else {
        assert capturedVar instanceof JParameter;
        clonedVarRef = capturedVar.makeRef(capturedVar.getSourceInfo());
      }
      assert clonedVarRef != null;
      clonedLambda.addCapturedVariable(clonedVarRef);
    }

    expression = clonedLambda;

    new RewriteThisRefOfLambda().accept(lambda.getMethod());

    return false;
  }

  private class RewriteThisRefOfLambda extends JVisitor {
    @Override
    public boolean visit(@Nonnull JThisRef jThisRef) {
      JThis jThis = targetMethod.getThis();
      assert jThis != null;
      assert jThis.getType().isSameType(jThisRef.getType());
      trRequest.append(new Replace(jThisRef, jThis.makeRef(jThisRef.getSourceInfo())));
      return false;
    }
  }

  @Nonnull
  private JParameter getNewParameter(@Nonnull JParameter oldParameter) {
    for (JParameter newParameter : targetMethod.getParams()) {
      if (newParameter.getName().equals(oldParameter.getName())) {
        return newParameter;
      }
    }
    throw new AssertionError();
  }
}
