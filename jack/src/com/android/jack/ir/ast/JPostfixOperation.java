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

import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;

import javax.annotation.Nonnull;

/**
 * Java postfix operation expression.
 */
@Description("Java postfix operation expression")
public abstract class JPostfixOperation extends JUnaryOperation {

  public JPostfixOperation(@Nonnull SourceInfo info, @Nonnull JExpression arg) {
    super(info, arg);
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      super.traverse(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    super.traverse(schedule);
  }

  @Nonnull
  public static JPostfixOperation create(@Nonnull SourceInfo info,
      @Nonnull JUnaryOperator op, @Nonnull JExpression arg) {
    JPostfixOperation result = null;
    switch (op) {
      case DEC:
        result = new JPostfixDecOperation(info, arg);
        break;
      case INC:
        result = new JPostfixIncOperation(info, arg);
        break;
      case BIT_NOT:
      case NEG:
      case NOT:
        break;
    }
    assert result != null : "Unknown operator";
    assert result.getOp() == op;
    return result;
  }
}
