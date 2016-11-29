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


import com.android.jack.ir.formatter.UserFriendlyFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Abstraction representing Java method call expressions.
 */
@Description("Abstraction of Java method call expressions")
public abstract class JAbstractMethodCall extends JExpression {

  @CheckForNull
  private JExpression instance;
  @Nonnull
  private JClassOrInterface receiverType;
  @Nonnull
  private final List<JExpression> args = new ArrayList<JExpression>();
  @Nonnull
  private JMethodId methodId;


  protected JAbstractMethodCall(@Nonnull SourceInfo info, @CheckForNull JExpression instance,
      @Nonnull JClassOrInterface receiverType, @Nonnull JMethodId methodId) {
    super(info);
    assert receiverType != null;
    assert methodId != null;
    this.instance = instance;
    this.receiverType = receiverType;
    this.methodId = methodId;
  }

  /**
   * Adds an argument to this method.
   */
  public void addArg(@Nonnull JExpression toAdd) {
    args.add(toAdd);
  }

  /**
   * Adds arguments to this method.
   */
  public void addArgs(@Nonnull List<JExpression> toAdd) {
    args.addAll(toAdd);
  }

  /**
   * Adds an argument to this method.
   */
  public void addArgs(@Nonnull JExpression... toAdd) {
    args.addAll(Arrays.asList(toAdd));
  }

  /**
   * Returns the call arguments.
   */
  @Nonnull
  public List<JExpression> getArgs() {
    return args;
  }

  @CheckForNull
  public JExpression getInstance() {
    return instance;
  }

  @Nonnull
  public JClassOrInterface getReceiverType() {
    return receiverType;
  }

  protected void setReceiverType(@Nonnull JClassOrInterface receiverType) {
    this.receiverType = receiverType;
  }

  @Override
  public boolean canThrow() {
    return true;
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!transform(args, existingNode, (JExpression) newNode, transformation)) {
      super.transform(existingNode, newNode, transformation);
    }
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException {
    assert newNode != null;

    if (instance == existingNode) {
      instance = (JExpression) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }

  @Override
  protected void removeImpl(@Nonnull JNode existingNode) throws UnsupportedOperationException {
    if (instance == existingNode) {
      instance = null;
    } else {
      super.removeImpl(existingNode);
    }
  }

  @Nonnull
  public JMethodId getMethodIdNotWide() {
    return methodId;
  }

  @Nonnull
  public JMethodIdWide getMethodId() {
    return methodId.getMethodIdWide();
  }

  @Nonnull
  @Override
  public JType getType() {
    return methodId.getType();
  }

  public void resolveMethodId(@Nonnull JMethodId methodId) {
    this.methodId = methodId;
  }

  protected void visitChildren(JVisitor visitor) {
    if (instance != null) {
      visitor.accept(instance);
    }
    visitor.accept(args);
  }

  protected void visitChildren(@Nonnull ScheduleInstance<? super Component> schedule)
      throws Exception {
    if (instance != null) {
      instance.traverse(schedule);
    }
    for (JExpression arg : args) {
      arg.traverse(schedule);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Nonnull
  public String getMethodName() {
    return methodId.getMethodIdWide().getName();
  }

  public abstract boolean isCallToPolymorphicMethod();

  static boolean isCallToPolymorphicMethod(@Nonnull JClassOrInterface receiverType,
      @Nonnull JMethodId methodId) {
    UserFriendlyFormatter formatter = UserFriendlyFormatter.getFormatter();
    String calledMethodName = methodId.getMethodIdWide().getName();
    List<JType> paramTypes = methodId.getMethodIdWide().getParamTypes();

    return receiverType != null
        && formatter.getName(methodId.getType()).equals("java.lang.Object")
        && formatter.getName(receiverType).equals("java.lang.invoke.MethodHandle")
        && (calledMethodName.equals("invoke") || calledMethodName.equals("invokeExact"))
        && paramTypes.size() == 1
        && formatter.getName(paramTypes.get(0)).equals("java.lang.Object[]");
  }
}
