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

import com.android.jack.Jack;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Description;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/** Represents an abstract CFG basic block implementation */
@Description("CFG Basic Block")
public abstract class JBasicBlock extends JNode {
  @Nonnull
  private final ArrayList<JBasicBlock> predecessors = new ArrayList<>();

  public JBasicBlock() {
    super(SourceInfo.UNKNOWN);
  }

  @Nonnull
  public JControlFlowGraph getCfg() {
    JNode parent = this.getParent();
    assert parent instanceof JControlFlowGraph;
    return (JControlFlowGraph) parent;
  }

  /** Validate the block */
  public boolean isValid() {
    return true;
  }

  /** Iterator over successors */
  @Nonnull
  public abstract Iterable<JBasicBlock> successors();

  /** Iterator over elements, can be forward or backward */
  @Nonnull
  public abstract List<JBasicBlockElement> elements(boolean forward);

  /** Access to the first element, asserts if there is no elements */
  @Nonnull
  public abstract JBasicBlockElement firstElement();

  /** Access to the last element, asserts if there is no elements */
  @Nonnull
  public abstract JBasicBlockElement lastElement();

  /** Is the element list empty? */
  public abstract boolean hasElements();

  /** Append a new basic block element the end of the basic block */
  public abstract void appendElement(@Nonnull JBasicBlockElement element);

  /** Replace all the successors equal to 'what' with 'with' */
  public abstract void replaceAllSuccessors(@Nonnull JBasicBlock what, @Nonnull JBasicBlock with);

  /** Iterator over predecessors */
  public final Iterable<JBasicBlock> predecessors() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(predecessors);
  }

  /** Resets successor/predecessor references for replacing 'current' with 'candidate' */
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
