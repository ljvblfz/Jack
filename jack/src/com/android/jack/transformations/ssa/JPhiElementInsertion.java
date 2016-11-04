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

import com.android.jack.dx.util.IntIterator;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JBasicBlockElement;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JPhiBlockElement;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.util.graph.DominanceFrontierInfoMarker;
import com.android.jack.util.graph.NodeIdMarker;
import com.android.jack.util.graph.NodeListMarker;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import java.util.BitSet;
import java.util.List;

/**
 * Split basic blocks for Phi element insertion.
 */
@Description("Insert Phi elements into the CFG.")
@Name("JPhiElementInsertion")
@Transform(add = {JPhiBlockElement.class},
    remove = {SsaBasicBlockSplitterMarker.class})
@Constraint(need = {DominanceFrontierInfoMarker.class})
@Filter(TypeWithoutPrebuiltFilter.class)
public class JPhiElementInsertion implements RunnableSchedulable<JControlFlowGraph> {

  @Override
  public void run(JControlFlowGraph cfg) {
    SsaBasicBlockSplitterMarker marker = cfg.removeMarker(SsaBasicBlockSplitterMarker.class);
    assert marker != null;
    placePhiFunctions(cfg);
    // Invalidates the block split marker.
  }

  /**
   * See Appel algorithm 19.6:
   *
   * Place Phi functions in appropriate locations.
   *
   */
  private void placePhiFunctions(JControlFlowGraph cfg) {
    JMethod method = cfg.getMethod();
    final int numLocals = SsaUtil.getTotalNumberOfLocals(method, cfg);
    int regCount;
    int blockCount;

    final List<JBasicBlock> bbMap = NodeListMarker.getNodeList(cfg);
    blockCount = bbMap.size();
    regCount = numLocals;


    // Bit set of registers vs block index "definition sites"
    BitSet[] defsites = new BitSet[regCount];

    // Bit set of registers vs block index "phi placement sites"
    BitSet[] phisites = new BitSet[regCount];

    for (int i = 0; i < regCount; i++) {
      defsites[i] = new BitSet(blockCount);
      phisites[i] = new BitSet(blockCount);
    }

    /*
     * For each register, build a set of all basic blocks where containing an assignment to that
     * register.
     */
    for (JBasicBlock b : bbMap) {
      for (JBasicBlockElement stmt : b.getElements(true)) {
        JVariableRef dv = SsaUtil.getDefinedVariable(stmt);
        if (dv != null) {
          JVariable rs = dv.getTarget();
          // We don't need to check for JThis because JThis will never be defined.
          int index = SsaUtil.getLocalIndex(method, cfg, rs);
          defsites[index].set(NodeIdMarker.getId(b));
        }
      }
    }

    BitSet worklist;
    /*
     * For each register, compute all locations for phi placement based on dominance-frontier
     * algorithm.
     */
    for (int reg = 0, s = regCount; reg < s; reg++) {
      int workBlockIndex;

      /* Worklist set starts out with each node where reg is assigned. */
      worklist = (BitSet) (defsites[reg].clone());

      while (0 <= (workBlockIndex = worklist.nextSetBit(0))) {
        worklist.clear(workBlockIndex);
        DominanceFrontierInfoMarker domInfo =
            DominanceFrontierInfoMarker.getDomInfo(bbMap.get(workBlockIndex));
        IntIterator dfIterator = domInfo.dominanceFrontiers.iterator();

        while (dfIterator.hasNext()) {
          int dfBlockIndex = dfIterator.next();
          JBasicBlock dfBlock = bbMap.get(dfBlockIndex);

          if (!phisites[reg].get(dfBlockIndex) && dfBlock != cfg.getExitBlock()) {
            phisites[reg].set(dfBlockIndex);

            JVariable target = SsaUtil.getVariableByIndex(method, cfg, reg);

            JPhiBlockElement phi = new JPhiBlockElement(target,
                dfBlock.getPredecessors().size(), method.getSourceInfo());
            dfBlock.insertElement(0, phi);

            if (!defsites[reg].get(dfBlockIndex)) {
              worklist.set(dfBlockIndex);
            }
          }
        }
      }
    }
  }
}
