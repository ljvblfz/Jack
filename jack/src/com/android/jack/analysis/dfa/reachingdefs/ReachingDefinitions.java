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

package com.android.jack.analysis.dfa.reachingdefs;

import com.android.jack.Options;
import com.android.jack.analysis.DefinitionMarker;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.cfg.PeiBasicBlock;
import com.android.jack.config.id.Private;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JThis;
import com.android.jack.util.ThreeAddressCodeFormUtils;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.ImplementationPropertyId;
import com.android.sched.util.config.id.PropertyId;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Compute reaching definitions.
 */
@Description("Compute reaching definitions.")
@Constraint(need = {ControlFlowGraph.class, DefinitionMarker.class})
@Transform(add = {ReachingDefsMarker.class})
@Protect(add = {JMethod.class, JStatement.class}, modify = {JMethod.class, JStatement.class})
@Use(ThreeAddressCodeFormUtils.class)
@HasKeyId
public class ReachingDefinitions implements RunnableSchedulable<JMethod> {

  @Nonnull
  public static final PropertyId<ReachingDefinitionsChecker> REACHING_DEFS_CHECKER =
      ImplementationPropertyId
          .create("jack.tests.reachingdefs.checker",
              "Define a checker that must be called at the end of reaching definitions analysis",
              ReachingDefinitionsChecker.class).addDefaultValue("none")
          .withCategory(Private.get());

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private final ReachingDefinitionsChecker checker =  ThreadConfig.get(REACHING_DEFS_CHECKER);

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    solve(method);

