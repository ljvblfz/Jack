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

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;


/**
 * {@link PeiBasicBlock} represents blocks ended by statement that can potentially trigger
 * exceptions.
 */
public class PeiBasicBlock extends NormalBasicBlock {

  private static final long serialVersionUID = 1L;

  @Nonnegative
  private static final int NO_CATCH_EXCEPTION_INDEX = 1;
  @Nonnegative
  private static final int EXCEPTION_BLOCKS_START_INDEX = 2;

  public PeiBasicBlock(@Nonnull ControlFlowGraph cfg, @Nonnull List<JStatement> statements) {
    super(cfg, statements, cfg.getNextBasicBlockId(), NORMAL_BLOCK_FIXED_BLOCK_COUNT + 1);
  }

  public void addExceptionBlock(@Nonnull CatchBasicBlock exceptionBb) {
    addSuccessor(EXCEPTION_BLOCKS_START_INDEX, exceptionBb);
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public List<CatchBasicBlock> getExceptionBlocks() {
    if (getInternalSuccessors().get(NO_CATCH_EXCEPTION_INDEX) != null) {
      return Collections.<CatchBasicBlock>emptyList();
    }
    return ((List<CatchBasicBlock>) (List<? extends BasicBlock>) getInternalSuccessors().subList(
        EXCEPTION_BLOCKS_START_INDEX, getInternalSuccessors().size()));
  }

  public void setNoExceptionCatchBlock() {
    addSuccessor(NO_CATCH_EXCEPTION_INDEX, cfg.getExitNode());
  }
}
