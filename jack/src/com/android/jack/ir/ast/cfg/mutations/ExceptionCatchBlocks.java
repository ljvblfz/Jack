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

package com.android.jack.ir.ast.cfg.mutations;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JBasicBlockElement;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JMethodCallBlockElement;
import com.android.jack.ir.ast.cfg.JPolymorphicMethodCallBlockElement;
import com.android.jack.ir.ast.cfg.JThrowBasicBlock;
import com.android.jack.ir.ast.cfg.JThrowBlockElement;
import com.android.jack.ir.ast.cfg.JThrowingBasicBlock;
import com.android.jack.ir.ast.cfg.JThrowingExpressionBasicBlock;
import com.android.jack.ir.ast.cfg.JVariableAsgBlockElement;
import com.android.jack.ir.sourceinfo.SourceInfo;

import java.util.List;
import javax.annotation.Nonnull;

/**
 * Represents an exception catch blocks information needed by throwing basic blocks.
 *
 * Provides factory methods for creating throwing basic blocks.
 */
public class ExceptionCatchBlocks {
  @Nonnull
  private JControlFlowGraph cfg;
  @Nonnull
  private JBasicBlock unhandledCatchBlock;
  @Nonnull
  private List<JBasicBlock> catchBlocks;

  public ExceptionCatchBlocks(@Nonnull JBasicBlock unhandledCatchBlock,
      @Nonnull List<JBasicBlock> catchBlocks, @Nonnull JControlFlowGraph cfg) {
    this.unhandledCatchBlock = unhandledCatchBlock;
    this.catchBlocks = Jack.getUnmodifiableCollections().getUnmodifiableList(catchBlocks);
    this.cfg = cfg;
  }

  @Nonnull
  public static ExceptionCatchBlocks fromThrowingBlock(@Nonnull JThrowingBasicBlock block) {
    return new ExceptionCatchBlocks(
        block.getUnhandledBlock(), block.getCatchBlocks(), block.getCfg());
  }

  /** Create a new basic block representing the 'throw' expression */
  @Nonnull
  public JThrowBasicBlock createThrowBlock(
      @Nonnull JExpression exception, @Nonnull JBasicBlockElement... elements) {
    JThrowBasicBlock block = new JThrowBasicBlock(cfg, unhandledCatchBlock);
    for (JBasicBlock handler : catchBlocks) {
      block.addHandler(handler);
    }
    for (JBasicBlockElement element : elements) {
      block.appendElement(element);
    }
    block.appendElement(new JThrowBlockElement(SourceInfo.UNKNOWN, exception));
    return block;
  }

  /** Create a new basic block representing potentially throwing expression */
  @Nonnull
  public JThrowingExpressionBasicBlock createThrowBlock(
      @Nonnull JBasicBlock primarySuccessor,
      @Nonnull JBasicBlockElement throwingElement,
      @Nonnull JBasicBlockElement... elements) {

    assert throwingElement instanceof JMethodCallBlockElement
        || throwingElement instanceof JVariableAsgBlockElement
        || throwingElement instanceof JPolymorphicMethodCallBlockElement;

    JThrowingExpressionBasicBlock block =
        new JThrowingExpressionBasicBlock(cfg, primarySuccessor, unhandledCatchBlock);
    for (JBasicBlock handler : catchBlocks) {
      block.addHandler(handler);
    }
    for (JBasicBlockElement element : elements) {
      block.appendElement(element);
    }
    block.appendElement(throwingElement);
    return block;
  }
}
