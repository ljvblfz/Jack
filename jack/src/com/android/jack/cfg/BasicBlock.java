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
import com.android.jack.ir.ast.JStatement;
import com.android.jack.util.graph.GraphNode;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A {@link BasicBlock} is a list of statements that could not escape from the block, except the
 * last statement. A {@link BasicBlock} belongs to {@link ControlFlowGraph}, and it contains a list
 * of predecessors and successors. Each {@link BasicBlock} has an unique id inside a
 * {@link ControlFlowGraph}.
 */
public abstract class BasicBlock extends GraphNode<BasicBlock> {

  @Nonnull
  protected final ControlFlowGraph cfg;

  @Nonnull
  private final List<JStatement> statements;

  @Nonnegative
  private final int id;

  @Nonnull
  static final List<JStatement> EMPTY_STATEMENT_LIST = new LinkedList<JStatement>();

  protected BasicBlock(@Nonnull ControlFlowGraph cfg, @Nonnull List<JStatement> statements,
      @Nonnegative int id, @Nonnegative int fixedSuccessorCount) {
    super(fixedSuccessorCount);
    this.cfg = cfg;
    this.statements = Jack.getUnmodifiableCollections().getUnmodifiableList(statements);
    this.id = id;
  }

  /**
   * @return the statements
   */
  @Nonnull
  public List<JStatement> getStatements() {
    return statements;
  }

  /**
   * @return the id
   */
  @Nonnegative
  public int getId() {
    return id;
  }

  @Nonnull
  @Override
  public String toString() {
    return ("BB-" + id);
  }

  @Nonnull
  public JStatement getLastInstruction() {
    // -1 means to get the last element.
    return statements.get(statements.size() - 1);
  }
}
