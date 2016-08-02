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

import com.android.jack.Jack;
import com.android.jack.ir.ast.JStatement;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;


/**
 * {@link PeiBasicBlock} represents blocks ended by statement that can potentially trigger
 * exceptions.
 */
public class PeiBasicBlock extends NormalBasicBlock {

  @Nonnegative
  protected static final int UNCAUGHT_PEI_EXIT_BLOCK_INDEX = 1;

  @Nonnegative
  private static final int EXCEPTION_BLOCKS_START_INDEX = 2;

  @Nonnegative
  // 1 for a branch to the normal successor and 1 for a branch to the exit node.
  private static final int SIZE_WHEN_NO_CATCH = 2;

  public PeiBasicBlock(@Nonnegative int id, @Nonnull List<JStatement> statements) {
    super(id, statements, SIZE_WHEN_NO_CATCH);
  }

  public void addExceptionBlock(@Nonnull CatchBasicBlock exceptionBb) {
    addSuccessor(EXCEPTION_BLOCKS_START_INDEX, exceptionBb);
  }

  public void setExitBlockWhenUncaught(@Nonnull ExitBlock exitBlock) {
    setSuccessor(UNCAUGHT_PEI_EXIT_BLOCK_INDEX, exitBlock);
  }

  public boolean isExceptionOrUncaughtBlock(@Nonnull BasicBlock basicBlock) {
    return getInternalSuccessors().get(0) != basicBlock;
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public List<CatchBasicBlock> getExceptionBlocks() {
    return ((List<CatchBasicBlock>) (List<? extends BasicBlock>) getInternalSuccessors().subList(
        EXCEPTION_BLOCKS_START_INDEX, getInternalSuccessors().size()));
  }

  @Override
  @Nonnull
  public List<BasicBlock> getSuccessors() {
    List<BasicBlock> filteredSuccessors = new ArrayList<BasicBlock>(Math
        .max(successors.size() - 1 /* means branch to the exit node */, SIZE_WHEN_NO_CATCH));
    for (BasicBlock succ : successors) {
      if (succ != null) {
        filteredSuccessors.add(succ);
      }
    }
    return Jack.getUnmodifiableCollections().getUnmodifiableList(filteredSuccessors);
  }
}
