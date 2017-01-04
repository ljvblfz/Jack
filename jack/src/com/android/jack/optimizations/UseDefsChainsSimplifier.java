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
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JLock;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JNumberValueLiteral;
import com.android.jack.ir.ast.JReinterpretCastOperation;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JUnlock;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.CloneExpressionVisitor;
import com.android.jack.util.ControlFlowHelper;
import com.android.jack.util.DefsAndUsesChainOptimizationTools;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Optimization will transform
 * a = b // called s0
 * ...
 * m(a) // called s1
 * will be transform to:
 * a = b
 * ...
 * m(b)
 *
 * Optimization can be apply if the following conditions are respected:
 *  - condition (0) a into s1 must used only one definition of a
 *  - condition (1) a must have a value and b must be variable reference or a literal value without
 *  side effect
 *  - condition (2) if b is a variable reference then if s0 and s1 are in the same block then the
 *  variable b must not be redefine between statement s0 and s1
 *  - condition (3) if b is a variable then if s0 and s1 are not into the same block then all
 *  definitions of b used by s0 must reach the block containing s1 and all of these definitions
 *  must dominates s0. The block that contains s1 must not redefine b between the beginning of
 *  this block and s1
 *
 * Temporary restriction of condition(3)
 * if several definitions of b exists, Jack must checks that all definition statements dominate s0.
 * Currently, as it can not be done efficiently (without walk through the cfg) we do not optimize
 * this case and restrict optimization to case where b has only one definitions.
 */
