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

package com.android.jack.statistics;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JForStatement;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.JWhileStatement;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * Compute the number of block and extra block created to manage stand alone statement.
 */
@Description("Compute the number of block and extra block created to manage stand alone statement.")
@Name("BlockStatistics")
@Constraint(need = {JMethodBody.class})
@Transform(add = BlockCountMarker.class)
@Synchronized
public class BlockStatistics implements RunnableSchedulable<JMethod> {

  private static class BlockStatisticsVisitor extends JVisitor {

    @Nonnull
    private final BlockCountMarker bcm;

    public BlockStatisticsVisitor(@Nonnull BlockCountMarker bcm) {
      this.bcm = bcm;
    }

    @Override
    public boolean visit(@Nonnull JBlock block) {
      bcm.addExistingBlockCount(1);
      return super.visit(block);
    }

    @Override
    public boolean visit(@Nonnull JCatchBlock catchBlock) {
      bcm.addExistingBlockCount(1);
      return super.visit(catchBlock);
    }

    @Override
    public boolean visit(@Nonnull JIfStatement ifStmt) {
      if (!(ifStmt.getThenStmt() instanceof JBlock)) {
        bcm.addExtraIfThenBlockCount(1);
      }

      if (!(ifStmt.getElseStmt() instanceof JBlock)) {
        bcm.addExtraIfElseBlockCount(1);
      }
      return super.visit(ifStmt);
    }

    @Override
    public boolean visit(@Nonnull JLabeledStatement labeledStmt) {
      if (!(labeledStmt.getBody() instanceof JBlock)) {
        bcm.addExtraLabeledStatementBlockCount(1);
      }
      return super.visit(labeledStmt);
    }

    @Override
    public boolean visit(@Nonnull JForStatement forStmt) {
      if (!(forStmt.getBody() instanceof JBlock)) {
        bcm.addExtraForBodyBlockCount(1);
      }

      JNode parent = forStmt.getParent();
      if (parent instanceof JBlock) {
        JBlock parentBlock = (JBlock) parent;
        if (parentBlock.getStatements().size() != 1) {
          bcm.addExtraImplicitForBlockCount(1);
        }
      }

      return super.visit(forStmt);
    }

    @Override
    public boolean visit(@Nonnull JWhileStatement whileStmt) {
      if (!(whileStmt.getBody() instanceof JBlock)) {
        bcm.addExtraWhileBlockCount(1);
      }
      return super.visit(whileStmt);
    }
  }

  @Override
  public synchronized void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract()) {
      return;
    }

    JSession session = Jack.getSession();
    BlockCountMarker bcm = session.getMarker(BlockCountMarker.class);
    if (bcm == null) {
      bcm = new BlockCountMarker();
      session.addMarker(bcm);
    }

    BlockStatisticsVisitor statistics = new BlockStatisticsVisitor(bcm);
    statistics.accept(method);
  }

}
