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
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java for statement.
 */
@Description("Java for statement")
public class JForStatement extends JStatement implements JLoop {

  private JStatement body;
  private final List<JExpressionStatement> increments;
  private final List<JStatement> initializers;
  private JExpression testExpr;

  public JForStatement(SourceInfo info, List<JStatement> initializers, JExpression testExpr,
      List<JExpressionStatement> increments, JStatement body) {
    super(info);
    this.initializers = initializers;
    this.testExpr = testExpr;
    this.increments = increments;
    this.body = body;
  }

  public JStatement getBody() {
    return body;
  }

  public List<JExpressionStatement> getIncrements() {
    return increments;
  }

  public List<JStatement> getInitializers() {
    return initializers;
  }

  public JExpression getTestExpr() {
    return testExpr;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(initializers);
      if (testExpr != null) {
        visitor.accept(testExpr);
      }
      visitor.accept(increments);
      if (body != null) {
        visitor.accept(body);
      }
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    for (JStatement initializer : initializers) {
      initializer.traverse(schedule);
    }
    if (testExpr != null) {
      testExpr.traverse(schedule);
    }
    for (JExpressionStatement increment : increments) {
      increment.traverse(schedule);
    }
    if (body != null) {
      body.traverse(schedule);
    }
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!transform(increments, existingNode, (JExpressionStatement) newNode, transformation)) {
      if (!transform(initializers, existingNode, (JStatement) newNode, transformation)) {
        super.transform(existingNode, newNode, transformation);
      }
    }
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode) {
    assert newNode != null;

    if (testExpr == existingNode) {
      testExpr = (JExpression) newNode;
    } else if (body == existingNode) {
      body = (JStatement) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }

  @Override
  protected void removeImpl(@Nonnull JNode existingNode) throws UnsupportedOperationException {
    if (testExpr == existingNode) {
      testExpr = null;
    } else if (body == existingNode) {
      body = null;
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
