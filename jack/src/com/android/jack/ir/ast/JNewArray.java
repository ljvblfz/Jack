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


import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * New array expression.
 */
@Description("New array expression")
public class JNewArray extends JExpression {

  @Nonnull
  private final List<JExpression> dims;
  @Nonnull
  private final List<JExpression> initializers;
  @Nonnull
  private final JArrayType type;

  @Nonnull
  public static JNewArray createWithInits(
      @Nonnull SourceInfo info, @Nonnull JArrayType type, @Nonnull List<JExpression> initializers) {
    List<JExpression> dims = new ArrayList<JExpression>();
    dims.add(new JIntLiteral(info, initializers.size()));
    return new JNewArray(info, type, dims, initializers);
  }

  @Nonnull
  public static JNewArray createWithDims(
      @Nonnull SourceInfo info, @Nonnull JArrayType type, @Nonnull List<JExpression> dims) {
    return new JNewArray(info, type, dims, Collections.<JExpression>emptyList());
  }

  private JNewArray(@Nonnull SourceInfo info, @Nonnull JArrayType type,
      @Nonnull List<JExpression> dims, @Nonnull List<JExpression> initializers) {
    super(info);
    this.type = type;
    this.dims = dims;
    this.initializers = initializers;
  }

  @Nonnull
  public JArrayType getArrayType() {
    return type;
  }

  @Override
  public JArrayType getType() {
    return type;
  }

  @Override
  public boolean canThrow() {
    return true;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {

      if (!dims.isEmpty()) {
        visitor.accept(dims);
      }

      if (!initializers.isEmpty()) {
        visitor.accept(initializers);
      }
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);

    for (JExpression dim : dims) {
      dim.traverse(schedule);
    }

    for (JExpression initializer : initializers) {
      initializer.traverse(schedule);
    }
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!transform(dims, existingNode, (JExpression) newNode, transformation)) {
      if (!transform(initializers, existingNode, (JExpression) newNode, transformation)) {
        super.transform(existingNode, newNode, transformation);
      }
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Nonnull
  public List<JExpression> getDims() {
    return dims;
  }

  @Nonnull
  public List<JExpression> getInitializers() {
    return initializers;
  }

  public boolean hasConstantInitializer() {
    JType eltType = getType().getElementType();

    if (eltType instanceof JPrimitiveType) {

      if (initializers.isEmpty()) {
        return false;
      }

      for (JExpression initExpression : initializers) {
        if (!(initExpression instanceof JValueLiteral)
            || !initExpression.getType().equals(eltType)) {
          return false;
        }
      }

      return true;
    }

    return false;
  }
}
