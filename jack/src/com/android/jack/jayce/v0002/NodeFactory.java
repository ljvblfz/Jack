/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.jayce.v0002;

import com.android.jack.ir.ast.JAbsentArrayDimension;
import com.android.jack.ir.ast.JAbstractMethodBody;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAddOperation;
import com.android.jack.ir.ast.JAlloc;
import com.android.jack.ir.ast.JAndOperation;
import com.android.jack.ir.ast.JAnnotationLiteral;
import com.android.jack.ir.ast.JAnnotationMethod;
import com.android.jack.ir.ast.JArrayLength;
import com.android.jack.ir.ast.JArrayLiteral;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JAsgAddOperation;
import com.android.jack.ir.ast.JAsgBitAndOperation;
import com.android.jack.ir.ast.JAsgBitOrOperation;
import com.android.jack.ir.ast.JAsgBitXorOperation;
import com.android.jack.ir.ast.JAsgConcatOperation;
import com.android.jack.ir.ast.JAsgDivOperation;
import com.android.jack.ir.ast.JAsgModOperation;
import com.android.jack.ir.ast.JAsgMulOperation;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JAsgShlOperation;
import com.android.jack.ir.ast.JAsgShrOperation;
import com.android.jack.ir.ast.JAsgShruOperation;
import com.android.jack.ir.ast.JAsgSubOperation;
import com.android.jack.ir.ast.JAssertStatement;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBitAndOperation;
import com.android.jack.ir.ast.JBitOrOperation;
import com.android.jack.ir.ast.JBitXorOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JBreakStatement;
import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JCharLiteral;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JConcatOperation;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JContinueStatement;
import com.android.jack.ir.ast.JDefinedAnnotation;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JDivOperation;
import com.android.jack.ir.ast.JDoStatement;
import com.android.jack.ir.ast.JDoubleLiteral;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JEnumField;
import com.android.jack.ir.ast.JEnumLiteral;
import com.android.jack.ir.ast.JEqOperation;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JFloatLiteral;
import com.android.jack.ir.ast.JForStatement;
import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JGtOperation;
import com.android.jack.ir.ast.JGteOperation;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JLock;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JLtOperation;
import com.android.jack.ir.ast.JLteOperation;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodLiteral;
import com.android.jack.ir.ast.JModOperation;
import com.android.jack.ir.ast.JMulOperation;
import com.android.jack.ir.ast.JMultiExpression;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JNativeMethodBody;
import com.android.jack.ir.ast.JNeqOperation;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JOrOperation;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPostfixDecOperation;
import com.android.jack.ir.ast.JPostfixIncOperation;
import com.android.jack.ir.ast.JPrefixBitNotOperation;
import com.android.jack.ir.ast.JPrefixDecOperation;
import com.android.jack.ir.ast.JPrefixIncOperation;
import com.android.jack.ir.ast.JPrefixNegOperation;
import com.android.jack.ir.ast.JPrefixNotOperation;
import com.android.jack.ir.ast.JReinterpretCastOperation;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JShlOperation;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JShrOperation;
import com.android.jack.ir.ast.JShruOperation;
import com.android.jack.ir.ast.JSubOperation;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JSynchronizedBlock;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JUnaryOperation;
import com.android.jack.ir.ast.JUnlock;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.JWhileStatement;
import com.android.jack.ir.ast.marker.GenericSignature;
import com.android.jack.ir.ast.marker.SimpleName;
import com.android.jack.ir.ast.marker.ThisRefTypeInfo;
import com.android.jack.ir.ast.marker.ThrownExceptionMarker;
import com.android.jack.jayce.v0002.nodes.NAbsentArrayDimension;
import com.android.jack.jayce.v0002.nodes.NAddOperation;
import com.android.jack.jayce.v0002.nodes.NAlloc;
import com.android.jack.jayce.v0002.nodes.NAndOperation;
import com.android.jack.jayce.v0002.nodes.NAnnotationLiteral;
import com.android.jack.jayce.v0002.nodes.NAnnotationMethod;
import com.android.jack.jayce.v0002.nodes.NAnnotationType;
import com.android.jack.jayce.v0002.nodes.NArrayLength;
import com.android.jack.jayce.v0002.nodes.NArrayLiteral;
import com.android.jack.jayce.v0002.nodes.NArrayRef;
import com.android.jack.jayce.v0002.nodes.NAsgAddOperation;
import com.android.jack.jayce.v0002.nodes.NAsgBitAndOperation;
import com.android.jack.jayce.v0002.nodes.NAsgBitOrOperation;
import com.android.jack.jayce.v0002.nodes.NAsgBitXorOperation;
import com.android.jack.jayce.v0002.nodes.NAsgConcatOperation;
import com.android.jack.jayce.v0002.nodes.NAsgDivOperation;
import com.android.jack.jayce.v0002.nodes.NAsgModOperation;
import com.android.jack.jayce.v0002.nodes.NAsgMulOperation;
import com.android.jack.jayce.v0002.nodes.NAsgOperation;
import com.android.jack.jayce.v0002.nodes.NAsgShlOperation;
import com.android.jack.jayce.v0002.nodes.NAsgShrOperation;
import com.android.jack.jayce.v0002.nodes.NAsgShruOperation;
import com.android.jack.jayce.v0002.nodes.NAsgSubOperation;
import com.android.jack.jayce.v0002.nodes.NAssertStatement;
import com.android.jack.jayce.v0002.nodes.NBitAndOperation;
import com.android.jack.jayce.v0002.nodes.NBitOrOperation;
import com.android.jack.jayce.v0002.nodes.NBitXorOperation;
import com.android.jack.jayce.v0002.nodes.NBlock;
import com.android.jack.jayce.v0002.nodes.NBooleanLiteral;
import com.android.jack.jayce.v0002.nodes.NBreakStatement;
import com.android.jack.jayce.v0002.nodes.NByteLiteral;
import com.android.jack.jayce.v0002.nodes.NCaseStatement;
import com.android.jack.jayce.v0002.nodes.NCatchBlock;
import com.android.jack.jayce.v0002.nodes.NCharLiteral;
import com.android.jack.jayce.v0002.nodes.NClassLiteral;
import com.android.jack.jayce.v0002.nodes.NClassType;
import com.android.jack.jayce.v0002.nodes.NConcatOperation;
import com.android.jack.jayce.v0002.nodes.NConditionalExpression;
import com.android.jack.jayce.v0002.nodes.NConstructor;
import com.android.jack.jayce.v0002.nodes.NContinueStatement;
import com.android.jack.jayce.v0002.nodes.NDivOperation;
import com.android.jack.jayce.v0002.nodes.NDoStatement;
import com.android.jack.jayce.v0002.nodes.NDoubleLiteral;
import com.android.jack.jayce.v0002.nodes.NDynamicCastOperation;
import com.android.jack.jayce.v0002.nodes.NEnumField;
import com.android.jack.jayce.v0002.nodes.NEnumLiteral;
import com.android.jack.jayce.v0002.nodes.NEnumType;
import com.android.jack.jayce.v0002.nodes.NEqOperation;
import com.android.jack.jayce.v0002.nodes.NExceptionRuntimeValue;
import com.android.jack.jayce.v0002.nodes.NExpressionStatement;
import com.android.jack.jayce.v0002.nodes.NField;
import com.android.jack.jayce.v0002.nodes.NFieldInitializer;
import com.android.jack.jayce.v0002.nodes.NFieldRef;
import com.android.jack.jayce.v0002.nodes.NFloatLiteral;
import com.android.jack.jayce.v0002.nodes.NForStatement;
import com.android.jack.jayce.v0002.nodes.NGenericSignature;
import com.android.jack.jayce.v0002.nodes.NGoto;
import com.android.jack.jayce.v0002.nodes.NGtOperation;
import com.android.jack.jayce.v0002.nodes.NGteOperation;
import com.android.jack.jayce.v0002.nodes.NIfStatement;
import com.android.jack.jayce.v0002.nodes.NInstanceOf;
import com.android.jack.jayce.v0002.nodes.NIntLiteral;
import com.android.jack.jayce.v0002.nodes.NInterfaceType;
import com.android.jack.jayce.v0002.nodes.NLabeledStatement;
import com.android.jack.jayce.v0002.nodes.NLocal;
import com.android.jack.jayce.v0002.nodes.NLocalRef;
import com.android.jack.jayce.v0002.nodes.NLock;
import com.android.jack.jayce.v0002.nodes.NLongLiteral;
import com.android.jack.jayce.v0002.nodes.NLtOperation;
import com.android.jack.jayce.v0002.nodes.NLteOperation;
import com.android.jack.jayce.v0002.nodes.NMarker;
import com.android.jack.jayce.v0002.nodes.NMethod;
import com.android.jack.jayce.v0002.nodes.NMethodBody;
import com.android.jack.jayce.v0002.nodes.NMethodCall;
import com.android.jack.jayce.v0002.nodes.NMethodLiteral;
import com.android.jack.jayce.v0002.nodes.NModOperation;
import com.android.jack.jayce.v0002.nodes.NMulOperation;
import com.android.jack.jayce.v0002.nodes.NMultiExpression;
import com.android.jack.jayce.v0002.nodes.NNameValuePair;
import com.android.jack.jayce.v0002.nodes.NNativeMethodBody;
import com.android.jack.jayce.v0002.nodes.NNeqOperation;
import com.android.jack.jayce.v0002.nodes.NNewArray;
import com.android.jack.jayce.v0002.nodes.NNewInstance;
import com.android.jack.jayce.v0002.nodes.NNullLiteral;
import com.android.jack.jayce.v0002.nodes.NOrOperation;
import com.android.jack.jayce.v0002.nodes.NParameter;
import com.android.jack.jayce.v0002.nodes.NParameterRef;
import com.android.jack.jayce.v0002.nodes.NPostfixDecOperation;
import com.android.jack.jayce.v0002.nodes.NPostfixIncOperation;
import com.android.jack.jayce.v0002.nodes.NPrefixBitNotOperation;
import com.android.jack.jayce.v0002.nodes.NPrefixDecOperation;
import com.android.jack.jayce.v0002.nodes.NPrefixIncOperation;
import com.android.jack.jayce.v0002.nodes.NPrefixNegOperation;
import com.android.jack.jayce.v0002.nodes.NPrefixNotOperation;
import com.android.jack.jayce.v0002.nodes.NProgram;
import com.android.jack.jayce.v0002.nodes.NReinterpretCastOperation;
import com.android.jack.jayce.v0002.nodes.NReturnStatement;
import com.android.jack.jayce.v0002.nodes.NShlOperation;
import com.android.jack.jayce.v0002.nodes.NShortLiteral;
import com.android.jack.jayce.v0002.nodes.NShrOperation;
import com.android.jack.jayce.v0002.nodes.NShruOperation;
import com.android.jack.jayce.v0002.nodes.NSimpleName;
import com.android.jack.jayce.v0002.nodes.NStringLiteral;
import com.android.jack.jayce.v0002.nodes.NSubOperation;
import com.android.jack.jayce.v0002.nodes.NSwitchStatement;
import com.android.jack.jayce.v0002.nodes.NSynchronizedBlock;
import com.android.jack.jayce.v0002.nodes.NThisRef;
import com.android.jack.jayce.v0002.nodes.NThisRefTypeInfo;
import com.android.jack.jayce.v0002.nodes.NThrowStatement;
import com.android.jack.jayce.v0002.nodes.NThrownExceptionMarker;
import com.android.jack.jayce.v0002.nodes.NTryStatement;
import com.android.jack.jayce.v0002.nodes.NUnlock;
import com.android.jack.jayce.v0002.nodes.NWhileStatement;
import com.android.sched.marker.Marker;
import com.android.sched.marker.SerializableMarker;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Node factory.
 */
