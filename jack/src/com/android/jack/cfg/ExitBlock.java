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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * {@link ExitBlock} is a special block to have only one exit block into the control flow graph.
 * {@link ExitBlock} will be targeted by a {@code ReturnBasicBlock} and {@code PeiBasicBlock}.
 */
public class ExitBlock extends BasicBlock {

  private static final long serialVersionUID = 1L;

  @Nonnegative
  private static final int FIXED_BLOCK_COUNT = 0;

  public ExitBlock(@Nonnull ControlFlowGraph cfg) {
    super(cfg, BasicBlock.EMPTY_STATEMENT_LIST, Integer.MAX_VALUE, FIXED_BLOCK_COUNT);
  }

}
