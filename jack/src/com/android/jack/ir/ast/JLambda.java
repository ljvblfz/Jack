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

  @Nonnull
  private final JMethodIdWithReturnType mthIdToImplement;

  @Nonnull
  private final JInterface type;

  @Nonnull
  private final List<JInterface> interfaceBounds;

  private boolean captureInstance;

  @Nonnull
  private final List<JVariableRef> capturedVariablesRef = new ArrayList<JVariableRef>(0);

  // TODO(jack-team): JMethod must be replace by a JCallable
  @Nonnull
  private final JMethod method;

  @Nonnull
  private final List<JMethodIdWithReturnType> bridges = new ArrayList<JMethodIdWithReturnType>();

  public JLambda(@Nonnull SourceInfo info, @Nonnull JMethodIdWithReturnType mthToImplement,
      @Nonnull JMethod method, @Nonnull JInterface type, boolean captureInstance,
      @Nonnull List<JInterface> interfaceBounds) {
    super(info);
    assert method != null;
    assert type != null;
    this.mthIdToImplement = mthToImplement;
    this.type = type;
    this.method = method;
    this.captureInstance = captureInstance;
    this.interfaceBounds = interfaceBounds;
  }

  @Nonnull
  public JMethodIdWithReturnType getMethodIdToImplement() {
    return mthIdToImplement;
  }

  @Nonnull
  public List<JMethodIdWithReturnType> getBridgeMethodIds() {
    return bridges;
  }

  public void addBridgeMethodId(@Nonnull JMethodIdWithReturnType bridgeMethodId) {
    this.bridges.add(bridgeMethodId);
  }

  public void addBridgeMethodIds(@Nonnull List<JMethodIdWithReturnType> bridgeMethodIds) {
    this.bridges.addAll(bridgeMethodIds);
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
    JMethodBody body = (JMethodBody) method.getBody();
    assert body != null;
    body.traverse(schedule);
  }

  @Override
  public void visit(JVisitor visitor, TransformRequest transformRequest) throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  @Nonnull
  public JInterface getType() {
    return type;
  }

  @Nonnull
  public JMethodBody getBody() {
    JMethodBody body = (JMethodBody) method.getBody();
    assert body != null;
    return body;
  }

  @Nonnull
  public List<JParameter> getParameters() {
    return method.getParams();
  }

  @Deprecated
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

  @Nonnull
  public List<JInterface> getInterfaceBounds() {
    return interfaceBounds;
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException {
    int nodeIdx = capturedVariablesRef.indexOf(existingNode);
    if (nodeIdx != -1) {
      capturedVariablesRef.set(nodeIdx, (JLocalRef) newNode);
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }
}
