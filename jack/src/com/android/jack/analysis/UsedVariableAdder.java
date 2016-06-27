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

package com.android.jack.analysis;

import com.android.jack.Options;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JLoop;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * Compute all variables used by a statement.
 */
@Description("Compute all variables used by a statement.")
@Constraint(need = {ControlFlowGraph.class, ThreeAddressCodeForm.class},
    no = {JLoop.class, JTryStatement.class})
@Transform(add = UsedVariableMarker.class)
@Protect(add = {JMethod.class, JStatement.class}, modify = {JMethod.class, JStatement.class})
@Filter(TypeWithoutPrebuiltFilter.class)
public class UsedVariableAdder implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  private static class ComputeReadVariables extends JVisitor {

    @Nonnull
    private final UsedVariableMarker readVarMarker;

    public ComputeReadVariables(@Nonnull UsedVariableMarker readVarMarker) {
      this.readVarMarker = readVarMarker;
    }

    @Override
    public boolean visit(@Nonnull JVariableRef varRef) {
      JNode parent = varRef.getParent();
      if (!(parent instanceof JAsgOperation) || ((JAsgOperation) parent).getLhs() != varRef) {
        readVarMarker.addUsedVariable(varRef);
      }
      return super.visit(varRef);
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

    for (BasicBlock bb : cfg.getNodes()) {
      for (JStatement stmt : bb.getStatements()) {
        UsedVariableMarker readVarMarker = stmt.getMarker(UsedVariableMarker.class);
        if (readVarMarker == null) {
          UsedVariableMarker newReadVarMarker = new UsedVariableMarker();
          readVarMarker = stmt.addMarker(newReadVarMarker);
          if (readVarMarker == null) {
            readVarMarker = newReadVarMarker;
          }
        }
        ComputeReadVariables cuv = new ComputeReadVariables(readVarMarker);
        cuv.accept(stmt);
      }
    }
  }
}