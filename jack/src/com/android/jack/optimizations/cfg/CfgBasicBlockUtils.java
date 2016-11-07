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

import com.android.jack.ir.ast.cfg.BasicBlockLiveProcessor;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JRegularBasicBlock;
import com.android.jack.ir.ast.cfg.JSimpleBasicBlock;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.LocalVarCreator;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import javax.annotation.Nonnull;

/** Implements series of basic block related utilities. */
@Transform(modify = JControlFlowGraph.class)
@Use(LocalVarCreator.class)
public final class CfgBasicBlockUtils {
  @Nonnull
  private final JControlFlowGraph cfg;

  public CfgBasicBlockUtils(@Nonnull JControlFlowGraph cfg) {
    this.cfg = cfg;
  }

  /** Mergers all simple blocks into their successors when possible */
  public void mergeSimpleBlocks(final boolean preserveSourceInfo) {
    new BasicBlockLiveProcessor(cfg, false) {
      @Override
      public boolean visit(@Nonnull JSimpleBasicBlock simple) {
        JBasicBlock primary = simple.getPrimarySuccessor();
        if (primary.getPredecessorCount() == 1) {
          if (!preserveSourceInfo
              || simple.getLastElement().getSourceInfo() == SourceInfo.UNKNOWN) {
            simple.mergeIntoSuccessor();
          }
        }
        return false;
      }
    }.process();
  }

  /** Maximally split all basic blocks */
  public void maximallySplitAllBasicBlocks() {
    new BasicBlockLiveProcessor(cfg, false) {
      @Override
      public boolean visit(@Nonnull JSimpleBasicBlock simple) {
        while (simple.getElementCount() > 2) {
          simple.split(1);
        }
        return false;
      }

      @Override
      public boolean visit(@Nonnull JRegularBasicBlock regular) {
        while (regular.getElementCount() > 1) {
          regular.split(1);
        }
        return false;
      }
    }.process();
  }
}
