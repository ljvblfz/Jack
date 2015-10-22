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

package com.android.jill.backend.jayce;


import javax.annotation.CheckForNull;

/**
 * Tokens of Jayce.
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

  ABSENT_ARRAY_DIMENSION("absent-array-dimension"),
  ADD_OPERATION("+"),
  ALLOC("alloc"),
  AND_OPERATION("&&"),
  ANNOTATION("annotation-literal"),
  ANNOTATION_METHOD("annotation-method"),
  ANNOTATION_TYPE("annotation"),
  ARRAY_LENGTH("array-length"),
  ARRAY_LITERAL("array-literal"),
  ARRAY_REF("array-ref"),
  ASG_ADD_OPERATION("+="),
  ASG_BIT_AND_OPERATION("&="),
  ASG_BIT_OR_OPERATION("|="),
  ASG_BIT_XOR_OPERATION("^="),
  ASG_CONCAT_OPERATION("asg-concat"),
  ASG_DIV_OPERATION("/="),
  ASG_MOD_OPERATION("%="),
  ASG_MUL_OPERATION("*="),
  ASG_OPERATION("="),
  ASG_SHL_OPERATION("<<="),
  ASG_SHR_OPERATION(">>="),
  ASG_SHRU_OPERATION(">>>="),
  ASG_SUB_OPERATION("-="),
  ASSERT_STATEMENT("assert"),
  BIT_AND_OPERATION("&"),
  BIT_OR_OPERATION("|"),
  BIT_XOR_OPERATION("^"),
  BLOCK("block"),
  BOOLEAN_LITERAL("boolean"),
  BREAK_STATEMENT("break"),
  BYTE_LITERAL("byte"),
  CASE_STATEMENT("case"),
  CATCH_BLOCK("catch"),
  CHAR_LITERAL("char"),
  CLASS("class"),
  CLASS_LITERAL("class-literal"),
  CONCAT_OPERATION("concat"),
  CONDITIONAL_EXPRESSION ("?"),
  CONSTRUCTOR ("constructor"),
  CONTINUE_STATEMENT("continue"),
  DIV_OPERATION("/"),
  DO_STATEMENT("do"),
  DOUBLE_LITERAL("double"),
  DYNAMIC_CAST_OPERATION("cast"),
  ENUM("enum"),
  ENUM_FIELD("enum-field"),
  ENUM_LITERAL("enum-literal"),
  EQ_OPERATION("=="),
  EXCEPTION_RUNTIME_VALUE("ex-runtime-value"),
  EXPRESSION_STATEMENT("expression-statement"),
  FIELD("field"),
  FIELD_INITIALIZER("field-intializer"),
  FIELD_REF("field-ref"),
  FLOAT_LITERAL("float"),
  FOR_STATEMENT("for"),
  GENERIC_SIGNATURE("generic-signature"),
  GOTO("goto"),
  GTE_OPERATION(">="),
  GT_OPERATION(">"),
  IF_STATEMENT("if"),
  INSTANCE_OF("instanceof"),
  INT_LITERAL("int"),
  INTERFACE("interface"),
  LABELED_STATEMENT("label"),
  LOCAL("local"),
  LOCAL_REF("local-ref"),
  LOCK("lock"),
  LONG_LITERAL("long"),
  LTE_OPERATION("<="),
  LT_OPERATION("<"),
  METHOD("method"),
  METHOD_BODY("body"),
  METHOD_CALL("call"),
  METHOD_LITERAL("method-literal"),
  MOD_OPERATION("%"),
  MUL_OPERATION("*"),
  MULTI_EXPRESSION("multi-expression"),
  NAME_VALUE_PAIR("name-value-pair"),
  NEQ_OPERATION("!="),
  NATIVE_METHOD_BODY("native-body"),
  NEW_ARRAY("new-array"),
  NEW_INSTANCE("new"),
  NULL_LITERAL("null-literal"),
  OR_OPERATION("||"),
  PARAMETER("parameter"),
  PARAMETER_REF("parameter-ref"),
  POSTFIX_DEC_OPERATION("postfix-dec"),
  POSTFIX_INC_OPERATION("postfix-inc"),
  PREFIX_BIT_NOT_OPERATION("~"),
  PREFIX_DEC_OPERATION("prefix-dec"),
  PREFIX_INC_OPERATION("prefix-inc"),
  PREFIX_NEG_OPERATION("neg"),
  PREFIX_NOT_OPERATION("not"),
  RESERVED("reserved"),
  REINTERPRETCAST_OPERATION("reinterpret-cast"),
  RETURN_STATEMENT("return"),
  SHL_OPERATION("<<"),
  SHORT_LITERAL("short"),
  SHR_OPERATION(">>"),
  SHRU_OPERATION(">>>"),
  SIMPLE_NAME("simple-name"),
  SUB_OPERATION("-"),
  STRING_LITERAL("string"),
  SWITCH_STATEMENT("switch"),
  SYNCHRONIZED_BLOCK("synchronized-block"),
  THIS_REF("this"),
  THIS_REF_TYPE_INFO("this-type-info"),
  THROW_STATEMENT("throw"),
  THROWN_EXCEPTION("thrown-exception"),
  TRY_STATEMENT("try"),
  UNLOCK("unlock"),
  WHILE_STATEMENT("while")
  ;

  @CheckForNull
  private String label;

  private Token(@CheckForNull String text) {
    this.label = text;
  }

  public int getId() {
    return ordinal();
  }

  @CheckForNull
  public String getText() {
    return label;
  }
}
