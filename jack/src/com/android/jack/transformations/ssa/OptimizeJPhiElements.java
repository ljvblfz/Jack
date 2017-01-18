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

package com.android.jack.transformations.ssa;

import com.google.common.collect.Lists;

import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JSsaVariableRef;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JBasicBlockElement;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JPhiBlockElement;
import com.android.jack.ir.ast.cfg.JRegularBasicBlock;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.Replace;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.transform.TransformRequest;

/**
 * Remove unnecessary Phi nodes.
 */
@Description("OptimizeJPhiElements")
@Name("OptimizeJPhiElements")
@Constraint(need = {JPhiBlockElement.class, JSsaVariableRef.class})
@Transform(modify = {JControlFlowGraph.class})
@Filter(TypeWithoutPrebuiltFilter.class)
public class OptimizeJPhiElements implements RunnableSchedulable<JControlFlowGraph> {
  @Override
  public void run(JControlFlowGraph t) {
    for (JBasicBlock bb : t.getAllBlocksUnordered()) {
      for (JBasicBlockElement e : Lists.newArrayList(bb.getElements(true))) {
        if (e instanceof JPhiBlockElement) {
          JPhiBlockElement phi = (JPhiBlockElement) e;
          // First remove it if it is redundant.
          if (phi.getLhs().getUses().isEmpty()) {
            ((JRegularBasicBlock) bb).removeElement(e);
            continue;
          }
          pruneUnreachableDef(phi);
        }
      }
    }
  }

  private void pruneUnreachableDef(JPhiBlockElement phi) {
    if (phi.getLhs().getTarget() instanceof JParameter) {
      return;
    }
    TransformRequest tr = new TransformRequest();
    for (JSsaVariableRef rhs : phi.getRhs()) {
      if (rhs.getVersion() == 0) {
        tr.append(new Replace(rhs, phi.getLhs().makeRef(rhs.getSourceInfo())));
      }
    }
    tr.commit();
  }
}
