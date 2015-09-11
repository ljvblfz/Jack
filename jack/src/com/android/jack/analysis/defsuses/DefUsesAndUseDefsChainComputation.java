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

package com.android.jack.analysis.defsuses;

import com.android.jack.Options;
import com.android.jack.analysis.DefinitionMarker;
import com.android.jack.analysis.UseDefsMarker;
import com.android.jack.analysis.UsedVariableMarker;
import com.android.jack.analysis.dfa.reachingdefs.ReachingDefsMarker;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.util.ThreeAddressCodeFormUtils;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Compute defs-uses and uses-defs chains.
 */
@Description("Compute defs-uses and uses-defs chains.")
@Constraint(need = {ControlFlowGraph.class, ReachingDefsMarker.class, UsedVariableMarker.class})
@Transform(add = {UseDefsMarker.class})
@Protect(add = {JMethod.class, JStatement.class}, modify = {JMethod.class, JStatement.class})
@Use(ThreeAddressCodeFormUtils.class)
public class DefUsesAndUseDefsChainComputation implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    ControlFlowGraph cfg = method.getMarker(ControlFlowGraph.class);
    assert cfg != null;

    for (BasicBlock bb : cfg.getNodes()) {
      ReachingDefsMarker reachingDefs = bb.getMarker(ReachingDefsMarker.class);
      assert reachingDefs != null;
      List<DefinitionMarker> currentDefs =
          new ArrayList<DefinitionMarker>(reachingDefs.getReachingDefs());

      for (JStatement stmt : bb.getStatements()) {

        UsedVariableMarker uvm = stmt.getMarker(UsedVariableMarker.class);
        assert uvm != null;

        for (JVariableRef usedVarRef : uvm.getUsedVariables()) {
          JVariable var = usedVarRef.getTarget();
          assert usedVarRef.getMarker(UseDefsMarker.class) == null;

          UseDefsMarker udm = new UseDefsMarker();
          usedVarRef.addMarker(udm);

          for (DefinitionMarker def : currentDefs) {
            if (def.getDefinedVariable() == var) {
              udm.addUsedDefinition(def, usedVarRef);
            }
          }

          assert !udm.isWithoutDefinition();
        }

        // Update of definitions must be done after management of usages since a statement can
        // change a definition used into it (for instance a = a + 1).
        updateCurrentDefs(stmt, currentDefs);
      }
    }
  }

  private void updateCurrentDefs(@Nonnull JStatement stmt,
      @Nonnull List<DefinitionMarker> currentDefs) {
    DefinitionMarker newDef = ThreeAddressCodeFormUtils.getDefinitionMarker(stmt);

    if (newDef != null) {
      JVariable varToRemove = newDef.getDefinedVariable();
      Iterator<DefinitionMarker> it = currentDefs.iterator();
      // Remove previous definitions
      while (it.hasNext()) {
        if (it.next().getDefinedVariable() == varToRemove) {
          it.remove();
        }
      }
      currentDefs.add(newDef);
    }
  }
}
