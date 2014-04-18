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
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * A new instance expression.
 */
@Description("A new instance expression")
public class JNewInstance extends JMethodCall {

  /**
   * Initialize a new instance operation equivalent to another one. The new
   * object has no arguments on initialization. This forces the caller to
   * potentially deal with cloning objects if needed.
   */
  public JNewInstance(JNewInstance other) {
    super(other, null);
  }

  public JNewInstance(@Nonnull SourceInfo info, @Nonnull JClassOrInterface receiverType,
      @Nonnull JMethodId ctor) {
    super(info, null, receiverType, ctor, receiverType, false /* isVirtualDispatch */);
  }

  @Nonnull
  @Override
  public JClass getType() {
    return (JClass) super.getType();
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitChildren(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    visitChildren(schedule);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
