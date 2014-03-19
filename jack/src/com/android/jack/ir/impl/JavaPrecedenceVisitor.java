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
package com.android.jack.ir.impl;


import com.android.jack.ir.ast.JAbsentArrayDimension;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAlloc;
import com.android.jack.ir.ast.JArrayLength;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JCharLiteral;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JDoubleLiteral;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JFloatLiteral;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMultiExpression;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPostfixOperation;
import com.android.jack.ir.ast.JPrefixOperation;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JVisitor;

import javax.annotation.Nonnull;

/**
 * See the Java Programming Language, 4th Edition, p. 750, Table 2. I just
 * numbered the table top to bottom as 0 through 14. Lower number means higher
 * precedence. I also gave primaries a precedence of 0; maybe I should have
 * started operators at 1, but in practice it won't matter since primaries can't
 * have children.
 */
class JavaPrecedenceVisitor extends JVisitor {

  public static int exec(JExpression expression) {
    JavaPrecedenceVisitor visitor = new JavaPrecedenceVisitor();
    visitor.accept(expression);
    assert visitor.answer >= 0 : "Precedence must be >= 0 (" + expression + ") "
        + expression.getClass();
    return visitor.answer;
  }

  private int answer = -1;

  private JavaPrecedenceVisitor() {
  }

  @Override
  public boolean visit(@Nonnull JAbsentArrayDimension x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JAlloc alloc) {
    answer = 2;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JArrayLength x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JArrayRef x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JBinaryOperation operation) {
    answer = operation.getOp().getPrecedence();
    return false;
  }

  @Override
  public boolean visit(@Nonnull JBooleanLiteral x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JByteLiteral x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JDynamicCastOperation operation) {
    answer = 2;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JCharLiteral x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JClassLiteral x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JConditionalExpression conditional) {
    answer = 13;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JDoubleLiteral x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JExceptionRuntimeValue x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JFieldRef x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JFloatLiteral x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JInstanceOf of) {
    answer = 6;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JIntLiteral x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JLocalRef x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JLongLiteral x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JMethodCall x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JMultiExpression x) {
    answer = 14;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JNewArray array) {
    answer = 2;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JNewInstance instance) {
    answer = 2;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JNullLiteral x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JParameterRef x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JPostfixOperation operation) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JPrefixOperation operation) {
    answer = 1;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JShortLiteral x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JAbstractStringLiteral x) {
    answer = 0;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JThisRef x) {
    answer = 0;
    return false;
  }

}
