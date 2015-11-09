/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.transformations.lambda;

import com.google.common.collect.Lists;

import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JLiberateVariable;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.formatter.IdentifierFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.scheduling.feature.SourceVersion8;
import com.android.jack.transformations.request.PrependStatement;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Convert lambda to native support.
 */
@Description("Convert lambda to native support.")
@Constraint(need = JLambda.class)
@Transform(add = CapturedVariable.class)
@Support(SourceVersion8.class)
@Synchronized
public class LambdaNativeSupportConverter implements RunnableSchedulable<JMethod> {

  private static class Visitor extends JVisitor {

    @Nonnull
    private Map<JVariable, JLocal> capturedVar2LiberatedVar = new HashMap<JVariable, JLocal>();

    @Nonnull
    private final Stack<Map<JVariable, JLocal>> stackOfCapturedVar =
        new Stack<Map<JVariable, JLocal>>();

    @Nonnull
    private final TransformationRequest tr;

    @Nonnull
    private final JMethod currentMethod;

    @Nonnegative
    private int anonymousCountByMeth = 0;

    @Nonnull
    private final String lambdaClassNamePrefix;

    public Visitor(@Nonnull TransformationRequest tr, @Nonnull JMethod method) {
      this.tr = tr;
      this.currentMethod = method;
      lambdaClassNamePrefix = NamingTools.getNonSourceConflictingName(
          IdentifierFormatter.getFormatter().getName(method) + "LambdaImpl");
    }

    @Override
    public void endVisit(@Nonnull JLambda x) {
      capturedVar2LiberatedVar = stackOfCapturedVar.pop();
    }

    @Override
    public boolean visit(@Nonnull JLambda lambdaExpr) {
      stackOfCapturedVar.push(capturedVar2LiberatedVar);
      capturedVar2LiberatedVar = new HashMap<JVariable, JLocal>();

      JMethod lambdaMethod = lambdaExpr.getMethod();
      lambdaMethod.getMethodId().setName(lambdaClassNamePrefix + anonymousCountByMeth++);
      lambdaMethod.setModifier(lambdaMethod.getModifier() | JModifier.STATIC);
      JParameter closure = new JParameter(SourceInfo.UNKNOWN, "closure",
          lambdaExpr.getType(), JModifier.DEFAULT, lambdaMethod);
      closure.addMarker(ForceClosureMarker.INSTANCE);
      lambdaMethod.getParams().add(0, closure);

      JMethodBody lambdaMethodBody = (JMethodBody) lambdaMethod.getBody();
      assert lambdaMethodBody != null;

      class CapturedVarToLiberatedVar {
        @Nonnull
        JParameter paramCapturingVar;
        @Nonnull
        JLocal liberatedVariable;

        public CapturedVarToLiberatedVar(@Nonnull JParameter paramCapturingVar,
            @Nonnull JLocal liberatedVariable) {
          this.liberatedVariable = liberatedVariable;
          this.paramCapturingVar = paramCapturingVar;
        }
      }

      List<CapturedVarToLiberatedVar> varToLiberates = new ArrayList<CapturedVarToLiberatedVar>();

      if (lambdaExpr.needToCaptureInstance()) {
        JThis capturedThis = currentMethod.getThis();
        assert capturedThis != null;

        JParameter paramCapturingVar = addParameter(lambdaMethod, capturedThis);

        JLocal liberatedVariable = createLiberateVariable(lambdaMethodBody, capturedThis);

        varToLiberates.add(new CapturedVarToLiberatedVar(paramCapturingVar, liberatedVariable));

        capturedVar2LiberatedVar.put(capturedThis, liberatedVariable);
      }

      for (JVariableRef capturedVarRef : lambdaExpr.getCapturedVariables()) {
        JLocal alreadyCapturedVar = stackOfCapturedVar.peek().get(capturedVarRef.getTarget());
        JVariable capturedVar = capturedVarRef.getTarget();
        if (alreadyCapturedVar != null) {
          tr.append(new Replace(capturedVarRef,
              new JLocalRef(capturedVarRef.getSourceInfo(), alreadyCapturedVar)));
        }

        JParameter paramCapturingVar = addParameter(lambdaMethod, capturedVar);

        JLocal liberatedVariable = createLiberateVariable(lambdaMethodBody, capturedVar);

        varToLiberates.add(new CapturedVarToLiberatedVar(paramCapturingVar, liberatedVariable));

        capturedVar2LiberatedVar.put(capturedVar, liberatedVariable);
      }

      for (CapturedVarToLiberatedVar varToLiberate : Lists.reverse(varToLiberates)) {
        tr.append(new PrependStatement(lambdaMethodBody.getBlock(),
            new JAsgOperation(SourceInfo.UNKNOWN,
                new JLocalRef(SourceInfo.UNKNOWN, varToLiberate.liberatedVariable),
                new JLiberateVariable(SourceInfo.UNKNOWN,
                    new JParameterRef(SourceInfo.UNKNOWN, closure),
                    new JParameterRef(SourceInfo.UNKNOWN, varToLiberate.paramCapturingVar)))
                        .makeStatement()));
      }

      accept(lambdaExpr.getBody());

      return false;
    }

    @Nonnull
    private JParameter addParameter(@Nonnull JMethod lambdaMethod, @Nonnull JVariable capturedVar) {
      JParameter paramCapturingVar =
          new JParameter(capturedVar.getSourceInfo(), "p" + capturedVar.getName(),
              capturedVar.getType(), capturedVar.getModifier(), lambdaMethod);
      lambdaMethod.addParam(paramCapturingVar);
      paramCapturingVar.addMarker(CapturedVariable.INSTANCE);
      return paramCapturingVar;
    }

    @Nonnull
    private JLocal createLiberateVariable(@Nonnull JMethodBody lambdaMethodBody,
        JVariable capturedVar) {
      JLocal liberatedVariable = new JLocal(capturedVar.getSourceInfo(), capturedVar.getName(),
          capturedVar.getType(), capturedVar.getModifier(), lambdaMethodBody);
      lambdaMethodBody.addLocal(liberatedVariable);
      liberatedVariable.updateParents(lambdaMethodBody);
      return liberatedVariable;
    }

    @Override
    public void endVisit(@Nonnull JVariableRef varRef) {
      JLocal liberatedVariable = capturedVar2LiberatedVar.get(varRef.getTarget());
      if (liberatedVariable != null) {
        tr.append(new Replace(varRef, new JLocalRef(varRef.getSourceInfo(), liberatedVariable)));
      }
    }
  }

  @Override
  public void run(JMethod method) throws Exception {
    TransformationRequest request = new TransformationRequest(method);
    Visitor visitor = new Visitor(request, method);
    visitor.accept(method);
    request.commit();
  }
}
