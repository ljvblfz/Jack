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

import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Fake {@code JLabeledStatement} used as target when label resolution is not done.
 */
@Description("Fake JLabeledStatement used as target when label resolution is not done")
public class JLabeledStatementUnresolved extends JLabeledStatement {

  @Nonnull
  public static final JLabeledStatementUnresolved INSTANCE = new JLabeledStatementUnresolved();


  private JLabeledStatementUnresolved() {
    super(SourceInfo.UNKNOWN, null, null);
  }

  @Override
  public JStatement getBody() {
    throw new AssertionError();
  }

  @Override
  public JLabel getLabel() {
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