public class NodeFactory {

  private static class Creator extends JVisitor {

    @CheckForNull
    private NNode newNode;

    @Override
    public boolean visit(@Nonnull JAbsentArrayDimension x) {
      newNode = new NAbsentArrayDimension();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JAlloc x) {
      newNode = new NAlloc();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JAnnotationLiteral annotationLiteral) {
      newNode = new NAnnotationLiteral();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JArrayLength x) {
      newNode = new NArrayLength();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JArrayLiteral arrayLiteral) {
      newNode = new NArrayLiteral();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JArrayRef x) {
      newNode = new NArrayRef();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JAssertStatement jAssertStatement) {
      newNode = new NAssertStatement();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JBinaryOperation x) {
      if (x instanceof JAddOperation) {
        newNode = new NAddOperation();
      } else if (x instanceof JAndOperation) {
        newNode = new NAndOperation();
      } else if (x instanceof JAsgAddOperation) {
        newNode = new NAsgAddOperation();
      } else if (x instanceof JAsgBitAndOperation) {
        newNode = new NAsgBitAndOperation();
      } else if (x instanceof JAsgBitOrOperation) {
        newNode = new NAsgBitOrOperation();
      } else if (x instanceof JAsgBitXorOperation) {
        newNode = new NAsgBitXorOperation();
      } else if (x instanceof JAsgConcatOperation) {
        newNode = new NAsgConcatOperation();
      } else if (x instanceof JAsgDivOperation) {
        newNode = new NAsgDivOperation();
      } else if (x instanceof JAsgModOperation) {
        newNode = new NAsgModOperation();
      } else if (x instanceof JAsgMulOperation) {
        newNode = new NAsgMulOperation();
      } else if (x instanceof JAsgOperation) {
        newNode = new NAsgOperation();
      } else if (x instanceof JAsgShlOperation) {
        newNode = new NAsgShlOperation();
      } else if (x instanceof JAsgShrOperation) {
        newNode = new NAsgShrOperation();
      } else if (x instanceof JAsgShruOperation) {
        newNode = new NAsgShruOperation();
      } else if (x instanceof JAsgSubOperation) {
        newNode = new NAsgSubOperation();
      } else if (x instanceof JBitAndOperation) {
        newNode = new NBitAndOperation();
      } else if (x instanceof JBitOrOperation) {
        newNode = new NBitOrOperation();
      } else if (x instanceof JBitXorOperation) {
        newNode = new NBitXorOperation();
      } else if (x instanceof JConcatOperation) {
        newNode = new NConcatOperation();
      } else if (x instanceof JDivOperation) {
        newNode = new NDivOperation();
      } else if (x instanceof JEqOperation) {
        newNode = new NEqOperation();
      } else if (x instanceof JGteOperation) {
        newNode = new NGteOperation();
      } else if (x instanceof JGtOperation) {
        newNode = new NGtOperation();
      } else if (x instanceof JLteOperation) {
        newNode = new NLteOperation();
      } else if (x instanceof JLtOperation) {
        newNode = new NLtOperation();
      } else if (x instanceof JModOperation) {
        newNode = new NModOperation();
      } else if (x instanceof JMulOperation) {
        newNode = new NMulOperation();
      } else if (x instanceof JOrOperation) {
        newNode = new NOrOperation();
      } else if (x instanceof JNeqOperation) {
        newNode = new NNeqOperation();
      } else if (x instanceof JShlOperation) {
        newNode = new NShlOperation();
      } else if (x instanceof JShrOperation) {
        newNode = new NShrOperation();
      } else if (x instanceof JShruOperation) {
        newNode = new NShruOperation();
      } else if (x instanceof JSubOperation) {
        newNode = new NSubOperation();
      }
      return false;
    }

