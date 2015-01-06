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
package com.android.jack.ir.ast;


import com.android.jack.ir.HasSourceInfo;
import com.android.jack.ir.JNodeInternalError;
import com.android.sched.transform.TransformRequest;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * A visitor for iterating through an AST.
 */
public class JVisitor {

  private final boolean needLoading;

  protected JVisitor() {
    this(true /* needLoading */);
  }

  protected JVisitor(boolean needLoading) {
    this.needLoading = needLoading;
  }

  public boolean needLoading() {
    return needLoading;
  }

  public void accept(@Nonnull JVisitable node) {
    try {
      node.traverse(this);
    } catch (RuntimeException e) {
      throw wrapException(node, e);
    } catch (Error e) {
      throw wrapException(node, e);
    }
  }

  public <T extends JNode> void accept(@Nonnull Collection<T> collection) {
    for (T element : collection) {
      try {
        element.traverse(this);
      } catch (RuntimeException e) {
        throw wrapException(element, e);
      } catch (Error e) {
        throw wrapException(element, e);
      }
    }
  }

  @Nonnull
  protected static JNodeInternalError wrapException(@Nonnull JVisitable node,
      @Nonnull Throwable e) {
    if (e instanceof VirtualMachineError) {
      // Always rethrow VM errors (an attempt to wrap may fail).
      throw (VirtualMachineError) e;
    }
    JNodeInternalError ice;
    if (e instanceof JNodeInternalError) {
      ice = (JNodeInternalError) e;
    } else {
      ice = new JNodeInternalError("Unexpected error during visit", e);
    }
    ice.addNode((HasSourceInfo) node);
    return ice;
  }

  public boolean didChange() {
    throw new UnsupportedOperationException();
  }

  public void endVisit(@Nonnull JAbsentArrayDimension x) {
    endVisit((JLiteral) x);
  }

  public void endVisit(@Nonnull JAbstractMethodBody x) {
    endVisit((JNode) x);
  }

  public void endVisit(@Nonnull JAbstractStringLiteral x) {
    endVisit((JValueLiteral) x);
  }

  public void endVisit(@Nonnull JAlloc x) {
    endVisit((JExpression) x);
  }

  public void endVisit(@Nonnull JAnnotationLiteral annotationLiteral) {
    endVisit((JExpression) annotationLiteral);
  }

  public void endVisit(@Nonnull JArrayLength x) {
    endVisit((JExpression) x);
  }

  public void endVisit(@Nonnull JArrayLiteral arrayLiteral) {
    endVisit((JExpression) arrayLiteral);
  }

  public void endVisit(@Nonnull JArrayRef x) {
    endVisit((JExpression) x);
  }

  public void endVisit(@Nonnull JArrayType x) {
    endVisit((JReferenceType) x);
  }

  public void endVisit(@Nonnull JAssertStatement x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JBinaryOperation x) {
    endVisit((JExpression) x);
  }

  public void endVisit(@Nonnull JReinterpretCastOperation x) {
    endVisit((JCastOperation) x);
  }

  public void endVisit(@Nonnull JBlock x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JBooleanLiteral x) {
    endVisit((JValueLiteral) x);
  }

  public void endVisit(@Nonnull JBreakStatement x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JByteLiteral x) {
    endVisit((JValueLiteral) x);
  }

  public void endVisit(@Nonnull JCaseStatement x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JCastOperation x) {
    endVisit((JExpression) x);
  }

  public void endVisit(@Nonnull JCompositeStringLiteral x) {
    endVisit((JAbstractStringLiteral) x);
  }

  public void endVisit(@Nonnull JDynamicCastOperation x) {
    endVisit((JCastOperation) x);
  }

  public void endVisit(@Nonnull JCatchBlock x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JCharLiteral x) {
    endVisit((JValueLiteral) x);
  }

  public void endVisit(@Nonnull JClassLiteral x) {
    endVisit((JLiteral) x);
  }

  public void endVisit(@Nonnull JDefinedClass x) {
    endVisit((JDefinedClassOrInterface) x);
  }

  public void endVisit(@Nonnull JConditionalExpression x) {
    endVisit((JExpression) x);
  }

