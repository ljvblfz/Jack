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

package com.android.jack.optimizations.cfg;

import com.android.jack.ir.ast.JAlloc;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JEqOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JConditionalBasicBlock;
import com.android.jack.ir.ast.cfg.JConditionalBlockElement;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JMethodCallBlockElement;
import com.android.jack.ir.ast.cfg.JPlaceholderBasicBlock;
import com.android.jack.ir.ast.cfg.JVariableAsgBlockElement;
import com.android.jack.ir.ast.cfg.mutations.CfgFragment;
import com.android.jack.ir.ast.cfg.mutations.ExceptionCatchBlocks;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import java.util.Collections;
import javax.annotation.Nonnull;

/**
 * Helps create nullability checks for preserving JLS-compliance needed
 * in some optimizations. Works on cfg-IR, rather than regular-IR.
 */
@Transform(add = { JAlloc.class, JAsgOperation.NonReusedAsg.class,
                   JEqOperation.class, JMethodCall.class, JNullLiteral.class })
@Use({ LocalVarCreator.class })
public final class CfgJlsNullabilityChecker {
  @Nonnull
  private final JControlFlowGraph cfg;
  @Nonnull
  private final LocalVarCreator varCreator;
  @Nonnull
  private final JPhantomLookup getPhantomLookup;

  public CfgJlsNullabilityChecker(
      @Nonnull JControlFlowGraph cfg,
      @Nonnull LocalVarCreator varCreator,
      @Nonnull JPhantomLookup getPhantomLookup) {
    this.cfg = cfg;
    this.varCreator = varCreator;
    this.getPhantomLookup = getPhantomLookup;
  }

  /**
   * Creates a CFG fragment representing the following 'if' statement:
   * <pre>
   * if ([expr] == null) {
   *   throw new NullPointerException();
   * }
   * </pre>
   */
  @Nonnull
  public CfgFragment createNullCheck(@Nonnull ExceptionCatchBlocks catchBlocks,
      @Nonnull JExpression expr, @Nonnull TransformationRequest request) {
    assert !expr.canThrow();

    // We build the following CFG fragment:
    //
    //              bbEntry (cond: expr == null)
    //           (true)                  (false)
    //             /                        |
    //            /                         |
    //          bbT1                        |
    //    (-t = alloc <NPE>)                |
    //            |                         |
    //          bbT2                        |
    //      (-t.<init>())                   |
    //            |                         |
    //          bbT3                        |
    //        (throw -t)                    |
    //            X                         |
    //                                      |
    //                        bbDone  <-----o
    //
    JClass exceptionType = getPhantomLookup
        .getClass(CommonTypes.JAVA_LANG_NULL_POINTER_EXCEPTION);

    JLocal tmp = varCreator.createTempLocal(exceptionType, SourceInfo.UNKNOWN, request);

    // throw -tmp
    JBasicBlock bbT3 =
        catchBlocks.createThrowBlock(tmp.makeRef(SourceInfo.UNKNOWN));

    // -tmp.<init>()
    JMethodCallBlockElement constructorCall =
        new JMethodCallBlockElement(SourceInfo.UNKNOWN,
            new JMethodCall(SourceInfo.UNKNOWN,
                tmp.makeRef(SourceInfo.UNKNOWN),
                exceptionType,
                exceptionType.getOrCreateMethodIdWide(
                    NamingTools.INIT_NAME,
                    Collections.<JType>emptyList(),
                    MethodKind.INSTANCE_NON_VIRTUAL),
                JPrimitiveType.JPrimitiveTypeEnum.VOID.getType(),
                false));
    JBasicBlock bbT2 =
        catchBlocks.createThrowBlock(bbT3, constructorCall);

    // -tmp = alloc <NPE>
    JVariableAsgBlockElement alloc =
        new JVariableAsgBlockElement(SourceInfo.UNKNOWN,
            new JAsgOperation(SourceInfo.UNKNOWN, tmp.makeRef(SourceInfo.UNKNOWN),
                new JAlloc(SourceInfo.UNKNOWN, exceptionType)));
    JBasicBlock bbT1 =
        catchBlocks.createThrowBlock(bbT2, alloc);

    // Exit block
    JPlaceholderBasicBlock exit = new JPlaceholderBasicBlock(cfg);

    // Conditional block
    JConditionalBasicBlock cond =
        new JConditionalBasicBlock(cfg, bbT1, exit);
    cond.appendElement(
        new JConditionalBlockElement(SourceInfo.UNKNOWN,
            new JEqOperation(SourceInfo.UNKNOWN, expr, new JNullLiteral(SourceInfo.UNKNOWN))));

    return new CfgFragment(cond, exit);
  }
}
