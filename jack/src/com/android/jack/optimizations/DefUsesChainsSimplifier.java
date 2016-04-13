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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.android.jack.Options;
import com.android.jack.analysis.DefinitionMarker;
import com.android.jack.analysis.UseDefsMarker;
import com.android.jack.analysis.UsedVariableMarker;
import com.android.jack.analysis.dfa.reachingdefs.ReachingDefsMarker;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.ControlFlowHelper;
import com.android.jack.util.OptimizationTools;
import com.android.jack.util.ThreeAddressCodeFormUtils;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Optimization will transform
 * Path 1
 * a = true   // s1
 * ...
 * Path 2
 * a = false  // s2
 * ...
 * Path 3 target of path 1 & 2
 * b = a      // s0
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
 * Optimization can be apply if the following conditions are respected:
 * (i)   all the possible paths from entry point to s0 are going through {si}
 * (ii)  all possible paths from {si} to s0 do not have b assigned or referenced in between
 * (iii) all a defined in {si} is only referenced in s0
 * (iv)  all possible paths between {bi} and their uses don’t go through {si}
 * (v)   there are no more definitions of a except those defined in {si}
 */
@Description("Simplify definition uses chains.")
@Constraint(need = { DefinitionMarker.class, UseDefsMarker.class, ThreeAddressCodeForm.class,
    ControlFlowGraph.class, UsedVariableMarker.class })