  public void endVisit(@Nonnull JConstructor x) {
    endVisit((JMethod) x);
  }

  public void endVisit(@Nonnull JContinueStatement x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JDefinedClassOrInterface x) {
    endVisit((JReferenceType) x);
  }

  public void endVisit(@Nonnull JDoStatement x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JDoubleLiteral x) {
    endVisit((JValueLiteral) x);
  }

  public void endVisit(@Nonnull JEnumLiteral enumLiteral) {
    endVisit((JNode) enumLiteral);
  }

  public void endVisit(@Nonnull JExceptionRuntimeValue x) {
    endVisit((JExpression) x);
  }

  public void endVisit(@Nonnull JExpression x) {
    endVisit((JNode) x);
  }

  public void endVisit(@Nonnull JExpressionStatement x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JField x) {
    endVisit((JNode) x);
  }

  public void endVisit(@Nonnull JFieldInitializer x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JFieldNameLiteral x) {
    endVisit((JAbstractStringLiteral) x);
  }

  public void endVisit(@Nonnull JFieldRef x) {
    endVisit((JExpression) x);
  }

  public void endVisit(@Nonnull JFloatLiteral x) {
    endVisit((JValueLiteral) x);
  }

  public void endVisit(@Nonnull JForStatement x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JGoto x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JIfStatement x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JInstanceOf x) {
    endVisit((JExpression) x);
  }

  public void endVisit(@Nonnull JDefinedInterface x) {
    endVisit((JDefinedClassOrInterface) x);
  }

  public void endVisit(@Nonnull JIntLiteral x) {
    endVisit((JValueLiteral) x);
  }

  public void endVisit(@Nonnull JLabel x) {
    endVisit((JNode) x);
  }

  public void endVisit(@Nonnull JLabeledStatement x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JLiteral x) {
    endVisit((JExpression) x);
  }

  public void endVisit(@Nonnull JLocal x) {
    endVisit((JVariable) x);
  }

  public void endVisit(@Nonnull JLocalRef x) {
    endVisit((JVariableRef) x);
  }

  public void endVisit(@Nonnull JLock x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JLongLiteral x) {
    endVisit((JValueLiteral) x);
  }

  public void endVisit(@Nonnull JMethod x) {
    endVisit((JNode) x);
  }

  public void endVisit(@Nonnull JMethodBody x) {
    endVisit((JAbstractMethodBody) x);
  }

  public void endVisit(@Nonnull JMethodCall x) {
    endVisit((JExpression) x);
  }

  public void endVisit(@Nonnull JMethodLiteral x) {
    endVisit((JLiteral) x);
  }

  public void endVisit(@Nonnull JMethodNameLiteral x) {
    endVisit((JAbstractStringLiteral) x);
  }

  public void endVisit(@Nonnull JMultiExpression x) {
    endVisit((JExpression) x);
  }

  public void endVisit(@Nonnull JNameValuePair nameValuePair) {
    endVisit((JNode) nameValuePair);
  }

  public void endVisit(@Nonnull JNewArray x) {
    endVisit((JExpression) x);
  }

  public void endVisit(@Nonnull JNewInstance newInstance) {
    endVisit((JMethodCall) newInstance);
  }

  /**
   * End visit of a {@link JNode}
   * @param jnode visited {@link JNode}
   */
  public void endVisit(@Nonnull JNode jnode) {
    // empty block
  }

  public void endVisit(@Nonnull JNullLiteral x) {
    endVisit((JValueLiteral) x);
  }

  public void endVisit(@Nonnull JNullType x) {
    endVisit((JReferenceType) x);
  }

  public void endVisit(@Nonnull JPackage x) {
    endVisit((JNode) x);
  }

  public void endVisit(@Nonnull JParameter x) {
    endVisit((JVariable) x);
  }

  public void endVisit(@Nonnull JParameterRef x) {
    endVisit((JVariableRef) x);
  }

  public void endVisit(@Nonnull JPhantomAnnotation x) {
    endVisit((JPhantomInterface) x);
  }

  public void endVisit(@Nonnull JPhantomClass x) {
    endVisit((JPhantomClassOrInterface) x);
  }

