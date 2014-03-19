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

import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JStatementList;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

/**
 * A {@code TransformationStep} allowing to insert a {@code JStatement} as the first statement of a
 * {@code JStatementList}.
 */
public class PrependStatement implements TransformationStep, TransformStep {
  @Nonnull
  private final JStatementList stmtList;

  @Nonnull
  private final JStatement statement;

  public PrependStatement(@Nonnull JStatementList block, @Nonnull JStatement statement) {
    assert block != null;
    assert statement != null;

    this.stmtList = block;
    this.statement = statement;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    stmtList.addStmt(0, statement);
    statement.updateParents(stmtList);
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("Prepend ");
    sb.append(statement.toSource());
    sb.append(" in ");
    sb.append(stmtList.toSource());
    return sb.toString();
  }

}
