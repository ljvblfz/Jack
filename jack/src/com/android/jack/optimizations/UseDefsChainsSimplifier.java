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
import com.android.jack.analysis.UsedVariableMarker;
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
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Simplify use definitions chains.
 */
@Description("Simplify use definitions chains.")
@Constraint(need = {UseDefsMarker.class, ThreeAddressCodeForm.class, ControlFlowGraph.class})
public class UseDefsChainsSimplifier extends DefUsesAndUseDefsChainsSimplifier
    implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private class Visitor extends JVisitor {

    @Override
    public boolean visit(@Nonnull JStatement stmt) {
      UsedVariableMarker uvm = stmt.getMarker(UsedVariableMarker.class);

      if (uvm != null) {
        List<JVariableRef> varRefToDelete = new ArrayList<JVariableRef>();
        List<JVariableRef> varRefToAdd = new ArrayList<JVariableRef>();

        for (JVariableRef usedVarRef : uvm.getUsedVariables()) {
          UseDefsMarker udm = usedVarRef.getMarker(UseDefsMarker.class);
          assert udm != null;

          List<DefinitionMarker> usedDefsMarker = udm.getDefs();

          /*
           * The transformation try to optimize statements that use variables that are
           * definitions initialized with another variables.
           *
           * For instance the following code:
           * a = b
           * m(a)
           * will be transform to:
           * m(b)
           *
           * The transformation can be apply only if a statement used only one definition of a
           * variable, for instance it could not be possible to optimize the code if m(a) use
           * two definitions of a (for instance a=b and a=true). Moreover, used variable
           * (in the above sample, variable b) must not be redefine between his usage (a=b) and
           * the statement that will be optimize.
           */
          if (usedDefsMarker.size() == 1) {
            DefinitionMarker defMarker = usedDefsMarker.get(0);

            if (defMarker.hasValue()
                && defMarker.getDefinedVariable().isSynthetic()
                && defMarker.getValue() instanceof JVariableRef) {
              JVariableRef defValue = (JVariableRef) defMarker.getValue();
              JVariable var = defValue.getTarget();

              if (!hasDefBetweenStatement(
                  var, (JStatement) defMarker.getDefinition().getParent(), stmt)) {

                JVariableRef newVarRef = getNewVarRef(defValue);
                UseDefsMarker newUdm = new UseDefsMarker();
                newVarRef.addMarker(newUdm);

                // Fill the UseDefsMarker for the new variable reference.
                UseDefsMarker udmToMove = defValue.getMarker(UseDefsMarker.class);
                assert udmToMove != null;
                newUdm.addUsedDefinitions(udmToMove.getDefs(), newVarRef);

                TransformationRequest tr = new TransformationRequest(stmt);
                tr.append(new Replace(usedVarRef, newVarRef));
                tr.commit();

                varRefToDelete.add(usedVarRef);
                varRefToAdd.add(newVarRef);

                defMarker.removeUse(usedVarRef);
              }
            }
          }
        }

        for (JVariableRef varRef : varRefToDelete) {
          uvm.getUsedVariables().remove(varRef);
          assert varRef.getMarker(UseDefsMarker.class) != null;
        }

        for (JVariableRef varRef : varRefToAdd) {
          uvm.getUsedVariables().add(varRef);
          assert varRef.getMarker(UseDefsMarker.class) != null;
        }
      }

      return super.visit(stmt);
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
    if (method.getEnclosingType().isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    ControlFlowGraph cfg = method.getMarker(ControlFlowGraph.class);
    assert cfg != null;

    for (BasicBlock bb : cfg.getNodes()) {
      for (JStatement stmt : bb.getStatements()) {
        Visitor visitor = new Visitor();
        visitor.accept(stmt);
      }
    }
  }
}
