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

package com.android.jack.ir.ast.cfg.mutations;

import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JPlaceholderBasicBlock;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Represents a CFG fragment with one entry block and zero, one or many exit
 * blocks. All exit blocks must converge on one single JPlaceholderBasicBlock
 * block representing one single 'sink' point.
 *
 * It is not enforced that the basic blocks of the fragment represent an isolated
 * fragment of the CFG, thus some of the basic blocks may reference outside blocks.
 *
 * This may be important in some cases, for example when the fragment includes
 * JThrowingBasicBlock blocks with already properly assigned unhandled exception
 * handler block.
 *
 * In case there a no edges coming out of the block, the exit block may be `null`.
 */
public class CfgFragment {
  @Nonnull
  public final JBasicBlock entry;
  @CheckForNull
  public final JPlaceholderBasicBlock exit;

  public CfgFragment(@Nonnull JBasicBlock entry, @CheckForNull JPlaceholderBasicBlock exit) {
    this.entry = entry;
    this.exit = exit;
  }

  /**
   * Replaces all the successors of `source` pointing to `target` with the entry
   * block of the fragment, and replaces the exit block of the fragment with `target`.
   */
  public void insert(@Nonnull JBasicBlock source, @Nonnull JBasicBlock target) {
    // Replace all the successor-references pointing to `target` with
    // CFG fragment entry block.
    source.replaceAllSuccessors(target, this.entry);

    // If there is an exit block, replace it with the target
    if (this.exit != null) {
      this.exit.detach(target);
    }
  }
}
