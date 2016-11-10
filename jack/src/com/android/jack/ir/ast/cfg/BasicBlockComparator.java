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
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAlloc;
import com.android.jack.ir.ast.JArrayLength;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JCharLiteral;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JDoubleLiteral;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JEnumLiteral;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JFloatLiteral;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodLiteral;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JPolymorphicMethodCall;
import com.android.jack.ir.ast.JReinterpretCastOperation;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JUnaryOperation;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Basic implementation of the comparator for basic blocks */
public class BasicBlockComparator {
  /** Comparator visitor implements 'shallow' comparison of two nodes */
  protected class Comparator extends JVisitor {
    private boolean differenceFound = false;

    @CheckForNull
    private JNode other;

    /**
     * Get the node representing comparison target, ensures the exact node type.
     * If node types don't match, marks the difference and returns the original node.
     */
    @Nonnull
    @SuppressWarnings(value = "unchecked")
    protected <T extends JNode> T otherOrMe(@Nonnull T expr) {
      assert other != null;
      if (expr.getClass() != other.getClass()) {
        differenceFound = true;
        return expr;
      }
      return (T) other;
    }

    /** Check if the value if true, marks the difference otherwise */
    protected void ensure(boolean expectedToBeTrue) {
      if (!expectedToBeTrue) {
        differenceFound = true;
      }
    }

    /** Compare two nodes w/o children */
    protected boolean areShallowlyEqual(@Nonnull JNode a, @Nonnull JNode b) {
      assert !differenceFound;
      other = b;
      accept(a);
      return !differenceFound;
    }

    /** Performs basic checks valid for all kinds of the nodes */
    protected void performCommonChecks(@Nonnull JNode node) {
    }

    /** Performs basic checks valid for all kinds of the expressions */
    protected void performCommonChecks(@Nonnull JExpression expr) {
      performCommonChecks((JNode) expr);
      ensure(equal(expr.getType(), otherOrMe(expr).getType()));
    }

    protected <T extends JType> boolean equal(@Nonnull List<T> a, @Nonnull List<T> b) {
      if (a.size() != b.size()) {
        return false;
      }
      for (int i = 0; i < a.size(); i++) {
        if (!equal(a.get(i), b.get(i))) {
          return false;
        }
      }
      return true;
    }

    protected boolean equal(@Nonnull JType a, @Nonnull JType b) {
      return a.isSameType(b);
    }

    protected boolean equal(@Nonnull JVariable a, @Nonnull JVariable b) {
      return a == b;
    }

    @Override public boolean visit(@Nonnull JBasicBlock block) {
      return false;
    }

    @Override public boolean visit(@Nonnull JBasicBlockElement element) {
      return false;
    }

    @Override public void endVisit(@Nonnull JBasicBlock block) {
      performCommonChecks(block);
      otherOrMe(block);
    }

    @Override public void endVisit(@Nonnull JBasicBlockElement element) {
      performCommonChecks(element);
      otherOrMe(element);
    }

    @Override public void endVisit(@Nonnull JConditionalBasicBlock block) {
      super.endVisit(block);
      JConditionalBasicBlock other = otherOrMe(block);
      ensure(other.isInverted() == block.isInverted());
    }

    @Override public void endVisit(@Nonnull JCatchBasicBlock block) {
      super.endVisit(block);
      JCatchBasicBlock other = otherOrMe(block);
      ensure(equal(other.getCatchTypes(), block.getCatchTypes()));
    }

    @Override public boolean visit(@Nonnull JExpression expr) {
      return false;
    }

    @Override public void endVisit(@Nonnull JExpression expr) {
      throw new JNodeInternalError(expr,
          "Unexpected expression in CFG: " + expr.toSource() +
              " (" + expr.getClass().getSimpleName() + ")");
    }

