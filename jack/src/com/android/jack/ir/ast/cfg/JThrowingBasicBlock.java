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
import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.ast.JVisitor;

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
  private final List<JBasicBlock> catchBlocks = new ArrayList<>();

  JThrowingBasicBlock(@CheckForNull JBasicBlock primary, @Nonnull JBasicBlock unhandledBlock) {
    super(primary);
    this.unhandledBlock = unhandledBlock;
    this.unhandledBlock.addPredecessor(this);
  }

  @Nonnull
  @Override
  public List<JBasicBlock> getSuccessors() {
    ArrayList<JBasicBlock> successors = new ArrayList<>();
    if (hasPrimarySuccessor()) {
      successors.add(getPrimarySuccessor());
    }
    successors.add(unhandledBlock);
    successors.addAll(catchBlocks);
    return successors;
  }

  /** Resets exception catch blocks from the EH context of the last element */
  public void resetCatchBlocks() {
    // Remove old catch blocks
    for (JBasicBlock block : catchBlocks) {
      this.removeSuccessor(block);
    }
    catchBlocks.clear();

    // Add new catch blocks
    catchBlocks.addAll(getLastElement().getEHContext().getCatchBlocks());
    for (JBasicBlock block : catchBlocks) {
      block.addPredecessor(this);
    }
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

  @Override
  public void checkValidity() {
    super.checkValidity();

    new JVisitor() {
      @Override public boolean visit(@Nonnull JMethodCallBlockElement node) {
        return false;
      }

      @Override public boolean visit(@Nonnull JPolymorphicMethodCallBlockElement node) {
        return false;
      }

      @Override public boolean visit(@Nonnull JStoreBlockElement node) {
        return false;
      }

      @Override public boolean visit(@Nonnull JLockBlockElement node) {
        return false;
      }

      @Override public boolean visit(@Nonnull JUnlockBlockElement node) {
        return false;
      }

      @Override public boolean visit(@Nonnull JThrowBlockElement node) {
        return false;
      }

      @Override public boolean visit(@Nonnull JGotoBlockElement node) {
        throw new JNodeInternalError(JThrowingBasicBlock.this,
            "JThrowingBasicBlock must NOT end with JGotoBlockElement");
      }

      @Override public boolean visit(@Nonnull JSwitchBlockElement node) {
        throw new JNodeInternalError(JThrowingBasicBlock.this,
            "JThrowingBasicBlock must NOT end with JSwitchBlockElement");
      }

      @Override public boolean visit(@Nonnull JConditionalBlockElement node) {
        throw new JNodeInternalError(JThrowingBasicBlock.this,
            "JThrowingBasicBlock must NOT end with JConditionalBlockElement");
      }

      @Override public boolean visit(@Nonnull JCaseBlockElement node) {
        throw new JNodeInternalError(JThrowingBasicBlock.this,
            "JThrowingBasicBlock must NOT end with JCaseBlockElement");
      }

      @Override public boolean visit(@Nonnull JVariableAsgBlockElement node) {
        if (!node.getAssignment().getRhs().canThrow()) {
          throw new JNodeInternalError(JThrowingBasicBlock.this,
              "JThrowingBasicBlock must NOT end with JVariableAsgBlockElement "
                  + "which does not throw");
        }
        return false;
      }

      @Override public boolean visit(@Nonnull JReturnBlockElement node) {
        throw new JNodeInternalError(JThrowingBasicBlock.this,
            "JThrowingBasicBlock must NOT end with JReturnBlockElement");
      }

      @Override public boolean visit(@Nonnull JPhiBlockElement node) {
        throw new JNodeInternalError(JThrowingBasicBlock.this,
            "JThrowingBasicBlock must NOT end with JPhiBlockElement");
      }
    }.accept(getLastElement());

    if (!(getUnhandledBlock() instanceof JExitBasicBlock)) {
      throw new JNodeInternalError(this, "Unhandled exception block of "
          + "JThrowingBasicBlock must always point to the exit block");
    }
  }
}
