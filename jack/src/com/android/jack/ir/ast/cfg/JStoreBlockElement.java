/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.ir.ast.cfg;

import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Represents field or array element store basic block element */
public class JStoreBlockElement extends JBasicBlockElement {
  @Nonnull
  private JAsgOperation asg;

  public JStoreBlockElement(@Nonnull SourceInfo info, @Nonnull JAsgOperation asg) {
    super(info);
    assert asg.getLhs().canThrow();
    assert asg.getLhs() instanceof JFieldRef || asg.getLhs() instanceof JArrayRef;
    this.asg = asg;
    this.asg.updateParents(this);
  }

  @Nonnull
  public JAsgOperation getAssignment() {
    return asg;
  }

  @CheckForNull
  public JFieldRef getLhsAsFieldRef() {
    JExpression lhs = asg.getLhs();
    return (lhs instanceof JFieldRef) ? (JFieldRef) lhs : null;
  }

  @CheckForNull
  public JArrayRef getLhsAsArrayRef() {
    JExpression lhs = asg.getLhs();
    return (lhs instanceof JArrayRef) ? (JArrayRef) lhs : null;
  }

  @Nonnull
  public JExpression getValueExpression() {
    return asg.getRhs();
  }

  @Override
  @Nonnull
  public JThrowingExpressionBasicBlock getBasicBlock() {
    JBasicBlock block = super.getBasicBlock();
    assert block instanceof JThrowingExpressionBasicBlock;
    return (JThrowingExpressionBasicBlock) block;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(asg);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    asg.traverse(schedule);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest request) throws Exception {
    visitor.visit(this, request);
  }

  @Override
  public boolean isTerminal() {
    return true;
  }
}
