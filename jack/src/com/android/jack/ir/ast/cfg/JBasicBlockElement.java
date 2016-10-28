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

import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/** Represents an abstract CFG basic block element. */
public abstract class JBasicBlockElement extends JNode {
  @Nonnull
  private ExceptionHandlingContext ehc;

  JBasicBlockElement(@Nonnull SourceInfo info, @Nonnull ExceptionHandlingContext ehc) {
    super(info);
    this.ehc = ehc;
  }

  @Nonnull
  public JBasicBlock getBasicBlock() {
    JNode parent = getParent();
    assert parent instanceof JBasicBlock;
    return (JBasicBlock) parent;
  }

  /** Is this a terminal basic block element */
  public abstract boolean isTerminal();

  /** Get exception handling context */
  @Nonnull
  public ExceptionHandlingContext getEHContext() {
    return ehc;
  }

  /** Reset exception handling context, update basic block */
  public void resetEHContext(@Nonnull ExceptionHandlingContext ehc) {
    this.ehc = ehc;
  }

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
    CfgExpressionValidator.validate(this);

    if (this.isTerminal()) {
      if (this != getBasicBlock().getLastElement()) {
        throw new JNodeInternalError(this,
            "Terminal block element must be the last element of the block");
      }
    } else {
      if (this.getBasicBlock() instanceof JThrowingBasicBlock
          && this == getBasicBlock().getLastElement()) {
        throw new JNodeInternalError(this,
            "Non-terminal block element must NOT be the last element of the throwing block");
      }
    }
  }
}
