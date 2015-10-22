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
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.ast.RefAsStatement;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Remove unused definition.
 */
@Description("Remove useless variable copies.")
@Constraint(need = {DefinitionMarker.class, UseDefsMarker.class, ThreeAddressCodeForm.class,
    ControlFlowGraph.class})
@Transform(add = {RefAsStatement.class})
public class UnusedDefinitionRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    public Visitor(@Nonnull TransformationRequest tr) {
      this.tr = tr;
    }

    @Override
    public boolean visit(@Nonnull JBinaryOperation binary) {
      JExpression rhs = binary.getRhs();

      if (binary instanceof JAsgOperation
          && !(rhs instanceof JExceptionRuntimeValue)
          && !rhs.canThrow()) {

        DefinitionMarker dm = binary.getMarker(DefinitionMarker.class);
        if (dm != null && dm.isUnused() && dm.getDefinedVariable().isSynthetic()) {
          assert !(binary.getLhs() instanceof JFieldRef || binary.getLhs() instanceof JArrayRef);
          removeUnusedDefinition((JAsgOperation) binary);
        }
      }

      return super.visit(binary);
    }

    private void removeUnusedDefinition(@Nonnull JAsgOperation binary) {
      assert !(binary.getRhs() instanceof JExceptionRuntimeValue);

      tr.append(new Remove(binary.getParent()));

      if (binary.getRhs() instanceof JVariableRef) {
        UseDefsMarker udm = ((JVariableRef) binary.getRhs()).getMarker(UseDefsMarker.class);
        assert udm != null;

        List<DefinitionMarker> previouslyUsedDef = udm.getDefs();

        udm.removeAllUsedDefinitions((JVariableRef) binary.getRhs());

        // Check if previous used definition are again useful or not.
        for (DefinitionMarker dmUsed : previouslyUsedDef) {
          JNode definition = dmUsed.getDefinition();

          if (dmUsed.isUnused() && dmUsed.hasValue()) {
            JExpression expr = dmUsed.getValue();
            if (!(expr instanceof JExceptionRuntimeValue) && !expr.canThrow()) {
              removeUnusedDefinition((JAsgOperation) definition);
            }
          }
        }
      }
    }

    @Override
    public boolean visit(@Nonnull JIfStatement ifStmt) {
      super.visit(ifStmt);
      this.accept(ifStmt.getIfExpr());
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

    TransformationRequest tr = new TransformationRequest(method);

    for (BasicBlock bb : cfg.getNodes()) {
      for (JStatement stmt : bb.getStatements()) {
        Visitor visitor = new Visitor(tr);
        visitor.accept(stmt);
      }
    }

    tr.commit();
  }
}
