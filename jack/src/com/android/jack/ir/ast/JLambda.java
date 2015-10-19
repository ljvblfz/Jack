/*
 * Copyright (C) 2015 The Android Open Source Project
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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * {@link JExpression} representing lambda expressions.
 */
@Description("Lambda expression")
public class JLambda extends JExpression {

  private boolean captureInstance;

  @Nonnull
  private final List<JVariableRef> capturedVariablesRef = new ArrayList<JVariableRef>(0);

  @Nonnull
  private final JMethod method;

  @Nonnull
  private final JDefinedInterface type;

  @Nonnull
  private final JMethodBody body;

  public JLambda(@Nonnull SourceInfo info, @Nonnull JMethod method, @Nonnull JDefinedInterface type,
      boolean captureInstance) {
    super(info);
    assert method != null;
    assert type != null;
    assert type.isSingleAbstractMethodType();
    this.type = type;
    this.method = method;
    this.captureInstance = captureInstance;
    JMethodBody localBody = (JMethodBody) method.getBody();
    assert localBody != null;
    body = localBody;
  }

  public void addCapturedVariable(@Nonnull JVariableRef capturedVariableRef) {
    capturedVariablesRef.add(capturedVariableRef);
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(capturedVariablesRef);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    body.traverse(schedule);
  }

  @Override
  public void visit(JVisitor visitor, TransformRequest transformRequest) throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  @Nonnull
  public JDefinedInterface getType() {
    return type;
  }

  @Nonnull
  public JBlock getBody() {
    return body.getBlock();
  }

  @Nonnull
  public List<JParameter> getParameters() {
    return method.getParams();
  }

  @Nonnull
  public JMethod getMethod() {
    return method;
  }

  @Nonnull
  public List<JVariableRef> getCapturedVariables() {
    return capturedVariablesRef;
  }

  public void setCaptureInstance(boolean captureInstance) {
    this.captureInstance = captureInstance;
  }

  public boolean needToCaptureInstance() {
    return captureInstance;
  }

  @Override
  public boolean canThrow() {
    return true;
  }
}
