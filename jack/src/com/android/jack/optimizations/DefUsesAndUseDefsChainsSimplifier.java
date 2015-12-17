/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.optimizations;

import com.android.jack.analysis.DefinitionMarker;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.BasicBlockMarker;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.util.ThreeAddressCodeFormUtils;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Use;

import java.util.Iterator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Common part for simplification of definition uses chains and use definitions chains.
 */
@Description("Common part for simplification of definition uses chains and use definitions chains.")
@Use(ThreeAddressCodeFormUtils.class)
@Constraint(need = {BasicBlockMarker.class})
public abstract class DefUsesAndUseDefsChainsSimplifier {

  @Nonnull
  protected JVariableRef getNewVarRef(@Nonnull JNode defExpr) {
    assert defExpr instanceof JVariableRef;
    return ((JVariableRef) defExpr).getTarget().makeRef(defExpr.getSourceInfo());
  }

  protected boolean hasLocalDef(@Nonnull JVariable var, @Nonnull BasicBlock basicBlock,
      @CheckForNull JStatement beginAfterStmt, @CheckForNull JStatement end) {
    return getLastLocalDef(var, basicBlock, beginAfterStmt, end) != null;
  }

  @CheckForNull
  protected DefinitionMarker getLastLocalDef(@Nonnull JVariable var, @Nonnull BasicBlock basicBlock,
      @CheckForNull JStatement beginAfterStmt, @CheckForNull JStatement end) {
    DefinitionMarker lastDefinition = null;
    List<JStatement> statements = basicBlock.getStatements();

    if (!statements.isEmpty()) {
      Iterator<JStatement> stmtIt = statements.iterator();
      JStatement stmt = null;

      if (beginAfterStmt != null) {
        assert statements.contains(beginAfterStmt);

        // Skip until first statement
        while (stmt != beginAfterStmt) {
          assert stmtIt.hasNext();
          stmt = stmtIt.next();
        }
      }

      // Analyze statements
      while (stmtIt.hasNext()) {
        stmt = stmtIt.next();

        if (stmt == end) {
          break;
        }

        assert stmt != null;
        DefinitionMarker dm = ThreeAddressCodeFormUtils.getDefinitionMarker(stmt);
        if (dm != null && dm.getDefinedVariable() == var) {
          lastDefinition = dm;
        }
      }
    }

    return lastDefinition;
  }
}
