/*
 * Copyright 2013 Google Inc.
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

import com.android.jack.ir.SourceOrigin;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Fake {@code JLocal} used as target when local variable resolution is not done.
 */
@Description("Fake JLocal used as target when local variable resolution is not done")
public class JLocalUnresolved extends JLocal {

  @Nonnull
  public static final JLocalUnresolved INSTANCE = new JLocalUnresolved();

  private JLocalUnresolved() {
    super(SourceOrigin.UNKNOWN, "-unresolved-", JPrimitiveTypeEnum.DOUBLE.getType(), 0, null);
  }

  @Override
  public JMethod getEnclosingMethod() {
    throw new AssertionError();
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    throw new AssertionError();
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    throw new AssertionError();
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode) {
    throw new AssertionError();
  }

  @Override
  protected void removeImpl(@Nonnull JNode existingNode) throws UnsupportedOperationException {
    throw new AssertionError();
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest) {
    throw new AssertionError();
  }
}
