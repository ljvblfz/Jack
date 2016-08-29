/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.cfg;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.util.graph.Graph;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.ArrayList;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This class represent the control flow graph of a {@link JMethod}.
 */
@Description("A ControlFlowGraph marker.")
@ValidOn(JMethod.class)
public class ControlFlowGraph extends Graph<BasicBlock> implements Marker {

  @Nonnull
  private final JMethod method;

  @Nonnegative
  private final int basicBlockMaxId;

  /**
   * Create the control flow graph associated with method argument.
   * @param method method associated to this control flow graph
   * @param basicBlockMaxId the biggest id of a BasicBlock in the graph
   * @param entryBlock entry BasicBlock
   * @param exitBlock exit BasicBlock
   * @param blocks all BasicBlocks in the graph
   */
  public ControlFlowGraph(@Nonnull JMethod method, @Nonnegative int basicBlockMaxId,
      @Nonnull EntryBlock entryBlock, @Nonnull ExitBlock exitBlock,
      @Nonnull ArrayList<BasicBlock> blocks) {
    super(entryBlock, exitBlock, blocks);
    assert method != null;
    assert entryBlock.getStatements().isEmpty();
    assert exitBlock.getStatements().isEmpty();
    assert !blocks.contains(entryBlock);
    assert !blocks.contains(exitBlock);

    this.method = method;
    this.basicBlockMaxId = basicBlockMaxId;
  }


  @Override
  @Nonnull
  public EntryBlock getEntryNode() {
    return (EntryBlock) super.getEntryNode();
  }

  /**
   * @return the method
   */
  @Nonnull
  public JMethod getMethod() {
    return method;
  }

  @Override
  @Nonnull
  public String toString() {
    return "Cfg of " + Jack.getUserFriendlyFormatter().getName(method.getEnclosingType()) + "."
        + Jack.getUserFriendlyFormatter().getName(method);
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }

  @Nonnegative
  public int getBasicBlockMaxId() {
    return basicBlockMaxId;
  }
}
