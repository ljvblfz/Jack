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

import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.AppendBefore;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import java.util.ArrayList;
import javax.annotation.Nonnull;

/** Helps replace expression with another expression. */
@Transform(add = {
    JAsgOperation.NonReusedAsg.class,
    JExpressionStatement.class,
    JLocalRef.class })
@Use(LocalVarCreator.class)
public class ExpressionReplaceHelper {
  @Nonnull
  private final LocalVarCreator varCreator;

  public ExpressionReplaceHelper(@Nonnull LocalVarCreator varCreator) {
    this.varCreator = varCreator;
  }

  /** Replaces the expression with literal value, adds a temporary if needed. */
  public void replace(
      @Nonnull JExpression expr, @Nonnull JValueLiteral value,
      @Nonnull TransformationRequest request) {

    if (expr.canThrow() || !value.canThrow()) {
      // Simple case, don't need a local
      request.append(new Replace(expr, value));
      return;
    }

    // When the expression being replaced was not throwing, but the
    // expression being inserted is throwing we introduce a temporary
    // to ensure we don't have two throwing expressions in the same
    // basic block.

    SourceInfo si = value.getSourceInfo();

    // Step outside the expression
    JStatement stmt = expr.getParent(JStatement.class);

    // Create a local with the value
    JLocal tmp = varCreator.createTempLocal(value.getType(), si, request);
    JAsgOperation assign = new JAsgOperation(si, tmp.makeRef(si), value);
    JExpressionStatement stmtAssignment = new JExpressionStatement(si, assign);
    stmtAssignment.setCatchBlocks(new ArrayList<>(stmt.getJCatchBlocks()));

    request.append(new Replace(expr, tmp.makeRef(si)));
    request.append(new AppendBefore(stmt, stmtAssignment));
  }
}
