/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.optimizations.ssa;

import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JSsaVariableRef;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JBasicBlockElement;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JPhiBlockElement;
import com.android.jack.ir.ast.cfg.JRegularBasicBlock;
import com.android.jack.ir.ast.cfg.JVariableAsgBlockElement;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

/**
 * Remove unnecessary Phi nodes.
 */
@Description("CopyPropagation")
@Name("CopyPropagation")
@Constraint(need = {JPhiBlockElement.class, JSsaVariableRef.class})
@Transform(modify = {JPhiBlockElement.class, JSsaVariableRef.class})
@Filter(TypeWithoutPrebuiltFilter.class)
public class CopyPropagation implements RunnableSchedulable<JControlFlowGraph> {
  @Override
  public void run(JControlFlowGraph cfg) {
    boolean changed;
    do {
      changed = false;
      TransformationRequest tr = new TransformationRequest(cfg);
      for (JBasicBlock bb : cfg.getAllBlocksUnordered()) {
        for (JBasicBlockElement e : bb.getElements(true)) {
          if (e instanceof JVariableAsgBlockElement) {
            JVariableAsgBlockElement assign = (JVariableAsgBlockElement) e;
            JExpression lhs = assign.getAssignment().getLhs();
            JExpression rhs = assign.getAssignment().getRhs();
            if (rhs instanceof JSsaVariableRef) {
              JSsaVariableRef rhsVarRef = (JSsaVariableRef) rhs;
              if (rhsVarRef.getVersion() > 0) {
                propagateVarRef((JSsaVariableRef) lhs, rhsVarRef, tr);
                ((JRegularBasicBlock) bb).removeElement(assign);
                changed = true;
              }
            }
          }

          if (e instanceof JPhiBlockElement) {
            JPhiBlockElement phi = (JPhiBlockElement) e;
            JSsaVariableRef rhs = canPropagatePhi(phi);
            if (rhs != null) {
              propagateVarRef(phi.getLhs(), rhs, tr);
              ((JRegularBasicBlock) bb).removeElement(phi);
              changed = true;
            }
          }
        }
      }
      if (changed) {
        tr.commit();
      }
    } while (changed);
  }

  public void propagateVarRef(JSsaVariableRef lhs, JSsaVariableRef rhs, TransformationRequest tr) {
    JSsaVariableRef def = rhs.getDef();
    for (JSsaVariableRef oldUse : lhs.getUses()) {
      JSsaVariableRef newUse = def.makeRef(oldUse.getSourceInfo());
      newUse.addAllMarkers(oldUse.getAllMarkers());
      tr.append(new Replace(oldUse, newUse));
    }
  }

  public JSsaVariableRef canPropagatePhi(JPhiBlockElement e) {
    JSsaVariableRef first = e.getRhs(e.getBasicBlock().getPredecessors().get(0));
    assert first != null;
    for (JSsaVariableRef operand : e.getRhs()) {
      if (first.getDef() != operand.getDef()) {
        return null;
      }
    }
    return first;
  }
}
