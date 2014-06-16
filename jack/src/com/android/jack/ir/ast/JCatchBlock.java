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
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A list of <code>JStatements</code> representing a catch block.
 */
@Description("A list of JStatements representing a catch block.")
public class JCatchBlock extends JStatementList {

  @Nonnull
  private final List<JClass> catchTypes;

  @Nonnull
  private final JLocal catchVar;

  public JCatchBlock(@Nonnull SourceInfo info, @Nonnull List<JClass> catchTypes,
      @Nonnull JLocal local) {
    super(info);
    this.catchTypes = catchTypes;
    catchVar = local;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(catchVar);
      visitor.accept(statements);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    catchVar.traverse(schedule);
    for (JStatement statement : statements) {
      statement.traverse(schedule);
    }
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) {
    if (!transform(statements, existingNode, (JStatement) newNode, transformation)) {
      super.transform(existingNode, newNode, transformation);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Nonnull
  public List<JClass> getCatchTypes() {
    return catchTypes;
  }

  @Nonnull
  public JLocal getCatchVar() {
    return catchVar;
  }
}
