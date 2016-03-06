/*
 * Copyright 2016 Google Inc.
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
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Reference to a method described by its {@link JMethodId} and its enclosing type.
 */
@Description("Reference to a method described by its JMethodId and its enclosing type")
public class JMethodIdRef extends JNode {

  @Nonnull
  private final JDefinedClassOrInterface enclosingType;

  @Nonnull
  private final JMethodId methodId;

  public JMethodIdRef(@Nonnull SourceInfo info, @Nonnull JDefinedClassOrInterface enclosingType,
      @Nonnull JMethodId methodId) {
    super(info);
    this.enclosingType = enclosingType;
    this.methodId = methodId;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
  }

  @Override
  public void visit(JVisitor visitor, TransformRequest transformRequest) throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  public void checkValidity() {
  }

  @Nonnull
  public JDefinedClassOrInterface getEnclosingType() {
    return enclosingType;
  }

  @Nonnull
  public JMethodId getMethodId() {
    return methodId;
  }
}
