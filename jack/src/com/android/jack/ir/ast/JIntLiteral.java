/*
 * Copyright 2008 Google Inc.
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

import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Java integer literal expression.
 */
@Description("Java integer literal expression")
public class JIntLiteral extends JNumberValueLiteral implements JIntegralConstant32,
    JNumberLiteral {

  private final int value;

  public JIntLiteral(SourceInfo sourceInfo, int value) {
    super(sourceInfo);
    this.value = value;
  }

  @Override
  public JType getType() {
    return JPrimitiveTypeEnum.INT.getType();
  }

  public int getValue() {
    return value;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  public int getIntValue() {
    return value;
  }

  @Override
  @Nonnull
  public Number getNumber() {
    return new Number(Integer.valueOf(value));
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JSwitchStatement || parent instanceof JCaseStatement)) {
      super.checkValidity();
    }
  }

  @Override
  public boolean isTypeValue() {
    return value == 0;
  }
}
