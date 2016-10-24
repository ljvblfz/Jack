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

package com.android.jack.scheduling.adapter;

import com.google.common.collect.Lists;

import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.sched.item.Description;
import com.android.sched.schedulable.AdapterSchedulable;

import java.util.Iterator;
import javax.annotation.Nonnull;

/**
 * Adapts a process on {@code JControlFlowGraph} onto one or several processes on each
 * {@code JBasicBlock} of this type control flow graph in forward depth first order.
 */
@Description("Adapts process on JControlFlowGraph to one or several "
    + "processes on each of its basic blocks in forward depth first order")
public class JBasicBlockForwardDepthFirstAdapter
    implements AdapterSchedulable<JControlFlowGraph, JBasicBlock> {

  /**
   * Returns every {@code JBasicBlock} of the given {@code JControlFlowGraph}.
   */
  @Override
  @Nonnull
  public Iterator<JBasicBlock> adapt(@Nonnull JControlFlowGraph cfg) {
    return Lists.newArrayList(cfg.getBlocksDepthFirst(true)).iterator();
  }
}
