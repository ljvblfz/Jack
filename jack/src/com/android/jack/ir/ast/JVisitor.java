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


import com.android.jack.JackAbortException;
import com.android.jack.ir.HasSourceInfo;
import com.android.jack.ir.JNodeInternalError;
import com.android.jack.load.JackLoadingException;
import com.android.sched.transform.TransformRequest;

import java.util.ArrayList;
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

  public <T extends JNode> void accept(@Nonnull ArrayList<T> array) {
    for (int i = 0, len = array.size(); i < len; ++i) {
      try {
        array.get(i).traverse(this);
      } catch (RuntimeException e) {
        throw wrapException(array.get(i), e);
      } catch (Error e) {
        throw wrapException(array.get(i), e);
      }
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
    if (e instanceof JackAbortException) {
      // No need to wrap JackAbortException
      throw (JackAbortException) e;
    }
    if (e instanceof JackLoadingException) {
      // No need to wrap JackLoadingException
      throw (JackLoadingException) e;
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

  public void endVisit(@Nonnull JAbsentArrayDimension absentArrayDimension) {
    endVisit((JLiteral) absentArrayDimension);
  }

  public void endVisit(@Nonnull JAbstractMethodBody abstractMethodBody) {
    endVisit((JNode) abstractMethodBody);
  }

  public void endVisit(@Nonnull JAbstractStringLiteral abstractStringLiteral) {
    endVisit((JValueLiteral) abstractStringLiteral);
  }

  public void endVisit(@Nonnull JAlloc alloc) {
    endVisit((JExpression) alloc);
  }

  public void endVisit(@Nonnull JAnnotation annotation) {
    endVisit((JExpression) annotation);
  }

  public void endVisit(@Nonnull JArrayLength arrayLength) {
    endVisit((JExpression) arrayLength);
  }

  public void endVisit(@Nonnull JArrayLiteral arrayLiteral) {
    endVisit((JExpression) arrayLiteral);
  }

  public void endVisit(@Nonnull JArrayRef arrayRef) {
    endVisit((JExpression) arrayRef);
  }

  public void endVisit(@Nonnull JArrayType arrayType) {
    endVisit((JReferenceType) arrayType);
  }

  public void endVisit(@Nonnull JAssertStatement assertStatement) {
    endVisit((JStatement) assertStatement);
  }

  public void endVisit(@Nonnull JBinaryOperation binaryOperation) {
    endVisit((JExpression) binaryOperation);
  }

  public void endVisit(@Nonnull JReinterpretCastOperation reinterpretCastOperation) {
    endVisit((JCastOperation) reinterpretCastOperation);
  }

  public void endVisit(@Nonnull JBlock block) {
    endVisit((JStatement) block);
  }

  public void endVisit(@Nonnull JBooleanLiteral booleanLiteral) {
    endVisit((JValueLiteral) booleanLiteral);
  }

  public void endVisit(@Nonnull JBreakStatement breakStatement) {
    endVisit((JStatement) breakStatement);
  }

  public void endVisit(@Nonnull JByteLiteral byteLiteral) {
    endVisit((JNumberValueLiteral) byteLiteral);
  }

  public void endVisit(@Nonnull JCaseStatement caseStatement) {
    endVisit((JStatement) caseStatement);
  }

  public void endVisit(@Nonnull JCastOperation castOperation) {
    endVisit((JExpression) castOperation);
  }

  public void endVisit(@Nonnull JCompositeStringLiteral compositeStringLiteral) {
    endVisit((JAbstractStringLiteral) compositeStringLiteral);
  }

  public void endVisit(@Nonnull JDynamicCastOperation dynamicCastOperation) {
    endVisit((JCastOperation) dynamicCastOperation);
  }

  public void endVisit(@Nonnull JCatchBlock catchBlock) {
    endVisit((JStatement) catchBlock);
  }

  public void endVisit(@Nonnull JCharLiteral charLiteral) {
    endVisit((JNumberValueLiteral) charLiteral);
  }

  public void endVisit(@Nonnull JClassLiteral classLiteral) {
    endVisit((JLiteral) classLiteral);
  }

  public void endVisit(@Nonnull JDefinedClass definedClass) {
    endVisit((JDefinedClassOrInterface) definedClass);
  }

  public void endVisit(@Nonnull JConditionalExpression conditionalExpression) {
    endVisit((JExpression) conditionalExpression);
  }

  public void endVisit(@Nonnull JConstructor constructor) {
    endVisit((JMethod) constructor);
  }

  public void endVisit(@Nonnull JContinueStatement continueStatement) {
    endVisit((JStatement) continueStatement);
  }

  public void endVisit(@Nonnull JDefinedClassOrInterface definedClassOrInterface) {
    endVisit((JReferenceType) definedClassOrInterface);
  }

  public void endVisit(@Nonnull JDoStatement doStatement) {
    endVisit((JStatement) doStatement);
  }

  public void endVisit(@Nonnull JDoubleLiteral doubleLiteral) {
    endVisit((JNumberValueLiteral) doubleLiteral);
  }

  public void endVisit(@Nonnull JEnumLiteral enumLiteral) {
    endVisit((JNode) enumLiteral);
  }

  public void endVisit(@Nonnull JExceptionRuntimeValue exceptionRuntimeValue) {
    endVisit((JExpression) exceptionRuntimeValue);
  }

  public void endVisit(@Nonnull JExpression expression) {
    endVisit((JNode) expression);
  }

  public void endVisit(@Nonnull JExpressionStatement expressionStatement) {
    endVisit((JStatement) expressionStatement);
  }

  public void endVisit(@Nonnull JField field) {
    endVisit((JNode) field);
  }

  public void endVisit(@Nonnull JFieldInitializer fieldInitializer) {
    endVisit((JStatement) fieldInitializer);
  }

  public void endVisit(@Nonnull JFieldNameLiteral fieldNameLiteral) {
    endVisit((JAbstractStringLiteral) fieldNameLiteral);
  }

  public void endVisit(@Nonnull JFieldRef fieldRef) {
    endVisit((JExpression) fieldRef);
  }

  public void endVisit(@Nonnull JFloatLiteral floatLiteral) {
    endVisit((JNumberValueLiteral) floatLiteral);
  }

  public void endVisit(@Nonnull JForStatement forStatement) {
    endVisit((JStatement) forStatement);
  }

  public void endVisit(@Nonnull JGoto gotoStatement) {
    endVisit((JStatement) gotoStatement);
  }

  public void endVisit(@Nonnull JIfStatement ifStatement) {
    endVisit((JStatement) ifStatement);
  }

  public void endVisit(@Nonnull JInstanceOf instanceOf) {
    endVisit((JExpression) instanceOf);
  }

  public void endVisit(@Nonnull JDefinedInterface definedInterface) {
    endVisit((JDefinedClassOrInterface) definedInterface);
  }

  public void endVisit(@Nonnull JIntLiteral intLiteral) {
    endVisit((JNumberValueLiteral) intLiteral);
  }

  public void endVisit(@Nonnull JLabel label) {
    endVisit((JNode) label);
  }

  public void endVisit(@Nonnull JLabeledStatement labeledStatement) {
    endVisit((JStatement) labeledStatement);
  }

  public void endVisit(@Nonnull JLambda lambda) {
    endVisit((JExpression) lambda);
  }

  public void endVisit(@Nonnull JLiteral literal) {
    endVisit((JExpression) literal);
  }

  public void endVisit(@Nonnull JLocal local) {
    endVisit((JVariable) local);
  }

  public void endVisit(@Nonnull JLocalRef localRef) {
    endVisit((JVariableRef) localRef);
  }

  public void endVisit(@Nonnull JLock lock) {
    endVisit((JStatement) lock);
  }

  public void endVisit(@Nonnull JLongLiteral longLiteral) {
    endVisit((JNumberValueLiteral) longLiteral);
  }

  public void endVisit(@Nonnull JMethod method) {
    endVisit((JNode) method);
  }

  public void endVisit(@Nonnull JMethodBody methodBody) {
    endVisit((JAbstractMethodBody) methodBody);
  }

  public void endVisit(@Nonnull JPolymorphicMethodCall polymorphicMethodCall) {
    endVisit((JExpression) polymorphicMethodCall);
  }

  public void endVisit(@Nonnull JMethodCall methodCall) {
    endVisit((JExpression) methodCall);
  }

  public void endVisit(@Nonnull JMethodIdRef methodIdRef) {
    endVisit((JNode) methodIdRef);
  }

  public void endVisit(@Nonnull JMethodLiteral methodLiteral) {
    endVisit((JLiteral) methodLiteral);
  }

  public void endVisit(@Nonnull JMethodNameLiteral methodNameLiteral) {
    endVisit((JAbstractStringLiteral) methodNameLiteral);
  }

  public void endVisit(@Nonnull JMultiExpression multiExpression) {
    endVisit((JExpression) multiExpression);
  }

  public void endVisit(@Nonnull JNameValuePair nameValuePair) {
    endVisit((JNode) nameValuePair);
  }

  public void endVisit(@Nonnull JNewArray newArray) {
    endVisit((JExpression) newArray);
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

  public void endVisit(@Nonnull JNullLiteral nullLiteral) {
    endVisit((JValueLiteral) nullLiteral);
  }

  public void endVisit(@Nonnull JNullType nullType) {
    endVisit((JReferenceType) nullType);
  }

  public void endVisit(@Nonnull JNumberValueLiteral numberValueLiteral) {
    endVisit((JValueLiteral) numberValueLiteral);
  }

  public void endVisit(@Nonnull JPackage packageDeclaration) {
    endVisit((JNode) packageDeclaration);
  }

  public void endVisit(@Nonnull JParameter parameter) {
    endVisit((JVariable) parameter);
  }

  public void endVisit(@Nonnull JParameterRef parameterRef) {
    endVisit((JVariableRef) parameterRef);
  }

  public void endVisit(@Nonnull JPhantomAnnotationType phantomAnnotationType) {
    endVisit((JPhantomInterface) phantomAnnotationType);
  }

  public void endVisit(@Nonnull JPhantomClass phantomClass) {
    endVisit((JPhantomClassOrInterface) phantomClass);
  }

  public void endVisit(@Nonnull JPhantomClassOrInterface phantomClassOrInterface) {
    endVisit((JReferenceType) phantomClassOrInterface);
  }

  public void endVisit(@Nonnull JPhantomInterface phantomInterface) {
    endVisit((JPhantomClassOrInterface) phantomInterface);
  }

  public void endVisit(@Nonnull JPostfixOperation postfixOperation) {
    endVisit((JUnaryOperation) postfixOperation);
  }

  public void endVisit(@Nonnull JPrefixOperation prefixOperation) {
    endVisit((JUnaryOperation) prefixOperation);
  }

  public void endVisit(@Nonnull JPrimitiveType primitiveType) {
    endVisit((JType) primitiveType);
  }

  public void endVisit(@Nonnull JSession session) {
    endVisit((JNode) session);
  }

  public void endVisit(@Nonnull JReferenceType referenceType) {
    endVisit((JType) referenceType);
  }

  public void endVisit(@Nonnull JReturnStatement returnStatement) {
    endVisit((JStatement) returnStatement);
  }

  public void endVisit(@Nonnull JShortLiteral shortLiteral) {
    endVisit((JNumberValueLiteral) shortLiteral);
  }

  public void endVisit(@Nonnull JStatement statement) {
    endVisit((JNode) statement);
  }

  public void endVisit(@Nonnull JStringLiteral stringLiteral) {
    endVisit((JAbstractStringLiteral) stringLiteral);
  }

  public void endVisit(@Nonnull JSwitchStatement switchStatement) {
    endVisit((JStatement) switchStatement);
  }

  public void endVisit(@Nonnull JSynchronizedBlock synchronizedBlock) {
    endVisit((JStatement) synchronizedBlock);
  }

  public void endVisit(@Nonnull JThis thisKeyword) {
    endVisit((JVariable) thisKeyword);
  }

  public void endVisit(@Nonnull JThisRef thisRef) {
    endVisit((JVariableRef) thisRef);
  }

  public void endVisit(@Nonnull JThrowStatement throwStatement) {
    endVisit((JStatement) throwStatement);
  }

  public void endVisit(@Nonnull JTryStatement tryStatement) {
    endVisit((JStatement) tryStatement);
  }

  public void endVisit(@Nonnull JType type) {
    endVisit((JNode) type);
  }

  public void endVisit(@Nonnull JTypeStringLiteral typeStringLiteral) {
    endVisit((JAbstractStringLiteral) typeStringLiteral);
  }

  public void endVisit(@Nonnull JUnaryOperation unaryOperation) {
    endVisit((JExpression) unaryOperation);
  }

  public void endVisit(@Nonnull JUnlock unlock) {
    endVisit((JStatement) unlock);
  }

  public void endVisit(@Nonnull JValueLiteral valueLiteral) {
    endVisit((JLiteral) valueLiteral);
  }

  public void endVisit(@Nonnull JVariable variable) {
    endVisit((JNode) variable);
  }

  public void endVisit(@Nonnull JVariableRef variableRef) {
    endVisit((JExpression) variableRef);
  }

  public void endVisit(@Nonnull JWhileStatement whileStatement) {
    endVisit((JStatement) whileStatement);
  }

  public boolean visit(@Nonnull JAbsentArrayDimension absentArrayDimension) {
    return visit((JLiteral) absentArrayDimension);
  }

  public boolean visit(@Nonnull JAbstractMethodBody abstractMethodBody) {
    return visit((JNode) abstractMethodBody);
  }

  public boolean visit(@Nonnull JAbstractStringLiteral abstractStringLiteral) {
    return visit((JValueLiteral) abstractStringLiteral);
  }

  public boolean visit(@Nonnull JAlloc alloc) {
    return visit((JExpression) alloc);
  }

  public boolean visit(@Nonnull JAnnotation annotation) {
    return visit((JExpression) annotation);
  }

  public boolean visit(@Nonnull JArrayLength arrayLength) {
    return visit((JExpression) arrayLength);
  }

  public boolean visit(@Nonnull JArrayLiteral arrayLiteral) {
    return visit((JExpression) arrayLiteral);
  }

  public boolean visit(@Nonnull JArrayRef arrayRef) {
    return visit((JExpression) arrayRef);
  }

  public boolean visit(@Nonnull JArrayType arrayType) {
    return visit((JReferenceType) arrayType);
  }

  public boolean visit(@Nonnull JAssertStatement assertStatement) {
    return visit((JStatement) assertStatement);
  }

  public boolean visit(@Nonnull JBinaryOperation binaryOperation) {
    return visit((JExpression) binaryOperation);
  }

  public boolean visit(@Nonnull JReinterpretCastOperation reinterpretCastOperation) {
    return visit((JCastOperation) reinterpretCastOperation);
  }

  public boolean visit(@Nonnull JBlock block) {
    return visit((JStatement) block);
  }

  public boolean visit(@Nonnull JBooleanLiteral booleanLiteral) {
    return visit((JValueLiteral) booleanLiteral);
  }

  public boolean visit(@Nonnull JBreakStatement breakStatement) {
    return visit((JStatement) breakStatement);
  }

  public boolean visit(@Nonnull JCaseStatement caseStatement) {
    return visit((JStatement) caseStatement);
  }

  public boolean visit(@Nonnull JByteLiteral byteLiteral) {
    return visit((JNumberValueLiteral) byteLiteral);
  }

  public boolean visit(@Nonnull JCastOperation castOperation) {
    return visit((JExpression) castOperation);
  }

  public boolean visit(@Nonnull JDynamicCastOperation dynamicCastOperation) {
    return visit((JCastOperation) dynamicCastOperation);
  }

  public boolean visit(@Nonnull JCatchBlock catchBlock) {
    return visit((JStatement) catchBlock);
  }

  public boolean visit(@Nonnull JCharLiteral charLiteral) {
    return visit((JNumberValueLiteral) charLiteral);
  }

  public boolean visit(@Nonnull JClassLiteral classLiteral) {
    return visit((JLiteral) classLiteral);
  }

  public boolean visit(@Nonnull JCompositeStringLiteral compositeStringLiteral) {
    return visit((JAbstractStringLiteral) compositeStringLiteral);
  }

  public boolean visit(@Nonnull JDefinedClass definedClass) {
    return visit((JDefinedClassOrInterface) definedClass);
  }

  public boolean visit(@Nonnull JConditionalExpression conditionalExpression) {
    return visit((JExpression) conditionalExpression);
  }

  public boolean visit(@Nonnull JConstructor constructor) {
    return visit((JMethod) constructor);
  }

  public boolean visit(@Nonnull JContinueStatement continueStatement) {
    return visit((JStatement) continueStatement);
  }

  public boolean visit(@Nonnull JDefinedClassOrInterface definedClassOrInterface) {
    return visit((JReferenceType) definedClassOrInterface);
  }

  public boolean visit(@Nonnull JDoStatement doStatement) {
    return visit((JStatement) doStatement);
  }

  public boolean visit(@Nonnull JDoubleLiteral doubleLiteral) {
    return visit((JNumberValueLiteral) doubleLiteral);
  }

  public boolean visit(@Nonnull JEnumLiteral enumLiteral) {
    return visit((JNode) enumLiteral);
  }

  public boolean visit(@Nonnull JExceptionRuntimeValue exceptionRuntimeValue) {
    return visit((JExpression) exceptionRuntimeValue);
  }

  public boolean visit(@Nonnull JExpression expression) {
    return visit((JNode) expression);
  }

  public boolean visit(@Nonnull JExpressionStatement expressionStatement) {
    return visit((JStatement) expressionStatement);
  }

  public boolean visit(@Nonnull JField field) {
    return visit((JNode) field);
  }

  public boolean visit(@Nonnull JFieldInitializer fieldInitializer) {
    return visit((JStatement) fieldInitializer);
  }

  public boolean visit(@Nonnull JFieldNameLiteral fieldNameLiteral) {
    return visit((JAbstractStringLiteral) fieldNameLiteral);
  }

  public boolean visit(@Nonnull JFieldRef fieldRef) {
    return visit((JExpression) fieldRef);
  }

  public boolean visit(@Nonnull JFloatLiteral floatLiteral) {
    return visit((JNumberValueLiteral) floatLiteral);
  }

  public boolean visit(@Nonnull JForStatement forStatement) {
    return visit((JStatement) forStatement);
  }

  public boolean visit(@Nonnull JGoto gotoStatement) {
    return visit((JStatement) gotoStatement);
  }

  public boolean visit(@Nonnull JIfStatement ifStatement) {
    return visit((JStatement) ifStatement);
  }

  public boolean visit(@Nonnull JInstanceOf instanceOf) {
    return visit((JExpression) instanceOf);
  }

  public boolean visit(@Nonnull JDefinedInterface definedInterface) {
    return visit((JDefinedClassOrInterface) definedInterface);
  }

  public boolean visit(@Nonnull JIntLiteral intLiteral) {
    return visit((JNumberValueLiteral) intLiteral);
  }

  public boolean visit(@Nonnull JLabel label) {
    return visit((JNode) label);
  }

  public boolean visit(@Nonnull JLabeledStatement labeledStatement) {
    return visit((JStatement) labeledStatement);
  }

  public boolean visit(@Nonnull JLambda lambda) {
    return visit((JExpression) lambda);
  }

  public boolean visit(@Nonnull JLiteral literal) {
    return visit((JExpression) literal);
  }

  public boolean visit(@Nonnull JLocal local) {
    return visit((JVariable) local);
  }

  public boolean visit(@Nonnull JLocalRef localRef) {
    return visit((JVariableRef) localRef);
  }

  public boolean visit(@Nonnull JLock lock) {
    return visit((JStatement) lock);
  }

  public boolean visit(@Nonnull JLongLiteral longLiteral) {
    return visit((JNumberValueLiteral) longLiteral);
  }

  public boolean visit(@Nonnull JMethod method) {
    return visit((JNode) method);
  }

  public boolean visit(@Nonnull JMethodBody methodBody) {
    return visit((JAbstractMethodBody) methodBody);
  }

  public boolean visit(@Nonnull JPolymorphicMethodCall polymorphicMethodCall) {
    return visit((JExpression) polymorphicMethodCall);
  }

  public boolean visit(@Nonnull JMethodCall methodCall) {
    return visit((JExpression) methodCall);
  }

  public boolean visit(@Nonnull JMethodIdRef methodIdRef) {
    return visit((JNode) methodIdRef);
  }

  public boolean visit(@Nonnull JMethodLiteral methodLiteral) {
    return visit((JLiteral) methodLiteral);
  }

  public boolean visit(@Nonnull JMethodNameLiteral methodNameLiteral) {
    return visit((JAbstractStringLiteral) methodNameLiteral);
  }

  public boolean visit(@Nonnull JMultiExpression multiExpression) {
    return visit((JExpression) multiExpression);
  }

  public boolean visit(@Nonnull JNewArray newArray) {
    return visit((JExpression) newArray);
  }

  public boolean visit(@Nonnull JNameValuePair nameValuePair) {
    return visit((JNode) nameValuePair);
  }

  public boolean visit(@Nonnull JNewInstance newInstance) {
    return visit((JMethodCall) newInstance);
  }

  /**
   * Visit of a {@link JNode}
   * @param jnode visited {@link JNode}
   */
  public boolean visit(@Nonnull JNode jnode) {
    return true;
  }

  public boolean visit(@Nonnull JNullLiteral nullLiteral) {
    return visit((JValueLiteral) nullLiteral);
  }

  public boolean visit(@Nonnull JNullType nullType) {
    return visit((JReferenceType) nullType);
  }

  public boolean visit(@Nonnull JNumberValueLiteral numberValueLiteral) {
    return visit((JValueLiteral) numberValueLiteral);
  }

  public boolean visit(@Nonnull JPackage packageDeclaration) {
    return visit((JNode) packageDeclaration);
  }

  public boolean visit(@Nonnull JParameter parameter) {
    return visit((JVariable) parameter);
  }

  public boolean visit(@Nonnull JParameterRef parameterRef) {
    return visit((JVariableRef) parameterRef);
  }

  public boolean visit(@Nonnull JPhantomAnnotationType phantomAnnotationType) {
    return visit((JPhantomInterface) phantomAnnotationType);
  }

  public boolean visit(@Nonnull JPhantomClass phantomClass) {
    return visit((JPhantomClassOrInterface) phantomClass);
  }

  public boolean visit(@Nonnull JPhantomClassOrInterface phantomClassOrInterface) {
    return visit((JReferenceType) phantomClassOrInterface);
  }

  public boolean visit(@Nonnull JPhantomInterface phantomInterface) {
    return visit((JPhantomClassOrInterface) phantomInterface);
  }

  public boolean visit(@Nonnull JPostfixOperation postfixOperation) {
    return visit((JUnaryOperation) postfixOperation);
  }

  public boolean visit(@Nonnull JPrefixOperation prefixOperation) {
    return visit((JUnaryOperation) prefixOperation);
  }

  public boolean visit(@Nonnull JPrimitiveType primitiveType) {
    return visit((JType) primitiveType);
  }

  public boolean visit(@Nonnull JSession session) {
    return visit((JNode) session);
  }

  public boolean visit(@Nonnull JReferenceType referenceType) {
    return visit((JType) referenceType);
  }

  public boolean visit(@Nonnull JReturnStatement returnStatement) {
    return visit((JStatement) returnStatement);
  }

  public boolean visit(@Nonnull JShortLiteral shortLiteral) {
    return visit((JNumberValueLiteral) shortLiteral);
  }

  public boolean visit(@Nonnull JStatement statement) {
    return visit((JNode) statement);
  }

  public boolean visit(@Nonnull JStringLiteral stringLiteral) {
    return visit((JAbstractStringLiteral) stringLiteral);
  }

  public boolean visit(@Nonnull JSwitchStatement switchStatement) {
    return visit((JStatement) switchStatement);
  }

  public boolean visit(@Nonnull JSynchronizedBlock synchronizedBlock) {
    return visit((JStatement) synchronizedBlock);
  }

  public boolean visit(@Nonnull JThis thisKeyword) {
    return visit((JVariable) thisKeyword);
  }

  public boolean visit(@Nonnull JThisRef thisRef) {
    return visit((JVariableRef) thisRef);
  }

  public boolean visit(@Nonnull JThrowStatement throwStatement) {
    return visit((JStatement) throwStatement);
  }

  public boolean visit(@Nonnull JTryStatement tryStatement) {
    return visit((JStatement) tryStatement);
  }

  public boolean visit(@Nonnull JType type) {
    return visit((JNode) type);
  }

  public boolean visit(@Nonnull JTypeStringLiteral typeStringLiteral) {
    return visit((JAbstractStringLiteral) typeStringLiteral);
  }

  public boolean visit(@Nonnull JUnaryOperation unaryOperation) {
    return visit((JExpression) unaryOperation);
  }

  public boolean visit(@Nonnull JUnlock unlock) {
    return visit((JStatement) unlock);
  }

  public boolean visit(@Nonnull JValueLiteral valueLiteral) {
    return visit((JLiteral) valueLiteral);
  }

  public boolean visit(@Nonnull JVariable variable) {
    return visit((JNode) variable);
  }

  public boolean visit(@Nonnull JVariableRef variableRef) {
    return visit((JExpression) variableRef);
  }

  public boolean visit(@Nonnull JWhileStatement whileStatement) {
    return visit((JStatement) whileStatement);
  }

  /*
   * Without context
   */

  public void visit(@Nonnull JAbsentArrayDimension absentArrayDimension,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JLiteral) absentArrayDimension, transformRequest);
  }

  public void visit(@Nonnull JAbstractMethodBody abstractMethodBody,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JNode) abstractMethodBody, transformRequest);
  }

  public void visit(@Nonnull JAbstractStringLiteral abstractStringLiteral,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JValueLiteral) abstractStringLiteral, transformRequest);
  }

  public void visit(@Nonnull JAlloc alloc, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) alloc, transformRequest);
  }

  public void visit(@Nonnull JAnnotation annotation, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) annotation, transformRequest);
  }

  public void visit(@Nonnull JArrayLength arrayLength, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) arrayLength, transformRequest);
  }

  public void visit(@Nonnull JArrayLiteral arrayLiteral, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) arrayLiteral, transformRequest);
  }

  public void visit(@Nonnull JArrayRef arrayRef, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) arrayRef, transformRequest);
  }

  public void visit(@Nonnull JArrayType arrayType, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JReferenceType) arrayType, transformRequest);
  }

  public void visit(@Nonnull JAssertStatement assertStatement,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JStatement) assertStatement, transformRequest);
  }

  public void visit(@Nonnull JBinaryOperation binaryOperation,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JExpression) binaryOperation, transformRequest);
  }

  public void visit(
      @Nonnull JReinterpretCastOperation reinterpretCastOperation,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JExpression) reinterpretCastOperation, transformRequest);
  }

  public void visit(@Nonnull JBlock block, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) block, transformRequest);
  }

  public void visit(@Nonnull JBooleanLiteral booleanLiteral,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JValueLiteral) booleanLiteral, transformRequest);
  }

  public void visit(@Nonnull JBreakStatement breakStatement,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JStatement) breakStatement, transformRequest);
  }

  public void visit(@Nonnull JCaseStatement caseStatement,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JStatement) caseStatement, transformRequest);
  }

  public void visit(@Nonnull JCastOperation castOperation,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JExpression) castOperation, transformRequest);
  }

  public void visit(@Nonnull JDynamicCastOperation dynamicCastOperation,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JCastOperation) dynamicCastOperation, transformRequest);
  }

  public void visit(@Nonnull JCatchBlock catchBlock, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) catchBlock, transformRequest);
  }

  public void visit(@Nonnull JCharLiteral charLiteral, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNumberValueLiteral) charLiteral, transformRequest);
  }

  public void visit(@Nonnull JClassLiteral classLiteral, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JLiteral) classLiteral, transformRequest);
  }

  public void visit(@Nonnull JCompositeStringLiteral compositeStringLiteral,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JAbstractStringLiteral) compositeStringLiteral, transformRequest);
  }

  public void visit(@Nonnull JDefinedClass definedClass, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JDefinedClassOrInterface) definedClass, transformRequest);
  }

  public void visit(@Nonnull JConditionalExpression conditionalExpression,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JExpression) conditionalExpression, transformRequest);
  }

  public void visit(@Nonnull JConstructor constructor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JMethod) constructor, transformRequest);
  }

  public void visit(@Nonnull JContinueStatement continueStatement,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JStatement) continueStatement, transformRequest);
  }

  public void visit(@Nonnull JDefinedClassOrInterface definedClassOrInterface,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JReferenceType) definedClassOrInterface, transformRequest);
  }

  public void visit(@Nonnull JDoStatement doStatement, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) doStatement, transformRequest);
  }

  public void visit(@Nonnull JDoubleLiteral doubleLiteral,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JNumberValueLiteral) doubleLiteral, transformRequest);
  }

  public void visit(@Nonnull JEnumLiteral enumLiteral, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) enumLiteral, transformRequest);
  }


  public void visit(@Nonnull JExceptionRuntimeValue exceptionRuntimeValue,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JExpression) exceptionRuntimeValue, transformRequest);
  }

  public void visit(@Nonnull JExpression expression, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) expression, transformRequest);
  }

  public void visit(@Nonnull JExpressionStatement expressionStatement,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JStatement) expressionStatement, transformRequest);
  }

  public void visit(@Nonnull JField field, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) field, transformRequest);
  }

  public void visit(@Nonnull JFieldInitializer fieldInitializer,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JStatement) fieldInitializer, transformRequest);
  }

  public void visit(@Nonnull JFieldNameLiteral fieldNameLiteral,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JAbstractStringLiteral) fieldNameLiteral, transformRequest);
  }

  public void visit(@Nonnull JFieldRef fieldRef, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) fieldRef, transformRequest);
  }

  public void visit(@Nonnull JFloatLiteral floatLiteral, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNumberValueLiteral) floatLiteral, transformRequest);
  }

  public void visit(@Nonnull JForStatement forStatement, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) forStatement, transformRequest);
  }

  public void visit(@Nonnull JGoto gotoStatement, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) gotoStatement, transformRequest);
  }

  public void visit(@Nonnull JIfStatement ifStatement, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) ifStatement, transformRequest);
  }

  public void visit(@Nonnull JInstanceOf instanceOf, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) instanceOf, transformRequest);
  }

  public void visit(@Nonnull JDefinedInterface definedInterface,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JDefinedClassOrInterface) definedInterface, transformRequest);
  }

  public void visit(@Nonnull JIntLiteral intLiteral, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNumberValueLiteral) intLiteral, transformRequest);
  }

  public void visit(@Nonnull JLabel label, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) label, transformRequest);
  }

  public void visit(@Nonnull JLabeledStatement labeledStatement,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JStatement) labeledStatement, transformRequest);
  }

  public void visit(@Nonnull JLambda lambda, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) lambda, transformRequest);
  }

  public void visit(@Nonnull JLiteral literal, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) literal, transformRequest);
  }

  public void visit(@Nonnull JLocal local, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JVariable) local, transformRequest);
  }

  public void visit(@Nonnull JLocalRef localRef, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JVariableRef) localRef, transformRequest);
  }

  public void visit(@Nonnull JLock lock, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) lock, transformRequest);
  }

  public void visit(@Nonnull JLongLiteral longLiteral, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNumberValueLiteral) longLiteral, transformRequest);
  }

  public void visit(@Nonnull JMethod method, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) method, transformRequest);
  }

  public void visit(@Nonnull JMethodBody methodBody, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JAbstractMethodBody) methodBody, transformRequest);
  }

  public void visit(@Nonnull JPolymorphicMethodCall polymorphicMethodCall,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JExpression) polymorphicMethodCall, transformRequest);
  }


  public void visit(@Nonnull JMethodCall methodCall, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) methodCall, transformRequest);
  }

  public void visit(@Nonnull JMethodIdRef methodIdRef, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) methodIdRef, transformRequest);
  }

  public void visit(@Nonnull JMethodLiteral methodLiteral,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JLiteral) methodLiteral, transformRequest);
  }

  public void visit(@Nonnull JMethodNameLiteral methodNameLiteral,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JAbstractStringLiteral) methodNameLiteral, transformRequest);
  }

  public void visit(@Nonnull JMultiExpression multiExpression,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JExpression) multiExpression, transformRequest);
  }

  public void visit(@Nonnull JNewArray newArray, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) newArray, transformRequest);
  }

  public void visit(
      @Nonnull JNameValuePair nameValuePair, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) nameValuePair, transformRequest);
  }

  public void visit(@Nonnull JNewInstance newInstance, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JMethodCall) newInstance, transformRequest);
  }

  /**
   * Visit of a {@link JNode} with a {@link TransformRequest} to apply on.
   * @param jnode visited {@link JNode}
   * @param transformRequest {@link TransformRequest} to apply on.
   */
  public void visit(@Nonnull JNode jnode, @Nonnull TransformRequest transformRequest)
      throws Exception {}

  public void visit(@Nonnull JNullLiteral nullLiteral, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JValueLiteral) nullLiteral, transformRequest);
  }

  public void visit(@Nonnull JNullType nullType, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JReferenceType) nullType, transformRequest);
  }

  public void visit(@Nonnull JNumberValueLiteral numberValueLiteral,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JValueLiteral) numberValueLiteral, transformRequest);
  }


  public void visit(@Nonnull JParameter parameter, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JVariable) parameter, transformRequest);
  }

  public void visit(@Nonnull JParameterRef parameterRef, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JVariableRef) parameterRef, transformRequest);
  }

  public void visit(@Nonnull JPhantomAnnotationType phantomAnnotationType,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JPhantomInterface) phantomAnnotationType, transformRequest);
  }

  public void visit(@Nonnull JPhantomClass phantomClass, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JPhantomClassOrInterface) phantomClass, transformRequest);
  }

  public void visit(@Nonnull JPhantomClassOrInterface phantomClassOrInterface,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JReferenceType) phantomClassOrInterface, transformRequest);
  }

  public void visit(@Nonnull JPhantomInterface phantomInterface,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JPhantomClassOrInterface) phantomInterface, transformRequest);
  }

  public void visit(@Nonnull JPostfixOperation postfixOperation,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JUnaryOperation) postfixOperation, transformRequest);
  }

  public void visit(@Nonnull JPrefixOperation prefixOperation,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JUnaryOperation) prefixOperation, transformRequest);
  }

  public void visit(@Nonnull JPrimitiveType primitiveType,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JType) primitiveType, transformRequest);
  }

  public void visit(@Nonnull JSession session, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) session, transformRequest);
  }

  public void visit(@Nonnull JReferenceType referenceType,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JType) referenceType, transformRequest);
  }

  public void visit(@Nonnull JReturnStatement returnStatement,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JStatement) returnStatement, transformRequest);
  }

  public void visit(@Nonnull JStatement statement, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) statement, transformRequest);
  }

  public void visit(@Nonnull JStringLiteral stringLiteral,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JAbstractStringLiteral) stringLiteral, transformRequest);
  }

  public void visit(@Nonnull JSwitchStatement switchStatement,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JStatement) switchStatement, transformRequest);
  }

  public void visit(@Nonnull JSynchronizedBlock synchronizedBlock,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JStatement) synchronizedBlock, transformRequest);
  }

  public void visit(@Nonnull JThisRef thisRef, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) thisRef, transformRequest);
  }

  public void visit(@Nonnull JThrowStatement throwStatement,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JStatement) throwStatement, transformRequest);
  }

  public void visit(@Nonnull JTryStatement tryStatement, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) tryStatement, transformRequest);
  }

  public void visit(@Nonnull JType type, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) type, transformRequest);
  }

  public void visit(@Nonnull JTypeStringLiteral typeStringLiteral,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JAbstractStringLiteral) typeStringLiteral, transformRequest);
  }

  public void visit(@Nonnull JUnaryOperation unaryOperation,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JExpression) unaryOperation, transformRequest);
  }

  public void visit(@Nonnull JUnlock unlock, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JStatement) unlock, transformRequest);
  }

  public void visit(@Nonnull JValueLiteral valueLiteral, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JLiteral) valueLiteral, transformRequest);
  }

  public void visit(@Nonnull JVariable variable, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JNode) variable, transformRequest);
  }

  public void visit(@Nonnull JVariableRef variableRef, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visit((JExpression) variableRef, transformRequest);
  }

  public void visit(@Nonnull JWhileStatement whileStatement,
      @Nonnull TransformRequest transformRequest) throws Exception {
    visit((JStatement) whileStatement, transformRequest);
  }

}
