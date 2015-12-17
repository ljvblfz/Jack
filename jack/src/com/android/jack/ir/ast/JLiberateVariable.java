/*
 * Copyright 2015 Google Inc.
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
import com.android.jack.transformations.lambda.ForceClosureMarker;
import com.android.jack.transformations.lambda.NeedsLambdaMarker;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Liberate captured variable into lambda body.
 */
@Description("Java instance of expression")
public class JLiberateVariable extends JExpression {

  @Nonnull
  private final JParameterRef closure;

  @Nonnull
  private final JParameterRef capturedVariable;

  public JLiberateVariable(@Nonnull SourceInfo info, @Nonnull JParameterRef closure,
      @Nonnull JParameterRef capturedVariable) {
    super(info);
    assert closure.getType() instanceof JDefinedInterface;
    assert ((JDefinedInterface) closure.getType()).getMarker(NeedsLambdaMarker.class) != null
        || closure.getTarget().getMarker(ForceClosureMarker.class) != null;
    this.closure = closure;
    this.capturedVariable = capturedVariable;
  }

  @Override
  public JType getType() {
    return capturedVariable.getType();
  }

  @Override
  public boolean canThrow() {
    return true;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(closure);
      visitor.accept(capturedVariable);
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

  public JParameterRef getClosure() {
    return closure;
  }

  public JParameterRef getCapturedVariable() {
    return capturedVariable;
  }
}
