/*
 * Copyright 2007 Google Inc.
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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java method call expression.
 */
@Description("Java method call expression")
public class JMethodCall extends JAbstractMethodCall {

  /**
   * Dispatch kind of method call.
   */
  public static enum DispatchKind {
    VIRTUAL,
    DIRECT;
  }

  @Nonnull
  private final DispatchKind dispatchKind;

  /**
   * Initialize a new method call equivalent to another one. A new instance must
   * be specified, and the new object has no arguments on initialization. This
   * forces the caller to potentially deal with cloning objects if needed.
   */
  public JMethodCall(@Nonnull JMethodCall other, @CheckForNull JExpression instance) {
    super(other.getSourceInfo(), instance, other.getReceiverType(), other.getMethodIdNotWide());
    dispatchKind = other.getDispatchKind();
    assert other.getReceiverType() == null || !JPolymorphicMethodCall
        .isCallToPolymorphicMethod(other.getReceiverType(), other.getMethodIdNotWide());
  }

  public JMethodCall(@Nonnull SourceInfo info, @CheckForNull JExpression instance,
      @Nonnull JClassOrInterface receiverType, @Nonnull JMethodId methodId,
      boolean isVirtualDispatch) {
    super(info, instance, receiverType, methodId);
    assert methodId != null;
    assert receiverType == null
        || !JPolymorphicMethodCall.isCallToPolymorphicMethod(receiverType, methodId);
    assert (!isVirtualDispatch) || getMethodId().getKind() == MethodKind.INSTANCE_VIRTUAL;
    this.dispatchKind = isVirtualDispatch ? DispatchKind.VIRTUAL : DispatchKind.DIRECT;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitChildren(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    visitChildren(schedule);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Nonnull
  public DispatchKind getDispatchKind() {
    return dispatchKind;
  }

  @Override
  public boolean isCallToPolymorphicMethod() {
    return isCallToPolymorphicMethod(getReceiverType(), getMethodIdNotWide());
  }
}
