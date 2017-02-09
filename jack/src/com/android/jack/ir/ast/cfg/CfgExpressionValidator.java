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
import com.android.jack.ir.ast.JAbstractMethodCall;
import com.android.jack.ir.ast.JAlloc;
import com.android.jack.ir.ast.JArrayLength;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JConditionalOperation;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPolymorphicMethodCall;
import com.android.jack.ir.ast.JReinterpretCastOperation;
import com.android.jack.ir.ast.JSsaVariableRef;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JUnaryOperation;
import com.android.jack.ir.ast.JVisitor;

import java.util.NoSuchElementException;
import javax.annotation.Nonnull;

/** Validates expressions inside the CFG basic block elements */
class CfgExpressionValidator extends JVisitor {
  @Nonnull
  private final JBasicBlockElement blockElement;
  private final boolean isThrowingExpected;

  private boolean seenThrowingExpression = false;

  private CfgExpressionValidator(@Nonnull JBasicBlockElement element) {
    this.blockElement = element;

    JBasicBlock basicBlock = element.getBasicBlock();
    this.isThrowingExpected =
        basicBlock instanceof JThrowingBasicBlock &&
            basicBlock.getLastElement() == element;
  }

  public static void validate(@Nonnull JBasicBlockElement element) {
    new CfgExpressionValidator(element).accept(element);
  }

  @Override
  public boolean visit(@Nonnull JBasicBlockElement element) {
    if (this.blockElement != element) {
      throw new JNodeInternalError(element, "Nested block element");
    }
    return true;
  }

  @Override
  public void endVisit(@Nonnull JBasicBlockElement element) {
    if (isThrowingExpected) {
      assert this.blockElement.getBasicBlock() instanceof JThrowingBasicBlock;
      boolean isImplicitThrow =
          this.blockElement instanceof JUnlockBlockElement
              || this.blockElement instanceof JLockBlockElement
              || this.blockElement instanceof JThrowBlockElement;

      if (isImplicitThrow) {
        if (seenThrowingExpression) {
          throw new JNodeInternalError(element,
              "An unexpected exception is thrown in lock/unlock/throw block element");
        }
      } else {
        if (!seenThrowingExpression) {
          throw new JNodeInternalError(element,
              "An exception is expected to be thrown in throwing block element");
        }
      }
    } else {
      if (seenThrowingExpression) {
        throw new JNodeInternalError(element,
            "An unexpected exception is thrown in block element");
      }
    }
    super.endVisit(element);
  }

  @Override
  public boolean visit(@Nonnull JExpression expr) {
    throw new JNodeInternalError(expr,
        "Unexpected expression in CFG: " + expr.toSource() +
            " (" + expr.getClass().getSimpleName() + ")");
  }

  @Override
  public boolean visit(@Nonnull JMethodCall expr) {
    return visitMethodCall(expr, JMethodCallBlockElement.class);
  }

  @Override
  public boolean visit(@Nonnull JPolymorphicMethodCall expr) {
    return visitMethodCall(expr, JPolymorphicMethodCallBlockElement.class);
  }

  @Override
  public boolean visit(@Nonnull JFieldRef expr) {
    return visitFieldOrArrayRef(expr);
  }

  @Override
  public boolean visit(@Nonnull JArrayRef expr) {
    return visitFieldOrArrayRef(expr);
  }

  @Override
  public boolean visit(@Nonnull JBinaryOperation expr) {
    if (expr instanceof JConditionalOperation) {
      throw new JNodeInternalError(expr,
          "JConditionalOperation is not allowed in cfg-IR");
    }
    if (expr instanceof JAsgOperation) {
      confirmBlockElement(expr);
      confirmParent(expr, JVariableAsgBlockElement.class, JStoreBlockElement.class);

    } else {
      // Some of binary operations can throw
      if (expr.canThrow()) {
        visitThrowingRValue(expr);
      } else {
        visitNonThrowingOperation(expr);
      }
    }

    return true;
  }

  @Override
  public boolean visit(@Nonnull JUnaryOperation expr) {
    return visitNonThrowingOperation(expr);
  }

