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

import com.google.common.collect.ImmutableList;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents a regular implementation of CFG basic block containing any
 * number of basic block elements.
 */
public abstract class JRegularBasicBlock extends JBasicBlock {
  @CheckForNull
  private JBasicBlock primarySuccessor;

  private final List<JBasicBlockElement> elements = new ArrayList<>();

  JRegularBasicBlock(@CheckForNull JBasicBlock primarySuccessor) {
    if ((primarySuccessor == null) == hasPrimarySuccessor()) {
      throw new AssertionError();
    }
    this.primarySuccessor = primarySuccessor;
    if (this.primarySuccessor != null) {
      this.primarySuccessor.addPredecessor(this);
    }
  }

  @Override
  @Nonnull
  public List<JBasicBlockElement> getElements(final boolean forward) {
    ImmutableList<JBasicBlockElement> list = ImmutableList.copyOf(elements);
    return forward ? list : list.reverse();
  }

  @Override
  @Nonnull
  public final List<JBasicBlock> getSuccessors() {
    ArrayList<JBasicBlock> result = new ArrayList<>();
    collectSuccessors(result);
    return Jack.getUnmodifiableCollections().getUnmodifiableList(result);
  }

  void collectSuccessors(@Nonnull ArrayList<JBasicBlock> successors) {
    if (hasPrimarySuccessor()) {
      assert primarySuccessor != null;
      successors.add(primarySuccessor);
    }
  }

  /** If the block has primary successor */
  public abstract boolean hasPrimarySuccessor();

  /**
   * Returns block's primary successor. Used in codegen.
   * Valid only if the primary successor exists, see hasPrimarySuccessor().
   */
  @Nonnull
  public JBasicBlock getPrimarySuccessor() {
    assert hasPrimarySuccessor();
    assert primarySuccessor != null;
    return primarySuccessor;
  }

  @Override
  public void replaceAllSuccessors(@Nonnull JBasicBlock what, @Nonnull JBasicBlock with) {
    if (this.primarySuccessor == what) {
      this.primarySuccessor = resetSuccessor(what, with);
    }
  }

  @Override
  @Nonnull
  public JBasicBlockElement getLastElement() {
    assert elements.size() > 0;
    return elements.get(elements.size() - 1);
  }

  @Override
  @Nonnull
  public JBasicBlockElement getFirstElement() {
    assert elements.size() > 0;
    return elements.get(0);
  }

  @Override
  public boolean hasElements() {
    return !elements.isEmpty();
  }

  @Nonnegative
  @Override
  public int getElementCount() {
    return elements.size();
  }

  @Override
  public void appendElement(@Nonnull JBasicBlockElement element) {
    elements.add(element);
    element.updateParents(this);
  }

  @Override
  public void insertElement(int at, @Nonnull JBasicBlockElement element) {
    int size = elements.size();
    if (at < 0) {
      at = size + at;
    }
    assert at >= 0 && at <= size;

    if (at == size) {
      appendElement(element);
    } else {
      elements.add(at, getLastElement());
      element.updateParents(this);
    }
  }

  final void acceptElements(@Nonnull JVisitor visitor) {
    visitor.accept(elements);
  }

  final void traverseElements(
      @Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    for (JBasicBlockElement element : elements) {
      element.traverse(schedule);
    }
  }

  @Override
  public void checkValidity() {
    super.checkValidity();
  }

  @Nonnull
  @Override
  public JSimpleBasicBlock split(int at) {
    List<JBasicBlockElement> elements = this.elements;

    int size = elements.size();
    if (at < 0) {
      at = size + at;
    }
    assert at >= 0 && at < size;

    JSimpleBasicBlock block = new JSimpleBasicBlock(getCfg(), this);

    // Move elements to a new block and append goto element at the end
    for (int i = 0; i < at; i++) {
      block.appendElement(elements.get(i));
    }
    block.appendElement(new JGotoBlockElement(SourceInfo.UNKNOWN));

    // Remove elements from this block
    if (at > 0) {
      int dest = 0;
      for (int src = at; src < size; src++) {
        elements.set(dest++, elements.get(src));
      }
      while (dest < size) {
        elements.remove(--size);
      }
    }

    // Re-point all the predecessors to a newly created simple block
    for (JBasicBlock pre : this.getPredecessorsSnapshot()) {
      if (pre != block) {
        pre.replaceAllSuccessors(this, block);
      }
    }

    return block;
  }

  /**
   * Removes the basic block by redirecting all the predecessors to point to the
   * primary successor of this block.
   */
  final void deleteByRedirectingToPrimarySuccessor() {
    assert hasPrimarySuccessor();
    JBasicBlock primary = getPrimarySuccessor();

    // Redirect all the successors to point the primary successor of this block
    for (JBasicBlock pre : this.getPredecessorsSnapshot()) {
      pre.replaceAllSuccessors(this, primary);
    }

    // Dereference the block from all successors
    replaceAllSuccessors(new JPlaceholderBasicBlock(getCfg()));
  }
}