  public void endVisit(@Nonnull JPhantomClassOrInterface x) {
    endVisit((JReferenceType) x);
  }

  public void endVisit(@Nonnull JPhantomInterface x) {
    endVisit((JPhantomClassOrInterface) x);
  }

  public void endVisit(@Nonnull JPostfixOperation x) {
    endVisit((JUnaryOperation) x);
  }

  public void endVisit(@Nonnull JPrefixOperation x) {
    endVisit((JUnaryOperation) x);
  }

  public void endVisit(@Nonnull JPrimitiveType x) {
    endVisit((JType) x);
  }

  public void endVisit(@Nonnull JSession x) {
    endVisit((JNode) x);
  }

  public void endVisit(@Nonnull JReferenceType x) {
    endVisit((JType) x);
  }

  public void endVisit(@Nonnull JReturnStatement x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JShortLiteral x) {
    endVisit((JValueLiteral) x);
  }

  public void endVisit(@Nonnull JStatement x) {
    endVisit((JNode) x);
  }

  public void endVisit(@Nonnull JStringLiteral x) {
    endVisit((JAbstractStringLiteral) x);
  }

  public void endVisit(@Nonnull JSwitchStatement x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JSynchronizedBlock x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JThis x) {
    endVisit((JVariable) x);
  }

  public void endVisit(@Nonnull JThisRef x) {
    endVisit((JVariableRef) x);
  }

  public void endVisit(@Nonnull JThrowStatement x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JTryStatement x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JType x) {
    endVisit((JNode) x);
  }

  public void endVisit(@Nonnull JTypeStringLiteral x) {
    endVisit((JAbstractStringLiteral) x);
  }

  public void endVisit(@Nonnull JUnaryOperation x) {
    endVisit((JExpression) x);
  }

  public void endVisit(@Nonnull JUnlock x) {
    endVisit((JStatement) x);
  }

  public void endVisit(@Nonnull JValueLiteral x) {
    endVisit((JLiteral) x);
  }

  public void endVisit(@Nonnull JVariable x) {
    endVisit((JNode) x);
  }

  public void endVisit(@Nonnull JVariableRef x) {
    endVisit((JExpression) x);
  }

  public void endVisit(@Nonnull JWhileStatement x) {
    endVisit((JStatement) x);
  }

  public boolean visit(@Nonnull JAbsentArrayDimension x) {
    return visit((JLiteral) x);
  }

  public boolean visit(@Nonnull JAbstractMethodBody x) {
    return visit((JNode) x);
  }

  public boolean visit(@Nonnull JAbstractStringLiteral x) {
    return visit((JValueLiteral) x);
  }

  public boolean visit(@Nonnull JAlloc x) {
    return visit((JExpression) x);
  }

  public boolean visit(@Nonnull JAnnotationLiteral annotationLiteral) {
    return visit((JExpression) annotationLiteral);
  }

  public boolean visit(@Nonnull JArrayLength x) {
    return visit((JExpression) x);
  }

  public boolean visit(@Nonnull JArrayLiteral arrayLiteral) {
    return visit((JExpression) arrayLiteral);
  }

  public boolean visit(@Nonnull JArrayRef x) {
    return visit((JExpression) x);
  }

  public boolean visit(@Nonnull JArrayType x) {
    return visit((JReferenceType) x);
  }

  public boolean visit(@Nonnull JAssertStatement x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JBinaryOperation x) {
    return visit((JExpression) x);
  }

  public boolean visit(@Nonnull JReinterpretCastOperation x) {
    return visit((JCastOperation) x);
  }

  public boolean visit(@Nonnull JBlock x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JBooleanLiteral x) {
    return visit((JValueLiteral) x);
  }

  public boolean visit(@Nonnull JBreakStatement x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JCaseStatement x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JByteLiteral x) {
    return visit((JValueLiteral) x);
  }

  public boolean visit(@Nonnull JCastOperation x) {
    return visit((JExpression) x);
  }

  public boolean visit(@Nonnull JDynamicCastOperation x) {
    return visit((JCastOperation) x);
  }

  public boolean visit(@Nonnull JCatchBlock x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JCharLiteral x) {
    return visit((JValueLiteral) x);
  }

