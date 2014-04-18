/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.ir.ast;

import com.android.jack.ir.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Goto statement.
 */
@Description("Goto statement.")
public class JGoto extends JStatement {

  @Nonnull
  private JLabeledStatement labeledStmt;

  public JGoto(@Nonnull SourceInfo info, @Nonnull JLabeledStatement labeledStmt) {
    super(info);
    assert (labeledStmt instanceof JLabeledStatementUnresolved)
        || labeledStmt.getBody() instanceof JBlock : "Goto must target labeled block.";
    this.labeledStmt = labeledStmt;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
  }

  /**
   * @return the labeledStmt that is the target of goto.
   */
  @Nonnull
  public JLabeledStatement getTargetBlock() {
    return labeledStmt;
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode) {
    if (labeledStmt == existingNode) {
      labeledStmt = (JLabeledStatement) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  public void setTarget(@Nonnull JLabeledStatement target) {
    assert target.getBody() instanceof JBlock : "Goto must target labeled block.";
    labeledStmt = target;
  }
}
