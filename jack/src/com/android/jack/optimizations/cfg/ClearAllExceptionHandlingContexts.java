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

package com.android.jack.optimizations.cfg;

import com.android.jack.ir.ast.cfg.ExceptionHandlingContext;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JBasicBlockElement;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/** Clear all exception handling contexts */
@Description("Clear all exception handling contexts")
@Transform(modify = JControlFlowGraph.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class ClearAllExceptionHandlingContexts
    implements RunnableSchedulable<JControlFlowGraph> {

  @Override
  public void run(@Nonnull final JControlFlowGraph cfg) {
    for (JBasicBlock block : cfg.getInternalBlocksUnordered()) {
      for (JBasicBlockElement element : block.getElements(true)) {
        element.resetEHContext(ExceptionHandlingContext.EMPTY);
      }
    }
  }
}