    @Override
    public boolean visit(@Nonnull JBlock jBlock) {
      newNode = new NBlock();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JBooleanLiteral x) {
      newNode = new NBooleanLiteral();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JBreakStatement x) {
      newNode = new NBreakStatement();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JByteLiteral x) {
      newNode = new NByteLiteral();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JCaseStatement x) {
      newNode = new NCaseStatement();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JDynamicCastOperation x) {
      newNode = new NDynamicCastOperation();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JReinterpretCastOperation x) {
      newNode = new NReinterpretCastOperation();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JCatchBlock x) {
      newNode = new NCatchBlock();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JCharLiteral x) {
      newNode = new NCharLiteral();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JClassLiteral x) {
      newNode = new NClassLiteral();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JDefinedClass x) {
      if (x instanceof JDefinedEnum) {
        newNode = new NEnumType();
        return false;
      }
      newNode = new NClassType();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JConditionalExpression x) {
      newNode = new NConditionalExpression();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JConstructor x) {
      newNode = new NConstructor();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JContinueStatement x) {
      newNode = new NContinueStatement();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JDoStatement doStatement) {
      newNode = new NDoStatement();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JDoubleLiteral x) {
      newNode = new NDoubleLiteral();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JEnumLiteral jEnumLiteral) {
      newNode = new NEnumLiteral();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JExceptionRuntimeValue jExceptionRuntime) {
      newNode = new NExceptionRuntimeValue();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JExpressionStatement jExpressionStatement) {
      newNode = new NExpressionStatement();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JField x) {
      if (x instanceof JEnumField) {
        newNode = new NEnumField();
        return false;
      }
      newNode = new NField();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JFieldInitializer x) {
      newNode = new NFieldInitializer();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JFieldRef x) {
      newNode = new NFieldRef();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JFloatLiteral x) {
      newNode = new NFloatLiteral();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JForStatement forStatement) {
      newNode = new NForStatement();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JGoto x) {
      newNode = new NGoto();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JIfStatement ifStatement) {
      newNode = new NIfStatement();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JInstanceOf x) {
      newNode = new NInstanceOf();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JDefinedInterface x) {
      if (x instanceof JDefinedAnnotation) {
        newNode = new NAnnotationType();
        return false;
      }
      newNode = new NInterfaceType();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JIntLiteral x) {
      newNode = new NIntLiteral();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JLabeledStatement x) {
      newNode = new NLabeledStatement();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JLocal x) {
      newNode = new NLocal();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JLocalRef x) {
      newNode = new NLocalRef();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JLock x) {
      newNode = new NLock();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JLongLiteral x) {
      newNode = new NLongLiteral();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JMethod method) {
      if (method instanceof JAnnotationMethod) {
        newNode = new NAnnotationMethod();
      } else {
        newNode = new NMethod();
      }
      return false;
    }

