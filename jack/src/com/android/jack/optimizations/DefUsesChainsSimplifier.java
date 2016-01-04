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
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import javax.annotation.Nonnull;

/**
 * Simplify definition uses chains.
 */
@Description("Simplify definition uses chains.")
@Constraint(need = {DefinitionMarker.class, UseDefsMarker.class, ThreeAddressCodeForm.class,
    ControlFlowGraph.class})
public class DefUsesChainsSimplifier extends DefUsesAndUseDefsChainsSimplifier
    implements RunnableSchedulable<JMethod> {

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
      JExpression rhs = binary.getRhs();

      if (binary instanceof JAsgOperation) {
        DefinitionMarker def = binary.getMarker(DefinitionMarker.class);

        if (def != null && def.hasValue()) {
          JExpression valueExpr = def.getValue();
          assert valueExpr == rhs;

          if (valueExpr instanceof JVariableRef) {
            UseDefsMarker udm = valueExpr.getMarker(UseDefsMarker.class);
            assert udm != null;

            if (allUsedDefsUseOnTimeAndNotRedefine(def, udm)) {
              TransformationRequest tr = new TransformationRequest(method);

              for (DefinitionMarker defMarker : udm.getDefs()) {
                assert defMarker.getDefinedExpr() instanceof JVariableRef;

                tr.append(
                    new Replace(defMarker.getDefinedExpr(), getNewVarRef(def.getDefinedExpr())));
                updateDefUsesAndUseDefsChains(defMarker, def);
              }

              def.removeAllUses();

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

    /**
     * Check that used definitions (in the following sample: a=true and a=false) are used only one
     * time and that there is no redefinition of the defined variable (in the following sample:
     * b=a, thus check that b is not redefine) between used definitions and the binary operation
     * that is a definition (in the following sample: definition of b)
     *
     * Check if the following code could be optimize from:
     *
     * Path 1
     * a = true
     * ...
     * Path 2
     * a = false
     * ...
     * Path 3 target of path 1 & 2
     * b = a
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
     * @param def Definition that will be remove if optimization will be apply.
     * @param usedDefs Used definitions.
     * @return True if optimization could be done, false otherwise.
     */
    private boolean allUsedDefsUseOnTimeAndNotRedefine(@Nonnull DefinitionMarker def,
        @Nonnull UseDefsMarker usedDefs) {
      boolean allDefsUsesInASameDefNotModify = true;

      for (DefinitionMarker defMarker : usedDefs.getDefs()) {
        if (defMarker.hasValue()
              && defMarker.isUsedOnlyOnce()
              && defMarker.getDefinedVariable().isSynthetic()
              && !hasDefBetweenStatement(def.getDefinedVariable(),
                  (JStatement) defMarker.getDefinition().getParent(),
                  (JStatement) def.getDefinition().getParent())) {
            continue;
          }

        allDefsUsesInASameDefNotModify = false;
        break;
      }

      return allDefsUsesInASameDefNotModify;
    }

    /**
     * Update definition uses and use definitions chains to reflect that the following code:
     * a = b (def1)
     * c = a (def2)
     * is transformed into
     * c = b (def1)
     * It is required to update the definition uses chains of the def1 and it is required to
     * update the use definitions chains of c to target def1 rather than def2.
     *
     * @param defToUpdate definition to update.
     * @param defUseByUpdate definition use to update {@code defToUpdate}.
     */
    private void updateDefUsesAndUseDefsChains(
        @Nonnull DefinitionMarker defToUpdate, @Nonnull DefinitionMarker defUseByUpdate) {
      defToUpdate.removeAllUses();

      for (JVariableRef useOfRemoveDef : defUseByUpdate.getUses()) {
        defToUpdate.addUse(useOfRemoveDef);
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

    for (BasicBlock bb : cfg.getNodes()) {
      for (JStatement stmt : bb.getStatements()) {
        Visitor visitor = new Visitor(method);
        visitor.accept(stmt);
      }
    }
  }
}
