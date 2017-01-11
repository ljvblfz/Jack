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

package com.android.jack.ir.ast.cfg;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.android.jack.Jack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/** Specifies the ordered set of catch blocks handling exceptions */
public final class ExceptionHandlingContext {
  @Nonnull
  public static final ExceptionHandlingContext EMPTY =
      new ExceptionHandlingContext(Collections.<JCatchBasicBlock>emptyList());

  @Nonnull
  private final List<JCatchBasicBlock> catchBlocks;

  private ExceptionHandlingContext(@Nonnull List<JCatchBasicBlock> catchBlocks) {
    this.catchBlocks = catchBlocks.isEmpty()
        ? Collections.<JCatchBasicBlock>emptyList() : ImmutableList.copyOf(catchBlocks);
  }

  @Nonnull
  public static ExceptionHandlingContext create(@Nonnull List<JCatchBasicBlock> catchBlocks) {
    return catchBlocks.isEmpty() ? EMPTY
        : new ExceptionHandlingContext(Jack.getUnmodifiableCollections()
            .getUnmodifiableList(Lists.newArrayList(catchBlocks)));
  }

  @Nonnull
  public List<JCatchBasicBlock> getCatchBlocks() {
    return catchBlocks;
  }

  /** Creates a pool of unique exception handler contexts */
  static class Pool {
    @Nonnull
    private final Map<List<JCatchBasicBlock>, ExceptionHandlingContext> pool = new HashMap<>();

    @Nonnull
    ExceptionHandlingContext getOrCreate(@Nonnull List<JCatchBasicBlock> catchBlocks) {
      ExceptionHandlingContext ehc = pool.get(catchBlocks);
      if (ehc == null) {
        ehc = ExceptionHandlingContext.create(catchBlocks);
        pool.put(ehc.getCatchBlocks(), ehc);
      }
      return ehc;
    }
  }
}
