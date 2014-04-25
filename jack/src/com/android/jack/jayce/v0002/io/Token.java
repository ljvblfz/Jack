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

package com.android.jack.jayce.v0002.io;

import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.v0002.NNode;
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
import com.android.jack.jayce.v0002.nodes.NSourceName;
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
  },
  ADD_OPERATION("+") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAddOperation();
    }
  },
  ALLOC("alloc") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAlloc();
    }
  },
  AND_OPERATION("&&") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAndOperation();
    }
  },
  ANNOTATION_LITERAL("annotation-literal", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NAnnotationLiteral();
    }
  },
  ANNOTATION_METHOD("annotation-method", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NAnnotationMethod();
    }
  },
  ANNOTATION_TYPE("annotation", NodeLevel.TYPES) {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAnnotationType();
    }
  },
  ARRAY_LENGTH("array-length") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NArrayLength();
    }
  },
  ARRAY_LITERAL("array-literal", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NArrayLiteral();
    }
  },
  ARRAY_REF("array-ref") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NArrayRef();
    }
  },
  ASG_ADD_OPERATION("+=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgAddOperation();
    }
  },
  ASG_BIT_AND_OPERATION("&=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgBitAndOperation();
    }
  },
  ASG_BIT_OR_OPERATION("|=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgBitOrOperation();
    }
  },
  ASG_BIT_XOR_OPERATION("^=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgBitXorOperation();
    }
  },
  ASG_CONCAT_OPERATION("asg-concat") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgConcatOperation();
    }
  },
  ASG_DIV_OPERATION("/=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgDivOperation();
    }
  },
  ASG_MOD_OPERATION("%=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgModOperation();
    }
  },
  ASG_MUL_OPERATION("*=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgMulOperation();
    }
  },
  ASG_OPERATION("=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgOperation();
    }
  },
  ASG_SHL_OPERATION("<<=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgShlOperation();
    }
  },
  ASG_SHR_OPERATION(">>=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgShrOperation();
    }
  },
  ASG_SHRU_OPERATION(">>>=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgShruOperation();
    }
  },
  ASG_SUB_OPERATION("-=") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAsgSubOperation();
    }
  },
  ASSERT_STATEMENT("assert") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NAssertStatement();
    }
  },
  BIT_AND_OPERATION("&") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NBitAndOperation();
    }
  },
  BIT_OR_OPERATION("|") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NBitOrOperation();
    }
  },
  BIT_XOR_OPERATION("^") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NBitXorOperation();
    }
  },
  BLOCK("block") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NBlock();
    }
  },
  BOOLEAN_LITERAL("boolean", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NBooleanLiteral();
    }
  },
  BREAK_STATEMENT("break") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NBreakStatement();
    }
  },
  BYTE_LITERAL("byte", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NByteLiteral();
    }
  },
  CASE_STATEMENT("case") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NCaseStatement();
    }
  },
  CATCH_BLOCK("catch") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NCatchBlock();
    }
  },
  CHAR_LITERAL("char", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NCharLiteral();
    }
  },
  CLASS("class", NodeLevel.TYPES) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NClassType();
    }
  },
  CLASS_LITERAL("class-literal", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NClassLiteral();
    }
  },
  CONCAT_OPERATION("concat") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NConcatOperation();
    }
  },
  CONDITIONAL_EXPRESSION ("?") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NConditionalExpression();
    }
  },
  CONSTRUCTOR ("constructor", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NConstructor();
    }
  },
  CONTINUE_STATEMENT("continue") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NContinueStatement();
    }
  },
  DIV_OPERATION("/") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NDivOperation();
    }
  },
  DO_STATEMENT("do") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NDoStatement();
    }
  },
  DOUBLE_LITERAL("double", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NDoubleLiteral();
    }
  },
  DYNAMIC_CAST_OPERATION("cast") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NDynamicCastOperation();
    }
  },
  ENUM("enum", NodeLevel.TYPES) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NEnumType();
    }
  },
  ENUM_FIELD("enum-field", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NEnumField();
    }
  },
  ENUM_LITERAL("enum-literal", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NEnumLiteral();
    }
  },
  EQ_OPERATION("==") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NEqOperation();
    }
  },
  EXCEPTION_RUNTIME_VALUE("ex-runtime-value") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NExceptionRuntimeValue();
    }
  },
  EXPRESSION_STATEMENT("expression-statement") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NExpressionStatement();
    }
  },
  FIELD("field", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NField();
    }
  },
  FIELD_INITIALIZER("field-intializer") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NFieldInitializer();
    }
  },
  FIELD_REF("field-ref") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NFieldRef();
    }
  },
  FLOAT_LITERAL("float", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NFloatLiteral();
    }
  },
  FOR_STATEMENT("for") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NForStatement();
    }
  },
  GENERIC_SIGNATURE("generic-signature", NodeLevel.TYPES) {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NGenericSignature();
    }
  },
  GOTO("goto") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NGoto();
    }
  },
  GTE_OPERATION(">=") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NGteOperation();
    }
  },
  GT_OPERATION(">") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NGtOperation();
    }
  },
  IF_STATEMENT("if") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NIfStatement();
    }
  },
  INSTANCE_OF("instanceof") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NInstanceOf();
    }
  },
  INT_LITERAL("int", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NIntLiteral();
    }
  },
  INTERFACE("interface", NodeLevel.TYPES) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NInterfaceType();
    }
  },
  LABELED_STATEMENT("label") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NLabeledStatement();
    }
  },
  LOCAL("local") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NLocal();
    }
  },
  LOCAL_REF("local-ref") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NLocalRef();
    }
  },
  LOCK("lock") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NLock();
    }
  },
  LONG_LITERAL("long", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NLongLiteral();
    }
  },
  LTE_OPERATION("<=") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NLteOperation();
    }
  },
  LT_OPERATION("<") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NLtOperation();
    }
  },
  METHOD("method", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NMethod();
    }
  },
  METHOD_BODY("body") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NMethodBody();
    }
  },
  METHOD_CALL("call") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NMethodCall();
    }
  },
  METHOD_LITERAL("method-literal", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NMethodLiteral();
    }
  },
  MOD_OPERATION("%") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NModOperation();
    }
  },
  MUL_OPERATION("*") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NMulOperation();
    }
  },
  MULTI_EXPRESSION("multi-expression") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NMultiExpression();
    }
  },
  NAME_VALUE_PAIR("name-value-pair", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NNameValuePair();
    }
  },
  NEQ_OPERATION("!=") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NNeqOperation();
    }
  },
  NATIVE_METHOD_BODY("native-body") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NNativeMethodBody();
    }
  },
  NEW_ARRAY("new-array") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NNewArray();
    }
  },
  NEW_INSTANCE("new") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NNewInstance();
    }
  },
  NULL_LITERAL("null-literal", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NNullLiteral();
    }
  },
  OR_OPERATION("||") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NOrOperation();
    }
  },
  PARAMETER("parameter", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NParameter();
    }
  },
  PARAMETER_REF("parameter-ref") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NParameterRef();
    }
  },
  POSTFIX_DEC_OPERATION("postfix-dec") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NPostfixDecOperation();
    }
  },
  POSTFIX_INC_OPERATION("postfix-inc") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NPostfixIncOperation();
    }
  },
  PREFIX_BIT_NOT_OPERATION("~") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NPrefixBitNotOperation();
    }
  },
  PREFIX_DEC_OPERATION("prefix-dec") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NPrefixDecOperation();
    }
  },
  PREFIX_INC_OPERATION("prefix-inc") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NPrefixIncOperation();
    }
  },
  PREFIX_NEG_OPERATION("neg") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NPrefixNegOperation();
    }
  },
  PREFIX_NOT_OPERATION("not") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NPrefixNotOperation();
    }
  },
  PROGRAM("program", NodeLevel.TYPES) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NProgram();
    }
  },
  REINTERPRETCAST_OPERATION("reinterpret-cast") {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NReinterpretCastOperation();
    }
  },
  RETURN_STATEMENT("return") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NReturnStatement();
    }
  },
  SHL_OPERATION("<<") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NShlOperation();
    }
  },
  SHORT_LITERAL("short", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NShortLiteral();
    }
  },
  SHR_OPERATION(">>") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NShrOperation();
    }
  },
  SHRU_OPERATION(">>>") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NShruOperation();
    }
  },
  SOURCE_NAME("source-name", NodeLevel.TYPES) {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NSourceName();
    }
  },
  SUB_OPERATION("-") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NSubOperation();
    }
  },
  STRING_LITERAL("string", NodeLevel.STRUCTURE) {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NStringLiteral();
    }
  },
  SWITCH_STATEMENT("switch") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NSwitchStatement();
    }
  },
  SYNCHRONIZED_BLOCK("synchronized-block") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NSynchronizedBlock();
    }
  },
  THIS_REF("this") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NThisRef();
    }
  },
  THIS_REF_TYPE_INFO("this-type-info") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NThisRefTypeInfo();
    }
  },
  THROW_STATEMENT("throw") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NThrowStatement();
    }
  },
  THROWN_EXCEPTION("thrown-exception", NodeLevel.STRUCTURE) {
    @Override
    @Nonnull
    public NNode newNode() {
      return new NThrownExceptionMarker();
    }
  },
  TRY_STATEMENT("try") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NTryStatement();
    }
  },
  UNLOCK("unlock") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NUnlock();
    }
  },
  WHILE_STATEMENT("while") {
    @Nonnull
    @Override
    public NNode newNode() {
      return new NWhileStatement();
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
    throw new InvalidTokenException("No node coresponding to token " + this);
  }

@Nonnull
  public NodeLevel getNodeLevel() {
    return nodeLevel;
  }
}