@Description("Simplify use definitions chains.")
@Constraint(need = {UseDefsMarker.class, ThreeAddressCodeForm.class, ControlFlowGraph.class})
@Use(DefsAndUsesChainOptimizationTools.class)
@Support(Optimizations.UseDefSimplifier.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class UseDefsChainsSimplifier extends DefUsesAndUseDefsChainsSimplifier
    implements RunnableSchedulable<JMethod> {

  @Nonnull
  private static final StatisticId<Counter> SIMPLIFIED_USE_DEF_SYNTH = new StatisticId<Counter>(
      "jack.optimization.usedef.synthetic", "Synthetic use def chain simplified",
      CounterImpl.class, Counter.class);

  @Nonnull
  private static final StatisticId<Counter> SIMPLIFIED_USE_DEF = new StatisticId<Counter>(
      "jack.optimization.usedef.java", "Use def chain simplified",
      CounterImpl.class, Counter.class);

  @Nonnull
  private static final StatisticId<Counter> SIMPLIFIED_USE_DEF_WITH_CST = new StatisticId<Counter>(
      "jack.optimization.usedef.constant", "Use def chain with constant simplified",
      CounterImpl.class, Counter.class);

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  private final boolean optimizeCstDef =
      ThreadConfig.get(Optimizations.UseDefSimplifier.OPTIMIZE_CST_DEF).booleanValue();

  private class Visitor extends JVisitor {

    @Override
    public boolean visit(@Nonnull JStatement s1) {
      List<JVariableRef> varsUsedBys1 = DefsAndUsesChainOptimizationTools.getUsedVariables(s1);

      // Copy variable used by s1 to update the list during the visit
      for (JVariableRef varRefOfa : varsUsedBys1.toArray(new JVariableRef[varsUsedBys1.size()])) {
        List<DefinitionMarker> defsOfa =
            DefsAndUsesChainOptimizationTools.getUsedDefinitions(varRefOfa);

        // Condition(0)
        if (defsOfa.size() == 1) {
          DefinitionMarker defOfa = defsOfa.get(0);

          if (stmtCanBeOptimized(s1, defOfa)) {

            if (defOfa.getDefinedVariable().isSynthetic()) {
              tracer.getStatistic(SIMPLIFIED_USE_DEF_SYNTH).incValue();
            } else {
              tracer.getStatistic(SIMPLIFIED_USE_DEF).incValue();
            }

            JExpression exprValueOfa = defOfa.getValue();
            TransformationRequest tr = new TransformationRequest(s1);

            if (isLiteralWithoutSideEffect(exprValueOfa)) {
              tracer.getStatistic(SIMPLIFIED_USE_DEF_WITH_CST).incValue();
              JExpression newExpr = new CloneExpressionVisitor().cloneExpression(exprValueOfa);
              if (newExpr instanceof JNullLiteral) {
                newExpr = new JReinterpretCastOperation(newExpr.getSourceInfo(),
                    varRefOfa.getType(), newExpr);
              }
              tr.append(new Replace(varRefOfa, newExpr));
            } else {
              JVariableRef varRefb = (JVariableRef) exprValueOfa;
              JVariableRef newVarRefb = getNewVarRef(varRefb, varRefOfa.getSourceInfo());

              UseDefsMarker udmOfNewVarRefb = new UseDefsMarker();
              udmOfNewVarRefb.addUsedDefinitions(
                  DefsAndUsesChainOptimizationTools.getUsedDefinitions(varRefb), newVarRefb);
              newVarRefb.addMarker(udmOfNewVarRefb);

              varsUsedBys1.add(newVarRefb);
              varsUsedBys1.remove(varRefOfa);

              defOfa.removeUse(varRefOfa);

              tr.append(new Replace(varRefOfa, newVarRefb));
            }
            tr.commit();
          }
        }
      }

      return super.visit(s1);
    }

    private boolean isLiteralWithoutSideEffect(@Nonnull JExpression expr) {
      return expr instanceof JBooleanLiteral || expr instanceof JNullLiteral
          || expr instanceof JNumberValueLiteral;
    }

    private boolean stmtCanBeOptimized(@Nonnull JStatement s1, @Nonnull DefinitionMarker defOfa) {
      // Condition (1)
      if (!defOfa.hasValue()) {
        return false;
      }

      // Condition (1)
      if (defOfa.getValue() instanceof JVariableRef) {
        JVariableRef varRefb = (JVariableRef) defOfa.getValue();
        BasicBlock bbOfs1 = ControlFlowHelper.getBasicBlock(s1);
        JStatement s0 = defOfa.getStatement();
        assert s0 != null;
        BasicBlock bbOfs0 = ControlFlowHelper.getBasicBlock(s0);
        JVariable b = varRefb.getTarget();

        // Condition (2)
        if (bbOfs0 == bbOfs1) {
          if (!hasLocalDef(b, bbOfs0, s0, s1)) {
            return true;
          }
        } else {
          // Condition (3)
          List<DefinitionMarker> defsOfbUseFroms0 =
              DefsAndUsesChainOptimizationTools.getUsedDefinitions(varRefb);
          if (defsOfbUseFroms0.size() == 1 && bbHasOnlyDefinitions(bbOfs1, b, defsOfbUseFroms0)
              && !hasLocalDef(b, bbOfs1, null, s1)) {
            return true;
          }
        }
        // Condition (1)
      } else if (optimizeCstDef && isLiteralWithoutSideEffect(defOfa.getValue())) {
        // No more check since it is a literal value without side effect and that can not be
        // redefined
        return true;
      }

      return false;
    }

    private boolean bbHasOnlyDefinitions(@Nonnull BasicBlock bb, @Nonnull JVariable var,
        @Nonnull List<DefinitionMarker> defsToFound) {
      int nbDef = 0;

      for (DefinitionMarker def : DefsAndUsesChainOptimizationTools.getReachingDefs(bb)) {
        if (def.getDefinedVariable() == var) {
          if (defsToFound.contains(def)) {
            nbDef++;
          } else {
            return false;
          }
        }
      }

      return defsToFound.size() == nbDef;
    }

    @Override
    public boolean visit(@Nonnull JLock x) {
      // Do not optimize lock expression, otherwise Jack can add variable aliasing that result
      // with lock/unlock expressions which do not use the same register, and it is not supported
      // by the runtime
      return false;
    }

    @Override
    public boolean visit(@Nonnull JUnlock x) {
      // Do not optimize unlock expression, otherwise Jack can add variable aliasing that result
      // with lock/unlock expressions which do not use the same register, and it is not supported
      // by the runtime
      return false;
    }

    @Override
    public boolean visit(@Nonnull JIfStatement jIf) {
      super.visit(jIf);
      accept(jIf.getIfExpr());
      return false;
    }

    @Override
    public boolean visit(@Nonnull JSwitchStatement switchStmt) {
      super.visit(switchStmt);
      this.accept(switchStmt.getExpr());
      return false;
    }
  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    ControlFlowGraph cfg = method.getMarker(ControlFlowGraph.class);
    assert cfg != null;

    Visitor visitor = new Visitor();

    for (BasicBlock bb : cfg.getNodes()) {
      for (JStatement stmt : bb.getStatements()) {
        visitor.accept(stmt);
      }
    }
  }
}
