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

import com.android.jack.Jack;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Represents blocks which potentially may trigger exceptions. */
public abstract class JThrowingBasicBlock extends JRegularBasicBlock {
  /** Successor for unhandled exception */
  @Nonnull
  private JBasicBlock unhandledBlock;
  @Nonnull
  private List<JBasicBlock> catchBlocks = new ArrayList<>();

  JThrowingBasicBlock(@CheckForNull JBasicBlock primary, @Nonnull JBasicBlock unhandledBlock) {
    super(primary);
    this.unhandledBlock = unhandledBlock;
    this.unhandledBlock.addPredecessor(this);
  }

  @Override
  void collectSuccessors(@Nonnull ArrayList<JBasicBlock> successors) {
    super.collectSuccessors(successors);
    successors.add(unhandledBlock);
    successors.addAll(catchBlocks);
  }

  /** Add a new exception handler successor */
  public void addHandler(@Nonnull JBasicBlock handler) {
    catchBlocks.add(handler);
    handler.addPredecessor(this);
  }

  @Nonnull
  public List<JBasicBlock> getCatchBlocks() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(catchBlocks);
  }

  @Nonnull
  public JBasicBlock getUnhandledBlock() {
    return unhandledBlock;
  }

  @Override
  public void replaceAllSuccessors(@Nonnull JBasicBlock what, @Nonnull JBasicBlock with) {
    super.replaceAllSuccessors(what, with);

    if (this.unhandledBlock == what) {
      this.unhandledBlock = resetSuccessor(what, with);
    }
    for (int i = 0; i < catchBlocks.size(); i++) {
      if (catchBlocks.get(i) == what) {
        catchBlocks.set(i, resetSuccessor(what, with));
      }
    }
  }
}
