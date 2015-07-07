/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.ir;

import com.android.jack.backend.dex.annotations.AnnotationMethodDefaultValue;
import com.android.jack.ir.ast.JAbsentArrayDimension;
import com.android.jack.ir.ast.JAddOperation;
import com.android.jack.ir.ast.JAndOperation;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JArrayLength;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBitAndOperation;
import com.android.jack.ir.ast.JBitOrOperation;
import com.android.jack.ir.ast.JBitXorOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JCharLiteral;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedAnnotationType;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JDivOperation;
import com.android.jack.ir.ast.JDoubleLiteral;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JEnumField;
import com.android.jack.ir.ast.JEnumLiteral;
import com.android.jack.ir.ast.JEqOperation;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JFloatLiteral;
import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JGtOperation;
import com.android.jack.ir.ast.JGteOperation;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLabel;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JLtOperation;
import com.android.jack.ir.ast.JLteOperation;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JModOperation;
import com.android.jack.ir.ast.JMulOperation;
import com.android.jack.ir.ast.JMultiExpression;
import com.android.jack.ir.ast.JNativeMethodBody;
import com.android.jack.ir.ast.JNeqOperation;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JNullType;
import com.android.jack.ir.ast.JOrOperation;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPhantomAnnotationType;
import com.android.jack.ir.ast.JPhantomClass;
import com.android.jack.ir.ast.JPhantomClassOrInterface;
import com.android.jack.ir.ast.JPhantomEnum;
import com.android.jack.ir.ast.JPhantomInterface;
import com.android.jack.ir.ast.JPrefixBitNotOperation;
import com.android.jack.ir.ast.JPrefixNegOperation;
import com.android.jack.ir.ast.JPrefixNotOperation;
import com.android.jack.ir.ast.JPrimitiveType.JBooleanType;
import com.android.jack.ir.ast.JPrimitiveType.JByteType;
import com.android.jack.ir.ast.JPrimitiveType.JCharType;
import com.android.jack.ir.ast.JPrimitiveType.JDoubleType;
import com.android.jack.ir.ast.JPrimitiveType.JFloatType;
import com.android.jack.ir.ast.JPrimitiveType.JIntType;
import com.android.jack.ir.ast.JPrimitiveType.JLongType;
import com.android.jack.ir.ast.JPrimitiveType.JShortType;
import com.android.jack.ir.ast.JPrimitiveType.JVoidType;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JShlOperation;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JShrOperation;
import com.android.jack.ir.ast.JShruOperation;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.ir.ast.JSubOperation;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JSynchronizedBlock;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.marker.GenericSignature;
import com.android.jack.ir.ast.marker.SimpleName;
import com.android.jack.ir.ast.marker.ThisRefTypeInfo;
import com.android.jack.optimizations.NotSimplifier;
import com.android.jack.transformations.ast.BooleanTestOutsideIf;
import com.android.jack.transformations.ast.ImplicitBoxingAndUnboxing;
import com.android.jack.transformations.ast.ImplicitCast;
import com.android.jack.transformations.ast.InitInNewArray;
import com.android.jack.transformations.ast.JPrimitiveClassLiteral;
import com.android.jack.transformations.ast.MultiDimensionNewArray;
import com.android.jack.transformations.ast.NoImplicitBlock;
import com.android.jack.transformations.ast.inner.InnerAccessor;
import com.android.jack.transformations.ast.switches.UselessSwitches;
import com.android.sched.item.AbstractComponent;
import com.android.sched.item.ComposedOf;
import com.android.sched.item.Description;
import com.android.sched.item.Name;

/**
 * Tag containing all JNodes, tags or markers that represent the Jack format.
 */
@Name("Jack Format IR")
@Description("All JNodes, tags or markers that represent the Jack format.")
@ComposedOf({AnnotationMethodDefaultValue.class,
    BooleanTestOutsideIf.class,
    ImplicitBoxingAndUnboxing.class,
    ImplicitCast.class,
    InitInNewArray.class,
    InnerAccessor.class,
    JAbsentArrayDimension.class,
    JAddOperation.class,
    JAndOperation.class,
    JAnnotation.class,
    JArrayLength.class,
    JArrayRef.class,
    JArrayType.class,
    JArrayType.class,
    JAsgOperation.class,
    JBitAndOperation.class,
    JBitOrOperation.class,
    JBitXorOperation.class,
    JBlock.class,
    JBooleanLiteral.class,
    JBooleanType.class,
    JByteLiteral.class,
    JByteType.class,
    JCaseStatement.class,
    JCatchBlock.class,
    JCharLiteral.class,
    JCharType.class,
    JClassLiteral.class,
    JConditionalExpression.class,
    JConstructor.class,
    JDefinedAnnotationType.class,
    JDefinedClass.class,
    JDefinedEnum.class,
    JDefinedInterface.class,
    JDivOperation.class,
    JDoubleLiteral.class,
    JDoubleType.class,
    JDynamicCastOperation.class,
    JEnumField.class,
    JEnumLiteral.class,
    JEqOperation.class,
    JExpressionStatement.class,
    JField.class,
    JFieldRef.class,
    JFloatLiteral.class,
    JFloatType.class,
    JGoto.class,
    JGteOperation.class,
    JGtOperation.class,
    JIfStatement.class,
    JInstanceOf.class,
    JIntLiteral.class,
    JIntType.class,
    JLabel.class,
    JLabeledStatement.class,
    JLambda.class,
    JLocal.class,
    JLocalRef.class,
    JLongLiteral.class,
    JLongType.class,
    JLteOperation.class,
    JLtOperation.class,
    JMethod.class,
    JMethodBody.class,
    JMethodCall.class,
    JModOperation.class,
    JMulOperation.class,
    JMultiExpression.class,
    JNativeMethodBody.class,
    JNeqOperation.class,
    JNewArray.class,
    JNewInstance.class,
    JNullLiteral.class,
    JNullType.class,
    JOrOperation.class,
    JParameter.class,
    JParameterRef.class,
    JPhantomAnnotationType.class,
    JPhantomClass.class,
    JPhantomClassOrInterface.class,
    JPhantomEnum.class,
    JPhantomInterface.class,
    JPrefixBitNotOperation.class,
    JPrefixNegOperation.class,
    JPrefixNotOperation.class,
    JPrimitiveClassLiteral.class,
    JSession.class,
    JReturnStatement.class,
    JShlOperation.class,
    JShortLiteral.class,
    JShortType.class,
    JShrOperation.class,
    JShruOperation.class,
    JStringLiteral.class,
    JSubOperation.class,
    JSwitchStatement.class,
    JSynchronizedBlock.class,
    JThisRef.class,
    JThrowStatement.class,
    JTryStatement.class,
    JTryStatement.FinallyBlock.class,
    JVoidType.class,
    MultiDimensionNewArray.class,
    NoImplicitBlock.class,
    NotSimplifier.NotExpressionsSimplified.class,
    GenericSignature.class,
    SimpleName.class,
    ThisRefTypeInfo.class,
    UselessSwitches.class})
public class JackFormatIr implements AbstractComponent {
}