  @Override
  public boolean visit(@Nonnull JExceptionRuntimeValue expr) {
    assert !expr.canThrow();
    confirmBlockElement(expr);
    confirmVariableAssignmentRhs(expr, JVariableAsgBlockElement.class);
    confirmParent(expr.getParent().getParent(), JCatchBasicBlock.class);
    return true;
  }

  @Override
  public boolean visit(@Nonnull JLocalRef expr) {
    return visitLocalOrParameter(expr);
  }

  @Override
  public boolean visit(@Nonnull JSsaVariableRef ref) {
    return visitLocalOrParameter(ref);
  }

  @Override
  public boolean visit(@Nonnull JParameterRef expr) {
    return visitLocalOrParameter(expr);
  }

  @Override
  public boolean visit(@Nonnull JLiteral expr) {
    return expr.canThrow() ? visitThrowingRValue(expr) : visitNonThrowingTriviaRValue(expr);
  }

  @Override
  public boolean visit(@Nonnull JNullLiteral expr) {
    return visitNonThrowingTriviaRValue(expr);
  }

  @Override
  public boolean visit(@Nonnull JThisRef expr) {
    return visitNonThrowingTriviaRValue(expr);
  }

  @Override
  public boolean visit(@Nonnull JAlloc expr) {
    return visitThrowingRValue(expr);
  }

  @Override
  public boolean visit(@Nonnull JDynamicCastOperation expr) {
    if (expr.getTypes().size() > 1) {
      throw new JNodeInternalError(expr, "Intersection types are invalid on this level");
    }
    return expr.canThrow() ? visitThrowingRValue(expr) : visitNonThrowingOperation(expr);
  }

  @Override
  public boolean visit(@Nonnull JReinterpretCastOperation expr) {
    assert !expr.canThrow();
    return visitNonThrowingOperation(expr);
  }

  @Override
  public boolean visit(@Nonnull JInstanceOf expr) {
    assert expr.canThrow();
    return visitThrowingRValue(expr);
  }

  @Override
  public boolean visit(@Nonnull JArrayLength expr) {
    return visitThrowingRValue(expr);
  }

  @Override
  public boolean visit(@Nonnull JNewArray expr) {
    return visitThrowingRValue(expr);
  }

  private void confirmParent(@Nonnull JNode expr, @Nonnull Class... parents) {
    JNode parent = expr.getParent();
    assert parent != null;
    Class<? extends JNode> actual = parent.getClass();
    for (Class<?> clazz : parents) {
      if (clazz.isAssignableFrom(actual)) {
        return;
      }
    }

    StringBuilder builder = new StringBuilder();
    String sep = "{";
    for (Class clazz : parents) {
      builder.append(sep).append(clazz.getSimpleName());
      sep = " or ";
    }
    builder.append("}");
    throw new JNodeInternalError(expr,
        "Node must be a child of " + builder.toString() +
            ", but real parent is: " + parent.getClass().getSimpleName() +
            ", cfg: " + expr.getParent(JControlFlowGraph.class).toSource());
  }

  private void confirmBlockElement(@Nonnull JExpression expr) {
    try {
      JBasicBlockElement element = expr.getParent(JBasicBlockElement.class);
      if (element == blockElement) {
        return;
      }
    } catch (NoSuchElementException e) {
      /* Ignore the exception */
    }

    throw new JNodeInternalError(expr,
        "Expression must be in basic block element: " + this.blockElement.toSource());
  }

  private void confirmThrowingIsAllowed(@Nonnull JExpression expr) {
    assert expr.canThrow();
    if (!isThrowingExpected) {
      throw new JNodeInternalError(expr,
          "Expression must be in the last element of the trowing basic block: "
              + this.blockElement.toSource());
    }

    if (this.seenThrowingExpression) {
      throw new JNodeInternalError(expr,
          "Multiple throwing exceptions in block element");
    }

    this.seenThrowingExpression = true;
  }

  private void confirmVariableAssignmentRhs(
      @Nonnull JExpression expr, @Nonnull Class expectedParent) {
    JNode parent = expr.getParent();
    if (!(parent instanceof JAsgOperation) ||
        ((JAsgOperation) parent).getRhs() != expr) {
      throw new JNodeInternalError(expr,
          "Expression must be the value in the assignment inside variable "
              + "assignment block element: " + this.blockElement.toSource());
    }
    confirmParent(parent, expectedParent);
  }