    checker.check(method);
  }

  private void solve(@Nonnull JMethod method) {
    ControlFlowGraph cfg = method.getMarker(ControlFlowGraph.class);
    assert cfg != null;

    List<DefinitionMarker> definitions = getAllDefinitions(method, cfg);
    int definitionsSize = definitions.size();

    int basicBlockMaxId = cfg.getBasicBlockMaxId();
    BitSet[] in = new BitSet[basicBlockMaxId];
    // Output are not the same if we are on an exception path or not. Thus we compute two
    // informations that are used according to the path.
    BitSet[] out = new BitSet[basicBlockMaxId];
    BitSet[] outException = new BitSet[basicBlockMaxId];

    for (BasicBlock bb : cfg.getNodes()) {
      int bbId = bb.getId();
      in[bbId] = new BitSet(definitionsSize);
      out[bbId] = new BitSet(definitionsSize);
      outException[bbId] = new BitSet(definitionsSize);
    }

    BasicBlock entryBb = cfg.getEntryNode();

    if (!method.isStatic() && method.getEnclosingType() instanceof JDefinedClass) {
      DefinitionMarker dm = getDefinitionMarkerForThis(method);
      in[entryBb.getId()].set(dm.getBitSetIdx());
      out[entryBb.getId()].set(dm.getBitSetIdx());
    }

    // Parameters are definitions
    for (JParameter param : method.getParams()) {
      DefinitionMarker dm = param.getMarker(DefinitionMarker.class);
      assert dm != null;
      in[entryBb.getId()].set(dm.getBitSetIdx());
      out[entryBb.getId()].set(dm.getBitSetIdx());
    }

    List<BasicBlock> changeNodes = new LinkedList<BasicBlock>(cfg.getNodes());

    while (!changeNodes.isEmpty()) {
      BasicBlock bb = changeNodes.remove(0);
      int bbId = bb.getId();
      List<BasicBlock> predecessors = bb.getPredecessors();

      if (!predecessors.isEmpty()) {
        BitSet unionOfPred = in[bbId];
        unionOfPred.clear();

        for (BasicBlock pred : predecessors) {
          if (pred instanceof PeiBasicBlock
              && ((PeiBasicBlock) pred).getExceptionBlocks().contains(bb)) {
            unionOfPred.or(outException[pred.getId()]);
          } else {
            unionOfPred.or(out[pred.getId()]);
          }
        }

        in[bbId] = unionOfPred;
      }

      BitSet oldOut = (BitSet) out[bbId].clone();

      computeOutput(definitions, in[bbId], out[bbId], outException[bbId], bb);

      if (!oldOut.equals(out[bbId])) {
        for (BasicBlock succ : bb.getSuccessors()) {
          if (!changeNodes.contains(succ) && succ != cfg.getExitNode()) {
            changeNodes.add(succ);
          }
        }
      }
    }

    assert cfg.getNodes().contains(entryBb);

    for (BasicBlock bb : cfg.getNodes()) {
      bb.addMarker(new ReachingDefsMarker(getDefinitions(definitions, in[bb.getId()])));
    }
  }

  @Nonnull
  private DefinitionMarker getDefinitionMarkerForThis(@Nonnull JMethod method) {
    // JThis is a definition
    JThis jThis = method.getThis();
    assert jThis != null;
    DefinitionMarker dm = jThis.getMarker(DefinitionMarker.class);
    assert dm != null;
    return dm;
  }

  private void computeOutput(@Nonnull List<DefinitionMarker> definitions, @Nonnull BitSet inBs,
      @Nonnull BitSet outBs, @Nonnull BitSet outExceptionBs, @Nonnull BasicBlock bb) {
    outBs.clear();
    outBs.or(inBs);

    List<JStatement> statements = bb.getStatements();

    for (JStatement stmt : statements) {
      if (stmt == statements.get(statements.size() - 1)) {
        // We are on the lastStatement
        outExceptionBs.clear();
        outExceptionBs.or(outBs);
      }

      DefinitionMarker currentDef = ThreeAddressCodeFormUtils.getDefinitionMarker(stmt);
      if (currentDef != null) {
        outBs.set(currentDef.getBitSetIdx());

        // Gen keeps only the last definition of a variable, previous are killed.
        for (int i = outBs.nextSetBit(0); i >= 0; i = outBs.nextSetBit(i + 1)) {
          DefinitionMarker dm = definitions.get(i);
          if (dm.getDefinedVariable() == currentDef.getDefinedVariable() && dm != currentDef) {
            outBs.clear(dm.getBitSetIdx());
          }
        }
      }
    }
  }

  @Nonnull
  private List<DefinitionMarker> getDefinitions(
      @Nonnull List<DefinitionMarker> definitions, @Nonnull BitSet in) {
    List<DefinitionMarker> reachingDefs = new ArrayList<DefinitionMarker>();
    for (int i = in.nextSetBit(0); i >= 0; i = in.nextSetBit(i + 1)) {
      reachingDefs.add(definitions.get(i));
    }
    return (reachingDefs);
  }

  @Nonnull
  private List<DefinitionMarker> getAllDefinitions(
      @Nonnull JMethod method, @Nonnull ControlFlowGraph cfg) {
    List<DefinitionMarker> definitions = new ArrayList<DefinitionMarker>();
    int bitSetIdx = 0;

    if (!method.isStatic() && method.getEnclosingType() instanceof JDefinedClass) {
      DefinitionMarker dm = getDefinitionMarkerForThis(method);
      dm.setBitSetIdx(bitSetIdx++);
      definitions.add(dm);
    }

    for (JParameter param : method.getParams()) {
      DefinitionMarker dm = param.getMarker(DefinitionMarker.class);
      assert dm != null;
      dm.setBitSetIdx(bitSetIdx++);
      definitions.add(dm);
    }

    for (BasicBlock bb : cfg.getNodes()) {
      for (JStatement stmt : bb.getStatements()) {
        DefinitionMarker dm = ThreeAddressCodeFormUtils.getDefinitionMarker(stmt);
        if (dm != null) {
          dm.setBitSetIdx(bitSetIdx++);
          definitions.add(dm);
        }
      }
    }

    return definitions;
  }


}
