/*
 * Copyright 2008 Google Inc.
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

import com.android.jack.ir.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java field reference expression.
 */
@Description("Java field reference expression")
public class JFieldRef extends JExpression {

  private static final long serialVersionUID = 1L;

  /**
   * The type in which the field is accessed.
   */
  @Nonnull
  private JClassOrInterface receiverType;

  @Nonnull
  private final JFieldId fieldId;

  /**
   * This can only be null if the referenced field is static.
   */
  @CheckForNull
  private JExpression instance;

  public JFieldRef(@Nonnull SourceInfo info, @CheckForNull JExpression instance,
      @Nonnull JFieldId fieldId, @Nonnull JClassOrInterface receiverType) {
    super(info);
    assert (instance != null || (fieldId.getKind() == FieldKind.STATIC));
    this.fieldId = fieldId;
    this.instance = instance;
    this.receiverType = receiverType;
  }

  @Override
  @Nonnull
  public JType getType() {
    return fieldId.getType();
  }

  public void setReceiverType(@Nonnull JClassOrInterface receiverType) {
    this.receiverType = receiverType;
  }

  @Nonnull
  public JClassOrInterface getReceiverType() {
    return receiverType;
  }

  @Nonnull
  public JFieldId getFieldId() {
    return fieldId;
  }

  @CheckForNull
  public JExpression getInstance() {
    return instance;
  }

  @Override
  public boolean canThrow() {
    return true;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      if (instance != null) {
        visitor.accept(instance);
      }
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    if (instance != null) {
      instance.traverse(schedule);
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

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
