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

package com.android.jack.ir.ast;

import com.android.jack.ir.SourceInfo;
import com.android.sched.item.Description;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A list of JStatements.
 */
@Description("A list of JStatements")
public abstract class JStatementList extends JStatement {

  private static final long serialVersionUID = 1L;

  @Nonnull
  protected List<JStatement> statements = new ArrayList<JStatement>();

  public JStatementList(@Nonnull SourceInfo info) {
    super(info);
  }

  /**
   * Insert a statement into this block.
   */
  public void addStmt(@Nonnegative int index, @Nonnull JStatement toAdd) {
    statements.add(index, toAdd);
  }

  /**
   * Add a statement to the end of this block.
   */
  public void addStmt(@Nonnull JStatement toAdd) {
    statements.add(toAdd);
  }

  /**
   * Insert a statements into this block.
   */
  public void addStmts(@Nonnegative int index, @Nonnull List<JStatement> toAdd) {
    statements.addAll(index, toAdd);
  }

  /**
   * Add statements to the end of this block.
   */
  public void addStmts(@Nonnull List<JStatement> toAdd) {
    statements.addAll(toAdd);
  }

  public void clear() {
    statements.clear();
  }

  /**
   * Return the statements in this block.
   */
  @Nonnull
  public List<JStatement> getStatements() {
    return statements;
  }

  /**
   * Removes the statement from this catch block at the specified index.
   */
  public void removeStmt(@Nonnegative int index) {
    statements.remove(index);
  }
}