// ReachingDefsMarker is no longer valid after this optimization, thus remove it directly
@Transform(remove = { ReachingDefsMarker.class })
@Support(Optimizations.DefUseSimplifier.class)
public class DefUsesChainsSimplifier extends DefUsesAndUseDefsChainsSimplifier
    implements RunnableSchedulable<JMethod> {

  @Nonnull
  public static final StatisticId<Counter> SIMPLIFIED_DEF_USE = new StatisticId<Counter>(
      "jack.optimization.defuse", "Def use chain simplified",
      CounterImpl.class, Counter.class);

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  /** Represents information for single optimization application */
  private static class OptInfo {
    final JVariable aVariable;
    final Set<DefinitionMarker> aDefinitions;
    final DefinitionMarker bDefinition;

    private OptInfo(
        @Nonnull JVariable aVariable,
        @Nonnull Set<DefinitionMarker> aDefinitions,
        @Nonnull DefinitionMarker bDefinition) {
      this.aVariable = aVariable;
      this.aDefinitions = aDefinitions;
      this.bDefinition = bDefinition;
    }
  }

  /** Collected information of variable definitions and usages */
  private static class VarInfo {
    /** Definitions of the variable */
    final Set<DefinitionMarker> defs = Sets.newIdentityHashSet();
    /** Statements referencing the variable */
    final Set<JStatement> refStmts = Sets.newIdentityHashSet();

    void mergeWith(@Nonnull VarInfo other) {
      // All defs of variable 'a' are now
      // patched to be new defs of variable 'b'
      this.defs.addAll(other.defs);
      // 'a' variable was supposed to only have one
      // referencing statement 's0' which we have removed
      assert other.refStmts.size() == 1;
    }
  }

  /** Collects all variables used within method with their definitions */
  @Nonnull
  private static Map<JVariable, VarInfo> collectDefinitions(@Nonnull ControlFlowGraph cfg) {
    Map<JVariable, VarInfo> defs = Maps.newIdentityHashMap();

    for (BasicBlock bb : cfg.getNodes()) {
      for (JStatement stmt : bb.getStatements()) {

        // store variable -> definition info
        DefinitionMarker dm = ThreeAddressCodeFormUtils.getDefinitionMarker(stmt);
        if (dm != null) {
          JVariable variable = dm.getDefinedVariable();
          if (!defs.containsKey(variable)) {
            defs.put(variable, new VarInfo());
          }
          defs.get(variable).defs.add(dm);
        }

        // store variable -> referencing statement info
        for (JVariableRef ref : OptimizationTools.getUsedVariables(stmt)) {
          JVariable variable = ref.getTarget();
          if (!defs.containsKey(variable)) {
            defs.put(variable, new VarInfo());
          }
          defs.get(variable).refStmts.add(stmt);
        }
      }
    }

    return defs;
  }

  /** Returns candidates for optimization satisfying conditions (iii) and (v). */
  @Nonnull
  private static List<OptInfo> collectCandidates(
      @Nonnull Map<JVariable, VarInfo> definitions) {

    ArrayList<OptInfo> result = new ArrayList<>();
    for (Map.Entry<JVariable, VarInfo> entry : definitions.entrySet()) {
      OptInfo info = considerCandidate(entry.getKey(), entry.getValue());
      if (info != null) {
        result.add(info);
      }
    }
    return result;
  }

  /**
   * Checks if the variable 'var' with the definitions and referencing statements
   * (precalculated in 'info') satisfy conditions (iii) and (v), and if it does,
   * returns a candidate pair {a(=var), b-definition(calculated)} info.
   *
   * Note that this pair still need to be checked against other conditions.
   */
  @CheckForNull
  private static OptInfo considerCandidate(
      @Nonnull JVariable var, @Nonnull VarInfo info) {

    // 'a' must be synthetic variable
    if (!var.isSynthetic()) {
      return null;
    }

    // There must be only one single assignment statement s0 : b = a.
    if (info.refStmts.size() != 1) {
      return null;
    }
    JStatement stmt = info.refStmts.iterator().next();
    if (!(stmt instanceof JExpressionStatement)) {
      return null;
    }
    JExpression assignment = ((JExpressionStatement) stmt).getExpr();
    if (!(assignment instanceof JAsgOperation)) {
      return null;
    }

    // All defs of a-variable should only be used once in the same assignment
    for (DefinitionMarker aDef : info.defs) {
      List<JVariableRef> aRefs = aDef.getUses();
      if (aRefs.size() == 1) {
        if (assignment != aRefs.get(0).getParent()) {
          return null;
        }
      }
    }

    // Just confirm that 'assignment' is a reference to the original variable
    DefinitionMarker bDef = assignment.getMarker(DefinitionMarker.class);
    if (bDef == null || !bDef.hasValue()) {
      return null;
    }
    JExpression valueExpr = bDef.getValue();
    if (!(valueExpr instanceof JVariableRef) ||
        ((JVariableRef) valueExpr).getTarget() != var) {
      return null;
    }

    // OK, we guarantee that conditions (iii) and (v) are satisfied                              \
    return new OptInfo(var, info.defs, bDef);
  }

  /*
   * Helper class implementing traversal CFG
   *
   * Lazily calculates and stores flags on each basic block indicating
   * the latest (in bb) reference to 'a' or 'b' or being entry point.
   *
   * Implements cfg traversal to detect conditions (i), (ii) and (iv).
   */
  private static final class CfgHelper {
    private static final int BB_NOT_CHECKED_YET = 0;
    private static final int BB_ACCESSES_NONE = 1;
    private static final int BB_ASSIGNS_OR_READS_B = 2;
    private static final int BB_ASSIGNS_A = 3;
    private static final int BB_ENTRY_POINT = 4;

    private final ControlFlowGraph cfg;
    private final int[] flags; // Lazily calculated
    private final JVariable aVar;
    private final JVariable bVar;

    CfgHelper(@Nonnull ControlFlowGraph cfg, @Nonnull JVariable aVar, @Nonnull JVariable bVar) {
      this.cfg = cfg;
      this.aVar = aVar;
      this.bVar = bVar;
      this.flags = new int[cfg.getBasicBlockMaxId()];
    }

    private interface IntPredicate {
      boolean test(int i);
    }

    private static final IntPredicate failTestFor1and2 = new IntPredicate() {
      @Override public boolean test(int flag) {
        // NOTE: even if 'b' is a parameter it still should be initialized
        //       via 'a' variable on all paths
        return flag == BB_ENTRY_POINT || flag == BB_ASSIGNS_OR_READS_B;
      }
    };

    private static final IntPredicate descendTestFor1and2 = new IntPredicate() {
      @Override public boolean test(int flag) {
        return flag != BB_ASSIGNS_A;
      }
    };

    boolean isCondition1or2Violated(@Nonnull JStatement startStmt) {
      return traverse(Sets.newHashSet(startStmt), failTestFor1and2, descendTestFor1and2);
    }

    private static final IntPredicate failTestFor4param = new IntPredicate() {
      @Override public boolean test(int flag) {
        // NOTE: non local variable (parameter or this) is assumed to be assigned
        //       just before the entry block, so it's OK if we reach it
        return flag == BB_ASSIGNS_A;
      }
    };

    private static final IntPredicate failTestFor4local = new IntPredicate() {
      @Override public boolean test(int flag) {
        assert flag != BB_ENTRY_POINT; // Local variable is supposed to be assigned
        return flag == BB_ASSIGNS_A;
      }
    };

    private static final IntPredicate descendFor4 = new IntPredicate() {
      @Override public boolean test(int flag) {
        return flag != BB_ASSIGNS_OR_READS_B;
      }
    };

    boolean isCondition4Violated(@Nonnull Set<JStatement> startStmts) {
      IntPredicate failed = (bVar instanceof JLocal) ? failTestFor4local : failTestFor4param;
      return traverse(startStmts, failed, descendFor4);
    }

    private boolean traverse(
        @Nonnull Set<JStatement> startStmts,
        @Nonnull IntPredicate failed,
        @Nonnull IntPredicate descend) {

      boolean[] queued = new boolean[flags.length];
      LinkedList<BasicBlock> queue = new LinkedList<>();

      // Fill in the queue with predecessors of basic blocks including start statements
      for (JStatement stmt : startStmts) {
        // NOTE: we don't mark this basic block here as queued since we only analyzed
        //       part of it [0...stmt), we still want to look at this block later if/when
        //       we reach it in traversal to analyze the remaining [stmt...N] statements.
        // NOTE: If there are no statements accessing 'a' or 'b' in the remaining
        //       section ([stmt...N]), the block will be marked as
        //       BB_ASSIGNS_OR_READS_B since 'b' is assigned or accessed in stmt
        BasicBlock bb = ControlFlowHelper.getBasicBlock(stmt);
        if (process(bb, stmt, failed, descend, queue, queued)) {
          return true; // Failed in [0...stmt) section!
        }
      }

      // Traverse basic blocks backwards starting at one or more basic blocks queued
      while (!queue.isEmpty()) {
        BasicBlock bb = queue.removeFirst();
        if (process(bb, null /* whole block */, failed, descend, queue, queued)) {
          return true; // Failed in [0...stmt) section!
        }
      }

      // traversal ended, not failed once
      return false;
    }

    private boolean process(
        @Nonnull BasicBlock bb,
        @CheckForNull JStatement stmt,
        @Nonnull IntPredicate failed,
        @Nonnull IntPredicate descend,
        @Nonnull LinkedList<BasicBlock> queue,
        @Nonnull boolean[] queued) {

      int bbFlag = stmt != null ? computeBasicBlockFlag(bb, stmt) : getBasicBlockFlag(bb);
      assert bbFlag != BB_NOT_CHECKED_YET;
      if (failed.test(bbFlag)) {
        return true; // Failed
      }

      if (descend.test(bbFlag)) {
        for (BasicBlock bbPredecessor : bb.getPredecessors()) {
          int id = bbPredecessor.getId();
          if (!queued[id]) {
            queue.addLast(bbPredecessor);
            queued[id] = true;
          }
        }
      }
      return false;
    }

    private int getBasicBlockFlag(@Nonnull BasicBlock basicBlock) {
      int id = basicBlock.getId();
      int flag = flags[id];
      if (flag == BB_NOT_CHECKED_YET) {
        flag = computeBasicBlockFlag(basicBlock, null);
        flags[id] = flag;
      }
      return flag;
    }

    private int computeBasicBlockFlag(
        @Nonnull BasicBlock basicBlock, @CheckForNull JStatement upperLimit) {

      List<JStatement> statements = basicBlock.getStatements();
      for (int i = statements.size() - 1; i >= 0; i--) {
        JStatement stmt = statements.get(i);

        // If upper limit statement is specified, ignore everything until we see it
        if (upperLimit != null) {
          if (upperLimit == stmt) {
            upperLimit = null;
          }
          continue; // to the next (actually previous) statement
        }

        // Assigns A or B?
        DefinitionMarker dm = ThreeAddressCodeFormUtils.getDefinitionMarker(stmt);
        if (dm != null) {
          JVariable variable = dm.getDefinedVariable();
          if (variable == aVar) {
            return BB_ASSIGNS_A;
          } else if (variable == bVar) {
            return BB_ASSIGNS_OR_READS_B;
          }
        }

        // Reads B?
        List<JVariableRef> refs = OptimizationTools.getUsedVariables(stmt);
        for (JVariableRef ref : refs) {
          if (ref.getTarget() == bVar) {
            return BB_ASSIGNS_OR_READS_B;
          }
        }
      }

      return cfg.getEntryNode() == basicBlock ? BB_ENTRY_POINT : BB_ACCESSES_NONE;
    }
  }

  /** Check if preconditions stand for this candidate and perform optimization if they do */
  private void handleCandidate(
      @Nonnull JMethod method,
      @Nonnull ControlFlowGraph cfg,
      @Nonnull Map<JVariable, VarInfo> definitions,
      @Nonnull OptInfo info) {

    // Checking preconditions
    DefinitionMarker bDef = info.bDefinition;
    JVariable bVariable = bDef.getDefinedVariable();

    // Build CFG helper to use for more complex analysis
    CfgHelper helper = new CfgHelper(cfg, info.aVariable, bVariable);

    JStatement s0 = bDef.getStatement();
    assert s0 != null;
    assert definitions.containsKey(info.bDefinition.getDefinedVariable());

    // Check for conditions (i) and (ii)
    if (helper.isCondition1or2Violated(s0)) {
      return;
    }

    // Check for condition (iv) for all references of variable 'b'
    VarInfo bVarInfo = definitions.get(bVariable);
    if (helper.isCondition4Violated(bVarInfo.refStmts)) {
      return;
    }

    // Apply optimization for the pair and patch precalculated info
    tracer.getStatistic(SIMPLIFIED_DEF_USE).incValue();
    TransformationRequest tr = new TransformationRequest(method);

    for (DefinitionMarker aDef : info.aDefinitions) {
      JVariableRef bRefNew = getNewVarRef(bDef.getDefinedExpr());
      tr.append(new Replace(aDef.getDefinedExpr(), bRefNew));

      // Definition of 'a' is becoming a definition of 'b'
      aDef.resetDefinedVariable(bVariable);
      // Also all uses of the original b definition are now uses of a new one
      // Update definitions as well if needed
      aDef.removeAllUses(); // should only be one
      for (JVariableRef bDefUse : bDef.getUses()) {
        aDef.addUse(bDefUse);
      }
    }

    // Update use/def information to reflect the change
    bDef.removeAllUses();

    // Update `definitions`
    bVarInfo.mergeWith(definitions.get(info.aVariable));
    definitions.remove(info.aVariable);

    tr.append(new Remove(s0));
    tr.commit();
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    ControlFlowGraph cfg = method.getMarker(ControlFlowGraph.class);
    assert cfg != null;

    // Collect all variables defined in the method with info we are going to use to
    // analyze them and make optimizations in one pass.
    Map<JVariable, VarInfo> definitions = collectDefinitions(cfg);

    // Define a list of candidates to be optimized which satisfy
    // conditions (iii) and (v), other conditions will be checked later.
    List<OptInfo> candidates = collectCandidates(definitions);

    // Now handle each candidate pair separately to check the remaining
    // conditions and apply optimization if they are satisfied.
    for (OptInfo info : candidates) {
      handleCandidate(method, cfg, definitions, info);
    }

    method.removeMarker(ReachingDefsMarker.class);
  }
}