    @Override
    public boolean visit(@Nonnull JMethodCall x) {
      newNode = new NMethodCall();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JMethodLiteral x) {
      newNode = new NMethodLiteral();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JMethodBody methodBody) {
      newNode = new NMethodBody();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JMultiExpression x) {
      newNode = new NMultiExpression();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JNameValuePair nameValuePair) {
      newNode = new NNameValuePair();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JNewArray x) {
      newNode = new NNewArray();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JNewInstance x) {
      newNode = new NNewInstance();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JAbstractMethodBody jAbstractMethodBody) {
      if (jAbstractMethodBody instanceof JNativeMethodBody) {
        newNode = new NNativeMethodBody();
      }
      return false;
    }

    @Override
    public boolean visit(@Nonnull JNode x) {
      return false;
    }

    @Override
    public boolean visit(@Nonnull JNullLiteral x) {
      newNode = new NNullLiteral();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JParameter x) {
      newNode = new NParameter();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JParameterRef x) {
      newNode = new NParameterRef();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JSession x) {
      newNode = new NProgram();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JReturnStatement x) {
      newNode = new NReturnStatement();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JShortLiteral x) {
      newNode = new NShortLiteral();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JAbstractStringLiteral x) {
      newNode = new NStringLiteral();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JSwitchStatement x) {
      newNode = new NSwitchStatement();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JSynchronizedBlock x) {
      newNode = new NSynchronizedBlock();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JThisRef x) {
      newNode = new NThisRef();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JThrowStatement x) {
      newNode = new NThrowStatement();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JTryStatement x) {
      newNode = new NTryStatement();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JUnaryOperation x) {
      if (x instanceof JPostfixDecOperation) {
        newNode = new NPostfixDecOperation();
      } else if (x instanceof JPostfixIncOperation) {
        newNode = new NPostfixIncOperation();
      } else if (x instanceof JPrefixBitNotOperation) {
        newNode = new NPrefixBitNotOperation();
      } else if (x instanceof JPrefixDecOperation) {
        newNode = new NPrefixDecOperation();
      } else if (x instanceof JPrefixIncOperation) {
        newNode = new NPrefixIncOperation();
      } else if (x instanceof JPrefixNegOperation) {
        newNode = new NPrefixNegOperation();
      } else if (x instanceof JPrefixNotOperation) {
        newNode = new NPrefixNotOperation();
      }
      return false;
    }

