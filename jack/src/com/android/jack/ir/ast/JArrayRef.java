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

import javax.annotation.Nonnull;

/**
 * Java array reference expression.
 */
@Description("Java array reference expression")
public class JArrayRef extends JExpression {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private JExpression indexExpr;
  @Nonnull
  private JExpression instance;

  public JArrayRef(SourceInfo info, @Nonnull JExpression instance, @Nonnull JExpression indexExpr) {
    super(info);
    this.instance = instance;
    this.indexExpr = indexExpr;
  }

  public JArrayType getArrayType() {
    JType type = instance.getType();
    if (type instanceof JNullType) {
      return null;
    }
    return (JArrayType) type;
  }

  @Nonnull
  public JExpression getIndexExpr() {
    return indexExpr;
  }

  @Nonnull
  public JExpression getInstance() {
    return instance;
  }

  @Override
  public JType getType() {
    JArrayType arrayType = getArrayType();
    return (arrayType == null) ? JNullType.INSTANCE : arrayType.getElementType();
  }

  @Override
  public boolean canThrow() {
    return true;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(instance);
      visitor.accept(indexExpr);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    instance.traverse(schedule);
    indexExpr.traverse(schedule);
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException {
    assert newNode != null;

    if (indexExpr == existingNode) {
      indexExpr = (JExpression) newNode;
    } else if (instance == existingNode) {
      instance = (JExpression) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
