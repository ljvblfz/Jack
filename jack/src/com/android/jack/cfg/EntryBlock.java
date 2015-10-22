/*
 * Copyright (C) 2013 The Android Open Source Project
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

/**
 * {@link EntryBlock} is a special block to have only one exit block into the control flow graph.
 * {@link EntryBlock} will be targeted by a {@code ReturnBasicBlock} and {@code PeiBasicBlock}.
 */
public class EntryBlock extends NormalBasicBlock {

  public EntryBlock(@Nonnegative int id) {
    super(id, BasicBlock.EMPTY_STATEMENT_LIST);
  }
}
