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
 * Method literal.
 */
@Description("Method literal")
public class JMethodLiteral extends JLiteral {

  @Nonnull
  private JMethod method;

  public JMethodLiteral(@Nonnull JMethod method, @Nonnull SourceInfo sourceInfo) {
    super(sourceInfo);
    this.method = method;
  }

  @Nonnull
  public JMethod getMethod() {
    return method;
  }


  public void setMethod(JMethod method) {
    this.method = method;
  }

  @Override
  @Nonnull
  public JType getType() {
    return method.getType();
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
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest tranformRequest)
      throws Exception {
    visitor.visit(this, tranformRequest);
  }
}
