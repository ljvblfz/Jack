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

package com.android.jack.cfg;

import com.android.jack.ir.ast.JStatement;

import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * {@link ConditionalBasicBlock} represents blocks ended by if statement.
 */
public class ConditionalBasicBlock extends BasicBlock {

  @Nonnegative
  private static final int THEN_BLOCK_INDEX = 0;
  @Nonnegative
  private static final int ELSE_BLOCK_INDEX = 1;
  @Nonnegative
  private static final int FIXED_BLOCK_COUNT = 2;

  public ConditionalBasicBlock(@Nonnull ControlFlowGraph cfg,
      @Nonnull List<JStatement> statements) {
    super(cfg, statements, cfg.getNextBasicBlockId(), FIXED_BLOCK_COUNT);
    cfg.addNode(this);
  }

  public void setThenBlock(@Nonnull BasicBlock thenBb) {
    setSuccessor(THEN_BLOCK_INDEX, thenBb);
  }

  public void setElseBlock(@Nonnull BasicBlock elseBb) {
    setSuccessor(ELSE_BLOCK_INDEX, elseBb);
  }

  @Nonnull
  public BasicBlock getThenBlock() {
    return getInternalSuccessors().get(THEN_BLOCK_INDEX);
  }

  @Nonnull
  public BasicBlock getElseBlock() {
    return getInternalSuccessors().get(ELSE_BLOCK_INDEX);
  }
}
