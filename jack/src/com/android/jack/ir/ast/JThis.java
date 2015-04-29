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

import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Java this variable definition.
 */
@Description("Java this variable definition")
public class JThis extends JVariable implements HasEnclosingMethod {

  @Nonnull
  private final JMethod enclosingMethod;

  public JThis(@Nonnull JMethod enclosingMethod) {
    super(SourceInfo.UNKNOWN, "this", enclosingMethod.getEnclosingType(), JModifier.DEFAULT);
    assert enclosingMethod.getEnclosingType() instanceof JDefinedClass;
    this.enclosingMethod = enclosingMethod;
  }

  @Override
  @Nonnull
  public JMethod getEnclosingMethod() {
    return enclosingMethod;
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
    for (JAnnotation annotation : annotations) {
      annotation.traverse(schedule);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JMethod)) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
  }
}