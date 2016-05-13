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

package com.android.jack.optimizations.common;

import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JEqOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.ast.splitnew.SplitNewInstance;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import java.util.Collections;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Helps create nullability checks for preserving JLS-compliance
 * needed in some optimizations.
 */
@Transform(add = { JBlock.class,
                   JEqOperation.class,
                   JExpressionStatement.class,
                   JIfStatement.class,
                   JNullLiteral.class,
                   JThrowStatement.class },
    remove = JNewInstance.class)
@Use({ LocalVarCreator.class,
       SplitNewInstance.NewExpressionSplitter.class })
public final class JlsNullabilityChecker {
  @Nonnull
  private final LocalVarCreator varCreator;
  @Nonnull
  private final JPhantomLookup getPhantomLookup;

  public JlsNullabilityChecker(
      @Nonnull LocalVarCreator varCreator,
      @Nonnull JPhantomLookup getPhantomLookup) {
    this.varCreator = varCreator;
    this.getPhantomLookup = getPhantomLookup;
  }

  /**
   * If the expression can be null, creates an 'if' statement of the following form and
   * inserts it before the statement including the expression:
   * <pre>
   * if ([expr] == null) {
   *   throw new NullPointerException();
   * }
   * </pre>
   */
  @CheckForNull
  public JStatement createNullCheckIfNeeded(
      @Nonnull JExpression expr, @Nonnull TransformationRequest request) {
    if (expr instanceof JThisRef) {
      return null;
    }

    SourceInfo srcInfo = expr.getSourceInfo();
    return new JIfStatement(
        srcInfo,
        new JEqOperation(srcInfo, expr, new JNullLiteral(srcInfo)),
        createThenBlock(srcInfo, request),
        null);
  }

  @Nonnull
  private JBlock createThenBlock(
      @Nonnull SourceInfo srcInfo,
      @Nonnull TransformationRequest request) {

    JClass exceptionType =
        getPhantomLookup
            .getClass(CommonTypes.JAVA_LANG_NULL_POINTER_EXCEPTION);
    JNewInstance newInstance = new JNewInstance(srcInfo, exceptionType,
        exceptionType.getOrCreateMethodIdWide(
            NamingTools.INIT_NAME,
            Collections.<JType>emptyList(),
            MethodKind.INSTANCE_NON_VIRTUAL));
    JExpression[] expressions = SplitNewInstance.NewExpressionSplitter
        .splitNewInstance(newInstance, request, varCreator);

    // NOTE: the code below relies on exact structure of the returned
    //       array, it should consist of three expressions, the last of
    //       which is the resulting exception instance we are throwing
    assert expressions.length == 3;
    JBlock block = new JBlock(srcInfo);
    block.addStmt(new JExpressionStatement(srcInfo, expressions[0]));
    block.addStmt(new JExpressionStatement(srcInfo, expressions[1]));
    block.addStmt(new JThrowStatement(srcInfo, expressions[2]));
    return block;
  }
}
