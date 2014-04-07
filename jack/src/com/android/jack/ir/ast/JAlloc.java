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
 * An allocation expression.
 */
@Description("An allocation expression")
public class JAlloc extends JExpression {

  @Nonnull
  private JClass instanceType;

  public JAlloc(JAlloc other) {
    super(other.getSourceInfo());
    this.instanceType = other.instanceType;
  }

  public JAlloc(@Nonnull SourceInfo info, @Nonnull JClass instanceType) {
    super(info);
    this.instanceType = instanceType;
  }

  @Nonnull
  @Override
  public JClass getType() {
    return instanceType;
  }

  @Nonnull
  public JClass getInstanceType() {
    return instanceType;
  }

  public void setInstanceType(@Nonnull JClass instanceType) {
    this.instanceType = instanceType;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      // nothing to visit
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
  }

  @Override
  public boolean canThrow() {
    return true;
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
