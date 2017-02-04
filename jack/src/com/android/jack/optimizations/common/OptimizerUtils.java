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

package com.android.jack.optimizations.common;

import com.google.common.collect.Sets;

import com.android.jack.analysis.DefinitionMarker;
import com.android.jack.analysis.UseDefsMarker;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDoubleLiteral;
import com.android.jack.ir.ast.JEnumLiteral;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JFloatLiteral;
import com.android.jack.ir.ast.JIntegralConstant32;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.util.CloneExpressionVisitor;

import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Common utilities used in optimizations */
public final class OptimizerUtils {
  /** Tests if the method is an instance or static initializer */
  public static boolean isConstructor(@Nonnull JMethod method) {
    return method instanceof JConstructor || JMethod.isClinit(method);
  }

  /** Is an expression in an assignment position */
  public static boolean isAssigned(@Nonnull JExpression expr) {
    JNode parent = expr.getParent();
    return (parent instanceof JAsgOperation) &&
        (((JAsgOperation) parent).getLhs() == expr);
  }

  /** Get the value being assigned */
  @Nonnull
  public static JExpression getAssignedValue(@Nonnull JExpression expr) {
    assert isAssigned(expr);
    return ((JAsgOperation) expr.getParent()).getRhs();
  }

  /**
   * Tests if the value literals represent the same values.
   *
   * Two literals are considered equal if they are represented by the same
   * j-node and have the same value. Thus, JIntLiteral(0) and JLongLiteral(0)
   * are different literals.
   *
   * This equality also considers string literals different from JStringLiteral
   * to be equal only if they are same j-node.
   *
   * Note that this is not an abstract equality of the value literals, but
   * equality with semantic specific to several optimizations using literal
   * value trackers.
   */
  public static boolean areSameValueLiterals(
      @Nonnull JValueLiteral a, @Nonnull final JValueLiteral b) {
    if (a == b) {
      return true;
    }

    if (a.getClass() != b.getClass()) {
      return false;
    }

    if (a instanceof JNullLiteral) {
      return true;

    } else if (a instanceof JEnumLiteral) {
      JEnumLiteral aEnum = (JEnumLiteral) a;
      JEnumLiteral bEnum = (JEnumLiteral) b;

      return aEnum.getType().isSameType(bEnum.getType()) &&
          aEnum.getFieldId().getName().equals(bEnum.getFieldId().getName());

    } else if (a instanceof JBooleanLiteral) {
      return ((JBooleanLiteral) a).getValue() == ((JBooleanLiteral) b).getValue();

    } else if (a instanceof JStringLiteral) {
      return ((JStringLiteral) a).getValue().equals(((JStringLiteral) b).getValue());

    } else if (a instanceof JAbstractStringLiteral) {
      return false; // All other string literal kinds

    } else if (a instanceof JFloatLiteral) {
      return ((JFloatLiteral) a).getValue() == ((JFloatLiteral) b).getValue();

    } else if (a instanceof JDoubleLiteral) {
      return ((JDoubleLiteral) a).getValue() == ((JDoubleLiteral) b).getValue();

    } else if (a instanceof JLongLiteral) {
      return ((JLongLiteral) a).getValue() == ((JLongLiteral) b).getValue();

    } else {
      assert a instanceof JIntegralConstant32;
      return ((JIntegralConstant32) a).getIntValue() == ((JIntegralConstant32) b).getIntValue();
    }
  }

  /** Clone the expression */
  @Nonnull
  public static <T extends JExpression> T cloneExpression(@Nonnull T expr) {
    return new CloneExpressionVisitor().cloneExpression(expr);
  }

  /**
   * Checks if the call expression from the constructor represents another
   * constructor delegation call to another constructor of this type.
   */
  public static boolean isConstructorDelegation(
      @Nonnull JMethodCall call, @Nonnull JConstructor constructor) {
    // Must be a constructor call on 'this'
    if (!call.getMethodIdWide().isInit() ||
        !(call.getInstance() instanceof JThisRef)) {
      return false;
    }

    assert call.getDispatchKind() == JMethodCall.DispatchKind.DIRECT;
    assert constructor.getMethodIdWide().getKind() == MethodKind.INSTANCE_NON_VIRTUAL;

    // Well, we assume that if the type of the receiver is not the type of the
    // constructor we are analyzing, this must be a call to a super constructor
    return call.getReceiverType().isSameType(constructor.getEnclosingType());
  }

  /**
   * Checks if the value of the expression is a literal value, unwinding
   * optional chain of synthesized variables. If it is not, returns default value
   */
  @CheckForNull
  public static JValueLiteral asLiteralOrDefault(
      @Nonnull JExpression expression, @CheckForNull JValueLiteral defaultValue) {
    if (expression instanceof JValueLiteral) {
      return (JValueLiteral) expression;
    }

    // The value may be a reference to a synthetic local created to hold
    // the actual value, we unroll such assignment chain in some simple cases.
    Set<JLocal> localsSeen = Sets.newIdentityHashSet();
    while (expression instanceof JLocalRef) {
      JLocal local = ((JLocalRef) expression).getLocal();
      if (!local.isSynthetic() || localsSeen.contains(local)) {
        break;
      }
      localsSeen.add(local);

      UseDefsMarker usedRefs = expression.getMarker(UseDefsMarker.class);
      if (usedRefs == null) {
        break;
      }
      List<DefinitionMarker> defs = usedRefs.getDefs();
      if (defs.size() != 1) {
        break;
      }
      expression = defs.get(0).getValue();
    }

    // If the expression is NOT a simple value literal, let's return default value provided
    return expression instanceof JValueLiteral ? (JValueLiteral) expression : defaultValue;
  }


  private OptimizerUtils() {
  }
}
