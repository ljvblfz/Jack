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

/**
 * Represents blocks ended by statement that can potentially trigger exceptions.
 */
public abstract class JThrowingBasicBlock extends JRegularBasicBlock {
  @Nonnull
  private JBasicBlock unhandled;
  @Nonnull
  private List<JBasicBlock> handlers = new ArrayList<>();

  JThrowingBasicBlock(@CheckForNull JBasicBlock primary, @Nonnull JBasicBlock unhandled) {
    super(primary);
    this.unhandled = unhandled;
    this.unhandled.addPredecessor(this);
  }

  @Override
  void collectSuccessors(@Nonnull ArrayList<JBasicBlock> successors) {
    super.collectSuccessors(successors);
    successors.add(unhandled);
    successors.addAll(handlers);
  }

  /** Add a new exception handler successor */
  public void addHandler(@Nonnull JBasicBlock handler) {
    handlers.add(handler);
    handler.addPredecessor(this);
  }

  @Nonnull
  public List<JBasicBlock> getHandlers() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(handlers);
  }

  @Override
  public void replaceAllSuccessors(@Nonnull JBasicBlock what, @Nonnull JBasicBlock with) {
    super.replaceAllSuccessors(what, with);

    if (this.unhandled == what) {
      this.unhandled = resetSuccessor(what, with);
    }
    for (int i = 0; i < handlers.size(); i++) {
      if (handlers.get(i) == what) {
        assert with instanceof JCatchBasicBlock;
        handlers.set(i, resetSuccessor(what, with));
      }
    }
  }
}
