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

package com.android.jack.optimizations.valuepropagation.argument;

import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JStatement;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/** Collecting method call argument values */
@Description("Argument value propagation, collecting method call argument values")
@Constraint(need = { ControlFlowGraph.class,
                     JMethodCall.class })
@Transform(add = { TaintedVirtualMethodsMarker.class,
                   TypeMethodCallArgumentsMarker.class })
@Name("ArgumentValuePropagation: CollectMethodCallArguments")
public class AvpCollectMethodCallArguments extends AvpSchedulable
    implements RunnableSchedulable<JMethod> {

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isAbstract() || method.isNative()) {
      return;
    }

    ControlFlowGraph cfg = method.getMarker(ControlFlowGraph.class);
    assert cfg != null;

    for (BasicBlock bb : cfg.getNodes()) {
      for (JStatement stmt : bb.getStatements()) {
        if (stmt instanceof JExpressionStatement) {
          JExpression expr = ((JExpressionStatement) stmt).getExpr();
          if (expr instanceof JAsgOperation) {
            expr = ((JAsgOperation) expr).getRhs();
          }
          if (expr instanceof JMethodCall) {
            JMethodCall call = (JMethodCall) expr;
            JMethodIdWide methodId = call.getMethodIdWide();
            // Track only method calls with at least one parameter
            if (methodId.getParamTypes().size() > 0) {
              TypeMethodCallArgumentsMarker.markCallOnReceiverType(
                  getMethodSignature(methodId, call.getType()), call);
            }
          }
        }
      }
    }
  }
}
