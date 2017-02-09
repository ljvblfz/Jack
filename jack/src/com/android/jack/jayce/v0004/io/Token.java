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

import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.v0004.NNode;
import com.android.jack.jayce.v0004.nodes.HasSourceInfo;
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
    @Nonnull
    public Class<NAbsentArrayDimension> getNNodeClass() {
      return NAbsentArrayDimension.class;
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
    @Nonnull
    public Class<NAddOperation> getNNodeClass() {
      return NAddOperation.class;
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
    @Nonnull
    public Class<NAlloc> getNNodeClass() {
      return NAlloc.class;
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
    @Nonnull
    public Class<NAndOperation> getNNodeClass() {
      return NAndOperation.class;
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
    @Nonnull
    public Class<NAnnotation> getNNodeClass() {
      return NAnnotation.class;
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
    @Nonnull
    public Class<NAnnotationMethod> getNNodeClass() {
      return NAnnotationMethod.class;
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
    @Nonnull
    public Class<NAnnotationType> getNNodeClass() {
      return NAnnotationType.class;
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
    @Nonnull
    public Class<NArrayLength> getNNodeClass() {
      return NArrayLength.class;
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
    @Nonnull
    public Class<NArrayLiteral> getNNodeClass() {
      return NArrayLiteral.class;
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
    @Nonnull
    public Class<NArrayRef> getNNodeClass() {
      return NArrayRef.class;
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
    @Nonnull
    public Class<NAsgAddOperation> getNNodeClass() {
      return NAsgAddOperation.class;
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
    @Nonnull
    public Class<NAsgBitAndOperation> getNNodeClass() {
      return NAsgBitAndOperation.class;
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
    @Nonnull
    public Class<NAsgBitOrOperation> getNNodeClass() {
      return NAsgBitOrOperation.class;
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
    @Nonnull
    public Class<NAsgBitXorOperation> getNNodeClass() {
      return NAsgBitXorOperation.class;
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
    @Nonnull
    public Class<NAsgConcatOperation> getNNodeClass() {
      return NAsgConcatOperation.class;
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
    @Nonnull
    public Class<NAsgDivOperation> getNNodeClass() {
      return NAsgDivOperation.class;
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
    @Nonnull
    public Class<NAsgModOperation> getNNodeClass() {
      return NAsgModOperation.class;
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
    @Nonnull
    public Class<NAsgMulOperation> getNNodeClass() {
      return NAsgMulOperation.class;
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
    @Nonnull
    public Class<NAsgOperation> getNNodeClass() {
      return NAsgOperation.class;
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
    @Nonnull
    public Class<NAsgShlOperation> getNNodeClass() {
      return NAsgShlOperation.class;
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
    @Nonnull
    public Class<NAsgShrOperation> getNNodeClass() {
      return NAsgShrOperation.class;
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
    @Nonnull
    public Class<NAsgShruOperation> getNNodeClass() {
      return NAsgShruOperation.class;
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
    @Nonnull
    public Class<NAsgSubOperation> getNNodeClass() {
      return NAsgSubOperation.class;
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
    @Nonnull
    public Class<NAssertStatement> getNNodeClass() {
      return NAssertStatement.class;
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
    @Nonnull
    public Class<NBitAndOperation> getNNodeClass() {
      return NBitAndOperation.class;
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
    @Nonnull
    public Class<NBitOrOperation> getNNodeClass() {
      return NBitOrOperation.class;
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
    @Nonnull
    public Class<NBitXorOperation> getNNodeClass() {
      return NBitXorOperation.class;
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
    @Nonnull
    public Class<NBlock> getNNodeClass() {
      return NBlock.class;
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
    @Nonnull
    public Class<NBooleanLiteral> getNNodeClass() {
      return NBooleanLiteral.class;
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
    @Nonnull
    public Class<NBreakStatement> getNNodeClass() {
      return NBreakStatement.class;
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
    @Nonnull
    public Class<NByteLiteral> getNNodeClass() {
      return NByteLiteral.class;
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
    @Nonnull
    public Class<NCaseStatement> getNNodeClass() {
      return NCaseStatement.class;
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
    @Nonnull
    public Class<NCatchBlock> getNNodeClass() {
      return NCatchBlock.class;
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
    @Nonnull
    public Class<NCharLiteral> getNNodeClass() {
      return NCharLiteral.class;
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
    @Nonnull
    public Class<NClassType> getNNodeClass() {
      return NClassType.class;
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
    @Nonnull
    public Class<NClassLiteral> getNNodeClass() {
      return NClassLiteral.class;
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
    @Nonnull
    public Class<NConcatOperation> getNNodeClass() {
      return NConcatOperation.class;
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
    @Nonnull
    public Class<NConditionalExpression> getNNodeClass() {
      return NConditionalExpression.class;
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
    @Nonnull
    public Class<NConstructor> getNNodeClass() {
      return NConstructor.class;
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
    @Nonnull
    public Class<NContainerAnnotation> getNNodeClass() {
      return NContainerAnnotation.class;
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
    @Nonnull
    public Class<NContinueStatement> getNNodeClass() {
      return NContinueStatement.class;
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
    @Nonnull
    public Class<NDebugVariableInfo> getNNodeClass() {
      return NDebugVariableInfo.class;
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
    @Nonnull
    public Class<NDivOperation> getNNodeClass() {
      return NDivOperation.class;
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
    @Nonnull
    public Class<NDoStatement> getNNodeClass() {
      return NDoStatement.class;
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
    @Nonnull
    public Class<NDoubleLiteral> getNNodeClass() {
      return NDoubleLiteral.class;
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
    @Nonnull
    public Class<NDynamicCastOperation> getNNodeClass() {
      return NDynamicCastOperation.class;
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
    @Nonnull
    public Class<NEnumType> getNNodeClass() {
      return NEnumType.class;
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
    @Nonnull
    public Class<NEnumField> getNNodeClass() {
      return NEnumField.class;
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
    @Nonnull
    public Class<NEnumLiteral> getNNodeClass() {
      return NEnumLiteral.class;
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
    @Nonnull
    public Class<NEqOperation> getNNodeClass() {
      return NEqOperation.class;
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
    @Nonnull
    public Class<NExceptionRuntimeValue> getNNodeClass() {
      return NExceptionRuntimeValue.class;
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
    @Nonnull
    public Class<NExpressionStatement> getNNodeClass() {
      return NExpressionStatement.class;
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
    @Nonnull
    public Class<NField> getNNodeClass() {
      return NField.class;
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
    @Nonnull
    public Class<NFieldInitializer> getNNodeClass() {
      return NFieldInitializer.class;
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
    @Nonnull
    public Class<NFieldRef> getNNodeClass() {
      return NFieldRef.class;
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
    @Nonnull
    public Class<NFloatLiteral> getNNodeClass() {
      return NFloatLiteral.class;
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
    @Nonnull
    public Class<NForStatement> getNNodeClass() {
      return NForStatement.class;
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
    @Nonnull
    public Class<NGenericSignature> getNNodeClass() {
      return NGenericSignature.class;
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
    @Nonnull
    public Class<NGoto> getNNodeClass() {
      return NGoto.class;
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
    @Nonnull
    public Class<NGteOperation> getNNodeClass() {
      return NGteOperation.class;
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
    @Nonnull
    public Class<NGtOperation> getNNodeClass() {
      return NGtOperation.class;
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
    @Nonnull
    public Class<NIfStatement> getNNodeClass() {
      return NIfStatement.class;
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
    @Nonnull
    public Class<NInstanceOf> getNNodeClass() {
      return NInstanceOf.class;
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
    @Nonnull
    public Class<NIntLiteral> getNNodeClass() {
      return NIntLiteral.class;
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
    @Nonnull
    public Class<NInterfaceType> getNNodeClass() {
      return NInterfaceType.class;
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
    @Nonnull
    public Class<NLabeledStatement> getNNodeClass() {
      return NLabeledStatement.class;
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
    @Nonnull
    public Class<NLambda> getNNodeClass() {
      return NLambda.class;
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
    @Nonnull
    public Class<NLambdaFromJill> getNNodeClass() {
      return NLambdaFromJill.class;
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
    @Nonnull
    public Class<NLocal> getNNodeClass() {
      return NLocal.class;
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
    @Nonnull
    public Class<NLocalRef> getNNodeClass() {
      return NLocalRef.class;
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
    @Nonnull
    public Class<NLock> getNNodeClass() {
      return NLock.class;
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
    @Nonnull
    public Class<NLongLiteral> getNNodeClass() {
      return NLongLiteral.class;
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
    @Nonnull
    public Class<NLteOperation> getNNodeClass() {
      return NLteOperation.class;
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
    @Nonnull
    public Class<NLtOperation> getNNodeClass() {
      return NLtOperation.class;
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
    @Nonnull
    public Class<NMethod> getNNodeClass() {
      return NMethod.class;
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
    @Nonnull
    public Class<NMethodBody> getNNodeClass() {
      return NMethodBody.class;
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
    @Nonnull
    public Class<NMethodCall> getNNodeClass() {
      return NMethodCall.class;
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
    @Nonnull
    public Class<NMethodId> getNNodeClass() {
      return NMethodId.class;
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
    @Nonnull
    public Class<NMethodLiteral> getNNodeClass() {
      return NMethodLiteral.class;
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
    @Nonnull
    public Class<NModOperation> getNNodeClass() {
      return NModOperation.class;
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
    @Nonnull
    public Class<NMulOperation> getNNodeClass() {
      return NMulOperation.class;
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
    @Nonnull
    public Class<NMultiExpression> getNNodeClass() {
      return NMultiExpression.class;
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
    @Nonnull
    public Class<NNameValuePair> getNNodeClass() {
      return NNameValuePair.class;
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
    @Nonnull
    public Class<NNeqOperation> getNNodeClass() {
      return NNeqOperation.class;
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
    @Nonnull
    public Class<NNativeMethodBody> getNNodeClass() {
      return NNativeMethodBody.class;
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
    @Nonnull
    public Class<NNewArray> getNNodeClass() {
      return NNewArray.class;
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
    @Nonnull
    public Class<NNewInstance> getNNodeClass() {
      return NNewInstance.class;
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
    @Nonnull
    public Class<NNullLiteral> getNNodeClass() {
      return NNullLiteral.class;
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
    @Nonnull
    public Class<NOrOperation> getNNodeClass() {
      return NOrOperation.class;
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
    @Nonnull
    public Class<NOriginDigest> getNNodeClass() {
      return NOriginDigest.class;
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
    @Nonnull
    public Class<NParameter> getNNodeClass() {
      return NParameter.class;
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
    @Nonnull
    public Class<NParameterRef> getNNodeClass() {
      return NParameterRef.class;
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
    @Nonnull
    public Class<NPolymorphicCall> getNNodeClass() {
      return NPolymorphicCall.class;
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
    @Nonnull
    public Class<NPostfixDecOperation> getNNodeClass() {
      return NPostfixDecOperation.class;
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
    @Nonnull
    public Class<NPostfixIncOperation> getNNodeClass() {
      return NPostfixIncOperation.class;
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
    @Nonnull
    public Class<NPrefixBitNotOperation> getNNodeClass() {
      return NPrefixBitNotOperation.class;
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
    @Nonnull
    public Class<NPrefixDecOperation> getNNodeClass() {
      return NPrefixDecOperation.class;
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
    @Nonnull
    public Class<NPrefixIncOperation> getNNodeClass() {
      return NPrefixIncOperation.class;
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
    @Nonnull
    public Class<NPrefixNegOperation> getNNodeClass() {
      return NPrefixNegOperation.class;
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
    @Nonnull
    public Class<NPrefixNotOperation> getNNodeClass() {
      return NPrefixNotOperation.class;
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NPrefixNotOperation.skipContent(reader);
    }
  },
  RESERVED("reserved"),
  REINTERPRETCAST_OPERATION("reinterpret-cast") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NReinterpretCastOperation();
    }

    @Override
    @Nonnull
    public Class<NReinterpretCastOperation> getNNodeClass() {
      return NReinterpretCastOperation.class;
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
    @Nonnull
    public Class<NReturnStatement> getNNodeClass() {
      return NReturnStatement.class;
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
    @Nonnull
    public Class<NShlOperation> getNNodeClass() {
      return NShlOperation.class;
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
    @Nonnull
    public Class<NShortLiteral> getNNodeClass() {
      return NShortLiteral.class;
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
    @Nonnull
    public Class<NShrOperation> getNNodeClass() {
      return NShrOperation.class;
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
    @Nonnull
    public Class<NShruOperation> getNNodeClass() {
      return NShruOperation.class;
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
    @Nonnull
    public Class<NSimpleName> getNNodeClass() {
      return NSimpleName.class;
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
    @Nonnull
    public Class<NSubOperation> getNNodeClass() {
      return NSubOperation.class;
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
    @Nonnull
    public Class<NStringLiteral> getNNodeClass() {
      return NStringLiteral.class;
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
    @Nonnull
    public Class<NSwitchStatement> getNNodeClass() {
      return NSwitchStatement.class;
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
    @Nonnull
    public Class<NSynchronizedBlock> getNNodeClass() {
      return NSynchronizedBlock.class;
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
    @Nonnull
    public Class<NThisRef> getNNodeClass() {
      return NThisRef.class;
    }

    @Override
    public void skip(@Nonnull JayceInternalReaderImpl reader) throws IOException {
      NThisRef.skipContent(reader);
    }
  },
  THIS_REF_TYPE_INFO("this-type-info", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NThisRefTypeInfo();
    }

    @Override
    @Nonnull
    public Class<NThisRefTypeInfo> getNNodeClass() {
      return NThisRefTypeInfo.class;
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
    @Nonnull
    public Class<NThrowStatement> getNNodeClass() {
      return NThrowStatement.class;
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
    @Nonnull
    public Class<NThrownExceptionMarker> getNNodeClass() {
      return NThrownExceptionMarker.class;
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
    @Nonnull
    public Class<NTryStatement> getNNodeClass() {
      return NTryStatement.class;
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
    @Nonnull
    public Class<NUnlock> getNNodeClass() {
      return NUnlock.class;
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
    @Nonnull
    public Class<NWhileStatement> getNNodeClass() {
      return NWhileStatement.class;
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

  private boolean hasSourceInfo;

  private Token(@CheckForNull String text) {
    this(text, NodeLevel.FULL);
  }
  private Token(@CheckForNull String text, NodeLevel nodeLevel) {
    this.label = text;
    this.nodeLevel = nodeLevel;
    try {
      hasSourceInfo = HasSourceInfo.class.isAssignableFrom(getNNodeClass());
    } catch (InvalidTokenException e) {
      hasSourceInfo = false;
    }
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
    throw new InvalidTokenException("No node corresponding to token " + this.toString());
  }

  @Nonnull
  public Class<? extends NNode> getNNodeClass() throws InvalidTokenException  {
    throw new InvalidTokenException("No node corresponding to token " + this.toString());
  }

  public final boolean hasSourceInfo() {
    return hasSourceInfo;
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
