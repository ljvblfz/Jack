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

import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Java instance of expression.
 */
@Description("Java instance of expression")
public class JInstanceOf extends JExpression {

  private static final long serialVersionUID = 1L;

  private JExpression expr;
  private final JReferenceType testType;

  public JInstanceOf(SourceInfo info, JReferenceType testType, JExpression expression) {
    super(info);
    this.testType = testType;
    this.expr = expression;
  }

  public JExpression getExpr() {
    return expr;
  }

  public JReferenceType getTestType() {
    return testType;
  }

  @Override
  public JType getType() {
    return JPrimitiveTypeEnum.BOOLEAN.getType();
  }

  @Override
  public boolean canThrow() {
    return true;
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
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException {
    assert newNode != null;

    if (expr == existingNode) {
      expr = (JExpression) newNode;
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
