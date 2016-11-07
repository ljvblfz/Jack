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

package com.android.jack.optimizations.cfg;

import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.cfg.BasicBlockLiveProcessor;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JBasicBlockElement;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JReturnBasicBlock;
import com.android.jack.ir.ast.cfg.JReturnBlockElement;
import com.android.jack.ir.ast.cfg.JSimpleBasicBlock;
import com.android.jack.ir.ast.cfg.JThrowBasicBlock;
import com.android.jack.ir.ast.cfg.JThrowBlockElement;
import com.android.jack.ir.ast.cfg.JThrowingBasicBlock;
import com.android.jack.ir.ast.cfg.mutations.BasicBlockBuilder;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.util.CloneExpressionVisitor;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/** Remove redundant goto edges to a simple return blocks */
@Description("Remove redundant goto edges to a simple return blocks")
@Transform(modify = JControlFlowGraph.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class RemoveRedundantGotoReturnEdges
    implements RunnableSchedulable<JControlFlowGraph> {

  @Override
  public void run(@Nonnull final JControlFlowGraph cfg) {
    new BasicBlockLiveProcessor(cfg, /* stepIntoElements = */ false) {
      @Nonnull
      private final CloneExpressionVisitor copier = new CloneExpressionVisitor();

      @Override
      public boolean visit(@Nonnull JSimpleBasicBlock block) {
        if (block.getLastElement().getSourceInfo() != SourceInfo.UNKNOWN) {
          // Don't handle blocks with GOTO element carrying source info
          return false;
        }

        if (block.getElementCount() < 2) {
          // Empty simple blocks will be optimized away by `RemoveEmptyBasicBlocks`
          return false;
        }

        // We detect the cases when the current simple block points to a
        // single-element return ot throw blocks, in which case we can avoid
        // unnecessary jump instruction by replacing this block's end element
        // with appropriate return or throw instruction.
        JBasicBlock primary = block.getPrimarySuccessor();
        boolean isReturnBlock = primary instanceof JReturnBasicBlock;
        boolean isThrowBlock = primary instanceof JThrowBasicBlock;
        if (!(isReturnBlock || isThrowBlock) || primary.getElementCount() != 1) {
          return false;
        }

        // Move all block elements except for the trailing goto element to a new block
        BasicBlockBuilder builder = new BasicBlockBuilder(cfg).append(block).removeLast();
        JBasicBlock newBlock;

        if (isThrowBlock) {
          // Create a new throw basic block
          JBasicBlockElement throwElement = primary.getLastElement();
          assert throwElement instanceof JThrowBlockElement;

          JExpression expression = ((JThrowBlockElement) throwElement).getExpression();
          builder.append(new JThrowBlockElement(
              throwElement.getSourceInfo(), throwElement.getEHContext(),
              copier.cloneExpression(expression)));

          newBlock = builder.createThrowBlock();
          ((JThrowingBasicBlock) newBlock).resetCatchBlocks();

        } else {
          // Create a new throw return block
          JBasicBlockElement retElement = primary.getLastElement();
          assert retElement instanceof JReturnBlockElement;

          // Note that the expression is optional in return block element
          JExpression expression = ((JReturnBlockElement) retElement).getExpression();
          builder.append(new JReturnBlockElement(
              retElement.getSourceInfo(), retElement.getEHContext(),
              expression == null ? null : copier.cloneExpression(expression)));

          newBlock = builder.createReturnBlock();
        }

        // Replace this block with a new one
        block.detach(newBlock);
        return false;
      }
    }.process();
  }
}
