/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.ast;

import com.android.jack.ir.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;

import javax.annotation.Nonnull;

/**
 * Java prefix or postfix operation expression.
 */
@Description("Java prefix or postfix operation expression")
public abstract class JUnaryOperation extends JExpression {

  private static final long serialVersionUID = 1L;
  private JExpression arg;

  public JUnaryOperation(@Nonnull SourceInfo info, @Nonnull JExpression arg) {
    super(info);
    this.arg = arg;
  }

  public JExpression getArg() {
    return arg;
  }

  @Nonnull
  public abstract JUnaryOperator getOp();

  @Override
  @Nonnull
  public JType getType() {
    return JPrimitiveType.getUnaryPromotion(arg.getType());
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    visitor.accept(arg);
  }

  @Override
  public void traverse(
      @Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    arg.traverse(schedule);
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException {
    assert newNode != null;

    if (arg == existingNode) {
      arg = (JExpression) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }
}
