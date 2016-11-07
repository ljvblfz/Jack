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

package com.android.jack.transformations.request;

import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JStatementList;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

/**
 * A {@link TransformationStep} allowing to add a {@link JStatement} as the last statement of a
 * {@link JBlock}.
 *
 * @see PrependStatement
 */
public class AppendStatement implements TransformationStep, TransformStep {
  @Nonnull
  private final JStatementList statements;

  @Nonnull
  private final JStatement statement;

  /**
   * Constructor specifying the {@code statement} to add  at the end of the given
   * {@code statements} list.
   *
   * @param statements the list of statements to update
   * @param statement the new statement to add at the end of the list of statements
   */
  public AppendStatement(@Nonnull JStatementList statements, @Nonnull JStatement statement) {
    assert statements != null;
    assert statement != null;

    this.statements = statements;
    this.statement = statement;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    statements.addStmt(statement);
    statement.updateParents(statements);
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("Append ");
    sb.append(statement.toSource());
    sb.append(" in ");
    sb.append(statements.toSource());
    return sb.toString();
  }

}
