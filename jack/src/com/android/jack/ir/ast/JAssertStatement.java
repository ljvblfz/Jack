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

import javax.annotation.Nonnull;

/**
 * Java assert statement.
 */
@Description("Java assert statement")
public class JAssertStatement extends JStatement {

  private JExpression arg;
  private JExpression testExpr;

  public JAssertStatement(SourceInfo info, JExpression testExpr, JExpression arg) {
    super(info);
    this.testExpr = testExpr;
    this.arg = arg;
  }

  public JExpression getArg() {
    return arg;
  }

  public JExpression getTestExpr() {
    return testExpr;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
       visitor.accept(testExpr);
      if (arg != null) {
        visitor.accept(arg);
      }
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    testExpr.traverse(schedule);
    if (arg != null) {
      arg.traverse(schedule);
    }
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException {
    assert newNode != null;

    if (testExpr == existingNode) {
      testExpr = (JExpression) newNode;
    } else if (arg == existingNode) {
      arg = (JExpression) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }

  @Override
  protected void removeImpl(@Nonnull JNode existingNode) throws UnsupportedOperationException {
    if (arg == existingNode) {
      arg = null;
    } else {
      super.removeImpl(existingNode);
    }
  }

  @Override
  public boolean canThrow() {
    return true;
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
