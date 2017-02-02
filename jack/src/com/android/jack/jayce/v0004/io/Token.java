/*
* Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.jayce.v0004.io;

import com.android.jack.jayce.JayceFormatException;
import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.v0004.NNode;
import com.android.jack.jayce.v0004.nodes.NAbsentArrayDimension;
import com.android.jack.jayce.v0004.nodes.NAddOperation;
import com.android.jack.jayce.v0004.nodes.NAlloc;
import com.android.jack.jayce.v0004.nodes.NAndOperation;
import com.android.jack.jayce.v0004.nodes.NAnnotation;
import com.android.jack.jayce.v0004.nodes.NAnnotationMethod;
import com.android.jack.jayce.v0004.nodes.NAnnotationType;
import com.android.jack.jayce.v0004.nodes.NArrayLength;
import com.android.jack.jayce.v0004.nodes.NArrayLiteral;
import com.android.jack.jayce.v0004.nodes.NArrayRef;
import com.android.jack.jayce.v0004.nodes.NAsgAddOperation;
import com.android.jack.jayce.v0004.nodes.NAsgBitAndOperation;
import com.android.jack.jayce.v0004.nodes.NAsgBitOrOperation;
import com.android.jack.jayce.v0004.nodes.NAsgBitXorOperation;
import com.android.jack.jayce.v0004.nodes.NAsgConcatOperation;
import com.android.jack.jayce.v0004.nodes.NAsgDivOperation;
import com.android.jack.jayce.v0004.nodes.NAsgModOperation;
import com.android.jack.jayce.v0004.nodes.NAsgMulOperation;
import com.android.jack.jayce.v0004.nodes.NAsgOperation;
import com.android.jack.jayce.v0004.nodes.NAsgShlOperation;
import com.android.jack.jayce.v0004.nodes.NAsgShrOperation;
import com.android.jack.jayce.v0004.nodes.NAsgShruOperation;
import com.android.jack.jayce.v0004.nodes.NAsgSubOperation;
import com.android.jack.jayce.v0004.nodes.NAssertStatement;
import com.android.jack.jayce.v0004.nodes.NBitAndOperation;
import com.android.jack.jayce.v0004.nodes.NBitOrOperation;
import com.android.jack.jayce.v0004.nodes.NBitXorOperation;
import com.android.jack.jayce.v0004.nodes.NBlock;
import com.android.jack.jayce.v0004.nodes.NBooleanLiteral;
import com.android.jack.jayce.v0004.nodes.NBreakStatement;
import com.android.jack.jayce.v0004.nodes.NByteLiteral;
import com.android.jack.jayce.v0004.nodes.NCaseStatement;
import com.android.jack.jayce.v0004.nodes.NCatchBlock;
import com.android.jack.jayce.v0004.nodes.NCharLiteral;
import com.android.jack.jayce.v0004.nodes.NClassLiteral;
import com.android.jack.jayce.v0004.nodes.NClassType;
import com.android.jack.jayce.v0004.nodes.NConcatOperation;
import com.android.jack.jayce.v0004.nodes.NConditionalExpression;
import com.android.jack.jayce.v0004.nodes.NConstructor;
import com.android.jack.jayce.v0004.nodes.NContainerAnnotation;
import com.android.jack.jayce.v0004.nodes.NContinueStatement;
import com.android.jack.jayce.v0004.nodes.NDebugVariableInfo;
import com.android.jack.jayce.v0004.nodes.NDivOperation;
import com.android.jack.jayce.v0004.nodes.NDoStatement;
import com.android.jack.jayce.v0004.nodes.NDoubleLiteral;
import com.android.jack.jayce.v0004.nodes.NDynamicCastOperation;
import com.android.jack.jayce.v0004.nodes.NEnumField;
import com.android.jack.jayce.v0004.nodes.NEnumLiteral;
import com.android.jack.jayce.v0004.nodes.NEnumType;
import com.android.jack.jayce.v0004.nodes.NEqOperation;
import com.android.jack.jayce.v0004.nodes.NExceptionRuntimeValue;
import com.android.jack.jayce.v0004.nodes.NExpressionStatement;
import com.android.jack.jayce.v0004.nodes.NField;
import com.android.jack.jayce.v0004.nodes.NFieldInitializer;
import com.android.jack.jayce.v0004.nodes.NFieldRef;
import com.android.jack.jayce.v0004.nodes.NFloatLiteral;
import com.android.jack.jayce.v0004.nodes.NForStatement;
import com.android.jack.jayce.v0004.nodes.NGenericSignature;
import com.android.jack.jayce.v0004.nodes.NGoto;
import com.android.jack.jayce.v0004.nodes.NGtOperation;
import com.android.jack.jayce.v0004.nodes.NGteOperation;
import com.android.jack.jayce.v0004.nodes.NIfStatement;
import com.android.jack.jayce.v0004.nodes.NInstanceOf;
import com.android.jack.jayce.v0004.nodes.NIntLiteral;
import com.android.jack.jayce.v0004.nodes.NInterfaceType;
import com.android.jack.jayce.v0004.nodes.NLabeledStatement;
import com.android.jack.jayce.v0004.nodes.NLambda;
import com.android.jack.jayce.v0004.nodes.NLambdaFromJill;
import com.android.jack.jayce.v0004.nodes.NLocal;
import com.android.jack.jayce.v0004.nodes.NLocalRef;
import com.android.jack.jayce.v0004.nodes.NLock;
import com.android.jack.jayce.v0004.nodes.NLongLiteral;
import com.android.jack.jayce.v0004.nodes.NLtOperation;
import com.android.jack.jayce.v0004.nodes.NLteOperation;
import com.android.jack.jayce.v0004.nodes.NMethod;
import com.android.jack.jayce.v0004.nodes.NMethodBody;
import com.android.jack.jayce.v0004.nodes.NMethodCall;
import com.android.jack.jayce.v0004.nodes.NMethodId;
import com.android.jack.jayce.v0004.nodes.NMethodLiteral;
import com.android.jack.jayce.v0004.nodes.NModOperation;
import com.android.jack.jayce.v0004.nodes.NMulOperation;
import com.android.jack.jayce.v0004.nodes.NMultiExpression;
import com.android.jack.jayce.v0004.nodes.NNameValuePair;
import com.android.jack.jayce.v0004.nodes.NNativeMethodBody;
import com.android.jack.jayce.v0004.nodes.NNeqOperation;
import com.android.jack.jayce.v0004.nodes.NNewArray;
import com.android.jack.jayce.v0004.nodes.NNewInstance;
import com.android.jack.jayce.v0004.nodes.NNullLiteral;
import com.android.jack.jayce.v0004.nodes.NOrOperation;
import com.android.jack.jayce.v0004.nodes.NOriginDigest;
import com.android.jack.jayce.v0004.nodes.NParameter;
import com.android.jack.jayce.v0004.nodes.NParameterRef;
import com.android.jack.jayce.v0004.nodes.NPolymorphicCall;
import com.android.jack.jayce.v0004.nodes.NPostfixDecOperation;
import com.android.jack.jayce.v0004.nodes.NPostfixIncOperation;
import com.android.jack.jayce.v0004.nodes.NPrefixBitNotOperation;
import com.android.jack.jayce.v0004.nodes.NPrefixDecOperation;
import com.android.jack.jayce.v0004.nodes.NPrefixIncOperation;
import com.android.jack.jayce.v0004.nodes.NPrefixNegOperation;
import com.android.jack.jayce.v0004.nodes.NPrefixNotOperation;
import com.android.jack.jayce.v0004.nodes.NReinterpretCastOperation;
import com.android.jack.jayce.v0004.nodes.NReturnStatement;
import com.android.jack.jayce.v0004.nodes.NShlOperation;
import com.android.jack.jayce.v0004.nodes.NShortLiteral;
import com.android.jack.jayce.v0004.nodes.NShrOperation;
import com.android.jack.jayce.v0004.nodes.NShruOperation;
import com.android.jack.jayce.v0004.nodes.NSimpleName;
import com.android.jack.jayce.v0004.nodes.NStringLiteral;
import com.android.jack.jayce.v0004.nodes.NSubOperation;
import com.android.jack.jayce.v0004.nodes.NSwitchStatement;
import com.android.jack.jayce.v0004.nodes.NSynchronizedBlock;
import com.android.jack.jayce.v0004.nodes.NThisRef;
import com.android.jack.jayce.v0004.nodes.NThisRefTypeInfo;
import com.android.jack.jayce.v0004.nodes.NThrowStatement;
import com.android.jack.jayce.v0004.nodes.NThrownExceptionMarker;
import com.android.jack.jayce.v0004.nodes.NTryStatement;
import com.android.jack.jayce.v0004.nodes.NUnlock;
import com.android.jack.jayce.v0004.nodes.NWhileStatement;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Tokens.
 */
