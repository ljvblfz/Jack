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

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Represents multiple ordered expressions as a single compound expression.
 */
@Description("Represents multiple ordered expressions")
public class JMultiExpression extends JExpression {

  public List<JExpression> exprs;

  public JMultiExpression(SourceInfo info, List<JExpression> exprs) {
    super(info);
    this.exprs = exprs;
  }

  @Override
  public JType getType() {
    int c = exprs.size();
    if (c == 0) {
      return JPrimitiveTypeEnum.VOID.getType();
    } else {
      return exprs.get(c - 1).getType();
    }
  }

  public List<JExpression> getExprs() {
    return exprs;
  }

  @Override
  protected boolean isResultOfExpressionUsed(JExpression expr) {
    assert exprs.contains(expr);
    JNode parent = getParent();
    assert parent != null;
    return exprs.lastIndexOf(expr) == (exprs.size() - 1) && parent.isResultOfExpressionUsed(this);
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(exprs);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    for (JExpression expr : exprs) {
     expr.traverse(schedule);
    }
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) {
    if (!transform(exprs, existingNode, (JExpression) newNode, transformation)) {
      super.transform(existingNode, newNode, transformation);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
