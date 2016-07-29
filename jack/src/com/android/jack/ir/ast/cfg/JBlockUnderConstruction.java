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

import com.android.jack.ir.ast.JVisitor;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Represents a special basic block to be used as a temporary block
 * representation during CFG construction.
 *
 * NOTE: the block can have any number of predecessors, but no successors.
 */
public final class JBlockUnderConstruction extends JBasicBlock {
  public JBlockUnderConstruction(@Nonnull JControlFlowGraph cfg) {
    updateParents(cfg);
  }

  @Override
  @Nonnull
  public Iterable<JBasicBlock> successors() {
    return Collections.emptyList();
  }

  @Override
  @Nonnull
  public List<JBasicBlockElement> elements(boolean forward) {
    throw new AssertionError();
  }

  @Override
  @Nonnull
  public JBasicBlockElement lastElement() {
    throw new AssertionError();
  }

  @Override
  @Nonnull
  public JBasicBlockElement firstElement() {
    throw new AssertionError();
  }

  @Override
  public boolean hasElements() {
    throw new AssertionError();
  }

  @Override
  public void appendElement(@Nonnull JBasicBlockElement element) {
    throw new AssertionError();
  }

  @Override
  public void replaceAllSuccessors(@Nonnull JBasicBlock what, @Nonnull JBasicBlock with) {
    throw new AssertionError();
  }

  @Override
  public void checkValidity() {
    throw new AssertionError();
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest request) throws Exception {
  }
}
