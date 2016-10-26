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

package com.android.jack.ir.ast.cfg;

import com.google.common.collect.Lists;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Description;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/** Represents an abstract CFG basic block implementation */
@Description("CFG Basic Block")
public abstract class JBasicBlock extends JNode {
  @Nonnull
  private final ArrayList<JBasicBlock> predecessors = new ArrayList<>();

  JBasicBlock() {
    super(SourceInfo.UNKNOWN);
  }

  @Nonnull
  public JControlFlowGraph getCfg() {
    JNode parent = this.getParent();
    assert parent instanceof JControlFlowGraph;
    return (JControlFlowGraph) parent;
  }

  /** Immutable successors snapshot */
  @Nonnull
  public abstract List<JBasicBlock> getSuccessors();

  /** Immutable list of all block elements, can be forward or backward */
  @Nonnull
  public abstract List<JBasicBlockElement> getElements(boolean forward);

  /** Elements count */
  @Nonnegative
  public abstract int getElementCount();

  /** Returns the first element, element must exist */
  @Nonnull
  public abstract JBasicBlockElement getFirstElement();

  /** Returns the last element, element must exist */
  @Nonnull
  public abstract JBasicBlockElement getLastElement();

  /** Is the element list empty? */
  public abstract boolean hasElements();

  /** Appends a new basic block element at the end of the basic block */
  public abstract void appendElement(@Nonnull JBasicBlockElement element);

  /**
   * Inserts a new basic block element into the list of this basic block's elements.
   * <p>
   * Index may be positive or negative:
   * <li> <b>at == 0</b>: `element` is inserted in the beginning </li>
   * <li> <b>at == elementCount()</b>: equal to appendElement(element) </li>
   * <li> <b>at == 1</b>: `element` is inserted after the first element
   * (there must be at least one element already) </li>
   * <li> <b>at == -1</b>: `element` is inserted before the last element
   * (there must be at least one element already) </li>
   */
  public abstract void insertElement(int at, @Nonnull JBasicBlockElement element);

  /** Replace all the successors equal to 'what' with 'with' */
  public abstract void replaceAllSuccessors(@Nonnull JBasicBlock what, @Nonnull JBasicBlock with);

  /**
   * Dereferences all successors of the current block. I.e. after the completion of
   * this method all the successor references of this block will point to the block
   * itself. Mostly used as part of detaching the basic block from the cfg.
   */
  public void dereferenceAllSuccessors() {
    for (JBasicBlock successor : new HashSet<>(getSuccessors())) {
      replaceAllSuccessors(successor, this);
    }
  }

  /** Immutable predecessors' list */
  public final List<JBasicBlock> getPredecessors() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(predecessors);
  }

  /** Snapshot of predecessors' list */
  public final List<JBasicBlock> getPredecessorsSnapshot() {
    return Lists.newArrayList(predecessors);
  }

  /**
   * Splits the block at the location 'at' into two blocks, with the first
   * block being JSimpleBasicBlock with block elements [0..at> of the original
   * basic block and the second block being the original block without block
   * elements moved to the first block.
   *
   * Note that `at` may be negative, with semantics same as in `insertElement(...)`.
   *
   * Value of `at` must not be pointing after the last element of
   * the block, since the last element must stay in the original block.
   *
   * <pre>
   *   Original:
   *      block {e0, e1, ... eAt, ... eN}
   *
   *   Split at '0':
   *      simple-block {goto-element} ---> block {e0, e1, ... eAt, ... eN}
   *
   *   Split at I:
   *      simple-block {e0, e1, ... e(I-1), goto-element} ---> block {eI, ... eN}
   * </pre>
   */
  @Nonnull
  public abstract JSimpleBasicBlock split(int at);

  /**
   * Resets successor/predecessor references for replacing 'current' with 'candidate',
   * returns a new successor.
   */
  @Nonnull
  JBasicBlock resetSuccessor(@Nonnull JBasicBlock current, @Nonnull JBasicBlock candidate) {
    if (candidate != current) {
      current.removePredecessor(this);
      candidate.addPredecessor(this);
    }
    return candidate;
  }

  /** Remove predecessor */
  private void removePredecessor(@Nonnull JBasicBlock predecessor) {
    assert predecessors.contains(predecessor);
    int sizeM1 = predecessors.size() - 1;
    for (int idx = 0; idx < sizeM1; idx++) {
      if (predecessors.get(idx) == predecessor) {
        predecessors.set(idx, predecessors.get(sizeM1));
        break;
      }
    }
    predecessors.remove(sizeM1);
  }

  /** Add predecessor */
  void addPredecessor(@Nonnull JBasicBlock predecessor) {
    predecessors.add(predecessor);
  }

  public void checkValidity() {
  }
}
