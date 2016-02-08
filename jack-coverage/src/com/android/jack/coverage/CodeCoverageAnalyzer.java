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
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.EmptyClinit;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Analyzes control flow graph of a method to determine the coverage probes required to instrument
 * the method.
 */
@Description("Code coverage analyzer")
@Support(CodeCoverageFeature.class)
@Constraint(need = {CodeCoverageMarker.Initialized.class, ControlFlowGraph.class},
    no = EmptyClinit.class)
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
    assert controlFlowGraph != null;

    analyzeCFG(m, controlFlowGraph, coverageMarker);
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
    // We use a LIFO work queue to follow a depth-first traversal of the CFG.
    final Deque<BasicBlock> workQueue = new ArrayDeque<BasicBlock>();

    // Remember which blocks has been visited.
    final Set<BasicBlock> visitedBlocks = new HashSet<BasicBlock>();

    final BasicBlock entryBlock = controlFlowGraph.getEntryNode();
    final BasicBlock exitBlock = controlFlowGraph.getExitNode();

    // We process each node using a depth-first traversal and stop when all nodes have been
    // visited. We push the successors of the entry block because we do not need to process it.
    workQueue.addAll(entryBlock.getSuccessors());

    // The current probe. When it is null, it means that a new probe must be created for the next
    // visited block.
    assert !workQueue.isEmpty();
    ProbeDescription currentProbe = coverageMarker.createProbe(method);

    while (!workQueue.isEmpty()) {
      final BasicBlock bb = workQueue.removeFirst();
      assert bb != entryBlock;
      assert bb != exitBlock;
      assert !visitedBlocks.contains(bb);
      assert !bb.containsMarker(ProbeMarker.class);
      assert currentProbe != null;

      // Remember that this block has been visited.
      visitedBlocks.add(bb);

      // Add successors to the queue.
      for (BasicBlock succ : bb.getSuccessors()) {
        if (succ == exitBlock) {
          // We don't want to process the exit block.
          continue;
        }
        if (visitedBlocks.contains(succ)) {
          // This successor has been visited already.
          continue;
        }
        if (workQueue.contains(succ)) {
          // This successor is already in the work queue.
          continue;
        }
        // Add it to the head of the queue to visit in depth-first mode.
        workQueue.addFirst(succ);
      }

      // Update probe with source information of each statement it covers.
      for (JStatement st : bb.getStatements()) {
        ProbeUpdater visitor = new ProbeUpdater(currentProbe);
        visitor.accept(st);
      }

      // Mark branch lines when coming from an 'if' (including conditional) or 'switch' statement.
      // This is required to generate the coverage report.
      for (BasicBlock pred : bb.getPredecessors()) {
        if (pred instanceof ConditionalBasicBlock || pred instanceof SwitchBasicBlock) {
          // Get last statement of predecessor.
          List<JStatement> predStatements = pred.getStatements();
          JStatement branchStatement = predStatements.get(predStatements.size() - 1);
          assert branchStatement instanceof JIfStatement ||
            branchStatement instanceof JSwitchStatement;
          SourceInfo branchSourceInfo = branchStatement.getSourceInfo();
          currentProbe.incrementLine(branchSourceInfo.getStartLine(), 1, true);
        }
      }

      // Is the block terminating a probe?
      if (isLastBlockForProbe(bb, exitBlock)) {
        // This basic blocks ends the current probe. The instrumentation will add extra statements
        // at its end to enable the probe at runtime.
        bb.addMarker(new ProbeMarker(currentProbe));

        // We need to start a new probe for the next block being visited, if there is at least one.
        if (!workQueue.isEmpty()) {
          currentProbe = coverageMarker.createProbe(method);
        }
      }
    }

    // We must have visited all the nodes of the cfg.
    assert visitedBlocks.size() == controlFlowGraph.getNodes().size();
  }

  // TODO(shertz) Jacoco does not seem to split probe on throwing instructions. If we want
  // to do the same thing, we could test whether the predecessor is a Pei block, in which
  // case we continue with the same probe instead of creating a new one.
  private static boolean isLastBlockForProbe(@Nonnull BasicBlock bb,
      @Nonnull BasicBlock exitBlock) {
    assert bb != exitBlock;
    List<BasicBlock> successors = bb.getSuccessors();
    int successorsCount = successors.size();
    if (successorsCount > 1) {
      // There are multiple paths from this block so the probe ends.
      return true;
    } else {
      assert successorsCount == 1;
      BasicBlock succ = successors.get(0);
      if (succ.getPredecessors().size() > 1) {
        // There are multiple paths to the successor so the probe ends.
        return true;
      } else if (succ == exitBlock) {
        // The block exits the method (return or throw) so the probe ends
        return true;
      }
    }
    return false;
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
