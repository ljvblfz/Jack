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

import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JSsaVariableRef;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JBasicBlockElement;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JPhiBlockElement;
import com.android.jack.ir.ast.cfg.JVariableAsgBlockElement;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.graph.DominatorTreeMarker;
import com.android.jack.util.graph.NodeIdMarker;
import com.android.jack.util.graph.NodeListMarker;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

/**
 * Complete transformation to SSA form by renaming all registers accessed.<p>
 *
 * See Appel algorithm 19.7<p>
 *
 * Unlike the original algorithm presented in Appel, this renamer converts
 * to a new flat (versionless) register space. The "version 0" registers,
 * which represent the initial state of the Rop registers and should never
 * actually be meaningfully accessed in a legal program, are represented
 * as the first N registers in the SSA namespace. Subsequent assignments
 * are assigned new unique names. Note that the incoming Rop representation
 * has a concept of register widths, where 64-bit values are stored into
 * two adjoining Rop registers. This adjoining register representation is
 * ignored in SSA form conversion and while in SSA form, each register can be e
 * either 32 or 64 bits wide depending on use. The adjoining-register
 * represention is re-created later when converting back to Rop form. <p>
 *
 * But, please note, the SSA Renamer's ignoring of the adjoining-register ROP
 * representation means that unaligned accesses to 64-bit registers are not
 * supported. For example, you cannot do a 32-bit operation on a portion of
 * a 64-bit register. This will never be observed to happen when coming
 * from Java code, of course.<p>
 *
 * The implementation here, rather than keeping a single register version
 * stack for the entire method as the dom tree is walked, instead keeps
 * a mapping table for the current block being processed. Once the
 * current block has been processed, this mapping table is then copied
 * and used as the initial state for child blocks.<p>
 */
