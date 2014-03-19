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

package com.android.jack.util;

import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JStatementList;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JTryStatement;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Helpers related to control flow management.
 */
public class ControlFlowHelper {

  @CheckForNull
  public static JStatement getConcreteStatement(@Nonnull JStatementList block) {
    List<JStatement> statements = block.getStatements();

    if (statements.isEmpty()) {
      return (ControlFlowHelper.getNextStatement(block));
    }

    JStatement firstStmt = statements.get(0);
    if (firstStmt instanceof JBlock) {
      return getConcreteStatement((JBlock) firstStmt);
    } else if (firstStmt instanceof JLabeledStatement) {
      return (getConcreteStatement((JBlock) ((JLabeledStatement) firstStmt).getBody()));
    } else if (firstStmt instanceof JTryStatement) {
      return (getConcreteStatement(((JTryStatement) firstStmt).getTryBlock()));
    }

    return firstStmt;
  }
  /**
   * Find the next statement of {@code statement}. Be careful it is not a generic version but an
   * internal version use by {@code TryCatchRemover} and {@code CfgBuilder}.
   * @param statement Statement whose we want to find the successor.
   * @return The successor statement of {@code statement}
   */
  @CheckForNull
  public static JStatement getNextStatement(@Nonnull JStatement statement) {
    JStatement nextStmt = null;

    JNode parent = statement.getParent();
    if (parent instanceof JMethodBody) {
      return null;
    }

    JStatement parentStmt = (JStatement) parent;

    if (parentStmt instanceof JStatementList) {
      List<JStatement> stmts = ((JStatementList) parentStmt).getStatements();
      int stmtPosition = stmts.indexOf(statement);
      int nextStmtPosition = stmtPosition + 1;

      if (nextStmtPosition >= stmts.size()) {
        nextStmt = getNextStatement(parentStmt);
      } else {
        nextStmt = stmts.get(nextStmtPosition);
      }
    } else if (parentStmt instanceof JLabeledStatement || parentStmt instanceof JIfStatement
        || parentStmt instanceof JSwitchStatement || parentStmt instanceof JTryStatement) {
      nextStmt = getNextStatement(parentStmt);
    } else {
      throw new AssertionError();
    }

    if (nextStmt instanceof JCatchBlock) {
      nextStmt = getNextStatement(nextStmt);
    }

    return (nextStmt);
  }
}
