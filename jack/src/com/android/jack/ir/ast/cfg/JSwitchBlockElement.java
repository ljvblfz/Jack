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
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/** Represents switch basic block element */
public final class JSwitchBlockElement extends JBasicBlockElement {
  @Nonnull
  private JExpression expr;

  JSwitchBlockElement(@Nonnull SourceInfo info,
      @Nonnull ExceptionHandlingContext ehc, @Nonnull JExpression expr) {
    super(info, ehc);
    assert !expr.canThrow();
    assert !JPrimitiveType.JPrimitiveTypeEnum.VOID.getType().isSameType(expr.getType());
    assert expr.getType() instanceof JPrimitiveType;
    this.expr = expr;
  }

  @Nonnull
  public JExpression getExpression() {
    return expr;
  }

  @Nonnull
  @Override
  public JSwitchBasicBlock getBasicBlock() {
    JBasicBlock block = super.getBasicBlock();
    assert block instanceof JSwitchBasicBlock;
    return (JSwitchBasicBlock) block;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(expr);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    expr.traverse(schedule);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest request) throws Exception {
    visitor.visit(this, request);
  }

  @Override
  public boolean isTerminal() {
    return true;
  }

  @Override
  public void checkValidity() {
    super.checkValidity();

    if (!(super.getBasicBlock() instanceof JSwitchBasicBlock)) {
      throw new JNodeInternalError(this, "The parent node must be JSwitchBasicBlock");
    }
    if (this != getBasicBlock().getLastElement()) {
      throw new JNodeInternalError(this, "Must be the last element of the basic block");
    }
  }

  @Override
  protected void replaceImpl(
      @Nonnull JNode existingNode, @Nonnull JNode newNode) throws UnsupportedOperationException {
    if (expr == existingNode) {
      expr = (JExpression) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }
}
