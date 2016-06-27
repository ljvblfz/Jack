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

package com.android.jack.analysis.dfa.reachingdefs;

import com.android.jack.Options;
import com.android.jack.analysis.DefinitionMarker;
import com.android.jack.analysis.common.ReachabilityAnalyzer;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JThis;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.util.ThreeAddressCodeFormUtils;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.category.Private;
import com.android.sched.util.config.id.ImplementationPropertyId;
import com.android.sched.util.config.id.PropertyId;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Compute reaching definitions.
 */
@Description("Compute reaching definitions.")
@Constraint(need = {ControlFlowGraph.class, DefinitionMarker.class})
@Transform(add = {ReachingDefsMarker.class})
@Protect(add = {JMethod.class, JStatement.class}, modify = {JMethod.class, JStatement.class})
@Use(ThreeAddressCodeFormUtils.class)
@HasKeyId
@Filter(TypeWithoutPrebuiltFilter.class)
public class ReachingDefinitions implements RunnableSchedulable<JMethod> {

  @Nonnull
  public static final PropertyId<ReachingDefinitionsChecker> REACHING_DEFS_CHECKER =
      ImplementationPropertyId
          .create("jack.tests.reachingdefs.checker",
              "Define a checker that must be called at the end of reaching definitions analysis",
              ReachingDefinitionsChecker.class).addDefaultValue("none")
          .addCategory(Private.class);

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  private final ReachingDefinitionsChecker checker =  ThreadConfig.get(REACHING_DEFS_CHECKER);

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    solve(method);

    checker.check(method);
  }

  private class Analyzer extends ReachabilityAnalyzer<BitSet> {
    @Nonnull
    final ControlFlowGraph cfg;
    @Nonnull
    final List<DefinitionMarker> definitions;
    final int initialized;

    private Analyzer(@Nonnull JMethod method) {
      ControlFlowGraph cfg = method.getMarker(ControlFlowGraph.class);
      assert cfg != null;
      this.cfg = cfg;
      this.definitions = getAllDefinitions(method, this.cfg);
      this.initialized = (method.getThis() != null ? 1 : 0) + method.getParams().size();
    }

    @Nonnull
    @Override protected ControlFlowGraph getCfg() {
      return cfg;
    }

    @Override public void finalize(
        @Nonnull List<BitSet> in, @Nonnull List<BitSet> out, @Nonnull List<BitSet> outException) {
      for (BasicBlock bb : getCfg().getNodes()) {
        bb.addMarker(new ReachingDefsMarker(getDefinitions(definitions, in.get(bb.getId()))));
      }
    }

    @Nonnull
    @Override public BitSet newState(boolean entry) {
      BitSet s = new BitSet(this.definitions.size());
      if (entry) {
        s.set(0, initialized);
      }
      return s;
    }

    @Override public void copyState(@Nonnull BitSet src, @Nonnull BitSet dest) {
      dest.clear();
      dest.or(src);
    }

    @Override public void mergeState(@Nonnull BitSet state, @Nonnull BitSet otherState) {
      state.or(otherState);
    }

    @Override public void processStatement(@Nonnull BitSet outBs, @Nonnull JStatement stmt) {
      DefinitionMarker currentDef = ThreeAddressCodeFormUtils.getDefinitionMarker(stmt);
      if (currentDef != null) {
        outBs.set(currentDef.getBitSetIdx());

        // Gen keeps only the last definition of a variable, previous are killed.
        for (int i = outBs.nextSetBit(0); i >= 0; i = outBs.nextSetBit(i + 1)) {
          DefinitionMarker dm = definitions.get(i);
          if (dm.getDefinedVariable() == currentDef.getDefinedVariable() && dm != currentDef) {
            outBs.clear(dm.getBitSetIdx());
          }
        }
      }
    }

    @Nonnull
    @Override public BitSet cloneState(@Nonnull BitSet state) {
      return (BitSet) state.clone();
    }
  }

  private void solve(@Nonnull JMethod method) {
    new Analyzer(method).analyze();
  }

  @Nonnull
  private DefinitionMarker getDefinitionMarkerForThis(@Nonnull JMethod method) {
    // JThis is a definition
    JThis jThis = method.getThis();
    assert jThis != null;
    DefinitionMarker dm = jThis.getMarker(DefinitionMarker.class);
    assert dm != null;
    return dm;
  }

  /**
   * List of returned definitions is a Set since it does not contains duplicated items.
   * Do not use Set Api due to a performance problem.
   */
  @Nonnull
  private List<DefinitionMarker> getDefinitions(
      @Nonnull List<DefinitionMarker> definitions, @Nonnull BitSet in) {
    List<DefinitionMarker> reachingDefs = new ArrayList<DefinitionMarker>();
    for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
      reachingDefs.add(definitions.get(i));
    }
    return (reachingDefs);
  }

  @Nonnull
  private List<DefinitionMarker> getAllDefinitions(
      @Nonnull JMethod method, @Nonnull ControlFlowGraph cfg) {
    List<DefinitionMarker> definitions = new ArrayList<DefinitionMarker>();
    int bitSetIdx = 0;

    if (method.getThis() != null) {
      DefinitionMarker dm = getDefinitionMarkerForThis(method);
      dm.setBitSetIdx(bitSetIdx++);
      definitions.add(dm);
    }

    for (JParameter param : method.getParams()) {
      DefinitionMarker dm = param.getMarker(DefinitionMarker.class);
      assert dm != null;
      dm.setBitSetIdx(bitSetIdx++);
      definitions.add(dm);
    }

    for (BasicBlock bb : cfg.getNodes()) {
      for (JStatement stmt : bb.getStatements()) {
        DefinitionMarker dm = ThreeAddressCodeFormUtils.getDefinitionMarker(stmt);
        if (dm != null) {
          dm.setBitSetIdx(bitSetIdx++);
          definitions.add(dm);
        }
      }
    }

    return definitions;
  }


}