@Description("Rename variables in the CFG for SSA properties.")
@Name("SsaRenamer")
@Constraint(need = {JPhiBlockElement.class})
@Transform(add = JSsaVariableRef.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class SsaRenamer implements RunnableSchedulable<JControlFlowGraph> {

  private static class GraphRenamer {
    private final JControlFlowGraph cfg;
    private final JMethod method;

    /** the number of original rop registers */
    private final int ropRegCount;
    /** next available SSA register */
    private final int[] nextSsaReg;

    private final List<JBasicBlock> bbMap;

    /**
     * indexed by block index; register version state for each block start.
     * This list is updated by each dom parent for its children. The only
     * sub-arrays that exist at any one time are the start states for blocks
     * yet to be processed by a {@code BlockRenamer} instance.
     */
    private final JSsaVariableRef[][] startsForBlocks;

    private GraphRenamer(JControlFlowGraph cfg) {
      this.cfg = cfg;
      this.method = cfg.getMethod();
      bbMap = NodeListMarker.getNodeList(cfg);
      ropRegCount = SsaUtil.getTotalNumberOfLocals(method, cfg);

      /*
       * Reserve the first N registers in the SSA register space for
       * "version 0" registers.
       */
      nextSsaReg = new int[ropRegCount];
      startsForBlocks = new JSsaVariableRef[bbMap.size()][];

      /*
       * Appel 19.7
       *
       * Initialization:
       *   for each variable a        // register i
       *      Count[a] <- 0           // nextSsaReg, flattened
       *      Stack[a] <- 0           // versionStack
       *      push 0 onto Stack[a]
       *
       */

      // top entry for the version stack is version 0
      JSsaVariableRef[] initialRegMapping = new JSsaVariableRef[ropRegCount];
      for (int i = 0; i < ropRegCount; i++) {
        JVariable target = SsaUtil.getVariableByIndex(method, cfg, i);
        initialRegMapping[i] = new JSsaVariableRef(method.getSourceInfo(), target, 0, null, true);
      }
      // Initial state for entry block
      int entryId = NodeIdMarker.getId(cfg.getEntryBlock());
      startsForBlocks[entryId] = initialRegMapping;
    }

    private void performRename() {
      // Rename each block in dom-tree DFS order.
      forEachBlockDepthFirstDom(cfg, new Visitor() {
        @Override
        public void visitBlock(JBasicBlock block, JBasicBlock unused) {
          new BlockRenamer(block).process();
        }
      });
    }

    public void forEachBlockDepthFirstDom(JControlFlowGraph cfg, Visitor v) {
      BitSet visited = new BitSet(bbMap.size() - 1);
      Stack<JBasicBlock> stack = new Stack<JBasicBlock>();

      stack.add(cfg.getEntryNode());

      while (stack.size() > 0) {
        JBasicBlock cur = stack.pop();
        List<JBasicBlock> curDomChildren = DominatorTreeMarker.getDomChild(cur);

        if (!visited.get(NodeIdMarker.getId(cur))) {
          // We walk the tree this way for historical reasons...
          for (int i = curDomChildren.size() - 1; i >= 0; i--) {
            JBasicBlock child = curDomChildren.get(i);
            stack.add(child);
          }
          visited.set(NodeIdMarker.getId(cur));
          v.visitBlock(cur, null);
        }
      }
    }

    /**
     * Processes all insns in a block and renames their registers
     * as appropriate.
     */
    private class BlockRenamer {
      /** {@code non-null;} block we're processing. */
      private final JBasicBlock block;

      /**
       * {@code non-null;} indexed by old register name. The current
       * top of the version stack as seen by this block. It's
       * initialized from the ending state of its dom parent,
       * updated as the block's instructions are processed, and then
       * copied to each one of its dom children.
       */
      private final JSsaVariableRef[] currentMapping;

      /**
       * contains the set of moves we need to keep to preserve local
       * var info. All other moves will be deleted.
       */
      private final HashSet<JVariableAsgBlockElement> movesToKeep;

      /**
       * maps the set of insns to replace after renaming is finished
       * on the block.
       */
      private final HashMap<JBasicBlockElement, JBasicBlockElement> insnsToReplace;

      // private final RenamingMapper mapper;

      /**
       * Constructs a block renamer instance. Call {@code process}
       * to process.
       *
       * @param block {@code non-null;} block to process
       */
      BlockRenamer(final JBasicBlock block) {
        this.block = block;
        currentMapping = startsForBlocks[NodeIdMarker.getId(block)];
        movesToKeep = new HashSet<JVariableAsgBlockElement>();
        insnsToReplace = new HashMap<JBasicBlockElement, JBasicBlockElement>();
        // We don't need our own start state anymore
        startsForBlocks[NodeIdMarker.getId(block)] = null;
      }

      /**
       * Renames all the variables in this block and inserts appriopriate
       * phis in successor blocks.
       */
      public void process() {
        /*
         * From Appel:
         *
         * Rename(n) =
         *   for each statement S in block n   // 'statement' in 'block'
         */
        for (JBasicBlockElement stmt : block.getElements(true)) {
          if (stmt instanceof JPhiBlockElement) {
            processPhiStmt((JPhiBlockElement) stmt);
          } else {
            processSourceReg(stmt);
            processResultReg(stmt);
          }
        }

        updateSuccessorPhis();

        // Delete all move insns in this block.
        List<JBasicBlockElement> insns = block.getElements(true);
        int szInsns = insns.size();

        for (int i = szInsns - 1; i >= 0; i--) {
          JBasicBlockElement insn = insns.get(i);
          JBasicBlockElement replaceInsn;

          replaceInsn = insnsToReplace.get(insn);

          if (replaceInsn != null) {
            insns.set(i, replaceInsn);
          } else if (isNormalMoveInsn(insn) && !movesToKeep.contains(insn)) {
            insns.remove(i);
          }
        }

        // Store the start states for our dom children.
        boolean first = true;
        List<JBasicBlock> domChildren = DominatorTreeMarker.getDomChild(block);
        for (JBasicBlock child : domChildren) {
          if (child != block) {
            // Don't bother duplicating the array for the first child.
            JSsaVariableRef[] childStart = first ? currentMapping : dupArray(currentMapping);
            startsForBlocks[NodeIdMarker.getId(child)] = childStart;
            first = false;
          }
        }

        // currentMapping is owned by a child now.
      }

      /**
       * Enforces a few contraints when a register mapping is added.
       *
       * <ol>
       * <li> Ensures that all new SSA registers specs in the mapping
       * table with the same register number are identical. In effect, once
       * an SSA register spec has received or lost a local variable name,
       * then every old-namespace register that maps to it should gain or
       * lose its local variable name as well.
       * <li> Records the local name associated with the
       * register so that a register is never associated with more than one
       * local.
       * <li> ensures that only one SSA register
       * at a time is considered to be associated with a local variable. When
       * {@code currentMapping} is updated and the newly added element
       * is named, strip that name from any other SSA registers.
       * </ol>
       *
       * @param ropReg {@code >= 0;} rop register number
       * @param ssaReg {@code non-null;} an SSA register that has just
       * been added to {@code currentMapping}
       */
      private void addMapping(int ropReg, JSsaVariableRef ssaReg) {
          currentMapping[ropReg] = ssaReg;
      }

      /**
       *
       * Phi insns have their result registers renamed.
       */
      public void processPhiStmt(JPhiBlockElement phi) {
        /* don't process sources for phi's */
        JVariable target = phi.getTarget();
        int index = -1;
        if (target instanceof JThis) {
          return; // I don't think we ever run into this.
        }
        index = SsaUtil.getLocalIndex(method, cfg, target);
        nextSsaReg[index]++;
        // It is probably ok to not have any debug marker here.
        JSsaVariableRef lhs =
            new JSsaVariableRef(phi.getSourceInfo(), target, nextSsaReg[index], phi, true);
        TransformationRequest tr = new TransformationRequest(phi);
        tr.append(new Replace(phi.getLhs(), lhs));
        addMapping(index, lhs);
        tr.commit();
      }

      /**
       * Renames the result register of this insn and updates the
       * current register mapping. Does nothing if this insn has no result.
       * Applied to all non-move insns.
       *
       * @param insn insn to process.
       */
      void processResultReg(JBasicBlockElement insn) {
        JVariableRef dv = SsaUtil.getDefinedVariable(insn);
        if (dv == null) {
          return;
        }

        JVariable ropReg = dv.getTarget();
        int index = SsaUtil.getLocalIndex(method, cfg, ropReg);

        nextSsaReg[index]++;
        JSsaVariableRef ref =
            new JSsaVariableRef(dv.getSourceInfo(), ropReg, nextSsaReg[index], insn, true);
        ref.addAllMarkers(dv.getAllMarkers());
        TransformationRequest tr = new TransformationRequest(method);
        tr.append(new Replace(dv, ref));
        tr.commit();
        addMapping(index, ref);
      }

      void processSourceReg(JBasicBlockElement insn) {
        List<JVariableRef> uv = SsaUtil.getUsedVariables(insn);
        if (uv.isEmpty()) {
          return;
        }

        TransformationRequest tr = new TransformationRequest(method);
        for (JVariableRef varRef : uv) {
          if (varRef instanceof JThisRef) {
            continue;
          }
          int index = SsaUtil.getLocalIndex(method, cfg, varRef.getTarget());
          if (index == -1) {
            System.out.println("this is not good for business");
          }
          JSsaVariableRef ref = currentMapping[index].makeRef(varRef.getSourceInfo());
          ref.addAllMarkers(varRef.getAllMarkers());
          tr.append(new Replace(varRef, ref));
        }
        tr.commit();
      }

      /**
       * Updates the phi insns in successor blocks with operands based
       * on the current mapping of the rop register the phis represent.
       */
      private void updateSuccessorPhis() {
        for (JBasicBlock successor : block.getSuccessors()) {
          for (JBasicBlockElement stmt : successor.getElements(true)) {
            if (stmt instanceof JPhiBlockElement) {
              JPhiBlockElement phi = (JPhiBlockElement) stmt;
              int ropReg = -1;
              JVariable target = phi.getTarget();
              if (target instanceof JThis) {
                continue;
              }
              ropReg = SsaUtil.getLocalIndex(method, cfg, target);
              /*
               * Never add a version 0 register as a phi operand. Version 0 registers represent the
               * initial register state, and thus are never significant. Furthermore, the register
               * liveness algorithm doesn't properly count them as "live in" at the beginning of the
               * method.
               */

              JSsaVariableRef stackTop = currentMapping[ropReg];
              if (!isVersionZeroRegister(stackTop)) {
                TransformationRequest tr = new TransformationRequest(phi);
                JSsaVariableRef rhs = stackTop.makeRef(phi.getSourceInfo());
                rhs.addAllMarkers(phi.getRhs(block).getAllMarkers());
                tr.append(new Replace(phi.getRhs(block), rhs));
                tr.commit();
              }
            }
          }
        }
      }
    }

    /**
     * Returns true if this SSA register is a "version 0"
     * register. All version 0 registers are assigned the first N register
     * numbers, where N is the count of original rop registers.
     *
     * @param ssaReg the SSA register in question
     * @return true if it is a version 0 register.
     */
    private static boolean isVersionZeroRegister(JSsaVariableRef ssaReg) {
      return ssaReg.getVersion() == 0;
    }
  }

  @Override
  public void run(JControlFlowGraph cfg) {
    new GraphRenamer(cfg).performRename();
  }

  private static JSsaVariableRef[] dupArray(JSsaVariableRef[] orig) {
    JSsaVariableRef[] copy = new JSsaVariableRef[orig.length];
    System.arraycopy(orig, 0, copy, 0, orig.length);
    return copy;
  }

  private static boolean isNormalMoveInsn(JBasicBlockElement stmt) {
    if (stmt instanceof JVariableAsgBlockElement) {
      JAsgOperation exp = ((JVariableAsgBlockElement) stmt).getAssignment();
        return exp.getRhs() instanceof JLocalRef;
    }
    return false;
  }

  /**
   * Visitor interface for basic blocks.
   */
  public static interface Visitor {
    /**
     * Indicates a block has been visited by an iterator method.
     *
     * @param v {@code non-null;} block visited
     * @param parent {@code null-ok;} parent node if applicable
     */
    void visitBlock(JBasicBlock v, JBasicBlock parent);
  }
}
