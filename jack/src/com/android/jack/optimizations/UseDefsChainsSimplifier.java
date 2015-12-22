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
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.ControlFlowHelper;
import com.android.jack.util.OptimizationTools;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
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
 *  - condition (1) a must have a value and b must be variable reference
 *  - condition (2) if s0 and s1 are in the same block then the variable b must not be redefine
 *  between statement s0 and s1
 *  - condition (3) if s0 and s1 are not into the same block then all definitions of b used
 *  by s0 must reach the block containing s1 and this block must not redefine b between the
 *  beginning of the block and s1
 *
 */
@Description("Simplify use definitions chains.")
@Constraint(need = {UseDefsMarker.class, ThreeAddressCodeForm.class, ControlFlowGraph.class})
@Use(OptimizationTools.class)
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
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  private class Visitor extends JVisitor {

    @Override
    public boolean visit(@Nonnull JStatement s1) {
      List<JVariableRef> varsUsedBys1 = OptimizationTools.getUsedVariables(s1);

      // Copy variable used by s1 to update the list during the visit
      for (JVariableRef varRefOfa : varsUsedBys1.toArray(new JVariableRef[varsUsedBys1.size()])) {
        List<DefinitionMarker> defsOfa = OptimizationTools.getUsedDefinitions(varRefOfa);

        if (defsOfa.size() == 1) {
          DefinitionMarker defOfa = defsOfa.get(0);

          if (stmtCanBeOptimized(s1, defOfa)) {

            if (defOfa.getDefinedVariable().isSynthetic()) {
              tracer.getStatistic(SIMPLIFIED_USE_DEF_SYNTH).incValue();
            } else {
              tracer.getStatistic(SIMPLIFIED_USE_DEF).incValue();
            }

            JVariableRef varRefb = (JVariableRef) defOfa.getValue();
            JVariableRef newVarRefb = getNewVarRef(varRefb);

            UseDefsMarker udmOfNewVarRefb = new UseDefsMarker();
            udmOfNewVarRefb.addUsedDefinitions(OptimizationTools.getUsedDefinitions(varRefb),
                newVarRefb);
            newVarRefb.addMarker(udmOfNewVarRefb);

            varsUsedBys1.add(newVarRefb);
            varsUsedBys1.remove(varRefOfa);

            defOfa.removeUse(varRefOfa);

            TransformationRequest tr = new TransformationRequest(s1);
            tr.append(new Replace(varRefOfa, newVarRefb));
            tr.commit();
          }
        }
      }

      return super.visit(s1);
    }

    private boolean stmtCanBeOptimized(@Nonnull JStatement s1, @Nonnull DefinitionMarker defOfa) {
      // Condition (1)
      if (!defOfa.hasValue() || !(defOfa.getValue() instanceof JVariableRef)) {
        return false;
      }

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
        List<DefinitionMarker> defsOfbUseFroms0 = OptimizationTools.getUsedDefinitions(varRefb);
        if (bbHasOnlyDefinitions(bbOfs1, b, defsOfbUseFroms0)
            && !hasLocalDef(b, bbOfs1, null, s1)) {
          return true;
        }
      }

      return false;
    }

    private boolean bbHasOnlyDefinitions(@Nonnull BasicBlock bb, @Nonnull JVariable var,
        @Nonnull List<DefinitionMarker> defsToFound) {
      int nbDef = 0;

      for (DefinitionMarker def : OptimizationTools.getReachingDefs(bb)) {
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
  public void run(@Nonnull JMethod method) throws Exception {
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
