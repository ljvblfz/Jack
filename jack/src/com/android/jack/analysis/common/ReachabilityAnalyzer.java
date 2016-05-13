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

package com.android.jack.analysis.common;

import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.cfg.PeiBasicBlock;
import com.android.jack.ir.ast.JStatement;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.annotation.Nonnull;

/** Compute reachability on basic blocks. */
public abstract class ReachabilityAnalyzer<S> {

  /** Get control flow graph being analyzed */
  @Nonnull
  protected abstract ControlFlowGraph getCfg();

  /** Wrap up the analysis */
  protected abstract void finalize(
      @Nonnull List<S> in, @Nonnull List<S> out, @Nonnull List<S> outException);

  /** Create a new initial state */
  @Nonnull
  protected abstract S newState(boolean entry);

  /** Copy state */
  protected abstract void copyState(@Nonnull S src, @Nonnull S dest);

  /** Create a new empty state */
  protected abstract void mergeState(@Nonnull S state, @Nonnull S otherState);

  /** Process basic block statement */
  protected abstract void processStatement(@Nonnull S outBs, @Nonnull JStatement stmt);

  /** Clone state */
  @Nonnull
  protected abstract S cloneState(@Nonnull S state);

  /** Perform the analysis */
  public final void analyze() {
    ControlFlowGraph cfg = getCfg();

    int basicBlockMaxId = cfg.getBasicBlockMaxId();
    List<S> in = new ArrayList<>(basicBlockMaxId);
    // Output are not the same if we are on an exception path or not. Thus we
    // compute two sets of information that are used according to the path.
    List<S> out = new ArrayList<>(basicBlockMaxId);
    List<S> outException = new ArrayList<>(basicBlockMaxId);

    // Assign an empty bit vector to each basic block
    int entryBlockId = cfg.getEntryNode().getId();
    for (int i = 0; i < basicBlockMaxId; i++) {
      in.add(newState(i == entryBlockId));
      out.add(newState(i == entryBlockId));
      outException.add(newState(i == entryBlockId));
    }

    Queue<BasicBlock> queue = new LinkedList<>(cfg.getNodes());
    BitSet mayBeQueued = new BitSet(queue.size());

    while (!queue.isEmpty()) {
      BasicBlock bb = queue.poll();
      int bbId = bb.getId();
      mayBeQueued.set(bbId);

      S bbIn = in.get(bbId);
      S bbOut = out.get(bbId);

      recalculateInSet(bb, /* ignoreExceptionPath: */ false, bbIn, out, outException);

      S oldOut = cloneState(bbOut);

      computeOutput(bb, bbIn, bbOut, outException.get(bbId));

      if (!oldOut.equals(bbOut)) {
        for (BasicBlock successor : bb.getSuccessors()) {
          if (mayBeQueued.get(successor.getId()) && successor != cfg.getExitNode()) {
            queue.offer(successor);
            mayBeQueued.clear(successor.getId());
          }
        }
      }
    }

    finalize(in, out, outException);
  }

  /** Re-calculate 'in' state based on out states of the predecessors */
  protected final void recalculateInSet(
      @Nonnull BasicBlock bb, boolean ignoreExceptionPath,
      @Nonnull S in, @Nonnull List<S> out, @Nonnull List<S> outException) {

    List<BasicBlock> predecessors = bb.getPredecessors();

    // Recalculate incoming items as a set union operation
    if (!predecessors.isEmpty()) {
      boolean mergeNeeded = false;

      for (BasicBlock predecessor : predecessors) {
        if (predecessor instanceof PeiBasicBlock
            && ((PeiBasicBlock) predecessor).isExceptionOrUncaughtBlock(bb)) {
          if (ignoreExceptionPath) {
            continue;
          }
          if (mergeNeeded) {
            mergeState(in, outException.get(predecessor.getId()));
          } else {
            copyState(outException.get(predecessor.getId()), in);
            mergeNeeded = true;
          }
        } else {
          if (mergeNeeded) {
            mergeState(in, out.get(predecessor.getId()));
          } else {
            copyState(out.get(predecessor.getId()), in);
            mergeNeeded = true;
          }
        }
      }
    }
  }

  private void computeOutput(
      @Nonnull BasicBlock bb, @Nonnull S inBs,
      @Nonnull S outBs, @Nonnull S outExceptionBs) {

    copyState(inBs, outBs);

    List<JStatement> statements = bb.getStatements();
    if (statements.size() > 0) {
      JStatement lastStmt = statements.get(statements.size() - 1);

      for (JStatement stmt : statements) {
        if (stmt == lastStmt) {
          // We are on the lastStatement
          copyState(outBs, outExceptionBs);
        }
        processStatement(outBs, stmt);
      }
    }
  }
}
