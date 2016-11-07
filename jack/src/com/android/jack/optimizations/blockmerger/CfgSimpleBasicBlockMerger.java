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

package com.android.jack.optimizations.blockmerger;

import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.cfg.BasicBlockComparator;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JEntryBasicBlock;
import com.android.jack.ir.ast.cfg.JRegularBasicBlock;
import com.android.jack.ir.ast.cfg.JSimpleBasicBlock;
import com.android.jack.ir.ast.cfg.JVariableAsgBlockElement;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.optimizations.Optimizations;
import com.android.jack.optimizations.cfg.CfgBasicBlockUtils;
import com.android.jack.optimizations.cfg.VariablesScope;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/** Simple CFG basic block merger */
@Description("Simple CFG basic block merger")
@Transform(modify = JControlFlowGraph.class)
@Use(CfgBasicBlockUtils.class)
public class CfgSimpleBasicBlockMerger
    implements RunnableSchedulable<JControlFlowGraph> {

  @Nonnull
  public static final StatisticId<Counter> BLOCKS_MERGED = new StatisticId<>(
      "jack.optimization.simple-block-merging.blocks-merged", "Blocks merged",
      CounterImpl.class, Counter.class);

  private final boolean preserveSourceInfo =
      ThreadConfig.get(Optimizations.SimpleBasicBlockMerging.PRESERVE_SOURCE_INFO).booleanValue();
  @Nonnull
  private final VariablesScope mergeVarsScope =
      ThreadConfig.get(Optimizations.SimpleBasicBlockMerging.MERGE_VARIABLES);

  @Override
  public void run(@Nonnull final JControlFlowGraph cfg) {
    CfgBasicBlockUtils basicBlockUtils = new CfgBasicBlockUtils(cfg);

    // #1 Maximally split basic blocks
    basicBlockUtils.maximallySplitAllBasicBlocks();

    // #2 Merge basic blocks
    mergeBlocks(cfg);

    // #3 Maximally merge simple basic blocks
    basicBlockUtils.mergeSimpleBlocks(preserveSourceInfo);
  }

  private void mergeBlocks(@Nonnull final JControlFlowGraph cfg) {
    new Processor(cfg).process();
  }

  /** Processing class */
  private class Processor {
    @Nonnull
    private final Tracer tracer = TracerFactory.getTracer();
    @Nonnull
    private final JControlFlowGraph cfg;

    /** Maps list of successors into a (stable) set of blocks having such successors */
    @Nonnull
    private final
    Map<List<JBasicBlock>, Set<JBasicBlock>> groupsBySuccessors = new LinkedHashMap<>();
    /**
     * Maps basic blocks into the cached list of their successors, this cache is
     * also used to access successors list after the block's successors are updated.
     */
    @Nonnull
    private final Map<JBasicBlock, List<JBasicBlock>> successorsCache = new LinkedHashMap<>();
    /** Processing queue */
    @Nonnull
    private final Set<List<JBasicBlock>> queue = new LinkedHashSet<>();

    /** Caches last used independent variables set */
    @CheckForNull
    private IndependentVariables independentVariablesCache = null;

    Processor(@Nonnull JControlFlowGraph cfg) {
      this.cfg = cfg;

      for (JBasicBlock block : cfg.getAllBlocksUnordered()) {
        if (block != this.cfg.getEntryBlock() && block != this.cfg.getExitBlock()) {
          assert block instanceof JRegularBasicBlock;
          successorsCache.put(block, block.getSuccessors());
          addBlockToSuccessorsGroup(block);
        }
      }

      // We process all groups of the basic blocks sharing the same list of
      // successors in stable order. In case we merged some of the blocks
      // we might need to update `groupsBySuccessors` map, and re-process
      // groups we have already processed.
      queue.addAll(groupsBySuccessors.keySet());
    }

    private void addBlockToSuccessorsGroup(@Nonnull JBasicBlock block) {
      List<JBasicBlock> successors = successorsCache.get(block);
      assert successors != null;
      Set<JBasicBlock> blocks = groupsBySuccessors.get(successors);
      if (blocks == null) {
        blocks = new LinkedHashSet<>();
        groupsBySuccessors.put(successors, blocks);
      }
      blocks.add(block);
    }

    void process() {
      while (!queue.isEmpty()) {
        Iterator<List<JBasicBlock>> iterator = queue.iterator();
        assert iterator.hasNext();
        List<JBasicBlock> group = iterator.next();
        iterator.remove();
        processGroup(group);
      }
    }

    private void processGroup(@Nonnull List<JBasicBlock> group) {
      Set<JBasicBlock> blocks = groupsBySuccessors.get(group);
      assert blocks != null;

      // Only process groups with 2 or more blocks
      if (blocks.size() < 2) {
        return;
      }

      List<JBasicBlock> blocksOfLength = new ArrayList<>();

      // We can only merge blocks of the same kind and of the same size.
      // Iterate blocks with sizes from 1 to max.
      int length = 1;
      doneWithGroup:
      while (true) {
        // Process the blocks of the specified length only, if we find two
        // blocks that can be be merged, do so.
        Iterator<JBasicBlock> iterator = blocks.iterator();

        sameLength:
        while (iterator.hasNext()) {
          JBasicBlock candidate = iterator.next();
          if (candidate.getElementCount() == length) {

            // Check if we can merge with any of the existing blocks
            for (JBasicBlock existing : blocksOfLength) {

              if (shouldReplaceCandidateWithReplacement(candidate, existing)) {
                // Merge the blocks
                replaceOriginalBlockWithReplacement(candidate, existing);

                // Merging blocks (actually replacing `candidate` block with `existing`
                // block) may change the current group in following ways:
                //
                //  1. If `candidate` is part of the group 'signature' (`group` list), *all*
                //     the blocks should be removed from this group since their successors
                //     have changed.
                //
                //  NOTE: deleting `candidate` basic block may only result in removing basic
                //        blocks other than `candidate` from `blocks` set if these blocks
                //        are predecessors of `candidate`, meaning `candidate` is part of
                //        group signature.
                if (blocks.isEmpty()) {
                  break doneWithGroup;
                }

                //  2. If removing `candidate` caused adding *new* blocks to `blocks` list it
                //     will also lead to this group being re-scheduled for processing, so we
                //     just stop here and let the whole group be re-processed later.
                if (queue.contains(group)) {
                  break doneWithGroup;
                }

                //  3. Otherwise we can just remove `candidate` from `blocks` list and continue
                //     processing blocks with the same length.
                iterator.remove();
                continue sameLength;
              }
            }

            // We didn't merge `candidate`
            blocksOfLength.add(candidate);
          }
        }

        // Try to grow the blocks in the group if possible
        if (!addTrivialPredecessors(blocks, length)) {
          break;
        }

        length++;
        blocksOfLength.clear();
      }
    }

    private void resetIndependentVariables() {
      independentVariablesCache = null;
    }

    /**
     * Returns true if `a` and `b` can substitute each other.
     * They can if they are considered to be independent and appropriate
     * options are set.
     */
    private boolean canSubstituteVariables(@Nonnull JLocal a, @Nonnull JLocal b) {
      if (mergeVarsScope == VariablesScope.NONE) {
        return false;
      }
      if (mergeVarsScope == VariablesScope.SYNTHETIC && (!a.isSynthetic() || !b.isSynthetic())) {
        return false;
      }

      // Get variables info
      if (independentVariablesCache == null) {
        independentVariablesCache = new IndependentVariables(cfg);
      }
      return independentVariablesCache.areIsolatedAndIndependent(a, b);
    }

    private boolean shouldReplaceCandidateWithReplacement(
        @Nonnull JBasicBlock candidate, @Nonnull JBasicBlock replacement) {
      // `candidate` and be replaced with `replacement` if these two blocks are
      // considered equal, except for allowed local variable differences.
      BasicBlockComparator comparator = new BasicBlockComparator() {
        @Nonnull @Override protected Comparator getComparator() {
          @Nonnull final Map<JLocal, JLocal> substitutions = new HashMap<>();

          return new Comparator() {
            @Override protected void performCommonChecks(@Nonnull JExpression expr) {
              super.performCommonChecks(expr);
              if (preserveSourceInfo) {
                // Make sure the two nodes have same source info
                ensure(expr.getSourceInfo().equals(otherOrMe(expr).getSourceInfo()));
              }
            }

            @Override protected boolean equal(@Nonnull JVariable a, @Nonnull JVariable b) {
              if (a == b) {
                return true;
              }
              if (a instanceof JLocal && b instanceof JLocal) {
                if (!a.getType().isSameType(b.getType())) {
                  return false; // Should be of different types
                }
                JLocal local = substitutions.get(a);
                if (local != null) {
                  return b == local; // Should not be mapped into different `b`
                }
                local = substitutions.get(b);
                if (local != null) {
                  return a == local; // Should not be mapped into different `a`
                }
                if (canSubstituteVariables((JLocal) a, (JLocal) b)) {
                  substitutions.put((JLocal) a, (JLocal) b);
                  substitutions.put((JLocal) b, (JLocal) a);
                  return true;
                }
              }
              return false;
            }
          };
        }
      };

      return comparator.compare(candidate, replacement);
    }

    /**
     * For all blocks of given length checks if their have one simple predecessor of
     * kind JSimpleBasicBlock. Such a predecessor can me merged into their successors.
     */
    private boolean addTrivialPredecessors(
        @Nonnull Set<JBasicBlock> blocks, @Nonnegative int length) {

      boolean seenLonger = false;
      for (JBasicBlock block : blocks) {
        if (block.getElementCount() == length && block.getPredecessorCount() == 1) {
          JBasicBlock predecessor = block.getPredecessors().get(0);
          assert predecessor != block;
          assert !blocks.contains(predecessor);

          if (predecessor instanceof JSimpleBasicBlock) {
            // We increase the length of the block by merging its
            // simple predecessor into the block
            JSimpleBasicBlock simple = (JSimpleBasicBlock) predecessor;
            if (!preserveSourceInfo
                || simple.getLastElement().getSourceInfo() == SourceInfo.UNKNOWN) {

              // Clean up the singleton group `simple` belongs to (should be
              // single-basic-block group with successors being { block }).
              Set<JBasicBlock> groupBlocks = groupsBySuccessors.get(successorsCache.get(simple));
              assert groupBlocks != null && groupBlocks.size() == 1;
              groupBlocks.remove(simple);

              // Merge `simple` into its only successor. Note that this operation
              // should not change the blocks in the current group.
              List<JBasicBlock> predecessors = simple.getPredecessorsSnapshot();
              simple.mergeIntoSuccessor();
              handleRemovedBlock(simple, predecessors);

              // Merging blocks may result in more independent variables, clear the cache
              resetIndependentVariables();
            }
          }
        }
        if (block.getElementCount() > length) {
          seenLonger = true;
        }
      }
      return seenLonger;
    }

    /**
     * Merge `original` and `replacement` blocks, such that the original block is deleted
     * and replaced with `replacement` block. Properly updates all maps and processing queue.
     *
     * After replacing with `replacement` block, `original` is removed from the CFG and all
     * its predecessors are remapped to point into `replacement`, their successor maps are
     * updated accordingly.
     */
    private void replaceOriginalBlockWithReplacement(
        @Nonnull JBasicBlock original, @Nonnull JBasicBlock replacement) {

      // Replace the block, but don't remove it from
      // `groupsBySuccessors` (it'll be done by the caller)
      List<JBasicBlock> originalPredecessors = original.getPredecessorsSnapshot();
      original.detach(replacement);
      handleRemovedBlock(original, originalPredecessors);
      tracer.getStatistic(BLOCKS_MERGED).incValue();
    }

    private void handleRemovedBlock(
        @Nonnull JBasicBlock block, @Nonnull List<JBasicBlock> predecessors) {
      successorsCache.remove(block);

      // Update predecessors
      for (JBasicBlock predecessor : predecessors) {
        List<JBasicBlock> successors = successorsCache.get(predecessor);
        if (successors == null) {
          assert predecessor instanceof JEntryBasicBlock || predecessor == block;
          continue;
        }

        // Remove the predecessor from the old group. Don't need to reprocess
        // `successors` group it was part of, since we only remove its element
        // and if it was processed before it should not change the merging result
        Set<JBasicBlock> blocks = groupsBySuccessors.get(successors);
        assert blocks != null;
        blocks.remove(predecessor);

        // Put the predecessor into a new group, schedule it for reprocessing
        successorsCache.put(predecessor, predecessor.getSuccessors());
        addBlockToSuccessorsGroup(predecessor);
        queue.add(successorsCache.get(predecessor));
      }
    }
  }

  /**
   * Represents a set of isolated variables which can be replaced with each other
   * during block merge (substitution).
   *
   * Isolated variable is a variable which value does not flow between basic blocks,
   * i.e. if the variable is used in the basic block it is assigned in this basic
   * block before any read.
   */
  private static class IndependentVariables {
    @Nonnull
    private final Map<JLocal, Set<JBasicBlock>> isolatedVariables = new HashMap<>();

    IndependentVariables(@Nonnull JControlFlowGraph cfg) {
      new JVisitor() {
        @Nonnull
        final Set<JLocal> assignedInsideBlock = new HashSet<>();
        @Nonnull
        final Set<JLocal> notIsolated = new HashSet<>();
        @CheckForNull
        JBasicBlock currentBlock = null;

        @Override public boolean visit(@Nonnull JBasicBlock block) {
          assert currentBlock == null;
          currentBlock = block;
          assignedInsideBlock.clear();
          return super.visit(block);
        }

        @Override public boolean visit(@Nonnull JLocalRef ref) {
          assert currentBlock != null;
          JLocal local = ref.getLocal();

          // Read before assignment?
          JNode parent = ref.getParent();
          boolean isAssignmentTarget =
              parent instanceof JAsgOperation && ((JAsgOperation) parent).getLhs() == ref;
          if (!isAssignmentTarget) {
            // Local is a read ...
            if (!assignedInsideBlock.contains(local)) {
              // ... and is not assigned yet in this block
              notIsolated.add(local);
            }
          }

          // Mark referencing block
          Set<JBasicBlock> blocks = isolatedVariables.get(local);
          if (blocks == null) {
            blocks = new HashSet<>();
            isolatedVariables.put(local, blocks);
          }
          blocks.add(currentBlock);

          return super.visit(ref);
        }

        @Override public void endVisit(@Nonnull JVariableAsgBlockElement element) {
          assert currentBlock != null;
          JVariable variable = element.getVariable();
          if (variable instanceof JLocal) {
            assignedInsideBlock.add((JLocal) variable);
          }
          super.endVisit(element);
        }

        @Override public void endVisit(@Nonnull JBasicBlock block) {
          assert currentBlock != null;
          currentBlock = null;
          super.endVisit(block);
        }

        @Override public void endVisit(@Nonnull JControlFlowGraph cfg) {
          // Remove non isolated locals
          for (JLocal local : notIsolated) {
            isolatedVariables.remove(local);
          }
          super.endVisit(cfg);
        }
      }.accept(cfg);
    }

    boolean areIsolatedAndIndependent(@Nonnull JLocal what, @Nonnull JLocal with) {
      Set<JBasicBlock> whatBlocks = isolatedVariables.get(what);
      if (whatBlocks == null) {
        return false;
      }
      Set<JBasicBlock> withBlocks = isolatedVariables.get(with);
      if (withBlocks == null) {
        return false;
      }
      for (JBasicBlock block : whatBlocks) {
        if (withBlocks.contains(block)) {
          return false;
        }
      }
      return true;
    }
  }
}