  public boolean visit(@Nonnull JClassLiteral x) {
    return visit((JLiteral) x);
  }

  public boolean visit(@Nonnull JCompositeStringLiteral x) {
    return visit((JAbstractStringLiteral) x);
  }

  public boolean visit(@Nonnull JDefinedClass x) {
    return visit((JDefinedClassOrInterface) x);
  }

  public boolean visit(@Nonnull JConditionalExpression x) {
    return visit((JExpression) x);
  }

  public boolean visit(@Nonnull JConstructor x) {
    return visit((JMethod) x);
  }

  public boolean visit(@Nonnull JContinueStatement x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JDefinedClassOrInterface x) {
    return visit((JReferenceType) x);
  }

  public boolean visit(@Nonnull JDoStatement x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JDoubleLiteral x) {
    return visit((JValueLiteral) x);
  }

  public boolean visit(@Nonnull JEnumLiteral enumLiteral) {
    return visit((JNode) enumLiteral);
  }

  public boolean visit(@Nonnull JExceptionRuntimeValue x) {
    return visit((JExpression) x);
  }

  public boolean visit(@Nonnull JExpression x) {
    return visit((JNode) x);
  }

  public boolean visit(@Nonnull JExpressionStatement x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JField x) {
    return visit((JNode) x);
  }

  public boolean visit(@Nonnull JFieldInitializer x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JFieldNameLiteral x) {
    return visit((JAbstractStringLiteral) x);
  }

  public boolean visit(@Nonnull JFieldRef x) {
    return visit((JExpression) x);
  }

  public boolean visit(@Nonnull JFloatLiteral x) {
    return visit((JValueLiteral) x);
  }

  public boolean visit(@Nonnull JForStatement x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JGoto x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JIfStatement x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JInstanceOf x) {
    return visit((JExpression) x);
  }

  public boolean visit(@Nonnull JDefinedInterface x) {
    return visit((JDefinedClassOrInterface) x);
  }

  public boolean visit(@Nonnull JIntLiteral x) {
    return visit((JValueLiteral) x);
  }

  public boolean visit(@Nonnull JLabel x) {
    return visit((JNode) x);
  }

  public boolean visit(@Nonnull JLabeledStatement x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JLiteral x) {
    return visit((JExpression) x);
  }

  public boolean visit(@Nonnull JLocal x) {
    return visit((JVariable) x);
  }

  public boolean visit(@Nonnull JLocalRef x) {
    return visit((JVariableRef) x);
  }

  public boolean visit(@Nonnull JLock x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JLongLiteral x) {
    return visit((JValueLiteral) x);
  }

  public boolean visit(@Nonnull JMethod x) {
    return visit((JNode) x);
  }

  public boolean visit(@Nonnull JMethodBody x) {
    return visit((JAbstractMethodBody) x);
  }

  public boolean visit(@Nonnull JMethodCall x) {
    return visit((JExpression) x);
  }

  public boolean visit(@Nonnull JMethodLiteral x) {
    return visit((JLiteral) x);
  }

  public boolean visit(@Nonnull JMethodNameLiteral x) {
    return visit((JAbstractStringLiteral) x);
  }

  public boolean visit(@Nonnull JMultiExpression x) {
    return visit((JExpression) x);
  }

  public boolean visit(@Nonnull JNewArray x) {
    return visit((JExpression) x);
  }

  public boolean visit(@Nonnull JNameValuePair nameValuePair) {
    return visit((JNode) nameValuePair);
  }

  public boolean visit(@Nonnull JNewInstance x) {
    return visit((JMethodCall) x);
  }

  /**
   * Visit of a {@link JNode}
   * @param jnode visited {@link JNode}
   */
  public boolean visit(@Nonnull JNode jnode) {
    return true;
  }

  public boolean visit(@Nonnull JNullLiteral x) {
    return visit((JValueLiteral) x);
  }

  public boolean visit(@Nonnull JNullType x) {
    return visit((JReferenceType) x);
  }

  public boolean visit(@Nonnull JPackage x) {
    return visit((JNode) x);
  }

  public boolean visit(@Nonnull JParameter x) {
    return visit((JVariable) x);
  }

