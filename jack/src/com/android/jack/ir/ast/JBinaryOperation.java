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

import javax.annotation.Nonnull;

/**
 * Binary operator expression.
 */
@Description("Binary operator expression")
public abstract class JBinaryOperation extends JExpression {

  private static final long serialVersionUID = 1L;
  @Nonnull
  private JExpression lhs;
  @Nonnull
  private JExpression rhs;

  JBinaryOperation(@Nonnull SourceInfo info,
      @Nonnull JExpression lhs, @Nonnull JExpression rhs) {
    super(info);
    this.lhs = lhs;
    this.rhs = rhs;
  }

  @Nonnull
  public JExpression getLhs() {
    return lhs;
  }

  @Nonnull
  public abstract JBinaryOperator getOp();

  @Nonnull
  public JExpression getRhs() {
    return rhs;
  }

  public boolean isAssignment() {
    return getOp().isAssignment();
  }

  public boolean isCompoundAssignment() {
    return getOp().isCompoundAssignment();
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(lhs);
      visitor.accept(rhs);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> instance) throws Exception {
    instance.process(this);
    lhs.traverse(instance);
    rhs.traverse(instance);
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException {
    assert newNode != null;

    if (rhs == existingNode) {
      rhs = (JExpression) newNode;
    } else if (lhs == existingNode) {
      lhs = (JExpression) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }

  @Nonnull
  public static JBinaryOperation create(@Nonnull SourceInfo info,
      @Nonnull JBinaryOperator op, @Nonnull JExpression lhs, @Nonnull JExpression rhs) {
    JBinaryOperation result = null;
    switch (op) {
      case ADD:
        result = new JAddOperation(info, lhs, rhs);
        break;
      case AND:
        result = new JAndOperation(info, lhs, rhs);
        break;
      case ASG:
        result = new JAsgOperation(info, lhs, rhs);
        break;
      case ASG_ADD:
        result = new JAsgAddOperation(info, lhs, rhs);
        break;
      case ASG_BIT_AND:
        result = new JAsgBitAndOperation(info, lhs, rhs);
        break;
      case ASG_BIT_OR:
        result = new JAsgBitOrOperation(info, lhs, rhs);
        break;
      case ASG_BIT_XOR:
        result = new JAsgBitXorOperation(info, lhs, rhs);
        break;
      case ASG_CONCAT:
        result = new JAsgConcatOperation(info, lhs, rhs);
        break;
      case ASG_DIV:
        result = new JAsgDivOperation(info, lhs, rhs);
        break;
      case ASG_MOD:
        result = new JAsgModOperation(info, lhs, rhs);
        break;
      case ASG_MUL:
        result = new JAsgMulOperation(info, lhs, rhs);
        break;
      case ASG_SHL:
        result = new JAsgShlOperation(info, lhs, rhs);
        break;
      case ASG_SHR:
        result = new JAsgShrOperation(info, lhs, rhs);
        break;
      case ASG_SHRU:
        result = new JAsgShruOperation(info, lhs, rhs);
        break;
      case ASG_SUB:
        result = new JAsgSubOperation(info, lhs, rhs);
        break;
      case BIT_AND:
        result = new JBitAndOperation(info, lhs, rhs);
        break;
      case BIT_OR:
        result = new JBitOrOperation(info, lhs, rhs);
        break;
      case BIT_XOR:
        result = new JBitXorOperation(info, lhs, rhs);
        break;
      case CONCAT:
        throw new AssertionError();
      case DIV:
        result = new JDivOperation(info, lhs, rhs);
        break;
      case EQ:
        result = new JEqOperation(info, lhs, rhs);
        break;
      case GT:
        result = new JGtOperation(info, lhs, rhs);
        break;
      case GTE:
        result = new JGteOperation(info, lhs, rhs);
        break;
      case LT:
        result = new JLtOperation(info, lhs, rhs);
        break;
      case LTE:
        result = new JLteOperation(info, lhs, rhs);
        break;
      case MOD:
        result = new JModOperation(info, lhs, rhs);
        break;
      case MUL:
        result = new JMulOperation(info, lhs, rhs);
        break;
      case NEQ:
        result = new JNeqOperation(info, lhs, rhs);
        break;
      case OR:
        result = new JOrOperation(info, lhs, rhs);
        break;
      case SHL:
        result = new JShlOperation(info, lhs, rhs);
        break;
      case SHR:
        result = new JShrOperation(info, lhs, rhs);
        break;
      case SHRU:
        result = new JShruOperation(info, lhs, rhs);
        break;
      case SUB:
        result = new JSubOperation(info, lhs, rhs);
        break;
    }
    assert result != null : "Unknown operator";
    assert result.getOp() == op;
    return result;
  }
}
