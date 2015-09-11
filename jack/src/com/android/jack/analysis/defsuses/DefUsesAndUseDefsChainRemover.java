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
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * Remove def-uses and use-defs chains.
 */
@Description("Remove def-uses and use-defs chains.")
@Constraint(need = {UseDefsMarker.class, UsedVariableMarker.class, ControlFlowGraph.class})
@Transform(remove = {UseDefsMarker.class}, modify = {DefinitionMarker.class})
public class DefUsesAndUseDefsChainRemover implements RunnableSchedulable<JMethod> {

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

      for (JStatement stmt : bb.getStatements()) {

        UsedVariableMarker uvm = stmt.getMarker(UsedVariableMarker.class);
        assert uvm != null;

        for (JVariableRef usedVarRef : uvm.getUsedVariables()) {
          UseDefsMarker udm = usedVarRef.getMarker(UseDefsMarker.class);
          assert udm != null;

          for (DefinitionMarker dm : udm.getDefs()) {
            dm.clearUses();
          }

          usedVarRef.removeMarker(UseDefsMarker.class);
        }
      }
    }
  }
}