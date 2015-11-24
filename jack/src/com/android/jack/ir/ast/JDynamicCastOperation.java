/*
 * Copyright 2015 Google Inc.
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

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Java cast expression with multiple target types that can throw at runtime.
 */
@Description("Java cast expression with multiple target types that can throw at runtime")
public class JDynamicCastOperation extends JCastOperation {

  @Nonnull
  private final List<JType> castTypes;

  public JDynamicCastOperation(@Nonnull SourceInfo info, @Nonnull JExpression expr,
      @Nonnull JType... castTypes) {
    super(info, expr);
    this.castTypes = Arrays.asList(castTypes);
    assert !this.castTypes.isEmpty();
    assert this.castTypes.size() == 1 || !hasJPrimitiveType();
  }

  public JDynamicCastOperation(@Nonnull SourceInfo info, @Nonnull JExpression expr,
      @Nonnull List<JType> castTypes) {
    super(info, expr);
    this.castTypes = castTypes;
    assert !this.castTypes.isEmpty();
  }

  @Nonnull
  public List<JType> getTypes() {
    return castTypes;
  }

  @Override
  @Nonnull
  public JType getType() {
    assert castTypes.size() == 1;
    return castTypes.iterator().next();
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(expr);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    expr.traverse(schedule);
  }

  @Override
  public boolean canThrow() {
    if (castTypes.size() > 1) {
      return true;
    }

    return !((getType() instanceof JPrimitiveType)
        && (getExpr().getType() instanceof JPrimitiveType));
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  private boolean hasJPrimitiveType() {
    for (JType type : castTypes) {
      if (type instanceof JPrimitiveType) {
        return true;
      }
    }
    return false;
  }
}
