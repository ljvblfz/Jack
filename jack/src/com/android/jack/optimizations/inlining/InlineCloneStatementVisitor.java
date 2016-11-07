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

package com.android.jack.optimizations.inlining;

import com.google.common.collect.Lists;

import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.CloneStatementVisitor;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import java.util.Deque;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class is used to make a clone of a method body in order to inline it into the caller
 * method's body.
 *
 * It makes use of {@link CloneStatementVisitor} but it produces a clone with some modification.
 *
 * More specifically, return statements will be replaced with {@link JGoto}. Reference to the
 * method's {@link JParameter} will be replaced with a {@link JLocalRef} that temporary holds the
 * value of the arguments. The callsite's catch blocks will also be added to each statement as well.
 */
@Transform(add = {JReturnStatement.class, JGoto.class, JAsgOperation.NonReusedAsg.class,
                  JExpressionStatement.class, JLocalRef.class})
@Use({CloneStatementVisitor.class})
public final class InlineCloneStatementVisitor extends CloneStatementVisitor {
  @Nonnull
  private final List<JCatchBlock> catchBlocks;
  @CheckForNull
  private final JLocal returnLocal;
  @Nonnull
  private final JLabeledStatement returnTarget;
  @CheckForNull
  private final JVariable targetInstanceVar;
  @Nonnull
  private final Deque<JBlock> curBlocks = Lists.newLinkedList();
  @Nonnull
  private final Map<JParameter, JLocal> parameterMap;

  /**
   * @param trRequest Shared TransformationRequest.
   * @param enclosingMethod The method that contains the JMethodCall to the method body that this
   *        class will clone.
   * @param catchBlocks The list of catch blocks that will be added to every statement inside the
   *        method body.
   * @param returnLocal The temporary variable that any return value should be stored at. null if
   *        the target function returns void.
   * @param returnTarget The branch target that the body should use in place of JReturn. null if any
   *        return should remain as a return.
   * @param targetInstanceVar The instance variable of the invocation. null if it is a static call.
   * @param parameterMap Maps the parameter to the temporary local variable that stores the value of
   *        the arguments.
   */
  public InlineCloneStatementVisitor(@Nonnull TransformationRequest trRequest,
      @Nonnull JMethod enclosingMethod, @Nonnull List<JCatchBlock> catchBlocks,
      @CheckForNull JLocal returnLocal, @Nonnull JLabeledStatement returnTarget,
      @CheckForNull JVariable targetInstanceVar, @Nonnull Map<JParameter, JLocal> parameterMap) {
    super(trRequest, enclosingMethod);
    this.catchBlocks = catchBlocks;
    this.returnLocal = returnLocal;
    this.returnTarget = returnTarget;
    this.targetInstanceVar = targetInstanceVar;
    this.parameterMap = parameterMap;
  }

  @Nonnull
  public JBlock cloneMethodBody(@Nonnull JMethodBody body) {
    assert !body.getMethod().getType().equals(JPrimitiveTypeEnum.VOID.getType())
        || returnLocal == null;
    assert body.getMethod().isStatic() || targetInstanceVar != null;
    return cloneStatement(body.getBlock());
  }

  @Override
  public boolean visit(@Nonnull JBlock orgBlock) {
    JBlock newBlock = new JBlock(orgBlock.getSourceInfo());
    curBlocks.push(newBlock);
    for (JStatement stmt : orgBlock.getStatements()) {
      JStatement newStmt = internalCloneStatement(stmt);
      newBlock.addStmt(newStmt);
    }
    curBlocks.poll();
    statement = updateCatchBlockList(newBlock, orgBlock);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JReturnStatement returnStmt) {
    SourceInfo info = returnStmt.getSourceInfo();
    JExpression expr = returnStmt.getExpr();
    JStatement newStmt;
    if (expr != null) {
      if (returnLocal == null) {
        newStmt = new JExpressionStatement(info, cloneExpression(expr));
      } else {
        newStmt = new JExpressionStatement(info,
            new JAsgOperation(info, returnLocal.makeRef(info), cloneExpression(expr)));
      }
      curBlocks.peek().addStmt(newStmt);
      updateCatchBlockList(newStmt, returnStmt);
    }
    statement = updateCatchBlockList(new JGoto(info, returnTarget), returnStmt);
    return false;
  }

  /**
   * @return The cloned statement with the catch block list updated
   */
  @Override
  @Nonnull
  protected JStatement updateCatchBlockList(@Nonnull JStatement clonedStmt,
      @Nonnull JStatement orignalStmt) {
    JStatement result = super.updateCatchBlockList(clonedStmt, orignalStmt);
    for (JCatchBlock catchBlock : catchBlocks) {
      result.appendCatchBlock(catchBlock);
    }
    return result;
  }

  @Override
  public boolean visit(@Nonnull JParameterRef ref) {
    expression = parameterMap.get(ref.getTarget()).makeRef(ref.getSourceInfo());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JThisRef ref) {
    assert targetInstanceVar != null;
    expression = targetInstanceVar.makeRef(ref.getSourceInfo());
    return false;
  }

  @Nonnull
  @Override
  protected String cloneLocalName(@Nonnull String orgName) {
    return super.cloneLocalName(orgName) + "_cloned";
  }
}
