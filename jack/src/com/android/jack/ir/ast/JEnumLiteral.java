/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.ir.ast;

import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * {@link JLiteral} representing a reference to an {@code Enum} field.
 */
@Description("JLiteral representing a reference to an Enum field.")
public class JEnumLiteral extends JValueLiteral {

  @Nonnull
  private final JFieldId value;

  public JEnumLiteral(@Nonnull SourceInfo sourceInfo, @Nonnull JFieldId value) {
    super(sourceInfo);
    this.value = value;
    assert value.getType() instanceof JEnum;
  }

  /**
   * The type of this expression is the Enum declaring the value.
   *
   * See JLS-7 8.9.2.
   */
  @Nonnull
  @Override
  public JEnum getType() {
    return (JEnum) value.getType();
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

  @Nonnull
  public JFieldId getFieldId() {
    return value;
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JExpression
        || parent instanceof JNameValuePair
        || parent instanceof JAnnotationMethod
        || parent instanceof JCaseStatement
        || parent instanceof JSwitchStatement
        || parent instanceof JReturnStatement
        || parent instanceof JFieldInitializer
        || parent instanceof JSynchronizedBlock)) {
      super.checkValidity();
    }
  }
}