    @Override public void endVisit(@Nonnull JMethodCall expr) {
      performCommonChecks(expr);
      JMethodCall other = otherOrMe(expr);
      ensure((expr.getInstance() == null) == (other.getInstance() == null));
      ensure(equal(expr.getReceiverType(), other.getReceiverType()));
      ensure(expr.getMethodId().equals(other.getMethodId()));
      ensure(expr.getDispatchKind() == other.getDispatchKind());
      ensure(expr.getArgs().size() == other.getArgs().size());
    }

    @Override public void endVisit(@Nonnull JPolymorphicMethodCall expr) {
      performCommonChecks(expr);
      JPolymorphicMethodCall other = otherOrMe(expr);
      ensure((expr.getInstance() == null) == (other.getInstance() == null));
      ensure(equal(expr.getReceiverType(), other.getReceiverType()));
      ensure(expr.getMethodId().equals(other.getMethodId()));
      ensure(equal(expr.getCallSiteParameterTypes(), other.getCallSiteParameterTypes()));
      ensure(expr.getArgs().size() == other.getArgs().size());
    }

    @Override public void endVisit(@Nonnull JFieldRef expr) {
      performCommonChecks(expr);
      JFieldRef other = otherOrMe(expr);
      ensure((expr.getInstance() == null) == (other.getInstance() == null));
      ensure(equal(expr.getReceiverType(), other.getReceiverType()));
      ensure(expr.getFieldId().equals(other.getFieldId()));
    }

    @Override public void endVisit(@Nonnull JArrayRef expr) {
      performCommonChecks(expr);
      otherOrMe(expr);
    }

    @Override public void endVisit(@Nonnull JBinaryOperation expr) {
      performCommonChecks(expr);
      JBinaryOperation other = otherOrMe(expr);
      ensure(expr.getOp() == other.getOp());
    }

    @Override public void endVisit(@Nonnull JUnaryOperation expr) {
      performCommonChecks(expr);
      JUnaryOperation other = otherOrMe(expr);
      ensure(expr.getOp() == other.getOp());
    }

    @Override public void endVisit(@Nonnull JExceptionRuntimeValue expr) {
      performCommonChecks(expr);
      otherOrMe(expr);
    }

    @Override public void endVisit(@Nonnull JVariableRef expr) {
      performCommonChecks(expr);
      JVariableRef other = otherOrMe(expr);
      ensure(equal(expr.getTarget(), other.getTarget()));
    }

    @Override public void endVisit(@Nonnull JLiteral expr) {
      throw new AssertionError(expr.getClass()); // should be implemented in derived classes
    }

    @Override public void endVisit(@Nonnull JValueLiteral expr) {
      throw new AssertionError(expr.getClass()); // should be implemented in derived classes
    }

    @Override public void endVisit(@Nonnull JClassLiteral expr) {
      performCommonChecks(expr);
      JClassLiteral other = otherOrMe(expr);
      ensure(equal(expr.getRefType(), other.getRefType()));
    }

    @Override public void endVisit(@Nonnull JMethodLiteral expr) {
      performCommonChecks(expr);
      JMethodLiteral other = otherOrMe(expr);
      ensure(expr.getMethod() == other.getMethod());
    }

    @Override public void endVisit(@Nonnull JEnumLiteral expr) {
      performCommonChecks(expr);
      JEnumLiteral other = otherOrMe(expr);
      ensure(expr.getFieldId().equals(other.getFieldId()));
    }

    @Override public void endVisit(@Nonnull JNullLiteral expr) {
      performCommonChecks(expr);
      otherOrMe(expr);
    }

    @Override public void endVisit(@Nonnull JBooleanLiteral expr) {
      performCommonChecks(expr);
      JBooleanLiteral other = otherOrMe(expr);
      ensure(expr.getValue() == other.getValue());
    }

    @Override public void endVisit(@Nonnull JAbstractStringLiteral expr) {
      performCommonChecks(expr);
      JAbstractStringLiteral other = otherOrMe(expr);
      ensure(expr.getValue().equals(other.getValue()));
    }

