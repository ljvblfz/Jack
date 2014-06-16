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

import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.item.Tag;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java switch statement.
 */
@Description("Java switch statement")
public class JSwitchStatement extends JStatement {

  /**
   * A {@link Tag} meaning that a {@link JSwitchStatement} may use enum values directly.
   */
  @Description("A JSwitchStatement may use enum values directly.")
  public static final class SwitchWithEnum implements Tag {
  }

  @Nonnull
  private final JBlock body;
  @Nonnull
  private JExpression expr;
  @Nonnull
  private final List<JCaseStatement> cases;
  @CheckForNull
  private JCaseStatement defaultCase;

  public JSwitchStatement(@Nonnull SourceInfo info, @Nonnull JExpression expr,
      @Nonnull JBlock body, @Nonnull List<JCaseStatement> cases,
      @CheckForNull JCaseStatement defaultCase) {
    super(info);
    this.expr = expr;
    this.body = body;
    this.cases = cases;
    this.defaultCase = defaultCase;
  }

  @Nonnull
  public JBlock getBody() {
    return body;
  }

  @Nonnull
  public JExpression getExpr() {
    return expr;
  }

  /**
   * @return the defaultCase
   */
  @CheckForNull
  public JCaseStatement getDefaultCase() {
    return defaultCase;
  }

  public void addCase(@Nonnull JCaseStatement caseStatement) {
    cases.add(caseStatement);
  }

  public void removeCase(@Nonnull JCaseStatement caseStatement) {
    cases.remove(caseStatement);
  }

  /**
   * @return the cases
   */
  @Nonnull
  public List<JCaseStatement> getCases() {
    return cases;
  }

  public void setDefaultCase(@CheckForNull JCaseStatement defaultCase) {
    this.defaultCase = defaultCase;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(expr);
      visitor.accept(body);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    expr.traverse(schedule);
    body.traverse(schedule);
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
