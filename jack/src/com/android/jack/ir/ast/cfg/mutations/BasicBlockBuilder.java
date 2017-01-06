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

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JBasicBlockElement;
import com.android.jack.ir.ast.cfg.JCaseBasicBlock;
import com.android.jack.ir.ast.cfg.JCatchBasicBlock;
import com.android.jack.ir.ast.cfg.JConditionalBasicBlock;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JReturnBasicBlock;
import com.android.jack.ir.ast.cfg.JSimpleBasicBlock;
import com.android.jack.ir.ast.cfg.JSwitchBasicBlock;
import com.android.jack.ir.ast.cfg.JThrowBasicBlock;
import com.android.jack.ir.ast.cfg.JThrowingExpressionBasicBlock;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A basic block builder, simplifies building the list of block
 * elements and initializing the basic block.
 */
public class BasicBlockBuilder {
  @Nonnull
  private final JControlFlowGraph cfg;
  @CheckForNull
  private List<JBasicBlockElement> elements = new ArrayList<>();

  public BasicBlockBuilder(@Nonnull JControlFlowGraph cfg) {
    this.cfg = cfg;
  }

  @Nonnull
  private List<JBasicBlockElement> elements() {
    List<JBasicBlockElement> elements = this.elements;
    if (elements == null) {
      throw new IllegalStateException("The builder has already been closed");
    }
    return elements;
  }

  /** Append element */
  @Nonnull
  public BasicBlockBuilder append(@Nonnull JBasicBlockElement element) {
    elements().add(element);
    return this;
  }

  /** Append elements */
  @Nonnull
  public BasicBlockBuilder append(@Nonnull List<JBasicBlockElement> elements) {
    for (JBasicBlockElement element : elements) {
      elements().add(element);
    }
    return this;
  }

  /** Append elements from basic block */
  @Nonnull
  public BasicBlockBuilder append(@Nonnull JBasicBlock block) {
    return append(block.getElements(true));
  }

  /** Remove the last element */
  @Nonnull
  public BasicBlockBuilder removeLast() {
    List<JBasicBlockElement> elements = elements();
    assert elements.size() > 0;
    elements.remove(elements.size() - 1);
    return this;
  }

  @Nonnull
  public JSimpleBasicBlock createSimpleBlock(@Nonnull JBasicBlock primary) {
    return commit(new JSimpleBasicBlock(cfg, primary));
  }

  @Nonnull
  public JConditionalBasicBlock createConditionalBlock(
      @Nonnull JBasicBlock ifTrue, @Nonnull JBasicBlock ifFalse) {
    return commit(new JConditionalBasicBlock(cfg, ifTrue, ifFalse));
  }

  @Nonnull
  public JCaseBasicBlock createCaseBlock(@Nonnull JBasicBlock primary) {
    return commit(new JCaseBasicBlock(cfg, primary));
  }

  @Nonnull
  public JSwitchBasicBlock createSwitchBlock(@Nonnull JBasicBlock defaultCase) {
    return commit(new JSwitchBasicBlock(cfg, defaultCase));
  }

  @Nonnull
  public JCatchBasicBlock createCatchBlock(
      @Nonnull JBasicBlock primary, @Nonnull List<JClass> catchTypes, @Nonnull JLocal catchLocal) {
    return commit(new JCatchBasicBlock(cfg, primary, catchTypes, catchLocal));
  }

  @Nonnull
  public JReturnBasicBlock createReturnBlock() {
    return commit(new JReturnBasicBlock(cfg));
  }

  @Nonnull
  public JThrowBasicBlock createThrowBlock() {
    JThrowBasicBlock block = commit(new JThrowBasicBlock(cfg));
    block.resetCatchBlocks();
    return block;
  }

  @Nonnull
  public JThrowingExpressionBasicBlock createThrowingExprBlock(@Nonnull JBasicBlock primary) {
    JThrowingExpressionBasicBlock block = commit(new JThrowingExpressionBasicBlock(cfg, primary));
    block.resetCatchBlocks();
    return block;
  }

  /** Initializes the block and closes the builder */
  @Nonnull
  public <T extends JBasicBlock> T commit(@Nonnull T block) {
    for (JBasicBlockElement element : elements()) {
      block.appendElement(element);
    }
    this.elements = null;
    return block;
  }
}