    @Override public void endVisit(@Nonnull JShortLiteral expr) {
      performCommonChecks(expr);
      JShortLiteral other = otherOrMe(expr);
      ensure(expr.getValue() == other.getValue());
    }

    @Override public void endVisit(@Nonnull JByteLiteral expr) {
      performCommonChecks(expr);
      JByteLiteral other = otherOrMe(expr);
      ensure(expr.getValue() == other.getValue());
    }

    @Override public void endVisit(@Nonnull JLongLiteral expr) {
      performCommonChecks(expr);
      JLongLiteral other = otherOrMe(expr);
      ensure(expr.getValue() == other.getValue());
    }

    @Override public void endVisit(@Nonnull JIntLiteral expr) {
      performCommonChecks(expr);
      JIntLiteral other = otherOrMe(expr);
      ensure(expr.getValue() == other.getValue());
    }

    @Override public void endVisit(@Nonnull JCharLiteral expr) {
      performCommonChecks(expr);
      JCharLiteral other = otherOrMe(expr);
      ensure(expr.getValue() == other.getValue());
    }

    @Override public void endVisit(@Nonnull JFloatLiteral expr) {
      performCommonChecks(expr);
      JFloatLiteral other = otherOrMe(expr);
      ensure(expr.getValue() == other.getValue());
    }

    @Override public void endVisit(@Nonnull JDoubleLiteral expr) {
      performCommonChecks(expr);
      JDoubleLiteral other = otherOrMe(expr);
      ensure(expr.getValue() == other.getValue());
    }

    @Override public void endVisit(@Nonnull JAlloc expr) {
      performCommonChecks(expr);
      otherOrMe(expr);
    }

    @Override public void endVisit(@Nonnull JDynamicCastOperation expr) {
      performCommonChecks(expr);
      JDynamicCastOperation other = otherOrMe(expr);
      ensure(equal(expr.getTypes(), other.getTypes()));
    }

    @Override public void endVisit(@Nonnull JReinterpretCastOperation expr) {
      performCommonChecks(expr);
      JReinterpretCastOperation other = otherOrMe(expr);
      ensure(equal(expr.getType(), other.getType()));
    }

    @Override public void endVisit(@Nonnull JInstanceOf expr) {
      performCommonChecks(expr);
      JInstanceOf other = otherOrMe(expr);
      ensure(equal(expr.getTestType(), other.getTestType()));
    }

    @Override public void endVisit(@Nonnull JArrayLength expr) {
      performCommonChecks(expr);
      otherOrMe(expr);
    }

    @Override public void endVisit(@Nonnull JNewArray expr) {
      performCommonChecks(expr);
      JNewArray other = otherOrMe(expr);
      ensure(equal(expr.getArrayType(), other.getArrayType()));
      ensure(expr.getDims().size() == other.getDims().size());
      ensure(expr.getInitializers().size() == other.getInitializers().size());
    }
  }

  /** Compares `a` to `b`, returns `true` if they are considered to be equal */
  public boolean compare(@Nonnull JBasicBlock a, @Nonnull JBasicBlock b) {
    List<JNode> aNodes = getAllNodesInPostOrder(a);
    List<JNode> bNodes = getAllNodesInPostOrder(b);

    int size = aNodes.size();
    if (size != bNodes.size()) {
      return false;
    }

    Comparator comparator = getComparator();
    for (int i = 0; i < size; i++) {
      if (!comparator.areShallowlyEqual(aNodes.get(i), bNodes.get(i))) {
        return false;
      }
    }

    return !comparator.differenceFound;
  }

  @Nonnull
  protected Comparator getComparator() {
    return new Comparator();
  }

  @Nonnull
  private List<JNode> getAllNodesInPostOrder(@Nonnull JBasicBlock block) {
    final List<JNode> result = new ArrayList<>();
    new JVisitor() {
      @Override
      public void endVisit(@Nonnull JNode e) {
        result.add(e);
      }
    }.accept(block);
    return result;
  }
}
