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


import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;
import com.android.sched.util.findbugs.SuppressFBWarnings;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Java method call expression.
 */
@Description("Java method call expression")
public class JPolymorphicMethodCall extends JAbstractMethodCall {

  @Nonnull
  private final JType callSiteReturnType;

  @Nonnull
  private final List<JType> callSiteParameterTypes;

  @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
  public JPolymorphicMethodCall(@Nonnull SourceInfo info, @Nonnull JExpression instance,
      @Nonnull JClassOrInterface receiverType, @Nonnull JMethodId methodId,
      @Nonnull JType callSiteReturnType, @Nonnull List<JType> callSiteParameterTypes) {
    super(info, instance, receiverType, methodId.getMethodIdWide(), methodId.getType());

    assert instance != null;
    assert isCallToPolymorphicMethod(receiverType, methodId.getMethodIdWide(), methodId.getType());

    this.callSiteReturnType = callSiteReturnType;
    this.callSiteParameterTypes = callSiteParameterTypes;
  }

  public JType getReturnTypeOfPolymorphicMethod() {
    return super.getType();
  }

  @Nonnull
  @Override
  public JType getType()  {
    return callSiteReturnType;
  }

  @Nonnull
  public List<JType> getCallSiteParameterTypes() {
    return callSiteParameterTypes;
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

  @Override
  public void checkValidity() {
    JExpression instance = getInstance();
    if (instance == null) {
      throw new JNodeInternalError(this,
          "Call to method with polymorphic signature must not be static");
    }
    if (!getReceiverType().isSameType(instance.getType())) {
      throw new JNodeInternalError(this, "Receiver type mismatch with instance type");
    }
    if (callSiteParameterTypes.size() != getArgs().size()) {
      throw new JNodeInternalError(this,
          "Number of method call arguments does not match the number of call site parameters");
    }
  }
}
