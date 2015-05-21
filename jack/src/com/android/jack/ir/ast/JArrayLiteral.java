/*
 * Copyright (C) 2012 The Android Open Source Project
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

import com.android.jack.Jack;
import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * {@link JLiteral} representing an array of other {@code JLiteral}.
 */
@Description("JLiteral representing a fixed array of other JLiteral.")
public class JArrayLiteral extends JLiteral {

  @Nonnull
  private final List<JLiteral> values;

  public JArrayLiteral(@Nonnull SourceInfo sourceInfo,
      @Nonnull List<JLiteral> values) {
    super(sourceInfo);
    this.values = values;
  }

  @Override
  @Nonnull
  public JType getType() {
    throw new AssertionError();
  }

  @Nonnull
  public List<JLiteral> getValues() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(values);
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      for (JLiteral value : values) {
        visitor.accept(value);
      }
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    for (JLiteral value : values) {
      value.traverse(schedule);
    }
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!transform(values, existingNode, (JLiteral) newNode, transformation)) {
      super.transform(existingNode, newNode, transformation);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JNameValuePair
        || parent instanceof JAnnotationMethod)) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
  }
}
