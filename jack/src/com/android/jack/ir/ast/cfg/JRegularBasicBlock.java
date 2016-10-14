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

import com.android.jack.ir.ast.JVisitor;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
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
  public List<JBasicBlockElement> elements(final boolean forward) {
    ImmutableList<JBasicBlockElement> list = ImmutableList.copyOf(elements);
    return forward ? list : list.reverse();
  }

  @Override
  @Nonnull
  public final ArrayList<JBasicBlock> successors() {
    ArrayList<JBasicBlock> result = new ArrayList<>();
    collectSuccessors(result);
    return result;
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
   * Returns block's primary successor.
   *
   * Throws an exception if hasPrimarySuccessor() returns false.
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
  public JBasicBlockElement lastElement() {
    assert elements.size() > 0;
    return elements.get(elements.size() - 1);
  }

  @Override
  @Nonnull
  public JBasicBlockElement firstElement() {
    assert elements.size() > 0;
    return elements.get(0);
  }

  @Override
  public boolean hasElements() {
    return !elements.isEmpty();
  }

  @Override
  public void appendElement(@Nonnull JBasicBlockElement element) {
    elements.add(element);
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
}