  public boolean visit(@Nonnull JParameterRef x) {
    return visit((JVariableRef) x);
  }

  public boolean visit(@Nonnull JPhantomAnnotation x) {
    return visit((JPhantomInterface) x);
  }

  public boolean visit(@Nonnull JPhantomClass x) {
    return visit((JPhantomClassOrInterface) x);
  }

  public boolean visit(@Nonnull JPhantomClassOrInterface x) {
    return visit((JReferenceType) x);
  }

  public boolean visit(@Nonnull JPhantomInterface x) {
    return visit((JPhantomClassOrInterface) x);
  }

  public boolean visit(@Nonnull JPostfixOperation x) {
    return visit((JUnaryOperation) x);
  }

  public boolean visit(@Nonnull JPrefixOperation x) {
    return visit((JUnaryOperation) x);
  }

  public boolean visit(@Nonnull JPrimitiveType x) {
    return visit((JType) x);
  }

  public boolean visit(@Nonnull JSession x) {
    return visit((JNode) x);
  }

  public boolean visit(@Nonnull JReferenceType x) {
    return visit((JType) x);
  }

  public boolean visit(@Nonnull JReturnStatement x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JShortLiteral x) {
    return visit((JValueLiteral) x);
  }

  public boolean visit(@Nonnull JStatement x) {
    return visit((JNode) x);
  }

  public boolean visit(@Nonnull JStringLiteral x) {
    return visit((JAbstractStringLiteral) x);
  }

  public boolean visit(@Nonnull JSwitchStatement x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JSynchronizedBlock x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JThis x) {
    return visit((JVariable) x);
  }

  public boolean visit(@Nonnull JThisRef x) {
    return visit((JVariableRef) x);
  }

  public boolean visit(@Nonnull JThrowStatement x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JTryStatement x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JType x) {
    return visit((JNode) x);
  }

  public boolean visit(@Nonnull JTypeStringLiteral x) {
    return visit((JAbstractStringLiteral) x);
  }

  public boolean visit(@Nonnull JUnaryOperation x) {
    return visit((JExpression) x);
  }

  public boolean visit(@Nonnull JUnlock x) {
    return visit((JStatement) x);
  }

  public boolean visit(@Nonnull JValueLiteral x) {
    return visit((JLiteral) x);
  }

  public boolean visit(@Nonnull JVariable x) {
    return visit((JNode) x);
  }

  public boolean visit(@Nonnull JVariableRef x) {
    return visit((JExpression) x);
  }

  public boolean visit(@Nonnull JWhileStatement x) {
    return visit((JStatement) x);
  }

  /*
   * Without context
   */

