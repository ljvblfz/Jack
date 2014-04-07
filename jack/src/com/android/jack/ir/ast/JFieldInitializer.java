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
 * Statement setting the initial value of fields.
 */
@Description("Statement setting the initial value of fields")
public class JFieldInitializer extends JStatement {

  @Nonnull
  private JExpression initializer;
  @Nonnull
  private JFieldRef fieldRef;

  public JFieldInitializer(@Nonnull SourceInfo info, @Nonnull JFieldRef fieldRef,
      @Nonnull JExpression intializer) {
    super(info);
    this.fieldRef = fieldRef;
    this.initializer = intializer;
  }

  @Nonnull
  public JExpression getInitializer() {
    return initializer;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(getFieldRef());
      visitor.accept(initializer);
    }
    visitor.endVisit(this);
  }

  /**
   * @return a ref to the initialized field.
   */
  public JFieldRef getFieldRef() {
    return fieldRef;
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    getFieldRef().traverse(schedule);
    initializer.traverse(schedule);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode) {
    if (existingNode == initializer) {
      initializer = (JExpression) newNode;
    } else if (fieldRef == existingNode) {
      fieldRef = (JFieldRef) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }
}
