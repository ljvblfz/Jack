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

import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPolymorphicMethodCall;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Represents a variable (local, this, parameter) assignment basic block element */
public final class JVariableAsgBlockElement extends JBasicBlockElement {
  @Nonnull
  private JAsgOperation asg;

  public JVariableAsgBlockElement(@Nonnull SourceInfo info,
      @Nonnull ExceptionHandlingContext ehc, @Nonnull JAsgOperation asg) {
    super(info, ehc);
    assert !asg.getLhs().canThrow();
    assert asg.getLhs() instanceof JVariableRef;
    this.asg = asg;
  }

  @Nonnull
  public JAsgOperation getAssignment() {
    return asg;
  }

  @Nonnull
  public JVariable getVariable() {
    JExpression lhs = asg.getLhs();
    assert lhs instanceof JVariableRef;
    return ((JVariableRef) lhs).getTarget();
  }

  @Nonnull
  public JExpression getValue() {
    return asg.getRhs();
  }

  public boolean isCatchVariableAssignment() {
    return asg.getRhs() instanceof JExceptionRuntimeValue;
  }

  public boolean isFieldLoad() {
    return asg.getRhs() instanceof JFieldRef;
  }

  public boolean isArrayElementLoad() {
    return asg.getRhs() instanceof JArrayRef;
  }

  public boolean isLoad() {
    return isFieldLoad() || isArrayElementLoad();
  }

  public boolean isMethodCall() {
    return asg.getRhs() instanceof JMethodCall;
  }

  public boolean isPolymorphicMethodCall() {
    return asg.getRhs() instanceof JPolymorphicMethodCall;
  }

  @Override
  @CheckForNull
  public JVariableRef getDefinedVariable() {
    JExpression lhs = getAssignment().getLhs();
    if (!(lhs instanceof JVariableRef)) {
      return null;
    } else {
      return (JVariableRef) lhs;
    }
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
    return asg.getRhs().canThrow() || isCatchVariableAssignment();
  }

  @Override
  public void checkValidity() {
    super.checkValidity();

    if (isCatchVariableAssignment()) {
      if (!(getBasicBlock() instanceof JCatchBasicBlock)) {
        throw new JNodeInternalError(this, "Parent block must be JCatchBasicBlock");
      }

    } else if (asg.getRhs().canThrow()) {
      if (!(getBasicBlock() instanceof JThrowingExpressionBasicBlock)) {
        throw new JNodeInternalError(this, "Parent block must be JThrowingExpressionBasicBlock");
      }
      if (getBasicBlock().getLastElement() != this) {
        throw new JNodeInternalError(this, "Element must be the last element of the block");
      }

    } else {
      if (getBasicBlock().getLastElement() == this) {
        throw new JNodeInternalError(this,
            "The element must not be the last element of the parent block");
      }
    }
  }

  @Override
  protected void replaceImpl(
      @Nonnull JNode existingNode, @Nonnull JNode newNode) throws UnsupportedOperationException {
    if (asg == existingNode) {
      asg = (JAsgOperation) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }
}
