/*
 * Copyright 2008 Google Inc.
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
package com.android.jack.util;

import com.android.jack.ir.ast.JAbsentArrayDimension;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAlloc;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JArrayLength;
import com.android.jack.ir.ast.JArrayLiteral;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JCharLiteral;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JConcatOperation;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JDoubleLiteral;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JEnumLiteral;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JFloatLiteral;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodLiteral;
import com.android.jack.ir.ast.JMultiExpression;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPostfixOperation;
import com.android.jack.ir.ast.JPrefixOperation;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.sched.marker.Marker;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A general purpose expression cloner.
 */
public class CloneExpressionVisitor extends JVisitor {
  @CheckForNull
  protected JExpression expression;

  public CloneExpressionVisitor() {
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public <T extends JExpression> T cloneExpression(@Nonnull T expr) {
    // double check that the expression is successfully cloned
    expression = null;

    this.accept(expr);

    if (expression == null) {
      throw new AssertionError("Unable to clone expression " + expr.toString());
    }

    for (Marker m : expr.getAllMarkers()) {
      expression.addMarker(m.cloneIfNeeded());
    }

    assert expression != null;
    return (T) expression;
  }

  @Nonnull
  public List<JExpression> cloneExpressions(@Nonnull List<? extends JExpression> exprs) {
    ArrayList<JExpression> result = new ArrayList<JExpression>();
    for (JExpression expr : exprs) {
      result.add(cloneExpression(expr));
    }
    return result;
  }

  @Override
  public boolean visit(@Nonnull JAbsentArrayDimension x) {
    expression = new JAbsentArrayDimension(x.getSourceInfo());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JAbstractStringLiteral x) {
    expression = (JAbstractStringLiteral) x.clone();
    return false;
  }

  @Override
  public boolean visit(@Nonnull JAlloc x) {
    expression = new JAlloc(x.getSourceInfo(), x.getInstanceType());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JAnnotation annotation) {
    JAnnotation clonedAnnotationliteral = new JAnnotation(annotation.getSourceInfo(),
        annotation.getRetentionPolicy(), annotation.getType());
    for (JNameValuePair nvp : annotation.getNameValuePairs()) {
      clonedAnnotationliteral.add(new JNameValuePair(nvp.getSourceInfo(), nvp.getMethodId(),
          cloneExpression(nvp.getValue())));
    }
    expression = clonedAnnotationliteral;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JArrayLength x) {
    expression = new JArrayLength(x.getSourceInfo(), cloneExpression(x.getInstance()));
    return false;
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public boolean visit(@Nonnull JArrayLiteral arrayLiteral) {
    expression = new JArrayLiteral(arrayLiteral.getSourceInfo(),
        (ArrayList<JLiteral>) (Object) cloneExpressions(arrayLiteral.getValues()));
    return false;
  }

  @Override
  public boolean visit(@Nonnull JArrayRef x) {
    expression =
        new JArrayRef(x.getSourceInfo(), cloneExpression(x.getInstance()), cloneExpression(x
            .getIndexExpr()));
    return false;
  }

  @Override
  public boolean visit(@Nonnull JEnumLiteral x) {
    expression =
        new JEnumLiteral(x.getSourceInfo(), x.getFieldId());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JBinaryOperation x) {
    if (x instanceof JConcatOperation) {
      expression = new JConcatOperation(x.getSourceInfo(), (JClass) x.getType(),
          cloneExpression(x.getLhs()), cloneExpression(x.getRhs()));
    } else {
      expression = JBinaryOperation.create(x.getSourceInfo(), x.getOp(),
          cloneExpression(x.getLhs()), cloneExpression(x.getRhs()));
    }
    return false;
  }

  @Override
  public boolean visit(@Nonnull JBooleanLiteral x) {
    expression = new JBooleanLiteral(x.getSourceInfo(), x.getValue());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JByteLiteral x) {
    expression = new JByteLiteral(x.getSourceInfo(), x.getValue());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JCharLiteral x) {
    expression = new JCharLiteral(x.getSourceInfo(), x.getValue());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JClassLiteral x) {
    JClassLiteral classLiteral =
        new JClassLiteral(x.getSourceInfo(), x.getRefType(), (JClass) x.getType());
    expression = classLiteral;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JConditionalExpression x) {
    expression =
        new JConditionalExpression(x.getSourceInfo(), cloneExpression(x.getIfTest()),
            cloneExpression(x.getThenExpr()), cloneExpression(x.getElseExpr()));
    return false;
  }

  @Override
  public boolean visit(@Nonnull JDoubleLiteral x) {
    expression = new JDoubleLiteral(x.getSourceInfo(), x.getValue());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JDynamicCastOperation x) {
    expression =
        new JDynamicCastOperation(x.getSourceInfo(), cloneExpression(x.getExpr()), x.getTypes());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JExceptionRuntimeValue x) {
    expression = new JExceptionRuntimeValue(x.getSourceInfo(), (JClassOrInterface) x.getType());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JFieldRef x) {
    JExpression instanceRef = x.getInstance();
    expression =
        new JFieldRef(x.getSourceInfo(), instanceRef != null ? cloneExpression(instanceRef) : null,
            x.getFieldId(), x.getReceiverType());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JFloatLiteral x) {
    expression = new JFloatLiteral(x.getSourceInfo(), x.getValue());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JInstanceOf x) {
    expression = new JInstanceOf(x.getSourceInfo(), x.getTestType(), cloneExpression(x.getExpr()));
    return false;
  }

  @Override
  public boolean visit(@Nonnull JIntLiteral x) {
    expression = new JIntLiteral(x.getSourceInfo(), x.getValue());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JLocalRef x) {
    expression = x.getLocal().makeRef(x.getSourceInfo());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JLongLiteral x) {
    expression = new JLongLiteral(x.getSourceInfo(), x.getValue());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JMethodCall x) {
    JExpression instance = x.getInstance();
    JExpression clonedInstance = null;
    if (instance != null) {
      clonedInstance = cloneExpression(instance);
    }
    JMethodCall newMethodCall = new JMethodCall(x, clonedInstance);
    newMethodCall.addArgs(cloneExpressions(x.getArgs()));
    expression = newMethodCall;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JMethodLiteral methodLiteral) {
    expression = new JMethodLiteral(methodLiteral.getMethod(), methodLiteral.getSourceInfo());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JMultiExpression x) {
    JMultiExpression multi = new JMultiExpression(x.getSourceInfo(), cloneExpressions(x.exprs));
    expression = multi;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JNewArray x) {
    if (x.getInitializers().isEmpty()) {
      expression = JNewArray.createWithDims(
          x.getSourceInfo(), x.getArrayType(), cloneExpressions(x.getDims()));
    } else {
      expression = JNewArray.createWithInits(
          x.getSourceInfo(), x.getArrayType(), cloneExpressions(x.getInitializers()));
    }
    return false;
  }

  @Override
  public boolean visit(@Nonnull JNewInstance x) {
    JNewInstance newInstance = new JNewInstance(x);
    newInstance.addArgs(cloneExpressions(x.getArgs()));
    expression = newInstance;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JNullLiteral x) {
    expression = new JNullLiteral(x.getSourceInfo());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JParameterRef x) {
    expression = x.getParameter().makeRef(x.getSourceInfo());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JPostfixOperation x) {
    expression =
        JPostfixOperation.create(x.getSourceInfo(), x.getOp(), cloneExpression(x.getArg()));
    return false;
  }

  @Override
  public boolean visit(@Nonnull JPrefixOperation x) {
    expression = JPrefixOperation.create(x.getSourceInfo(), x.getOp(), cloneExpression(x.getArg()));
    return false;
  }

  @Override
  public boolean visit(@Nonnull JShortLiteral x) {
    expression = new JShortLiteral(x.getSourceInfo(), x.getValue());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JThisRef x) {
    expression = x.getTarget().makeRef(x.getSourceInfo());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JLambda lambda) {
    JLambda clonedLambda = new JLambda(lambda.getSourceInfo(), lambda.getMethodIdWithErasure(),
        lambda.getMethodIdRef(), lambda.getType(), lambda.getInterfaceBounds(),
        lambda.getMethodIdWithoutErasure());

    for (JExpression capturedVar : lambda.getCapturedVariables()) {
      clonedLambda.addCapturedVariable(cloneExpression(capturedVar));
    }

    expression = clonedLambda;

    return false;
  }
}