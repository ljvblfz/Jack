/*
 * Copyright (C) 2007 The Android Open Source Project
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

import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * A (name, value) pair. These are used as the contents of an annotation.
 */
@Description("A (name, value) pair. These are used as the contents of an annotation.")
public final class JNameValuePair extends JNode {

  @Nonnull
  private JMethodId methodId;

  @Nonnull
  private JLiteral value;

  public JNameValuePair(@Nonnull SourceInfo sourceInfo,
      @Nonnull JMethodId methodId,
      @Nonnull JLiteral value) {
    super(sourceInfo);
    this.methodId = methodId;
    this.value = value;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  @Nonnull
  public String getName() {
    return methodId.getName();
  }

  @Nonnull
  public JMethodId getMethodId() {
    return methodId;
  }

  /**
   * Gets the value.
   *
   * @return the value
   */
  @Nonnull
  public JLiteral getValue() {
    return value;
  }

  /**
   * @param value the new value.
   */
  public void setValue(@Nonnull JLiteral value) {
    this.value = value;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(value);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    value.traverse(schedule);
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException {
    if (existingNode == value) {
      value = (JLiteral) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  public void resolveMethodId(@Nonnull JMethodId methodId) {
    this.methodId = methodId;
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JAnnotationLiteral)) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
  }
}
