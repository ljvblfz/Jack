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

import com.google.common.collect.Lists;

import com.android.jack.Options;
import com.android.jack.backend.dex.rop.CodeItemBuilder;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBodyCfg;
import com.android.jack.ir.ast.JSsaVariableDefRef;
import com.android.jack.ir.ast.JSsaVariableRef;
import com.android.jack.ir.ast.JSsaVariableUseRef;
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
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * This is a pass that performs copy propagation based on SSA values. The final output should
 * reminds SSA valid.
 *
 * Based somewhat around DX's SSA rename algorithm, which performs copy propagation during
 * construction, this pass only a sparse analysis on the Phi nodes and SSA variable references.
 */
@Description("Copy Propagation of Locals")
@Name("CopyPropagation")
@Constraint(need = {JPhiBlockElement.class, JSsaVariableRef.class})
@Transform(modify = {JPhiBlockElement.class, JSsaVariableRef.class})
@Filter(TypeWithoutPrebuiltFilter.class)
public class CopyPropagation implements RunnableSchedulable<JMethodBodyCfg> {

  private final boolean emitSyntheticLocalDebugInfo =
      ThreadConfig.get(CodeItemBuilder.EMIT_SYNTHETIC_LOCAL_DEBUG_INFO).booleanValue();
  private final boolean emitLocalDebugInfo =
      ThreadConfig.get(Options.EMIT_LOCAL_DEBUG_INFO).booleanValue();

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  @Override
  public void run(JMethodBodyCfg body) {
    JMethod method = body.getMethod();
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    boolean changed;
    do {
      changed = false;
      for (JBasicBlock bb : body.getCfg().getAllBlocksUnordered()) {
        for (JBasicBlockElement e : Lists.newArrayList(bb.getElements(true))) {
          if (e instanceof JVariableAsgBlockElement) {
            changed = tryPropagateAssignment((JVariableAsgBlockElement) e, body.getCfg());
          } else if (e instanceof JPhiBlockElement) {
            JPhiBlockElement phi = (JPhiBlockElement) e;
            changed = tryRemoveUselessPhi(phi);
            if (!changed) {
              changed = tryPropagatePhi(phi, body.getCfg());
            }
          }
        }
      }
    } while (changed);
  }

  private boolean shouldKeepVariable(JSsaVariableRef varRef) {
    if (varRef.getTarget().isSynthetic()) {
      return emitSyntheticLocalDebugInfo;
    } else {
      return emitLocalDebugInfo;
    }
  }

  /**
   * If we have an assignment a = b, we try to replace all access of 'a' to 'b' when possible.
   *
   * @return true if an optimizations was performed.
   */
  private boolean tryPropagateAssignment(JVariableAsgBlockElement assign, JControlFlowGraph cfg) {
    JExpression lhs = assign.getAssignment().getLhs();
    JExpression rhs = assign.getAssignment().getRhs();
    if (!(rhs instanceof JSsaVariableRef)) {
      return false;
    }

    JSsaVariableUseRef rhsVarRef = (JSsaVariableUseRef) rhs;
    JSsaVariableDefRef lhsVarRef = (JSsaVariableDefRef) lhs;

    // Check for debug build. Make sure we are keeping locals if that's the case.
    if (shouldKeepVariable(lhsVarRef)) {
      return false;
    }

    // We don't propagate unreachable values.
    if (rhsVarRef.getVersion() == 0) {
      return false;
    }

    TransformationRequest tr = new TransformationRequest(cfg);
    propagateVarRef(lhsVarRef, rhsVarRef, tr);
    ((JRegularBasicBlock) assign.getBasicBlock()).removeElement(assign);
    tr.commit();
    return true;
  }

  private boolean tryPropagatePhi(JPhiBlockElement phi, JControlFlowGraph cfg) {
    JSsaVariableUseRef rhsVarRef = canPropagatePhi(phi);
    JSsaVariableDefRef lhsVarRef = phi.getLhs();

    // Check for debug build. Make sure we are keeping locals if that's the case.
    if (shouldKeepVariable(lhsVarRef)) {
      return false;
    }

    // Not every path is the same variable.
    if (rhsVarRef == null) {
      return false;
    }

    TransformationRequest tr = new TransformationRequest(cfg);
    propagateVarRef(lhsVarRef, rhsVarRef, tr);
    tr.commit();
    ((JRegularBasicBlock) phi.getBasicBlock()).removeElement(phi);
    return true;
  }

  private boolean tryRemoveUselessPhi(JPhiBlockElement phi) {
    if (!phi.getLhs().hasUses()) {
      ((JRegularBasicBlock) phi.getBasicBlock()).removeElement(phi);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Replace all the references to the lhs JSsaVariableRef with a new rhs JSsaVariableRef.
   */
  private void propagateVarRef(JSsaVariableDefRef lhs, JSsaVariableUseRef rhs,
      TransformationRequest tr) {
    JSsaVariableDefRef def = rhs.getDef();
    for (JSsaVariableRef oldUse : Lists.newArrayList(lhs.getUses())) {
      JSsaVariableRef newUse = def.makeRef(oldUse.getSourceInfo());
      newUse.addAllMarkers(oldUse.getAllMarkers());
      tr.append(new Replace(oldUse, newUse));
    }
    lhs.removeUses();
  }

  /**
   * If we have a = phi(b,b,b,b), it is ok with replace a with b.
   *
   * @return The right hand side values that the left hand side should be replaced with. Otherwise
   *         null.
   */
  public JSsaVariableUseRef canPropagatePhi(JPhiBlockElement e) {
    JSsaVariableUseRef first = e.getRhs(e.getBasicBlock().getPredecessors().get(0));
    assert first != null;
    for (JSsaVariableUseRef operand : e.getRhs()) {
      if (first.getDef() != operand.getDef()) {
        return null;
      }
    }
    return first;
  }
}
