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

package com.android.jack.optimizations.tailrecursion;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.annotations.DisableTailRecursionOptimisation;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JLabel;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JStatementList;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.request.AddJLocalInMethodBody;
import com.android.jack.transformations.request.AppendStatement;
import com.android.jack.transformations.request.PrependStatement;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.NamingTools;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.util.ArrayList;
import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This visitor optimizes tail recursive calls
 */
@Description("Optimizes tail recursive calls")
@Constraint(need = {JReturnStatement.class, JMethodCall.class},
            no = {ThreeAddressCodeForm.class})
@Transform(add = {JLabeledStatement.class,
    JLabel.class,
    JBlock.class,
    JLocal.class,
    JAsgOperation.class,
    JLocalRef.class,
    JParameterRef.class,
    JExpressionStatement.class,
    JGoto.class})
public class TailRecursionOptimizer implements RunnableSchedulable<JMethod> {
  /*
   This schedulable transforms detected tail recursive calls to a series of
   statements that compute arguments into temporary variables, store them
   into proper argument variables and perform goto to the beginning of method.
   Example:

   int factTail(int k, int ret) {
      if (k > 0) {
        java.lang.System.out.println(k);
        return test1.Test.factTail(k - 1, ret * k);
      }
      return ret;
    }

    transforms to:

    int factTail(int k, int ret) {
      method.start : {
      }
      if (k > 0) {
        java.lang.System.out.println(k);
        tmp.k = k - 1;
        tmp.ret = ret * k;
        k = tmp.k;
        ret = tmp.ret;
        goto method.start;
      }
      return ret;
    }

    Current implementation doesn't handle void type tail recursion, because
    it's difficult to check if a recursive call is the last instruction.
  */
  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private static final StatisticId<Counter> TAIL_RECURSION_OPTS = new StatisticId< Counter >(
      "jack.optimization.tail-recursion", "Tail recursion optimizations",
      CounterImpl.class, Counter.class);

  @Nonnull
  private final JAnnotationType annotationType =
    Jack.getSession().getPhantomLookup().getAnnotationType(
    NamingTools.getTypeSignatureName(DisableTailRecursionOptimisation.class.getName()));

  private class TailRecursionVisitor extends JVisitor {

    @Nonnull
    private final JMethod enclosingMethod;

    @Nonnull
    private final TransformationRequest tr;

    @CheckForNull
    private JLabeledStatement labeledFirstStatement = null;

    private TailRecursionVisitor(@Nonnull JMethod method,
        @Nonnull TransformationRequest tr) {
      this.enclosingMethod = method;
      this.tr = tr;
    }

    private void labelFirstStatement() {
      JMethodBody body = (JMethodBody) enclosingMethod.getBody();
      assert body != null;
      JBlock block = body.getBlock();
      JStatement firstStatement = block.getStatements().get(0);
      assert firstStatement != null;
      if (firstStatement instanceof JLabeledStatement) {
        labeledFirstStatement = (JLabeledStatement) firstStatement;
      } else {
        SourceInfo srcInfo = firstStatement.getSourceInfo();
        labeledFirstStatement = new JLabeledStatement(srcInfo, new JLabel(srcInfo, "method.start"),
            new JBlock(srcInfo));
        tr.append(new PrependStatement(block, labeledFirstStatement));
      }
    }

    @Override
    public boolean visit(@Nonnull JTryStatement tryStatement) {
      return false;
    }

    @Override
    public boolean visit(@Nonnull JReturnStatement returnStatement) {
      JExpression retExpr = returnStatement.getExpr();
      if (retExpr instanceof JMethodCall) {
        JMethodCall methodCall = (JMethodCall) retExpr;
        JExpression instance = methodCall.getInstance();
        if (methodCall.getMethodId().equals(enclosingMethod.getMethodId())
            && (instance == null || (instance.getType().isSameType(methodCall.getReceiverType())
            && instance instanceof JThisRef))) {
          tracer.getStatistic(TAIL_RECURSION_OPTS).incValue();
          if (labeledFirstStatement == null) {
            labelFirstStatement();
          }
          assert labeledFirstStatement != null;
          SourceInfo srcInfo = returnStatement.getSourceInfo();
          JMethodBody body = (JMethodBody) enclosingMethod.getBody();
          assert body != null;

          Iterator<JParameter> paramIt = enclosingMethod.getParams().iterator();
          Iterator<JExpression> exprIt = methodCall.getArgs().iterator();
          ArrayList<JStatement> tmpAssignments  =
              new ArrayList<JStatement>();
          ArrayList<JStatement> argAssignments =
              new ArrayList<JStatement>();
          while (paramIt.hasNext() && exprIt.hasNext()) {
            JParameter param = paramIt.next();
            JExpression expr = exprIt.next();
            JLocal tempVar = new JLocal(srcInfo, "tmp." + param.getName(),
                param.getType(), JModifier.FINAL, body);
            tr.append(new AddJLocalInMethodBody(tempVar, body));
            JAsgOperation asgToTemp = new JAsgOperation(srcInfo,
                new JLocalRef(srcInfo, tempVar), expr);
            JExpressionStatement asgToTempStmt =
                new JExpressionStatement(srcInfo, asgToTemp);
            tmpAssignments.add(asgToTempStmt);
            JAsgOperation tempToArg = new JAsgOperation(srcInfo,
                new JParameterRef(srcInfo, param),
                new JLocalRef(srcInfo, tempVar));
            JStatement tempToArgStmt = new JExpressionStatement(srcInfo, tempToArg);
            argAssignments.add(tempToArgStmt);
          }

          for (JStatement asgStmt : tmpAssignments) {
            tr.append(
                new AppendStatement((JStatementList) returnStatement.getParent(), asgStmt));
          }
          for (JStatement asgStmt : argAssignments) {
            tr.append(
                new AppendStatement((JStatementList) returnStatement.getParent(), asgStmt));
          }

          JGoto tailCall = new JGoto(returnStatement.getSourceInfo(), labeledFirstStatement);
          tailCall.setCatchBlocks(returnStatement.getJCatchBlocks());
          tr.append(new AppendStatement((JStatementList) returnStatement.getParent(), tailCall));
          tr.append(new Remove(returnStatement));
        }
      }
      return false;
    }

  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.getEnclosingType().isExternal() || method.isNative() || method.isAbstract()
        || method.isSynthetic() || !filter.accept(this.getClass(), method)
        || !method.getAnnotations(annotationType).isEmpty()
        || !method.getEnclosingType().getAnnotations(annotationType).isEmpty()) {
      return;
    }

    TransformationRequest request = new TransformationRequest(method);
    TailRecursionVisitor visitor = new TailRecursionVisitor(method, request);
    visitor.accept(method);
    request.commit();
  }

}