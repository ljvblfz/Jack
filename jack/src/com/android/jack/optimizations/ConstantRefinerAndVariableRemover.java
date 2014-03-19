/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.optimizations;

import com.android.jack.Options;
import com.android.jack.analysis.DefinitionMarker;
import com.android.jack.analysis.UseDefsMarker;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperator;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JCastOperation;
import com.android.jack.ir.ast.JCharLiteral;
import com.android.jack.ir.ast.JDoubleLiteral;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JFloatLiteral;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNumberLiteral;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.Number;
import com.android.jack.ir.impl.CloneExpressionVisitor;
import com.android.jack.transformations.ast.ImplicitCast;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
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

import javax.annotation.Nonnull;
/**
 * Remove and refine constant variables.
 */
@Description("Remove and refine constant variables.")
@Constraint(need = {UseDefsMarker.class, ControlFlowGraph.class},
    no = {ImplicitCast.class})
@Transform(add = {JByteLiteral.class,
    JCharLiteral.class,
    JShortLiteral.class,
    JLongLiteral.class,
    JFloatLiteral.class,
    JDoubleLiteral.class})
public class ConstantRefinerAndVariableRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  public static final StatisticId<Counter> REFINED_CONSTANT = new StatisticId<Counter>(
      "jack.constant.refined", "Refined constant",
      CounterImpl.class, Counter.class);

  @Nonnull
  public static final StatisticId<Counter> REMOVED_CONSTANT_VARIABLE = new StatisticId<Counter>(
      "jack.constant.variable.removed", "Variable removed since they are constant",
      CounterImpl.class, Counter.class);

  @Nonnull
  public static final StatisticId<Counter> CONSTANT_MOVE_TO_HIS_USAGE = new StatisticId<Counter>(
      "jack.constant.moved", "Constant moved to his usage",
      CounterImpl.class, Counter.class);

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Nonnull
    private final JMethod method;

    @Nonnull
    private final Tracer tracer;

    @Nonnull
    private final CloneExpressionVisitor cloneExpr = new CloneExpressionVisitor();

    public Visitor(@Nonnull JMethod method) {
      this.method = method;
      tracer = TracerFactory.getTracer();
    }

    @Override
    public boolean visit(@Nonnull JDynamicCastOperation cast) {
      boolean deepVisit = super.visit(cast);

      JExpression castedExpr = cast.getExpr();
      if (castedExpr instanceof JNumberLiteral && cast.getCastType() instanceof JPrimitiveType) {
        TransformationRequest tr = new TransformationRequest(method);
        SourceInfo si = cast.getSourceInfo();
        Number numberValue = ((JNumberLiteral) castedExpr).getNumber();
        tr.append(new Replace(cast, refineCst(si, numberValue,
            ((JPrimitiveType) cast.getCastType()).getPrimitiveTypeEnum())));
        tracer.getStatistic(REFINED_CONSTANT).incValue();
        tr.commit();
      }

      return deepVisit;
    }

    @Override
    public boolean visit(@Nonnull JBinaryOperation binOp) {
      if (!binOp.getOp().isAssignment()) {
        moveConstantIfNeeded(binOp.getLhs());
        moveConstantIfNeeded(binOp.getRhs());
      } else if (binOp.getOp() == JBinaryOperator.ASG) {
        moveConstantIfNeeded(binOp.getRhs());
      }

      return super.visit(binOp);
    }

    private void moveConstantIfNeeded(@Nonnull JCastOperation expr) {
      moveConstantIfNeeded(expr.getExpr());
    }

    private void moveConstantIfNeeded(@Nonnull JVariableRef varRef) {
      UseDefsMarker udm = varRef.getMarker(UseDefsMarker.class);
      assert udm != null;
      if (udm.isUsingOnlyOneDefinition()) {
        DefinitionMarker dm = udm.getDefs().get(0);

        if (dm.hasValue() && dm.getValue() instanceof JValueLiteral && !dm.getValue().canThrow()) {
          TransformationRequest tr = new TransformationRequest(method);

          if (varRef.getParent() instanceof JCastOperation) {
            JCastOperation cast = (JCastOperation) varRef.getParent();
            if (cast.getCastType() == dm.getValue().getType()) {
              // Remove useless cast directly since it trigger new opportunities.
              tr.append(new Replace(cast, cloneExpr.cloneExpression(dm.getValue())));
            } else {
              if (dm.getValue() instanceof JNumberLiteral
                  && cast.getCastType() instanceof JPrimitiveType) {
                SourceInfo si = cast.getSourceInfo();
                Number numberValue = ((JNumberLiteral) dm.getValue()).getNumber();
                tr.append(new Replace(cast, refineCst(si, numberValue,
                    ((JPrimitiveType) cast.getCastType()).getPrimitiveTypeEnum())));
                tracer.getStatistic(REFINED_CONSTANT).incValue();
              } else {
                tr.append(new Replace(varRef, cloneExpr.cloneExpression(dm.getValue())));
              }
            }
          } else {
            tr.append(new Replace(varRef, cloneExpr.cloneExpression(dm.getValue())));
          }

          udm.removeAllUsedDefinitions(varRef);
          tracer.getStatistic(CONSTANT_MOVE_TO_HIS_USAGE).incValue();


          if (dm.isUnused()) {
            tr.append(new Remove(dm.getDefinition().getParent()));
            tracer.getStatistic(REMOVED_CONSTANT_VARIABLE).incValue();
          }

          tr.commit();
        }
      }
    }

    private void moveConstantIfNeeded(@Nonnull JExpression expr) {
      if (expr instanceof JVariableRef) {
        moveConstantIfNeeded((JVariableRef) expr);
      } else if (expr instanceof JCastOperation) {
        moveConstantIfNeeded((JCastOperation) expr);
      }
    }

    @Nonnull
    private JValueLiteral refineCst(@Nonnull SourceInfo si, @Nonnull Number numberValue,
        @Nonnull JPrimitiveTypeEnum destType) {
      switch (destType) {
        case BOOLEAN:
          assert numberValue.byteValue() == 1 || numberValue.byteValue() == 0;
          return (new JBooleanLiteral(si, numberValue.byteValue() == 1 ? true : false));
        case BYTE:
          return (new JByteLiteral(si, numberValue.byteValue()));
        case SHORT:
          return (new JShortLiteral(si, numberValue.shortValue()));
        case CHAR:
          return (new JCharLiteral(si, numberValue.charValue()));
        case INT:
          return (new JIntLiteral(si, numberValue.intValue()));
        case FLOAT:
          return (new JFloatLiteral(si, numberValue.floatValue()));
        case DOUBLE:
          return (new JDoubleLiteral(si, numberValue.doubleValue()));
        case LONG:
          return (new JLongLiteral(si, numberValue.longValue()));
        default:
          throw new AssertionError("Type not supported to refine a constant");
      }
    }

    @Override
    public boolean visit(@Nonnull JIfStatement jIf) {
      visit((JStatement) jIf);
      accept(jIf.getIfExpr());
      return false;
    }

    @Override
    public boolean visit(@Nonnull JSwitchStatement switchStmt) {
      this.accept(switchStmt.getExpr());
      return false;
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.getEnclosingType().isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    ControlFlowGraph cfg = method.getMarker(ControlFlowGraph.class);
    assert cfg != null;

    Visitor visitor = new Visitor(method);

    for (BasicBlock bb : cfg.getNodes()) {
      for (JStatement stmt : bb.getStatements()) {
        visitor.accept(stmt);
      }
    }
  }
}