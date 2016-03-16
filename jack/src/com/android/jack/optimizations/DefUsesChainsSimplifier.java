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
import com.android.jack.analysis.dfa.reachingdefs.ReachingDefsMarker;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.ControlFlowHelper;
import com.android.jack.util.OptimizationTools;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Optimization will transform
 * Path 1
 * a = true   // s1
 * ...
 * Path 2
 * a = false  // s2
 * ...
 * Path 3 target of path 1 & 2
 * b = a      // s0
 *
 * To:
 *
 * Path 1
 * b = true
 * ...
 * Path2
 * b = false
 * ...
 * Path 3 target of path 1 & 2
 *
 * Optimization can be apply if the following conditions are respected:
 * - Condition (0) s0 and s1 are into the same block then b must not defined between s1 and s0
 * - Condition (1) s0 and s1 are not into the same block then b must not be defined into the
 * block of s0 and before s0
 * - Condition (2) s0 and s1 are not into the same block then b must not be defined into one of
 *  the block of s1 to sn and after one statement from s1 to sn
 *  - Condition (3) s0 and s1 are not into the same block then all definitions of b reaching
 *  the block of s0 must be know before statement s1 from sn
 */
@Description("Simplify definition uses chains.")
@Constraint(need = {DefinitionMarker.class, UseDefsMarker.class, ThreeAddressCodeForm.class,
    ControlFlowGraph.class})
// ReachingDefsMarker is no longer valid after this optimization, thus remove it directly
@Transform(remove = {ReachingDefsMarker.class})
@Support(Optimizations.DefUseSimplifier.class)
public class DefUsesChainsSimplifier extends DefUsesAndUseDefsChainsSimplifier
    implements RunnableSchedulable<JMethod> {

  @Nonnull
  public static final StatisticId<Counter> SIMPLIFIED_DEF_USE = new StatisticId<Counter>(
      "jack.optimization.defuse", "Def use chain simplified",
      CounterImpl.class, Counter.class);

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  private class Visitor extends JVisitor {

    @Nonnull
    private final JMethod method;

    public Visitor(@Nonnull JMethod method) {
      this.method = method;
    }

    @Override
    public boolean visit(@Nonnull JBinaryOperation binary) {
      if (binary instanceof JAsgOperation) {
        DefinitionMarker defOfb = binary.getMarker(DefinitionMarker.class);

        if (defOfb != null && defOfb.hasValue()) {
          JExpression valueExpr = defOfb.getValue();
          assert valueExpr == binary.getRhs();

          if (valueExpr instanceof JVariableRef) {
            JStatement s0 = defOfb.getStatement();
            assert s0 != null;
            List<DefinitionMarker> defsOfa =
                OptimizationTools.getUsedDefinitions((JVariableRef) valueExpr);

            if (canApplyOptimisation(s0, defsOfa, defOfb)) {
              tracer.getStatistic(SIMPLIFIED_DEF_USE).incValue();

              TransformationRequest tr = new TransformationRequest(method);

              for (DefinitionMarker defOfa : defsOfa) {
                tr.append(
                    new Replace(defOfa.getDefinedExpr(), getNewVarRef(defOfb.getDefinedExpr())));
                updateUsagesOfDefinitions(defOfa, defOfb);
              }

              defOfb.removeAllUses();

              tr.append(new Remove(binary.getParent()));
              tr.commit();
            }
          }
        }
      }
      return super.visit(binary);
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

    private boolean canApplyOptimisation(@Nonnull JStatement s0,
        @Nonnull List<DefinitionMarker> defaUsedBys0,
        @Nonnull DefinitionMarker defOfb) {
      BasicBlock bbOfs0 = ControlFlowHelper.getBasicBlock(s0);
      JVariable b = defOfb.getDefinedVariable();

      if (defaUsedBys0.size() == 1) {
        DefinitionMarker defOfa = defaUsedBys0.get(0);
        if (isOptimizableDefinition(defOfa)) {
          JStatement s1 = defOfa.getStatement();
          assert s1 != null;
          BasicBlock bbOfs1 = ControlFlowHelper.getBasicBlock(s1);
          if (bbOfs1 == bbOfs0) {
            // Condition (0)
            return hasLocalDef(b, bbOfs1, s1, s0);
          }
        }
      }

      // Condition (1)
      if (hasLocalDef(b, bbOfs0, null, s0)) {
        return false;
      }

      Collection<DefinitionMarker> defsOfbReachingBbOfs0 =
          OptimizationTools.getReachingDefs(bbOfs0, b);

      for (DefinitionMarker defOfa : defaUsedBys0) {
        if (!isOptimizableDefinition(defOfa)) {
          return false;
        }

        JStatement stmtOfa = defOfa.getStatement();
        assert stmtOfa != null;
        BasicBlock bbOfa = ControlFlowHelper.getBasicBlock(stmtOfa);

        // Condition (2)
        if (hasLocalDef(b, bbOfa, stmtOfa, null)) {
          return false;
        } else {
          DefinitionMarker previousDefOfB = getLastLocalDef(b, bbOfa, null, stmtOfa);
          if (previousDefOfB != null) {
            defsOfbReachingBbOfs0.remove(previousDefOfB);
          } else {
            defsOfbReachingBbOfs0.removeAll(OptimizationTools.getReachingDefs(bbOfa, b));
          }
        }
      }

      // Condition (3)
      return defsOfbReachingBbOfs0.isEmpty();
    }

    private boolean isOptimizableDefinition(@Nonnull DefinitionMarker definition) {
      return (definition.hasValue()
          && definition.getUses().size() == 1
          && definition.getDefinedVariable().isSynthetic());
    }

    private void updateUsagesOfDefinitions(
        @Nonnull DefinitionMarker oldDefOfaAndNewDefOfb, @Nonnull DefinitionMarker oldDefOfb) {
      oldDefOfaAndNewDefOfb.removeAllUses();

      for (JVariableRef useOfOldDefOfb : oldDefOfb.getUses()) {
        oldDefOfaAndNewDefOfb.addUse(useOfOldDefOfb);
      }
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
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

    method.removeMarker(ReachingDefsMarker.class);
  }
}
