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
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.util.ThreeAddressCodeFormUtils;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Use;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Common part for simplification of definition uses chains and use definitions chains.
 */
@Description("Common part for simplification of definition uses chains and use definitions chains.")
@Use(ThreeAddressCodeFormUtils.class)
@Constraint(need = {BasicBlockMarker.class})
public abstract class DefUsesAndUseDefsChainsSimplifier {

  /**
   * Check if a definition of {@code var} appears between the statement {@code fromStmt} and the
   * statement {@code toStmt}.
   *
   * @param var Check that a definition of this variable exists.
   * @param fromStmt Statement where the search start
   * @param toStmt Statement where the search end.
   * @return true if a definition exists, false otherwise.
   */
  protected boolean hasDefBetweenStatement(
      @Nonnull JVariable var, @Nonnull JStatement fromStmt, @Nonnull JStatement toStmt) {
    BasicBlockMarker startBbm = fromStmt.getMarker(BasicBlockMarker.class);
    assert startBbm != null;

    BasicBlockMarker endBbm = toStmt.getMarker(BasicBlockMarker.class);
    assert endBbm != null;

    HashMap<BasicBlock, Boolean> bbCanReachEndBb = new HashMap<BasicBlock, Boolean>();
    BasicBlock endBb = endBbm.getBasicBlock();
    bbCanReachEndBb.put(endBb, Boolean.valueOf(true));

    return (hasDefBetweenNodes(startBbm.getBasicBlock(), endBb,
    /* path */ new Stack<BasicBlock>(),
    /* bbCanReachToBb */ bbCanReachEndBb,
    /* hasLocalDefOnPreviousBlocks */ false, var, fromStmt, toStmt));
  }

  @Nonnull
  protected JVariableRef getNewVarRef(@Nonnull JNode defExpr) {
    JVariableRef newVarAccess;

    if (defExpr instanceof JLocalRef) {
      newVarAccess = new JLocalRef(
          defExpr.getSourceInfo(), (JLocal) ((JLocalRef) defExpr).getTarget());
    } else if (defExpr instanceof JThisRef){
      JThis jThis = ((JThisRef) defExpr).getTarget();
      JType thisType = jThis.getType();
      assert thisType instanceof JDefinedClass;
      newVarAccess = new JThisRef(defExpr.getSourceInfo(), jThis);
    } else {
      assert defExpr instanceof JParameterRef;
      newVarAccess = new JParameterRef(
          defExpr.getSourceInfo(), (JParameter) ((JParameterRef) defExpr).getTarget());
    }

    return newVarAccess;
  }

  private boolean hasDefBetweenNodes(@Nonnull BasicBlock from,
      @Nonnull BasicBlock to,
      @Nonnull Stack<BasicBlock> currentPath,
      @Nonnull HashMap<BasicBlock, Boolean> bbCanReachToBb,
      boolean hasLocalDefOnPreviousBlocks,
      @Nonnull JVariable var,
      @CheckForNull JStatement beginAfterStmt,
      @Nonnull JStatement end) {

    // There is a definition if previous definition already exist on the path or
    // if a definition exist into the 'from' basic block.
    boolean hasDef =
         hasLocalDefOnPreviousBlocks || hasLocalDef(var, from, beginAfterStmt, end);

    // A definition exists if the 'from' basic block can reach the 'to' basic block and that a
    // definition exists.
    // 'bbCanReachToBb' is used to speed-up analysis by reusing information previously
    // computed
    Boolean fromBbCanReachToBb = bbCanReachToBb.get(from);
    if (fromBbCanReachToBb != null && fromBbCanReachToBb.booleanValue() == true && hasDef == true) {
      return true;
    }

    if (from != to && bbCanReachToBb.get(from) == null) {

      currentPath.push(from);

      boolean hasEndIntoSucc = false;
      for (BasicBlock succ : from.getSuccessors()) {
        // 'currentPath' avoid to cycle by skipping basic blocks that are already into the
        // current path.
        if (!currentPath.contains(succ)) {
          if (hasDefBetweenNodes(succ, to, currentPath, bbCanReachToBb,
              hasDef, var, null, end)) {
            return true;
          }
        }
        Boolean hasPathtoDest = bbCanReachToBb.get(succ);
        if (succ == to ||  (hasPathtoDest != null && hasPathtoDest.booleanValue() == true)) {
          hasEndIntoSucc = true;
        }
      }

      // All successors of 'from' are analyzed, in order to speed-up analysis we save if the
      // 'from' basic block can reach the 'to' basic block.
      bbCanReachToBb.put(from, Boolean.valueOf(hasEndIntoSucc));

      currentPath.pop();
    }

    return false;
  }

  protected boolean hasLocalDef(@Nonnull JVariable var, @Nonnull BasicBlock basicBlock,
      @CheckForNull JStatement beginAfterStmt, @Nonnull JStatement end) {

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
          return true;
        }
      }
    }

    return false;
  }
}
