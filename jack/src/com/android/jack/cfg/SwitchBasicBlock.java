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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * {@link SwitchBasicBlock} represents blocks ended by switch statement.
 */
public class SwitchBasicBlock extends BasicBlock {

  @Nonnegative
  private static final int DEFAULT_BLOCK_INDEX = 0;
  @Nonnegative
  private static final int CASE_BLOCK_START_INDEX = 1;
  @Nonnegative
  private static final int FIXED_BLOCK_COUNT = 1;

  public SwitchBasicBlock(@Nonnegative int id, @Nonnull List<JStatement> statements) {
    super(id, statements, FIXED_BLOCK_COUNT);
  }

  public void setDefault(@Nonnull BasicBlock defaultBb) {
    setSuccessor(DEFAULT_BLOCK_INDEX, defaultBb);
  }

  public void addCaseBlock(@Nonnull BasicBlock caseBb) {
    addSuccessor(CASE_BLOCK_START_INDEX, caseBb);
  }

  @Nonnull
  public BasicBlock getDefaultBlock() {
    return getInternalSuccessors().get(DEFAULT_BLOCK_INDEX);
  }

  @Nonnull
  public List<BasicBlock> getCasesBlock() {
    // Case block must be reverse since cfg is built with ordered case values but addCaseBlock
    // inverse the case block since it append to a list at a specified index.
    List<BasicBlock> cases =
        getInternalSuccessors().subList(CASE_BLOCK_START_INDEX, getInternalSuccessors().size());
    ArrayList<BasicBlock> result = new ArrayList<BasicBlock>();
    for (BasicBlock bb : cases) {
      result.add(0, bb);
    }
    return result;
  }
}