  private void confirmNotAssignmentTarget(@Nonnull JExpression expr) {
    JNode parent = expr.getParent();
    if (parent instanceof JAsgOperation &&
        ((JAsgOperation) parent).getLhs() == expr) {
      throw new JNodeInternalError(expr,
          "Expression must NOT be assignment target, "
              + "block element: " + this.blockElement.toSource());
    }
  }

  private void confirmParentForTrivia(@Nonnull JExpression expr) {
    assert !expr.canThrow();
    confirmParent(expr,
        // Methods: both arguments and receiver
        JMethodCall.class, JPolymorphicMethodCall.class,
        // Operators
        JUnaryOperation.class, JBinaryOperation.class,
        // Expression holding block elements
        JCaseBlockElement.class, JSwitchBlockElement.class,
        JConditionalBlockElement.class, JReturnBlockElement.class,
        JLockBlockElement.class, JUnlockBlockElement.class,
        JPhiBlockElement.class,
        // Misc expressions
        JFieldRef.class, JArrayRef.class, JArrayLength.class, JNewArray.class,
        JDynamicCastOperation.class, JReinterpretCastOperation.class,
        JThrowBlockElement.class, JInstanceOf.class);
  }

  private boolean visitMethodCall(@Nonnull JAbstractMethodCall expr, @Nonnull Class parentClass) {
    assert expr.canThrow();
    confirmBlockElement(expr);
    confirmThrowingIsAllowed(expr);

    // Should be either in standalone method call block element
    // or in variable assignment block element
    confirmParent(expr, JAsgOperation.class, parentClass);
    confirmNotAssignmentTarget(expr);

    return true;
  }

  private boolean visitFieldOrArrayRef(@Nonnull JExpression expr) {
    assert expr.canThrow();
    confirmBlockElement(expr);
    confirmThrowingIsAllowed(expr);

    // Array/field ref may be referenced either in assignment or load context
    confirmParent(expr, JAsgOperation.class);
    JAsgOperation asgExpr = (JAsgOperation) expr.getParent();
    if (asgExpr.getLhs() == expr) {
      // Assignment context, assignment must be a child of store block element
      confirmParent(asgExpr, JStoreBlockElement.class);

    } else {
      // Load context, value in variable assignment block element
      confirmVariableAssignmentRhs(expr, JVariableAsgBlockElement.class);
    }
    return true;
  }

  private boolean visitLocalOrParameter(@Nonnull JExpression expr) {
    confirmBlockElement(expr);
    confirmParentForTrivia(expr);

    JNode parent = expr.getParent();
    if (parent instanceof JAsgOperation) {
      if (((JAsgOperation) parent).getLhs() == expr) {
        // Assignment context, assignment must be part of variable assignment block element
        confirmParent(parent, JVariableAsgBlockElement.class);
      }
    }
    return true;
  }

  private boolean visitThrowingRValue(@Nonnull JExpression expr) {
    assert expr.canThrow();
    confirmBlockElement(expr);
    confirmThrowingIsAllowed(expr);
    confirmVariableAssignmentRhs(expr, JVariableAsgBlockElement.class);
    return true;
  }

  private boolean visitNonThrowingTriviaRValue(@Nonnull JExpression expr) {
    assert !expr.canThrow();
    confirmBlockElement(expr);
    confirmParentForTrivia(expr);
    confirmNotAssignmentTarget(expr);
    return true;
  }

  private boolean visitNonThrowingOperation(@Nonnull JExpression expr) {
    assert !expr.canThrow();
    confirmBlockElement(expr);
    if (expr instanceof JReinterpretCastOperation) {
      // It is probably not the right thing to allow reinterpret cast
      // as an instance of array/field access or the receiver of the
      // method call, keeping it this way until we discuss it
      confirmParent(expr, JAsgOperation.class,
          JArrayRef.class, JFieldRef.class, JMethodCall.class);

    } else if (expr instanceof JDynamicCastOperation) {
      confirmParent(expr, JAsgOperation.class);

    } else {
      confirmParent(expr, JConditionalBlockElement.class, JAsgOperation.class);
    }
    confirmNotAssignmentTarget(expr);
    return true;
  }
}
