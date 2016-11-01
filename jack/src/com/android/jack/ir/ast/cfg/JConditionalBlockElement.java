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

import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/** Represents conditional basic block element */
public final class JConditionalBlockElement extends JBasicBlockElement {
  @Nonnull
  private JExpression cond;

  public JConditionalBlockElement(@Nonnull SourceInfo info,
      @Nonnull ExceptionHandlingContext ehc, @Nonnull JExpression cond) {
    super(info, ehc);
    assert !cond.canThrow();
    assert JPrimitiveType.JPrimitiveTypeEnum.BOOLEAN.getType().isSameType(cond.getType());
    this.cond = cond;
    this.cond.updateParents(this);
  }

  @Nonnull
  public JExpression getCondition() {
    return cond;
  }

  @Override
  @Nonnull
  public JConditionalBasicBlock getBasicBlock() {
    JBasicBlock block = super.getBasicBlock();
    assert block instanceof JConditionalBasicBlock;
    return (JConditionalBasicBlock) block;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(cond);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    cond.traverse(schedule);
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
  protected void replaceImpl(
      @Nonnull JNode existingNode, @Nonnull JNode newNode) throws UnsupportedOperationException {
    if (cond == existingNode) {
      cond = (JExpression) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }
}
