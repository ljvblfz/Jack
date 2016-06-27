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

import com.google.common.collect.Lists;
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
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.ControlFlowHelper;
import com.android.jack.util.OptimizationTools;
import com.android.jack.util.ThreeAddressCodeFormUtils;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
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
import java.util.LinkedHashMap;
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
@Filter(TypeWithoutPrebuiltFilter.class)
public class DefUsesChainsSimplifier extends DefUsesAndUseDefsChainsSimplifier
    implements RunnableSchedulable<JMethod> {

  @Nonnull
  public static final StatisticId<Counter> SIMPLIFIED_DEF_USE = new StatisticId<Counter>(
      "jack.optimization.defuse", "Def use chain simplified",
      CounterImpl.class, Counter.class);

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  /** Represents information for single optimization application */
  private static class OptInfo {
    @Nonnull final VarInfo aVarInfo;
    @Nonnull final DefinitionMarker bDefinition;

    private OptInfo(
        @Nonnull VarInfo aVarInfo,
        @Nonnull DefinitionMarker bDefinition) {
      this.aVarInfo = aVarInfo;
      this.bDefinition = bDefinition;
    }
  }

  /** Collected information of variable definitions and usages */
  private static class VarInfo {
    @Nonnull final JVariable var;
    /** Definitions of the variable */
    @Nonnull final Set<DefinitionMarker> defs = Sets.newIdentityHashSet();
    /** Statements referencing the variable */
    @Nonnull final List<JStatement> refStmts = new ArrayList<>();

    VarInfo(@Nonnull JVariable var) {
      this.var = var;
    }

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
  private static LinkedHashMap<JVariable, VarInfo> collectDefinitions(
      @Nonnull ControlFlowGraph cfg) {
    LinkedHashMap<JVariable, VarInfo> defs = Maps.newLinkedHashMap(); // Keep the insertion order

    for (BasicBlock bb : cfg.getNodes()) {
      for (JStatement stmt : bb.getStatements()) {

        // store variable -> definition info
        DefinitionMarker dm = ThreeAddressCodeFormUtils.getDefinitionMarker(stmt);
        if (dm != null) {
          JVariable variable = dm.getDefinedVariable();
          VarInfo info = defs.get(variable);
          if (info == null) {
            info = new VarInfo(variable);
            defs.put(variable, info);
          }
          info.defs.add(dm);
        }

        // store variable -> referencing statement info
        for (JVariableRef ref : OptimizationTools.getUsedVariables(stmt)) {
          JVariable variable = ref.getTarget();
          VarInfo info = defs.get(variable);
          if (info == null) {
            info = new VarInfo(variable);
            defs.put(variable, info);
          }
          info.refStmts.add(stmt);
        }
      }
    }

    return defs;
  }

  /** Returns candidates for optimization satisfying conditions (iii) and (v). */
  @Nonnull
  private static LinkedHashMap<JVariable, OptInfo> collectCandidates(
      @Nonnull LinkedHashMap<JVariable, VarInfo> definitions) {

    LinkedHashMap<JVariable, OptInfo> result = Maps.newLinkedHashMap(); // Keep the insertion order
    for (Map.Entry<JVariable, VarInfo> info : definitions.entrySet()) {
      OptInfo opt = considerCandidate(info.getValue());
      if (opt != null) {
        result.put(opt.aVarInfo.var, opt);
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
  private static OptInfo considerCandidate(@Nonnull VarInfo info) {

    // 'a' must be synthetic variable
    if (!info.var.isSynthetic()) {
      return null;
    }

    // There must be only one single assignment statement s0 : b = a.
    if (info.refStmts.size() != 1) {
      return null;
    }
    JStatement stmt = info.refStmts.get(0);
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
        ((JVariableRef) valueExpr).getTarget() != info.var) {
      return null;
    }

    // OK, we guarantee that conditions (iii) and (v) are satisfied                              \
    return new OptInfo(info, bDef);
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
    private static final byte BB_NOT_CHECKED_YET = 0;
    private static final byte BB_ACCESSES_NONE = 1;
    private static final byte BB_ASSIGNS_OR_READS_B = 2;
    private static final byte BB_ASSIGNS_A = 4;
    private static final byte BB_ENTRY_POINT = 8;

    private final ControlFlowGraph cfg;
    private final byte[] flags; // Lazily calculated
    private final JVariable aVar;
    private final JVariable bVar;

    CfgHelper(@Nonnull ControlFlowGraph cfg, @Nonnull JVariable aVar, @Nonnull JVariable bVar) {
      this.cfg = cfg;
      this.aVar = aVar;
      this.bVar = bVar;
      this.flags = new byte[cfg.getBasicBlockMaxId()];
    }

    boolean isCondition1or2Violated(@Nonnull JStatement startStmt) {
      return traverse(
          Lists.newArrayList(startStmt),
          (byte) (BB_ENTRY_POINT | BB_ASSIGNS_OR_READS_B), BB_ASSIGNS_A);
    }

    boolean isCondition4Violated(@Nonnull List<JStatement> startStmts) {
      return traverse(
          startStmts, BB_ASSIGNS_A, BB_ASSIGNS_OR_READS_B);
    }

    private boolean traverse(
        @Nonnull List<JStatement> startStmts, byte violatingFlags, byte ignorePredecessorFlags) {

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
        if (process(bb, stmt, violatingFlags, ignorePredecessorFlags, queue, queued)) {
          return true; // Violated in [0...stmt) section!
        }
      }

      // Traverse basic blocks backwards starting at one or more basic blocks queued
      while (!queue.isEmpty()) {
        BasicBlock bb = queue.removeFirst();
        if (process(bb, null, violatingFlags, ignorePredecessorFlags, queue, queued)) {
          return true; // Violated in the whole block
        }
      }

      // traversal ended, not failed once
      return false;
    }

    private boolean process(
        @Nonnull BasicBlock bb,
        @CheckForNull JStatement stmt,
        byte violatingFlags,
        byte ignorePredecessorFlags,
        @Nonnull LinkedList<BasicBlock> queue,
        @Nonnull boolean[] queued) {

      int bbFlag = stmt != null ? computeBasicBlockFlag(bb, stmt) : getBasicBlockFlag(bb);
      assert bbFlag != BB_NOT_CHECKED_YET;
      if ((violatingFlags & bbFlag) != 0) {
        return true; // Condition violated
      }

      if ((ignorePredecessorFlags & bbFlag) == 0) {
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

    private byte getBasicBlockFlag(@Nonnull BasicBlock basicBlock) {
      int id = basicBlock.getId();
      byte flag = flags[id];
      if (flag == BB_NOT_CHECKED_YET) {
        flag = computeBasicBlockFlag(basicBlock, null);
        flags[id] = flag;
      }
      return flag;
    }

    private byte computeBasicBlockFlag(
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
  private void processCandidate(
      @Nonnull JMethod method,
      @Nonnull ControlFlowGraph cfg,
      @Nonnull LinkedHashMap<JVariable, VarInfo> definitions,
      @Nonnull OptInfo info) {

    // Checking preconditions
    DefinitionMarker bDef = info.bDefinition;
    JVariable bVariable = bDef.getDefinedVariable();

    // Build CFG helper to use for more complex analysis
    CfgHelper helper = new CfgHelper(cfg, info.aVarInfo.var, bVariable);

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

    for (DefinitionMarker aDef : info.aVarInfo.defs) {
      JVariableRef bRefNew =
          getNewVarRef(bDef.getDefinedExpr(), aDef.getDefinedExpr().getSourceInfo());
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
    bVarInfo.mergeWith(definitions.get(info.aVarInfo.var));
    bVarInfo.defs.remove(bDef);
    definitions.remove(info.aVarInfo.var);

    tr.append(new Remove(s0));
    tr.commit();
  }

  /**
   * Process the candidates in order taking into account possible dependencies,
   * break cycles if any.
   *
   * There may be a sequences of the variables that can be optimized,
   * like in the following case:
   *
   *    $tmp1 = 1     $tmp1 = 2
   *           \       /
   *   S0.a: $tmp2 = $tmp1   $tmp2 = 3
   *                   \    /
   *            S0.b: x = $tmp2
   *
   * In this case we want optimizations to be processed is certain order: first take care
   * of the case when S0 is S0.b, then when S0 is S0.a. This makes it easier to correctly update
   * pre-collected data as we do optimizations.
   *
   * We also want to deterministically break cycles like { $t1 = $t0; $t2 = $t1; $t0 = $t2 }
   * in case they happen.
   */
  private void processCandidatesWithDependencies(
      @Nonnull JMethod method,
      @Nonnull ControlFlowGraph cfg,
      @Nonnull LinkedHashMap<JVariable, OptInfo> candidates,
      @Nonnull LinkedHashMap<JVariable, VarInfo> definitions) {

    Set<OptInfo> queued = Sets.newIdentityHashSet();
    for (Map.Entry<JVariable, OptInfo> entry : candidates.entrySet()) {
      processCandidateWithDependencies(
          method, cfg, entry.getValue(), candidates, definitions, queued);
    }
  }

  private void processCandidateWithDependencies(
      @Nonnull JMethod method,
      @Nonnull ControlFlowGraph cfg,
      @Nonnull OptInfo candidate,
      @Nonnull LinkedHashMap<JVariable, OptInfo> candidates,
      @Nonnull LinkedHashMap<JVariable, VarInfo> definitions,
      @Nonnull Set<OptInfo> queued) {

    if (queued.contains(candidate)) {
      // This candidate is either already processed or there is a dependency cycle
      // and this one is scheduled to be processed but waits for it's dependencies,
      // in both cases don't want to process it now.
      return;
    }

    queued.add(candidate);

    // Check if this candidate has dependency on other, i.e. current candidate's 'b'
    // variable is an 'a' variable in some other possible optimization. Process that
    // optimization first
    JVariable bVar = candidate.bDefinition.getDefinedVariable();
    if (candidates.containsKey(bVar)) {
      // We do have 'b' in our candidate's list, process it first
      processCandidateWithDependencies(
          method, cfg, candidates.get(bVar), candidates, definitions, queued);
    }

    // Process this candidate
    processCandidate(method, cfg, definitions, candidate);
  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    ControlFlowGraph cfg = method.getMarker(ControlFlowGraph.class);
    assert cfg != null;

    // Collect all variables defined in the method with info we are going to use to
    // analyze them and make optimizations in one pass.
    // NOTE: we preserve the insertion order to make it deterministic
    LinkedHashMap<JVariable, VarInfo> definitions = collectDefinitions(cfg);

    // Define a list of candidates to be optimized which satisfy
    // conditions (iii) and (v), other conditions will be checked later.
    // NOTE: we preserve the insertion order to make it deterministic
    LinkedHashMap<JVariable, OptInfo> candidates = collectCandidates(definitions);

    // Now handle each candidate pair separately to check the remaining
    // conditions and apply optimization if they are satisfied.
    processCandidatesWithDependencies(method, cfg, candidates, definitions);

    method.removeMarker(ReachingDefsMarker.class);
  }
}
