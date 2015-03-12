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
 * {@link NormalBasicBlock} represents blocks targeting the following statement. This following
 * statement is also targeted by others blocks.
 */
public class NormalBasicBlock extends BasicBlock {

  @Nonnegative
  private static final int TARGET_BLOCK_INDEX = 0;

  @Nonnegative
  protected static final int NORMAL_BLOCK_FIXED_BLOCK_COUNT = 1;

  public NormalBasicBlock(@Nonnegative int id, @Nonnull List<JStatement> statements) {
    super(id, statements, NORMAL_BLOCK_FIXED_BLOCK_COUNT);
  }

  protected NormalBasicBlock(@Nonnegative int id, @Nonnull List<JStatement> statements,
      @Nonnegative int fixedSuccessorCount) {
    super(id, statements, fixedSuccessorCount);
  }

  public void setTarget(@Nonnull BasicBlock target) {
    setSuccessor(TARGET_BLOCK_INDEX, target);
  }

  @Nonnull
  public BasicBlock getTarget() {
    return (getInternalSuccessors().get(TARGET_BLOCK_INDEX));
  }
}
