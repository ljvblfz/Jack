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
import com.android.sched.item.Tag;
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

  /**
   * A {@link Tag} meaning that default bridges were added into {@link JLambda} when they are
   * needed.
   */
  @Description("Default bridges were added into JLambda when they are needed.")
  public static final class DefaultBridgeAddedInLambda implements Tag {
  }

  @Nonnull
  private JMethodId mthIdWithErasure;

  @Nonnull
  private final JMethodId mthIdWithoutErasure;

  @Nonnull
  private final JInterface type;

  @Nonnull
  private final List<JInterface> interfaceBounds;

  @Nonnull
  private final List<JExpression> capturedVariables = new ArrayList<JExpression>(0);

  @Nonnull
  private final JMethodIdRef methodIdRef;

  @Nonnull
  private final List<JMethodId> bridges = new ArrayList<JMethodId>();

  public JLambda(@Nonnull SourceInfo info, @Nonnull JMethodId mthIdWithErasure,
      @Nonnull JMethodIdRef methodRef, @Nonnull JInterface type,
      @Nonnull List<JInterface> interfaceBounds, @Nonnull JMethodId mthIdWithoutErasure) {
    super(info);
    assert methodRef != null;
    assert type != null;
    this.mthIdWithErasure = mthIdWithErasure;
    this.type = type;
    this.methodIdRef = methodRef;
    this.interfaceBounds = interfaceBounds;
    this.mthIdWithoutErasure = mthIdWithoutErasure;
  }

  @Nonnull
  public JMethodId getMethodIdWithErasure() {
    return mthIdWithErasure;
  }

  @Nonnull
  public JMethodId getMethodIdWithoutErasure() {
    return mthIdWithoutErasure;
  }

  @Nonnull
  public List<JMethodId> getBridgeMethodIds() {
    return bridges;
  }

  public void addBridgeMethodId(@Nonnull JMethodId bridgeMethodId) {
    this.bridges.add(bridgeMethodId);
  }

  public void addBridgeMethodIds(@Nonnull List<JMethodId> bridgeMethodIds) {
    this.bridges.addAll(bridgeMethodIds);
  }

  public void addCapturedVariable(@Nonnull JExpression capturedVariable) {
    capturedVariables.add(capturedVariable);
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
  @Nonnull
  public JInterface getType() {
    return type;
  }

  @Nonnull
  public JMethodIdRef getMethodIdRef() {
    return methodIdRef;
  }

  @Nonnull
  public List<JExpression> getCapturedVariables() {
    return capturedVariables;
  }

  @Override
  public boolean canThrow() {
    return true;
  }

  @Nonnull
  public List<JInterface> getInterfaceBounds() {
    return interfaceBounds;
  }

  public void resolveMethodId(@Nonnull JMethodId methodId) {
    this.mthIdWithErasure = methodId;
  }
}