public enum Token {

  EOF(null),

  DOUBLE_QUOTE("\""),

  LPARENTHESIS("("),
  RPARENTHESIS(")"),
  SHARP("#"),
  LBRACKET("["),
  RBRACKET("]"),
  LCURLY_ADD("{+"),
  LCURLY_REMOVE("{-"),
  RCURLY("}"),
  NULL("null"),

  NUMBER_VALUE(null),
  STRING_VALUE(null),

  ABSENT_ARRAY_DIMENSION("absent-array-dimension") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAbsentArrayDimension();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAbsentArrayDimension.skipContent(reader);
    }
  },
  ADD_OPERATION("+") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAddOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAddOperation.skipContent(reader);
    }
  },
  ALLOC("alloc") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAlloc();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAlloc.skipContent(reader);
    }
  },
  AND_OPERATION("&&") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAndOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAndOperation.skipContent(reader);
    }
  },
  ANNOTATION("annotation-literal", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NAnnotation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAnnotation.skipContent(reader);
    }
  },
  ANNOTATION_METHOD("annotation-method", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NAnnotationMethod();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAnnotationMethod.skipContent(reader);
    }
  },
  ANNOTATION_TYPE("annotation", NodeLevel.TYPES) {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAnnotationType();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAnnotationType.skipContent(reader);
    }
  },
  ARRAY_LENGTH("array-length") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NArrayLength();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NArrayLength.skipContent(reader);
    }
  },
  ARRAY_LITERAL("array-literal", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NArrayLiteral();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NArrayLiteral.skipContent(reader);
    }
  },
  ARRAY_REF("array-ref") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NArrayRef();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NArrayRef.skipContent(reader);
    }
  },
  ASG_ADD_OPERATION("+=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgAddOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAsgAddOperation.skipContent(reader);
    }
  },
  ASG_BIT_AND_OPERATION("&=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgBitAndOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAsgBitAndOperation.skipContent(reader);
    }
  },
  ASG_BIT_OR_OPERATION("|=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgBitOrOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAsgBitOrOperation.skipContent(reader);
    }
  },
  ASG_BIT_XOR_OPERATION("^=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgBitXorOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAsgBitXorOperation.skipContent(reader);
    }
  },
  ASG_CONCAT_OPERATION("asg-concat") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgConcatOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAsgConcatOperation.skipContent(reader);
    }
  },
  ASG_DIV_OPERATION("/=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgDivOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAsgDivOperation.skipContent(reader);
    }
  },
  ASG_MOD_OPERATION("%=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgModOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAsgModOperation.skipContent(reader);
    }
  },
  ASG_MUL_OPERATION("*=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgMulOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAsgMulOperation.skipContent(reader);
    }
  },
  ASG_OPERATION("=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAsgOperation.skipContent(reader);
    }
  },
  ASG_SHL_OPERATION("<<=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgShlOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAsgShlOperation.skipContent(reader);
    }
  },
  ASG_SHR_OPERATION(">>=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgShrOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAsgShrOperation.skipContent(reader);
    }
  },
  ASG_SHRU_OPERATION(">>>=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgShruOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAsgShruOperation.skipContent(reader);
    }
  },
  ASG_SUB_OPERATION("-=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgSubOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAsgSubOperation.skipContent(reader);
    }
  },
  ASSERT_STATEMENT("assert") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAssertStatement();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NAssertStatement.skipContent(reader);
    }
  },
  BIT_AND_OPERATION("&") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NBitAndOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NBitAndOperation.skipContent(reader);
    }
  },
  BIT_OR_OPERATION("|") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NBitOrOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NBitOrOperation.skipContent(reader);
    }
  },
  BIT_XOR_OPERATION("^") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NBitXorOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NBitXorOperation.skipContent(reader);
    }
  },
  BLOCK("block") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NBlock();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NBlock.skipContent(reader);
    }
  },
  BOOLEAN_LITERAL("boolean", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NBooleanLiteral();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NBooleanLiteral.skipContent(reader);
    }
  },
  BREAK_STATEMENT("break") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NBreakStatement();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NBreakStatement.skipContent(reader);
    }
  },
  BYTE_LITERAL("byte", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NByteLiteral();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NByteLiteral.skipContent(reader);
    }
  },
  CASE_STATEMENT("case") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NCaseStatement();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NCaseStatement.skipContent(reader);
    }
  },
  CATCH_BLOCK("catch") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NCatchBlock();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NCatchBlock.skipContent(reader);
    }
  },
  CHAR_LITERAL("char", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NCharLiteral();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NCharLiteral.skipContent(reader);
    }
  },
  CLASS("class", NodeLevel.TYPES) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NClassType();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NClassType.skipContent(reader);
    }
  },
  CLASS_LITERAL("class-literal", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NClassLiteral();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NClassLiteral.skipContent(reader);
    }
  },
  CONCAT_OPERATION("concat") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NConcatOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NConcatOperation.skipContent(reader);
    }
  },
  CONDITIONAL_EXPRESSION ("?") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NConditionalExpression();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NConditionalExpression.skipContent(reader);
    }
  },
  CONSTRUCTOR ("constructor", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NConstructor();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NConstructor.skipContent(reader);
    }
  },
  CONTAINER_ANNOTATION("container-annotation", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NContainerAnnotation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NContainerAnnotation.skipContent(reader);
    }
  },
  CONTINUE_STATEMENT("continue") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NContinueStatement();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NContinueStatement.skipContent(reader);
    }
  },
  DEBUG_VARIABLE_INFORMATION("debug-var", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NDebugVariableInfo();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NDebugVariableInfo.skipContent(reader);
    }
  },
  DIV_OPERATION("/") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NDivOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NDivOperation.skipContent(reader);
    }
  },
  DO_STATEMENT("do") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NDoStatement();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NDoStatement.skipContent(reader);
    }
  },
  DOUBLE_LITERAL("double", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NDoubleLiteral();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NDoubleLiteral.skipContent(reader);
    }
  },
  DYNAMIC_CAST_OPERATION("cast") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NDynamicCastOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NDynamicCastOperation.skipContent(reader);
    }
  },
  ENUM("enum", NodeLevel.TYPES) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NEnumType();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NEnumType.skipContent(reader);
    }
  },
  ENUM_FIELD("enum-field", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NEnumField();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NEnumType.skipContent(reader);
    }
  },
  ENUM_LITERAL("enum-literal", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NEnumLiteral();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NEnumLiteral.skipContent(reader);
    }
  },
  EQ_OPERATION("==") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NEqOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NEqOperation.skipContent(reader);
    }
  },
  EXCEPTION_RUNTIME_VALUE("ex-runtime-value") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NExceptionRuntimeValue();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NExceptionRuntimeValue.skipContent(reader);
    }
  },
  EXPRESSION_STATEMENT("expression-statement") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NExpressionStatement();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NExpressionStatement.skipContent(reader);
    }
  },
  FIELD("field", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NField();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NField.skipContent(reader);
    }
  },
  FIELD_INITIALIZER("field-intializer") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NFieldInitializer();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) {
      NFieldInitializer.skipContent(reader);
    }
  },
  FIELD_REF("field-ref") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NFieldRef();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NFieldRef.skipContent(reader);
    }
  },
  FLOAT_LITERAL("float", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NFloatLiteral();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NFloatLiteral.skipContent(reader);
    }
  },
  FOR_STATEMENT("for") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NForStatement();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NForStatement.skipContent(reader);
    }
  },
  GENERIC_SIGNATURE("generic-signature", NodeLevel.TYPES) {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NGenericSignature();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NGenericSignature.skipContent(reader);
    }
  },
  GOTO("goto") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NGoto();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NGoto.skipContent(reader);
    }
  },
  GTE_OPERATION(">=") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NGteOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NGteOperation.skipContent(reader);
    }
  },
  GT_OPERATION(">") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NGtOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NGtOperation.skipContent(reader);
    }
  },
  IF_STATEMENT("if") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NIfStatement();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NIfStatement.skipContent(reader);
    }
  },
  INSTANCE_OF("instanceof") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NInstanceOf();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NInstanceOf.skipContent(reader);
    }
  },
  INT_LITERAL("int", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NIntLiteral();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NIntLiteral.skipContent(reader);
    }
  },
  INTERFACE("interface", NodeLevel.TYPES) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NInterfaceType();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NInterfaceType.skipContent(reader);
    }
  },
  LABELED_STATEMENT("label") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NLabeledStatement();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NLabeledStatement.skipContent(reader);
    }
  },
  LAMBDA("lambda") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NLambda();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NLambda.skipContent(reader);
    }
  },
  LAMBDA_FROM_JILL("lambda-from-jill") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NLambdaFromJill();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) {
      NLambdaFromJill.skipContent(reader);
    }
  },
  LOCAL("local") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NLocal();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NLocal.skipContent(reader);
    }
  },
  LOCAL_REF("local-ref") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NLocalRef();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NLocalRef.skipContent(reader);
    }
  },
  LOCK("lock") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NLock();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NLock.skipContent(reader);
    }
  },
  LONG_LITERAL("long", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NLongLiteral();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NLongLiteral.skipContent(reader);
    }
  },
  LTE_OPERATION("<=") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NLteOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NLteOperation.skipContent(reader);
    }
  },
  LT_OPERATION("<") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NLtOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NLtOperation.skipContent(reader);
    }
  },
  METHOD("method", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NMethod();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NMethod.skipContent(reader);
    }
  },
  METHOD_BODY("body") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NMethodBody();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NMethodBody.skipContent(reader);
    }
  },
  METHOD_CALL("call") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NMethodCall();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NMethodCall.skipContent(reader);
    }
  },
  METHODID_WITH_RETURN_TYPE("method-id-with-return-type", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NMethodId();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NMethodId.skipContent(reader);
    }
  },
  METHOD_LITERAL("method-literal", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NMethodLiteral();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NMethodLiteral.skipContent(reader);
    }
  },
  MOD_OPERATION("%") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NModOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NModOperation.skipContent(reader);
    }
  },
  MUL_OPERATION("*") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NMulOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NMulOperation.skipContent(reader);
    }
  },
  MULTI_EXPRESSION("multi-expression") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NMultiExpression();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NMultiExpression.skipContent(reader);
    }
  },
  NAME_VALUE_PAIR("name-value-pair", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NNameValuePair();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NNameValuePair.skipContent(reader);
    }
  },
  NEQ_OPERATION("!=") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NNeqOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NNeqOperation.skipContent(reader);
    }
  },
  NATIVE_METHOD_BODY("native-body") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NNativeMethodBody();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) {
      NNativeMethodBody.skipContent(reader);
    }
  },
  NEW_ARRAY("new-array") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NNewArray();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NNewArray.skipContent(reader);
    }
  },
  NEW_INSTANCE("new") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NNewInstance();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NNewInstance.skipContent(reader);
    }
  },
  NULL_LITERAL("null-literal", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NNullLiteral();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) {
      NNullLiteral.skipContent(reader);
    }
  },
  OR_OPERATION("||") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NOrOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NOrOperation.skipContent(reader);
    }
  },
  ORIGIN_DIGEST("origin-digest", NodeLevel.STRUCTURE) {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NOriginDigest();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NOriginDigest.skipContent(reader);
    }
  },
  PARAMETER("parameter", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NParameter();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NParameter.skipContent(reader);
    }
  },
  PARAMETER_REF("parameter-ref") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NParameterRef();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NParameterRef.skipContent(reader);
    }
  },
  POLYMORPHIC_CALL("polymorphic-call") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NPolymorphicCall();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NPolymorphicCall.skipContent(reader);
    }
  },
  POSTFIX_DEC_OPERATION("postfix-dec") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NPostfixDecOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NPostfixDecOperation.skipContent(reader);
    }
  },
  POSTFIX_INC_OPERATION("postfix-inc") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NPostfixIncOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NPostfixIncOperation.skipContent(reader);
    }
  },
  PREFIX_BIT_NOT_OPERATION("~") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NPrefixBitNotOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NPrefixBitNotOperation.skipContent(reader);
    }
  },
  PREFIX_DEC_OPERATION("prefix-dec") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NPrefixDecOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NPrefixDecOperation.skipContent(reader);
    }
  },
  PREFIX_INC_OPERATION("prefix-inc") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NPrefixIncOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NPrefixIncOperation.skipContent(reader);
    }
  },
  PREFIX_NEG_OPERATION("neg") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NPrefixNegOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NPrefixNegOperation.skipContent(reader);
    }
  },
  PREFIX_NOT_OPERATION("not") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NPrefixNotOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NPrefixNotOperation.skipContent(reader);
    }
  },
  RESERVED("reserved") {
    @Nonnull
    @Override
    public NNode newNode() {
      throw new JayceFormatException("Unexpected node '" + toString() + "'");
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) {
      throw new JayceFormatException("Unexpected node '" + toString() + "'");
    }
  },
  REINTERPRETCAST_OPERATION("reinterpret-cast") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NReinterpretCastOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NReinterpretCastOperation.skipContent(reader);
    }
  },
  RETURN_STATEMENT("return") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NReturnStatement();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NReturnStatement.skipContent(reader);
    }
  },
  SHL_OPERATION("<<") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NShlOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NShlOperation.skipContent(reader);
    }
  },
  SHORT_LITERAL("short", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NShortLiteral();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NShortLiteral.skipContent(reader);
    }
  },
  SHR_OPERATION(">>") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NShrOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NShrOperation.skipContent(reader);
    }
  },
  SHRU_OPERATION(">>>") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NShruOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NShruOperation.skipContent(reader);
    }
  },
  SIMPLE_NAME("simple-name", NodeLevel.TYPES) {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NSimpleName();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NSimpleName.skipContent(reader);
    }
  },
  SUB_OPERATION("-") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NSubOperation();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NSubOperation.skipContent(reader);
    }
  },
  STRING_LITERAL("string", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NStringLiteral();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NStringLiteral.skipContent(reader);
    }

  },
  SWITCH_STATEMENT("switch") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NSwitchStatement();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NSwitchStatement.skipContent(reader);
    }
  },
  SYNCHRONIZED_BLOCK("synchronized-block") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NSynchronizedBlock();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NSynchronizedBlock.skipContent(reader);
    }
  },
  THIS_REF("this") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NThisRef();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NThisRef.skipContent(reader);
    }
  },
  THIS_REF_TYPE_INFO("this-type-info") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NThisRefTypeInfo();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NThisRefTypeInfo.skipContent(reader);
    }
  },
  THROW_STATEMENT("throw") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NThrowStatement();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NThrowStatement.skipContent(reader);
    }
  },
  THROWN_EXCEPTION("thrown-exception", NodeLevel.STRUCTURE) {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NThrownExceptionMarker();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NThrownExceptionMarker.skipContent(reader);
    }
  },
  TRY_STATEMENT("try") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NTryStatement();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NTryStatement.skipContent(reader);
    }
  },
  UNLOCK("unlock") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NUnlock();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NUnlock.skipContent(reader);
    }
  },
  WHILE_STATEMENT("while") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NWhileStatement();
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NWhileStatement.skipContent(reader);
    }
  },
  ;

  @Nonnull
  private final NodeLevel nodeLevel;

  @CheckForNull
  private String label;

  private Token(@CheckForNull String text) {
    this(text, NodeLevel.FULL);
  }
  private Token(@CheckForNull String text, NodeLevel nodeLevel) {
    this.label = text;
    this.nodeLevel = nodeLevel;
  }

  public int getId() {
    return ordinal();
  }

  @CheckForNull
  public String getText() {
    return label;
  }

  @Nonnull
  public NNode newNode() throws InvalidTokenException {
    throw new InvalidTokenException("No node coresponding to token " + this.toString());
  }

  @SuppressWarnings("unused")
  public void skip(@Nonnull JayceInternalReaderImpl reader)
      throws InvalidTokenException, IOException {
    throw new InvalidTokenException("No node coresponding to token " + this.toString());
  }

@Nonnull
  public NodeLevel getNodeLevel() {
    return nodeLevel;
  }
}
