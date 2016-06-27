/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.coverage;

import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ConditionalBasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.cfg.SwitchBasicBlock;
import com.android.jack.cfg.ThrowBasicBlock;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.annotation.Nonnull;

/**
 * Analyzes control flow graph of a method to determine the coverage probes required to instrument
 * the method.
 */
@Description("Code coverage analyzer")
@Support(CodeCoverage.class)
@Constraint(need = {CodeCoverageMarker.Initialized.class, ControlFlowGraph.class})
@Transform(
    add = {CodeCoverageMarker.Analyzed.class, ProbeMarker.class}, modify = CodeCoverageMarker.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class CodeCoverageAnalyzer implements RunnableSchedulable<JMethod> {
  @Override
  public void run(@Nonnull JMethod m) {
    if (m.isNative() || m.isAbstract()) {
      // This method has no code so cannot be instrumented.
      return;
    }

    JDefinedClassOrInterface declaringClass = m.getEnclosingType();
    CodeCoverageMarker coverageMarker = declaringClass.getMarker(CodeCoverageMarker.class);
    if (coverageMarker == null) {
      // The declaring class is not selected for code coverage.
      return;
    }

    ControlFlowGraph controlFlowGraph = m.getMarker(ControlFlowGraph.class);
    assert controlFlowGraph != null : "No control flow graph";

    analyzeCFG(m, controlFlowGraph, coverageMarker);
    findProbeLocations(controlFlowGraph);
  }

  /**
   * Analyzes the control flow graph to identify all required probes. This does not modify the IR,
   * only adding {@link ProbeMarker} on {@link BasicBlock}.
   * @param method the method being analyzed
   * @param controlFlowGraph
   * @param coverageMarker
   */
  private static void analyzeCFG(@Nonnull JMethod method,
      @Nonnull ControlFlowGraph controlFlowGraph, @Nonnull CodeCoverageMarker coverageMarker) {
    // Analyze the CFG to identify probes.
    Queue<BasicBlock> blocks = new LinkedList<BasicBlock>();
    blocks.add(controlFlowGraph.getEntryNode());
    for (BasicBlock bb = blocks.poll(); bb != null; bb = blocks.poll()) {
      if (bb.containsMarker(ProbeMarker.class)) {
        // The block has already been processed.
        continue;
      }
      List<BasicBlock> predecessors = bb.getPredecessors();
      int predecessorsCount = predecessors.size();
      ProbeDescription probe = null;
      if (predecessorsCount == 0) {
        // This is the start of a new probe.
        probe = assignNewProbe(method, coverageMarker, bb);
      } else if (predecessorsCount > 1) {
        // There are different paths to reach this block: use a new probe.
        probe = assignNewProbe(method, coverageMarker, bb);
      } else { // predecessorsCount == 1
        // The block has only one predecessor.
        BasicBlock pred = predecessors.get(0);
        int successorsOfPredecessorCount = pred.getSuccessors().size();
        if (successorsOfPredecessorCount > 1) {
          // TODO(shertz) Jacoco does not seem to split probe on throwing instructions. If we want
          // to do the same thing, we could test whether the predecessor is a Pei block, in which
          // case we continue with the same probe instead of creating a new one.
          probe = assignNewProbe(method, coverageMarker, bb);
        } else {
          // There is an unconditional path from the predecessor to this block: use the same
          // probe.
          ProbeMarker probeMarker = pred.getMarker(ProbeMarker.class);
          assert probeMarker != null;
          bb.addMarker(probeMarker);
          probe = probeMarker.getProbe();
        }
      }

      // Update probe with source information of each statement it covers.
      assert probe != null;
      for (JStatement st : bb.getStatements()) {
        ProbeUpdater visitor = new ProbeUpdater(probe);
        visitor.accept(st);
      }

      // Visit all its successors
      blocks.addAll(bb.getSuccessors());
    }

    // Mark branch lines. We need that all blocks have been processed once so the ProbeMarker is
    // installed.
    for (BasicBlock bb : controlFlowGraph.getNodes()) {
      if (bb instanceof ConditionalBasicBlock || bb instanceof SwitchBasicBlock) {
        // For Jacoco, we need to remember these are branching instructions and how many
        // possible branches exist.
        JStatement branchStatement = bb.getStatements().get(0);
        SourceInfo branchSourceInfo = branchStatement.getSourceInfo();
        for (BasicBlock succ : bb.getSuccessors()) {
          ProbeMarker marker = succ.getMarker(ProbeMarker.class);
          assert marker != null;
          marker.getProbe().incrementLine(branchSourceInfo.getStartLine(), 1, true);
        }
      }
    }
  }

  /**
   * Iterates over basic block to identify which ones are terminating their respective probes.
   * @param controlFlowGraph
   */
  private static void findProbeLocations(@Nonnull ControlFlowGraph controlFlowGraph) {
    BasicBlock entryBlock = controlFlowGraph.getEntryNode();
    BasicBlock exitBlock = controlFlowGraph.getExitNode();
    for (BasicBlock bb : controlFlowGraph.getNodes()) {
      if (bb == entryBlock || bb == exitBlock) {
        // we do not process these special blocks.
        continue;
      }

      ProbeMarker probeMarker = bb.getMarker(ProbeMarker.class);
      assert probeMarker != null;
      List<BasicBlock> successors = bb.getSuccessors();
      if (successors.size() > 1) {
        // There is more than one path from this block so its probe ends.
        assert doSuccessorsHaveDifferentProbes(probeMarker, successors);
        probeMarker.setInsertionBlock(bb);
      } else if (successors.size() == 1) {
        BasicBlock successor = successors.get(0);
        if (successor == exitBlock) {
          // We're going to leave the method (return or throw) so we must terminate the probe.
          probeMarker.setInsertionBlock(bb);
        } else {
          // Is it successor covered by a different probe?
          ProbeMarker successorProbeMarker = successor.getMarker(ProbeMarker.class);
          assert successorProbeMarker != null;
          if (successorProbeMarker.getProbe() != probeMarker.getProbe()) {
            // The successor starts a different probe so we must terminate ours.
            probeMarker.setInsertionBlock(bb);
          }
        }
      } else { // no successor
        assert bb instanceof ThrowBasicBlock;
        probeMarker.setInsertionBlock(bb);
      }
    }
  }

  /**
   * Only used for assertion.
   */
  private static boolean doSuccessorsHaveDifferentProbes(
      @Nonnull ProbeMarker marker, @Nonnull List<BasicBlock> successors) {
    for (BasicBlock bb : successors) {
      ProbeMarker probeMarker = bb.getMarker(ProbeMarker.class);
      assert probeMarker != null;
      if (probeMarker == marker) {
        return false;
      }
    }
    return true;
  }

  private static ProbeDescription assignNewProbe(
      @Nonnull JMethod method, @Nonnull CodeCoverageMarker coverageMarker, @Nonnull BasicBlock bb) {
    ProbeDescription p = coverageMarker.createProbe(method);
    bb.addMarker(new ProbeMarker(p));
    return p;
  }

  /**
   * Updates probes with source information.
   */
  private static class ProbeUpdater extends JVisitor {
    @Nonnull
    private final ProbeDescription probe;

    public ProbeUpdater(@Nonnull ProbeDescription probe) {
      this.probe = probe;
    }

    @Override
    public void endVisit(@Nonnull JExpression x) {
      updateProbe(x.getSourceInfo());
    }

    @Override
    public boolean visit(@Nonnull JIfStatement x) {
      // Do not visit 'then' and 'else' statements: they will be assigned to different probes
      // so we do not want to update the current probe with their source information.
      accept(x.getIfExpr());
      return false;
    }

    @Override
    public boolean visit(@Nonnull JSwitchStatement x) {
      // Do not visit 'case' and 'default' statements: they will be assigned to different probes
      // so we do not want to update the current probe with their source information.
      accept(x.getExpr());
      return false;
    }

    @Override
    public boolean visit(@Nonnull JStatement x) {
      updateProbe(x.getSourceInfo());
      return false;
    }

    private void updateProbe(@Nonnull SourceInfo sourceInfo) {
      // Without debug info, JaCoCo considers instruction's line to be -1.
      int line = (sourceInfo != SourceInfo.UNKNOWN) ? sourceInfo.getStartLine() : -1;
      probe.incrementLine(line, 1, false);
    }
  }
}
