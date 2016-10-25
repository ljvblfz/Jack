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

import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/** Represents an abstract CFG basic block element. */
public abstract class JBasicBlockElement extends JNode {
  JBasicBlockElement(@Nonnull SourceInfo info) {
    super(info);
  }

  @Nonnull
  public JBasicBlock getBasicBlock() {
    JNode parent = getParent();
    assert parent instanceof JBasicBlock;
    return (JBasicBlock) parent;
  }

  /** Is this a terminal basic block element */
  public abstract boolean isTerminal();

  @Override
  public abstract void traverse(@Nonnull JVisitor visitor);

  @Override
  public abstract void traverse(
      @Nonnull ScheduleInstance<? super Component> schedule) throws Exception;

  @Override
  public abstract void visit(@Nonnull JVisitor visitor,
      @Nonnull TransformRequest request) throws Exception;

  @Override
  public void checkValidity() {
  }
}
