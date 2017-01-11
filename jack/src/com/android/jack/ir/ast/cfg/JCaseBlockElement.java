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
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Represents case branch value basic block element */
public final class JCaseBlockElement extends JBasicBlockElement {
  /** The value is null in case the block represents the default case of the switch */
  @CheckForNull
  private JLiteral literal;

  JCaseBlockElement(@Nonnull SourceInfo info,
      @Nonnull ExceptionHandlingContext ehc, @CheckForNull JLiteral literal) {
    super(info, ehc);
    this.literal = literal;
  }

  @CheckForNull
  public JLiteral getLiteral() {
    return literal;
  }

  @Override
  @Nonnull
  public JCaseBasicBlock getBasicBlock() {
    JBasicBlock block = super.getBasicBlock();
    assert block instanceof JCaseBasicBlock;
    return (JCaseBasicBlock) block;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      if (literal != null) {
        visitor.accept(literal);
      }
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    if (literal != null) {
      literal.traverse(schedule);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest request) throws Exception {
    visitor.visit(this, request);
  }

  @Override
  public boolean isTerminal() {
    // Must terminate JCaseBasicBlock
    return true;
  }

  @Override
  public void checkValidity() {
    super.checkValidity();

    if (!(super.getBasicBlock() instanceof JCaseBasicBlock)) {
      throw new JNodeInternalError(this, "The parent node must be JCaseBasicBlock");
    }
    if (this != getBasicBlock().getLastElement()) {
      throw new JNodeInternalError(this, "Must be the last element of the basic block");
    }
  }

  @Override
  protected void replaceImpl(
      @Nonnull JNode existingNode, @Nonnull JNode newNode) throws UnsupportedOperationException {
    if (literal == existingNode) {
      literal = (JLiteral) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }
}