  public void visit(@Nonnull JAbsentArrayDimension x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JAbstractMethodBody x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) x, transformRequest);
  }

  public void visit(@Nonnull JAbstractStringLiteral x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JValueLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JAlloc x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(
      @Nonnull JAnnotationLiteral annotationLiteral, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) annotationLiteral, transformRequest);
  }

  public void visit(@Nonnull JArrayLength x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(@Nonnull JArrayLiteral arrayLiteral, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) arrayLiteral, transformRequest);
  }

  public void visit(@Nonnull JArrayRef x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(@Nonnull JArrayType x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JReferenceType) x, transformRequest);
  }

  public void visit(@Nonnull JAssertStatement x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JBinaryOperation x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(
      @Nonnull JReinterpretCastOperation x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(@Nonnull JBlock x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JBooleanLiteral x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JValueLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JBreakStatement x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JCaseStatement x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JCastOperation x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(@Nonnull JDynamicCastOperation x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JCastOperation) x, transformRequest);
  }

  public void visit(@Nonnull JCatchBlock x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JCharLiteral x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JValueLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JClassLiteral x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JCompositeStringLiteral x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JAbstractStringLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JDefinedClass x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JDefinedClassOrInterface) x, transformRequest);
  }

  public void visit(@Nonnull JConditionalExpression x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(@Nonnull JConstructor x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JMethod) x, transformRequest);
  }

  public void visit(@Nonnull JContinueStatement x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JDefinedClassOrInterface x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JReferenceType) x, transformRequest);
  }

  public void visit(@Nonnull JDoStatement x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JDoubleLiteral x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JValueLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JEnumLiteral enumLiteral, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) enumLiteral, transformRequest);
  }


  public void visit(@Nonnull JExceptionRuntimeValue x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(@Nonnull JExpression x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) x, transformRequest);
  }

  public void visit(@Nonnull JExpressionStatement x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JField x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) x, transformRequest);
  }

  public void visit(@Nonnull JFieldInitializer x,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JFieldNameLiteral x,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JAbstractStringLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JFieldRef x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(@Nonnull JFloatLiteral x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JValueLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JForStatement x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JGoto x, @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JIfStatement x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JInstanceOf x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(@Nonnull JDefinedInterface x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JDefinedClassOrInterface) x, transformRequest);
  }

  public void visit(@Nonnull JIntLiteral x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JValueLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JLabel x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) x, transformRequest);
  }

  public void visit(@Nonnull JLabeledStatement x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JLiteral x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(@Nonnull JLocal x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JVariable) x, transformRequest);
  }

  public void visit(@Nonnull JLocalRef x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JVariableRef) x, transformRequest);
  }

  public void visit(@Nonnull JLock x, @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JLongLiteral x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JValueLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JMethod x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) x, transformRequest);
  }

  public void visit(@Nonnull JMethodBody x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JAbstractMethodBody) x, transformRequest);
  }

  public void visit(@Nonnull JMethodCall x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(@Nonnull JMethodLiteral x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JMethodNameLiteral x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JAbstractStringLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JMultiExpression x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(@Nonnull JNewArray x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(
      @Nonnull JNameValuePair nameValuePair, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) nameValuePair, transformRequest);
  }

  public void visit(@Nonnull JNewInstance x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JMethodCall) x, transformRequest);
  }

  /**
   * Visit of a {@link JNode} with a {@link TransformRequest} to apply on.
   * @param jnode visited {@link JNode}
   * @param transformRequest {@link TransformRequest} to apply on.
   */
  public void visit(@Nonnull JNode jnode, @Nonnull TransformRequest transformRequest)
      throws Exception {}

  public void visit(@Nonnull JNullLiteral x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JValueLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JNullType x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JReferenceType) x, transformRequest);
  }

  public void visit(@Nonnull JParameter x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JVariable) x, transformRequest);
  }

  public void visit(@Nonnull JParameterRef x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JVariableRef) x, transformRequest);
  }

  public void visit(@Nonnull JPhantomAnnotation x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JPhantomInterface) x, transformRequest);
  }

  public void visit(@Nonnull JPhantomClass x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JPhantomClassOrInterface) x, transformRequest);
  }

  public void visit(@Nonnull JPhantomClassOrInterface x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JReferenceType) x, transformRequest);
  }

  public void visit(@Nonnull JPhantomInterface x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JPhantomClassOrInterface) x, transformRequest);
  }

  public void visit(@Nonnull JPostfixOperation x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JUnaryOperation) x, transformRequest);
  }

  public void visit(@Nonnull JPrefixOperation x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JUnaryOperation) x, transformRequest);
  }

  public void visit(@Nonnull JPrimitiveType x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JType) x, transformRequest);
  }

  public void visit(@Nonnull JSession x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) x, transformRequest);
  }

  public void visit(@Nonnull JReferenceType x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JType) x, transformRequest);
  }

  public void visit(@Nonnull JReturnStatement x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JStatement x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) x, transformRequest);
  }

  public void visit(@Nonnull JStringLiteral x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JAbstractStringLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JSwitchStatement x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JSynchronizedBlock x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JThisRef x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(@Nonnull JThrowStatement x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JTryStatement x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JType x, @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JNode) x, transformRequest);
  }

  public void visit(@Nonnull JTypeStringLiteral x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JAbstractStringLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JUnaryOperation x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(@Nonnull JUnlock x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

  public void visit(@Nonnull JValueLiteral x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JLiteral) x, transformRequest);
  }

  public void visit(@Nonnull JVariable x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) x, transformRequest);
  }

  public void visit(@Nonnull JVariableRef x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) x, transformRequest);
  }

  public void visit(@Nonnull JWhileStatement x, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) x, transformRequest);
  }

}
