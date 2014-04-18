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
 * Java if statement.
 */
@Description("Java if statement")
public class JIfStatement extends JStatement {

  private JStatement elseStmt;
  private JExpression ifExpr;
  private JStatement thenStmt;

  public JIfStatement(
      SourceInfo info, JExpression ifExpr, JStatement thenStmt, JStatement elseStmt) {
    super(info);
    this.ifExpr = ifExpr;
    this.thenStmt = thenStmt;
    this.elseStmt = elseStmt;
  }

  public JStatement getElseStmt() {
    return elseStmt;
  }

  public JExpression getIfExpr() {
    return ifExpr;
  }

  public JStatement getThenStmt() {
    return thenStmt;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(ifExpr);
      if (thenStmt != null) {
        visitor.accept(thenStmt);
      }
      if (elseStmt != null) {
        visitor.accept(elseStmt);
      }
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    ifExpr.traverse(schedule);
    if (thenStmt != null) {
      thenStmt.traverse(schedule);
    }
    if (elseStmt != null) {
      elseStmt.traverse(schedule);
    }
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode) {
    assert newNode != null;

    if (elseStmt == existingNode) {
      elseStmt = (JStatement) newNode;
    } else if (thenStmt == existingNode) {
      thenStmt = (JStatement) newNode;
    } else if (ifExpr == existingNode) {
      ifExpr = (JExpression) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
      return;
    }
  }

  @Override
  protected void removeImpl(@Nonnull JNode existingNode) throws UnsupportedOperationException {
    if (thenStmt == existingNode) {
      thenStmt = null;
    } else if (elseStmt == existingNode) {
      elseStmt = null;
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