    @Override
    public boolean visit(@Nonnull JUnlock x) {
      newNode = new NUnlock();
      return false;
    }

    @Override
    public boolean visit(@Nonnull JWhileStatement whileStatement) {
      newNode = new NWhileStatement();
      return false;
    }
  }

  public NodeFactory() {
  }

  @CheckForNull
  public NNode createNNode(@Nonnull Object from) {
    if (from instanceof JNode) {
      Creator creator = new Creator();
      ((JNode) from).traverse(creator);
      if (creator.newNode != null) {
        return creator.newNode;
      }
    } else if (from instanceof Marker) {
      return createMarkerNode((Marker) from);
    }
    throw new AssertionError("Not yet implemented (" + from.getClass() + ")");
  }

  @CheckForNull
  private NMarker createMarkerNode(@Nonnull Marker from) {
    NMarker nMarker = null;
    if (from instanceof GenericSignature) {
      nMarker = new NGenericSignature();
    } else if (from instanceof SimpleName) {
      nMarker = new NSimpleName();
    } else if (from instanceof ThisRefTypeInfo) {
      nMarker = new NThisRefTypeInfo();
    } else if (from instanceof ThrownExceptionMarker) {
      nMarker = new NThrownExceptionMarker();
    }
    // no NMarker if and only if the given Marker was not Jayce capable.
    assert (nMarker == null) == (!(from instanceof SerializableMarker)) : from.getClass();
    return nMarker;
  }

}
