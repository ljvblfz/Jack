/*
* Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.opcodes;

import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import javax.annotation.Nonnull;

public class OpcodesTests extends RuntimeTest {

  @Nonnull
  private RuntimeTestInfo INVOKE_STATIC = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.invoke_static"),
    "com.android.jack.opcodes.invoke_static.Test_invoke_static");

  @Nonnull
  private RuntimeTestInfo ARRAY_LENGTH = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.array_length"),
    "com.android.jack.opcodes.array_length.Test_array_length");

  @Nonnull
  private RuntimeTestInfo NEG_FLOAT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.neg_float"),
    "com.android.jack.opcodes.neg_float.Test_neg_float");

  @Nonnull
  private RuntimeTestInfo SUB_DOUBLE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.sub_double"),
    "com.android.jack.opcodes.sub_double.Test_sub_double");

  @Nonnull
  private RuntimeTestInfo AGET = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aget"),
    "com.android.jack.opcodes.aget.Test_aget");

  @Nonnull
  private RuntimeTestInfo APUT_BYTE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aput_byte"),
    "com.android.jack.opcodes.aput_byte.Test_aput_byte");

  @Nonnull
  private RuntimeTestInfo OPC_NEW = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.opc_new"),
    "com.android.jack.opcodes.opc_new.Test_opc_new");

  @Nonnull
  private RuntimeTestInfo CMPL_DOUBLE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.cmpl_double"),
    "com.android.jack.opcodes.cmpl_double.Test_cmpl_double");

  @Nonnull
  private RuntimeTestInfo IF_GTZ = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_gtz"),
    "com.android.jack.opcodes.if_gtz.Test_if_gtz");

  @Nonnull
  private RuntimeTestInfo FLOAT_TO_LONG = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.float_to_long"),
    "com.android.jack.opcodes.float_to_long.Test_float_to_long");

  @Nonnull
  private RuntimeTestInfo ADD_LONG = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.add_long"),
    "com.android.jack.opcodes.add_long.Test_add_long");

  @Nonnull
  private RuntimeTestInfo ADD_FLOAT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.add_float"),
    "com.android.jack.opcodes.add_float.Test_add_float");

  @Nonnull
  private RuntimeTestInfo IPUT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.iput"),
    "com.android.jack.opcodes.iput.Test_iput");

  @Nonnull
  private RuntimeTestInfo DIV_FLOAT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.div_float"),
    "com.android.jack.opcodes.div_float.Test_div_float");

  @Nonnull
  private RuntimeTestInfo USHR_LONG = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.ushr_long"),
    "com.android.jack.opcodes.ushr_long.Test_ushr_long");

  @Nonnull
  private RuntimeTestInfo CONST_WIDE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.const_wide"),
    "com.android.jack.opcodes.const_wide.Test_const_wide");

  @Nonnull
  private RuntimeTestInfo XOR_INT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.xor_int"),
    "com.android.jack.opcodes.xor_int.Test_xor_int");

  @Nonnull
  private RuntimeTestInfo AGET_OBJECT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aget_object"),
    "com.android.jack.opcodes.aget_object.Test_aget_object");

  @Nonnull
  private RuntimeTestInfo CONST4_16 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.const4_16"),
    "com.android.jack.opcodes.const4_16.Test_const4_16");

  @Nonnull
  private RuntimeTestInfo REM_LONG = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.rem_long"),
    "com.android.jack.opcodes.rem_long.Test_rem_long");

  @Nonnull
  private RuntimeTestInfo USHR_INT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.ushr_int"),
    "com.android.jack.opcodes.ushr_int.Test_ushr_int");

  @Nonnull
  private RuntimeTestInfo IF_GE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_ge"),
    "com.android.jack.opcodes.if_ge.Test_if_ge");

  @Nonnull
  private RuntimeTestInfo SUB_LONG = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.sub_long"),
    "com.android.jack.opcodes.sub_long.Test_sub_long");

  @Nonnull
  private RuntimeTestInfo FLOAT_TO_INT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.float_to_int"),
    "com.android.jack.opcodes.float_to_int.Test_float_to_int");

  @Nonnull
  private RuntimeTestInfo INT_TO_LONG = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.int_to_long"),
    "com.android.jack.opcodes.int_to_long.Test_int_to_long");

  @Nonnull
  private RuntimeTestInfo OR_LONG = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.or_long"),
    "com.android.jack.opcodes.or_long.Test_or_long");

  @Nonnull
  private RuntimeTestInfo IF_GEZ = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_gez"),
    "com.android.jack.opcodes.if_gez.Test_if_gez");

  @Nonnull
  private RuntimeTestInfo SHL_INT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.shl_int"),
    "com.android.jack.opcodes.shl_int.Test_shl_int");

  @Nonnull
  private RuntimeTestInfo LONG_TO_FLOAT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.long_to_float"),
    "com.android.jack.opcodes.long_to_float.Test_long_to_float");

  @Nonnull
  private RuntimeTestInfo DIV_INT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.div_int"),
    "com.android.jack.opcodes.div_int.Test_div_int");

  @Nonnull
  private RuntimeTestInfo AND_LONG = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.and_long"),
    "com.android.jack.opcodes.and_long.Test_and_long");

  @Nonnull
  private RuntimeTestInfo DOUBLE_TO_LONG = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.double_to_long"),
    "com.android.jack.opcodes.double_to_long.Test_double_to_long");

  @Nonnull
  private RuntimeTestInfo MUL_LONG = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.mul_long"),
    "com.android.jack.opcodes.mul_long.Test_mul_long");

  @Nonnull
  private RuntimeTestInfo DOUBLE_TO_INT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.double_to_int"),
    "com.android.jack.opcodes.double_to_int.Test_double_to_int");

  @Nonnull
  private RuntimeTestInfo IF_NEZ = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_nez"),
    "com.android.jack.opcodes.if_nez.Test_if_nez");

  @Nonnull
  private RuntimeTestInfo APUT_OBJECT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aput_object"),
    "com.android.jack.opcodes.aput_object.Test_aput_object");

  @Nonnull
  private RuntimeTestInfo IF_LT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_lt"),
    "com.android.jack.opcodes.if_lt.Test_if_lt");

  @Nonnull
  private RuntimeTestInfo INT_TO_DOUBLE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.int_to_double"),
    "com.android.jack.opcodes.int_to_double.Test_int_to_double");

  @Nonnull
  private RuntimeTestInfo MUL_INT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.mul_int"),
    "com.android.jack.opcodes.mul_int.Test_mul_int");

  @Nonnull
  private RuntimeTestInfo SPUT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.sput"),
    "com.android.jack.opcodes.sput.Test_sput");

  @Nonnull
  private RuntimeTestInfo SHL_LONG = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.shl_long"),
    "com.android.jack.opcodes.shl_long.Test_shl_long");

  @Nonnull
  private RuntimeTestInfo NEG_LONG = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.neg_long"),
    "com.android.jack.opcodes.neg_long.Test_neg_long");

  @Nonnull
  private RuntimeTestInfo LONG_TO_INT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.long_to_int"),
    "com.android.jack.opcodes.long_to_int.Test_long_to_int");

  @Nonnull
  private RuntimeTestInfo OPC_GOTO = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.opc_goto"),
    "com.android.jack.opcodes.opc_goto.Test_opc_goto");

  @Nonnull
  private RuntimeTestInfo INT_TO_FLOAT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.int_to_float"),
    "com.android.jack.opcodes.int_to_float.Test_int_to_float");

  @Nonnull
  private RuntimeTestInfo XOR_LONG = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.xor_long"),
    "com.android.jack.opcodes.xor_long.Test_xor_long");

  @Nonnull
  private RuntimeTestInfo MONITOR_ENTER = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.monitor_enter"),
    "com.android.jack.opcodes.monitor_enter.Test_monitor_enter");

  @Nonnull
  private RuntimeTestInfo IF_EQZ = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_eqz"),
    "com.android.jack.opcodes.if_eqz.Test_if_eqz");

  @Nonnull
  private RuntimeTestInfo INVOKE_DIRECT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.invoke_direct"),
    "com.android.jack.opcodes.invoke_direct.Test_invoke_direct");

  @Nonnull
  private RuntimeTestInfo CMPL_FLOAT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.cmpl_float"),
    "com.android.jack.opcodes.cmpl_float.Test_cmpl_float");

  @Nonnull
  private RuntimeTestInfo CHECK_CAST = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.check_cast"),
    "com.android.jack.opcodes.check_cast.Test_check_cast");

  @Nonnull
  private RuntimeTestInfo OPC_THROW = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.opc_throw"),
    "com.android.jack.opcodes.opc_throw.Test_opc_throw");

  @Nonnull
  private RuntimeTestInfo INT_TO_SHORT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.int_to_short"),
    "com.android.jack.opcodes.int_to_short.Test_int_to_short");

  @Nonnull
  private RuntimeTestInfo PACKED_SWITCH = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.packed_switch"),
    "com.android.jack.opcodes.packed_switch.Test_packed_switch");

  @Nonnull
  private RuntimeTestInfo AGET_CHAR = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aget_char"),
    "com.android.jack.opcodes.aget_char.Test_aget_char");

  @Nonnull
  private RuntimeTestInfo RETURN_OBJECT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.return_object"),
    "com.android.jack.opcodes.return_object.Test_return_object");

  @Nonnull
  private RuntimeTestInfo OPC_CONST = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.opc_const"),
    "com.android.jack.opcodes.opc_const.Test_opc_const");

  @Nonnull
  private RuntimeTestInfo SUB_INT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.sub_int"),
    "com.android.jack.opcodes.sub_int.Test_sub_int");

  @Nonnull
  private RuntimeTestInfo APUT_CHAR = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aput_char"),
    "com.android.jack.opcodes.aput_char.Test_aput_char");

  @Nonnull
  private RuntimeTestInfo NEG_INT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.neg_int"),
    "com.android.jack.opcodes.neg_int.Test_neg_int");

  @Nonnull
  private RuntimeTestInfo MUL_DOUBLE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.mul_double"),
    "com.android.jack.opcodes.mul_double.Test_mul_double");

  @Nonnull
  private RuntimeTestInfo DOUBLE_TO_FLOAT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.double_to_float"),
    "com.android.jack.opcodes.double_to_float.Test_double_to_float");

  @Nonnull
  private RuntimeTestInfo INT_TO_BYTE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.int_to_byte"),
    "com.android.jack.opcodes.int_to_byte.Test_int_to_byte");

  @Nonnull
  private RuntimeTestInfo IF_LE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_le"),
    "com.android.jack.opcodes.if_le.Test_if_le");

  @Nonnull
  private RuntimeTestInfo INVOKE_VIRTUAL = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.invoke_virtual"),
    "com.android.jack.opcodes.invoke_virtual.Test_invoke_virtual");

  @Nonnull
  private RuntimeTestInfo DIV_DOUBLE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.div_double"),
    "com.android.jack.opcodes.div_double.Test_div_double");

  @Nonnull
  private RuntimeTestInfo IF_GT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_gt"),
    "com.android.jack.opcodes.if_gt.Test_if_gt");

  @Nonnull
  private RuntimeTestInfo AGET_SHORT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aget_short"),
    "com.android.jack.opcodes.aget_short.Test_aget_short");

  @Nonnull
  private RuntimeTestInfo CONST_STRING = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.const_string"),
    "com.android.jack.opcodes.const_string.Test_const_string");

  @Nonnull
  private RuntimeTestInfo OR_INT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.or_int"),
    "com.android.jack.opcodes.or_int.Test_or_int");

  @Nonnull
  private RuntimeTestInfo REM_INT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.rem_int"),
    "com.android.jack.opcodes.rem_int.Test_rem_int");

  @Nonnull
  private RuntimeTestInfo REM_DOUBLE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.rem_double"),
    "com.android.jack.opcodes.rem_double.Test_rem_double");

  @Nonnull
  private RuntimeTestInfo LONG_TO_DOUBLE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.long_to_double"),
    "com.android.jack.opcodes.long_to_double.Test_long_to_double");

  @Nonnull
  private RuntimeTestInfo IF_NE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_ne"),
    "com.android.jack.opcodes.if_ne.Test_if_ne");

  @Nonnull
  private RuntimeTestInfo CMP_LONG = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.cmp_long"),
    "com.android.jack.opcodes.cmp_long.Test_cmp_long");

  @Nonnull
  private RuntimeTestInfo SGET = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.sget"),
    "com.android.jack.opcodes.sget.Test_sget");

  @Nonnull
  private RuntimeTestInfo AGET_WIDE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aget_wide"),
    "com.android.jack.opcodes.aget_wide.Test_aget_wide");

  @Nonnull
  private RuntimeTestInfo IF_LTZ = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_ltz"),
    "com.android.jack.opcodes.if_ltz.Test_if_ltz");

  @Nonnull
  private RuntimeTestInfo OPC_INSTANCEOF = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.opc_instanceof"),
    "com.android.jack.opcodes.opc_instanceof.Test_opc_instanceof");

  @Nonnull
  private RuntimeTestInfo NEG_DOUBLE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.neg_double"),
    "com.android.jack.opcodes.neg_double.Test_neg_double");

  @Nonnull
  private RuntimeTestInfo RETURN_WIDE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.return_wide"),
    "com.android.jack.opcodes.return_wide.Test_return_wide");

  @Nonnull
  private RuntimeTestInfo FLOAT_TO_DOUBLE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.float_to_double"),
    "com.android.jack.opcodes.float_to_double.Test_float_to_double");

  @Nonnull
  private RuntimeTestInfo APUT_SHORT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aput_short"),
    "com.android.jack.opcodes.aput_short.Test_aput_short");

  @Nonnull
  private RuntimeTestInfo CMPG_DOUBLE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.cmpg_double"),
    "com.android.jack.opcodes.cmpg_double.Test_cmpg_double");

  @Nonnull
  private RuntimeTestInfo IF_LEZ = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_lez"),
    "com.android.jack.opcodes.if_lez.Test_if_lez");

  @Nonnull
  private RuntimeTestInfo IF_EQ = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_eq"),
    "com.android.jack.opcodes.if_eq.Test_if_eq");

  @Nonnull
  private RuntimeTestInfo NEW_ARRAY = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.new_array"),
    "com.android.jack.opcodes.new_array.Test_new_array");

  @Nonnull
  private RuntimeTestInfo SHR_LONG = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.shr_long"),
    "com.android.jack.opcodes.shr_long.Test_shr_long");

  @Nonnull
  private RuntimeTestInfo ADD_DOUBLE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.add_double"),
    "com.android.jack.opcodes.add_double.Test_add_double");

  @Nonnull
  private RuntimeTestInfo DIV_LONG = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.div_long"),
    "com.android.jack.opcodes.div_long.Test_div_long");

  @Nonnull
  private RuntimeTestInfo SPARSE_SWITCH = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.sparse_switch"),
    "com.android.jack.opcodes.sparse_switch.Test_sparse_switch");

  @Nonnull
  private RuntimeTestInfo INVOKE_INTERFACE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.invoke_interface"),
    "com.android.jack.opcodes.invoke_interface.Test_invoke_interface");

  @Nonnull
  private RuntimeTestInfo APUT_WIDE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aput_wide"),
    "com.android.jack.opcodes.aput_wide.Test_aput_wide");

  @Nonnull
  private RuntimeTestInfo AGET_BYTE = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aget_byte"),
    "com.android.jack.opcodes.aget_byte.Test_aget_byte");

  @Nonnull
  private RuntimeTestInfo APUT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aput"),
    "com.android.jack.opcodes.aput.Test_aput");

  @Nonnull
  private RuntimeTestInfo SHR_INT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.shr_int"),
    "com.android.jack.opcodes.shr_int.Test_shr_int");

  @Nonnull
  private RuntimeTestInfo OPC_RETURN = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.opc_return"),
    "com.android.jack.opcodes.opc_return.Test_opc_return");

  @Nonnull
  private RuntimeTestInfo INVOKE_SUPER = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.invoke_super"),
    "com.android.jack.opcodes.invoke_super.Test_invoke_super");

  @Nonnull
  private RuntimeTestInfo MUL_FLOAT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.mul_float"),
    "com.android.jack.opcodes.mul_float.Test_mul_float");

  @Nonnull
  private RuntimeTestInfo SUB_FLOAT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.sub_float"),
    "com.android.jack.opcodes.sub_float.Test_sub_float");

  @Nonnull
  private RuntimeTestInfo ADD_INT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.add_int"),
    "com.android.jack.opcodes.add_int.Test_add_int");

  @Nonnull
  private RuntimeTestInfo INT_TO_CHAR = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.int_to_char"),
    "com.android.jack.opcodes.int_to_char.Test_int_to_char");

  @Nonnull
  private RuntimeTestInfo AND_INT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.and_int"),
    "com.android.jack.opcodes.and_int.Test_and_int");

  @Nonnull
  private RuntimeTestInfo CMPG_FLOAT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.cmpg_float"),
    "com.android.jack.opcodes.cmpg_float.Test_cmpg_float");

  @Nonnull
  private RuntimeTestInfo IGET = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.iget"),
    "com.android.jack.opcodes.iget.Test_iget");

  @Nonnull
  private RuntimeTestInfo REM_FLOAT = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.rem_float"),
    "com.android.jack.opcodes.rem_float.Test_rem_float");


  @BeforeClass
  public static void setUpClass() {
    OpcodesTests.class.getClassLoader().setDefaultAssertionStatus(true);
  }
  @Test
  //@Category(RuntimeRegressionTest.class)
  public void invoke_static() throws Exception {
    new RuntimeTestHelper(INVOKE_STATIC)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void array_length() throws Exception {
    new RuntimeTestHelper(ARRAY_LENGTH)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void neg_float() throws Exception {
    new RuntimeTestHelper(NEG_FLOAT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void sub_double() throws Exception {
    new RuntimeTestHelper(SUB_DOUBLE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void aget() throws Exception {
    new RuntimeTestHelper(AGET)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void aput_byte() throws Exception {
    new RuntimeTestHelper(APUT_BYTE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void opc_new() throws Exception {
    new RuntimeTestHelper(OPC_NEW)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void cmpl_double() throws Exception {
    new RuntimeTestHelper(CMPL_DOUBLE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void if_gtz() throws Exception {
    new RuntimeTestHelper(IF_GTZ)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void float_to_long() throws Exception {
    new RuntimeTestHelper(FLOAT_TO_LONG)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void add_long() throws Exception {
    new RuntimeTestHelper(ADD_LONG)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void add_float() throws Exception {
    new RuntimeTestHelper(ADD_FLOAT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void iput() throws Exception {
    new RuntimeTestHelper(IPUT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void div_float() throws Exception {
    new RuntimeTestHelper(DIV_FLOAT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void ushr_long() throws Exception {
    new RuntimeTestHelper(USHR_LONG)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void const_wide() throws Exception {
    new RuntimeTestHelper(CONST_WIDE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void xor_int() throws Exception {
    new RuntimeTestHelper(XOR_INT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void aget_object() throws Exception {
    new RuntimeTestHelper(AGET_OBJECT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void const4_16() throws Exception {
    new RuntimeTestHelper(CONST4_16)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void rem_long() throws Exception {
    new RuntimeTestHelper(REM_LONG)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void ushr_int() throws Exception {
    new RuntimeTestHelper(USHR_INT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void if_ge() throws Exception {
    new RuntimeTestHelper(IF_GE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void sub_long() throws Exception {
    new RuntimeTestHelper(SUB_LONG)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void float_to_int() throws Exception {
    new RuntimeTestHelper(FLOAT_TO_INT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void int_to_long() throws Exception {
    new RuntimeTestHelper(INT_TO_LONG)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void or_long() throws Exception {
    new RuntimeTestHelper(OR_LONG)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void if_gez() throws Exception {
    new RuntimeTestHelper(IF_GEZ)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void shl_int() throws Exception {
    new RuntimeTestHelper(SHL_INT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void long_to_float() throws Exception {
    new RuntimeTestHelper(LONG_TO_FLOAT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void div_int() throws Exception {
    new RuntimeTestHelper(DIV_INT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void and_long() throws Exception {
    new RuntimeTestHelper(AND_LONG)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void double_to_long() throws Exception {
    new RuntimeTestHelper(DOUBLE_TO_LONG)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void mul_long() throws Exception {
    new RuntimeTestHelper(MUL_LONG)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void double_to_int() throws Exception {
    new RuntimeTestHelper(DOUBLE_TO_INT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void if_nez() throws Exception {
    new RuntimeTestHelper(IF_NEZ)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void aput_object() throws Exception {
    new RuntimeTestHelper(APUT_OBJECT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void if_lt() throws Exception {
    new RuntimeTestHelper(IF_LT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void int_to_double() throws Exception {
    new RuntimeTestHelper(INT_TO_DOUBLE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void mul_int() throws Exception {
    new RuntimeTestHelper(MUL_INT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void sput() throws Exception {
    new RuntimeTestHelper(SPUT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void shl_long() throws Exception {
    new RuntimeTestHelper(SHL_LONG)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void neg_long() throws Exception {
    new RuntimeTestHelper(NEG_LONG)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void long_to_int() throws Exception {
    new RuntimeTestHelper(LONG_TO_INT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void opc_goto() throws Exception {
    new RuntimeTestHelper(OPC_GOTO)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void int_to_float() throws Exception {
    new RuntimeTestHelper(INT_TO_FLOAT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void xor_long() throws Exception {
    new RuntimeTestHelper(XOR_LONG)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void monitor_enter() throws Exception {
    new RuntimeTestHelper(MONITOR_ENTER)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void if_eqz() throws Exception {
    new RuntimeTestHelper(IF_EQZ)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void invoke_direct() throws Exception {
    new RuntimeTestHelper(INVOKE_DIRECT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void cmpl_float() throws Exception {
    new RuntimeTestHelper(CMPL_FLOAT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void check_cast() throws Exception {
    new RuntimeTestHelper(CHECK_CAST)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void opc_throw() throws Exception {
    new RuntimeTestHelper(OPC_THROW)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void int_to_short() throws Exception {
    new RuntimeTestHelper(INT_TO_SHORT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void packed_switch() throws Exception {
    new RuntimeTestHelper(PACKED_SWITCH)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void aget_char() throws Exception {
    new RuntimeTestHelper(AGET_CHAR)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void return_object() throws Exception {
    new RuntimeTestHelper(RETURN_OBJECT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void opc_const() throws Exception {
    new RuntimeTestHelper(OPC_CONST)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void sub_int() throws Exception {
    new RuntimeTestHelper(SUB_INT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void aput_char() throws Exception {
    new RuntimeTestHelper(APUT_CHAR)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void neg_int() throws Exception {
    new RuntimeTestHelper(NEG_INT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void mul_double() throws Exception {
    new RuntimeTestHelper(MUL_DOUBLE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void double_to_float() throws Exception {
    new RuntimeTestHelper(DOUBLE_TO_FLOAT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void int_to_byte() throws Exception {
    new RuntimeTestHelper(INT_TO_BYTE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void if_le() throws Exception {
    new RuntimeTestHelper(IF_LE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void invoke_virtual() throws Exception {
    new RuntimeTestHelper(INVOKE_VIRTUAL)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void div_double() throws Exception {
    new RuntimeTestHelper(DIV_DOUBLE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void if_gt() throws Exception {
    new RuntimeTestHelper(IF_GT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void aget_short() throws Exception {
    new RuntimeTestHelper(AGET_SHORT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void const_string() throws Exception {
    new RuntimeTestHelper(CONST_STRING)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void or_int() throws Exception {
    new RuntimeTestHelper(OR_INT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void rem_int() throws Exception {
    new RuntimeTestHelper(REM_INT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void rem_double() throws Exception {
    new RuntimeTestHelper(REM_DOUBLE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void long_to_double() throws Exception {
    new RuntimeTestHelper(LONG_TO_DOUBLE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void if_ne() throws Exception {
    new RuntimeTestHelper(IF_NE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void cmp_long() throws Exception {
    new RuntimeTestHelper(CMP_LONG)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void sget() throws Exception {
    new RuntimeTestHelper(SGET)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void aget_wide() throws Exception {
    new RuntimeTestHelper(AGET_WIDE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void if_ltz() throws Exception {
    new RuntimeTestHelper(IF_LTZ)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void opc_instanceof() throws Exception {
    new RuntimeTestHelper(OPC_INSTANCEOF)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void neg_double() throws Exception {
    new RuntimeTestHelper(NEG_DOUBLE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void return_wide() throws Exception {
    new RuntimeTestHelper(RETURN_WIDE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void float_to_double() throws Exception {
    new RuntimeTestHelper(FLOAT_TO_DOUBLE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void aput_short() throws Exception {
    new RuntimeTestHelper(APUT_SHORT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void cmpg_double() throws Exception {
    new RuntimeTestHelper(CMPG_DOUBLE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void if_lez() throws Exception {
    new RuntimeTestHelper(IF_LEZ)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void if_eq() throws Exception {
    new RuntimeTestHelper(IF_EQ)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void new_array() throws Exception {
    new RuntimeTestHelper(NEW_ARRAY)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void shr_long() throws Exception {
    new RuntimeTestHelper(SHR_LONG)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void add_double() throws Exception {
    new RuntimeTestHelper(ADD_DOUBLE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void div_long() throws Exception {
    new RuntimeTestHelper(DIV_LONG)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void sparse_switch() throws Exception {
    new RuntimeTestHelper(SPARSE_SWITCH)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void invoke_interface() throws Exception {
    new RuntimeTestHelper(INVOKE_INTERFACE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void aput_wide() throws Exception {
    new RuntimeTestHelper(APUT_WIDE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void aget_byte() throws Exception {
    new RuntimeTestHelper(AGET_BYTE)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void aput() throws Exception {
    new RuntimeTestHelper(APUT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void shr_int() throws Exception {
    new RuntimeTestHelper(SHR_INT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void opc_return() throws Exception {
    new RuntimeTestHelper(OPC_RETURN)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void invoke_super() throws Exception {
    new RuntimeTestHelper(INVOKE_SUPER)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void mul_float() throws Exception {
    new RuntimeTestHelper(MUL_FLOAT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void sub_float() throws Exception {
    new RuntimeTestHelper(SUB_FLOAT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void add_int() throws Exception {
    new RuntimeTestHelper(ADD_INT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void int_to_char() throws Exception {
    new RuntimeTestHelper(INT_TO_CHAR)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void and_int() throws Exception {
    new RuntimeTestHelper(AND_INT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void cmpg_float() throws Exception {
    new RuntimeTestHelper(CMPG_FLOAT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void iget() throws Exception {
    new RuntimeTestHelper(IGET)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }

  @Test
  //@Category(RuntimeRegressionTest.class)
  public void rem_float() throws Exception {
    new RuntimeTestHelper(REM_FLOAT)
    .setSrcDirName("jm")
    .setRefDirName( ".")
    .addReferenceExtraSources(new File(AbstractTestTools.getJackRootDir(), "toolchain/jack/jack-tests/src/com/android/jack/DxTestCase.java"))
    .compileAndRunTest();
  }


  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(INVOKE_STATIC);
    rtTestInfos.add(ARRAY_LENGTH);
    rtTestInfos.add(NEG_FLOAT);
    rtTestInfos.add(SUB_DOUBLE);
    rtTestInfos.add(AGET);
    rtTestInfos.add(APUT_BYTE);
    rtTestInfos.add(OPC_NEW);
    rtTestInfos.add(CMPL_DOUBLE);
    rtTestInfos.add(IF_GTZ);
    rtTestInfos.add(FLOAT_TO_LONG);
    rtTestInfos.add(ADD_LONG);
    rtTestInfos.add(ADD_FLOAT);
    rtTestInfos.add(IPUT);
    rtTestInfos.add(DIV_FLOAT);
    rtTestInfos.add(USHR_LONG);
    rtTestInfos.add(CONST_WIDE);
    rtTestInfos.add(XOR_INT);
    rtTestInfos.add(AGET_OBJECT);
    rtTestInfos.add(CONST4_16);
    rtTestInfos.add(REM_LONG);
    rtTestInfos.add(USHR_INT);
    rtTestInfos.add(IF_GE);
    rtTestInfos.add(SUB_LONG);
    rtTestInfos.add(FLOAT_TO_INT);
    rtTestInfos.add(INT_TO_LONG);
    rtTestInfos.add(OR_LONG);
    rtTestInfos.add(IF_GEZ);
    rtTestInfos.add(SHL_INT);
    rtTestInfos.add(LONG_TO_FLOAT);
    rtTestInfos.add(DIV_INT);
    rtTestInfos.add(AND_LONG);
    rtTestInfos.add(DOUBLE_TO_LONG);
    rtTestInfos.add(MUL_LONG);
    rtTestInfos.add(DOUBLE_TO_INT);
    rtTestInfos.add(IF_NEZ);
    rtTestInfos.add(APUT_OBJECT);
    rtTestInfos.add(IF_LT);
    rtTestInfos.add(INT_TO_DOUBLE);
    rtTestInfos.add(MUL_INT);
    rtTestInfos.add(SPUT);
    rtTestInfos.add(SHL_LONG);
    rtTestInfos.add(NEG_LONG);
    rtTestInfos.add(LONG_TO_INT);
    rtTestInfos.add(OPC_GOTO);
    rtTestInfos.add(INT_TO_FLOAT);
    rtTestInfos.add(XOR_LONG);
    rtTestInfos.add(MONITOR_ENTER);
    rtTestInfos.add(IF_EQZ);
    rtTestInfos.add(INVOKE_DIRECT);
    rtTestInfos.add(CMPL_FLOAT);
    rtTestInfos.add(CHECK_CAST);
    rtTestInfos.add(OPC_THROW);
    rtTestInfos.add(INT_TO_SHORT);
    rtTestInfos.add(PACKED_SWITCH);
    rtTestInfos.add(AGET_CHAR);
    rtTestInfos.add(RETURN_OBJECT);
    rtTestInfos.add(OPC_CONST);
    rtTestInfos.add(SUB_INT);
    rtTestInfos.add(APUT_CHAR);
    rtTestInfos.add(NEG_INT);
    rtTestInfos.add(MUL_DOUBLE);
    rtTestInfos.add(DOUBLE_TO_FLOAT);
    rtTestInfos.add(INT_TO_BYTE);
    rtTestInfos.add(IF_LE);
    rtTestInfos.add(INVOKE_VIRTUAL);
    rtTestInfos.add(DIV_DOUBLE);
    rtTestInfos.add(IF_GT);
    rtTestInfos.add(AGET_SHORT);
    rtTestInfos.add(CONST_STRING);
    rtTestInfos.add(OR_INT);
    rtTestInfos.add(REM_INT);
    rtTestInfos.add(REM_DOUBLE);
    rtTestInfos.add(LONG_TO_DOUBLE);
    rtTestInfos.add(IF_NE);
    rtTestInfos.add(CMP_LONG);
    rtTestInfos.add(SGET);
    rtTestInfos.add(AGET_WIDE);
    rtTestInfos.add(IF_LTZ);
    rtTestInfos.add(OPC_INSTANCEOF);
    rtTestInfos.add(NEG_DOUBLE);
    rtTestInfos.add(RETURN_WIDE);
    rtTestInfos.add(FLOAT_TO_DOUBLE);
    rtTestInfos.add(APUT_SHORT);
    rtTestInfos.add(CMPG_DOUBLE);
    rtTestInfos.add(IF_LEZ);
    rtTestInfos.add(IF_EQ);
    rtTestInfos.add(NEW_ARRAY);
    rtTestInfos.add(SHR_LONG);
    rtTestInfos.add(ADD_DOUBLE);
    rtTestInfos.add(DIV_LONG);
    rtTestInfos.add(SPARSE_SWITCH);
    rtTestInfos.add(INVOKE_INTERFACE);
    rtTestInfos.add(APUT_WIDE);
    rtTestInfos.add(AGET_BYTE);
    rtTestInfos.add(APUT);
    rtTestInfos.add(SHR_INT);
    rtTestInfos.add(OPC_RETURN);
    rtTestInfos.add(INVOKE_SUPER);
    rtTestInfos.add(MUL_FLOAT);
    rtTestInfos.add(SUB_FLOAT);
    rtTestInfos.add(ADD_INT);
    rtTestInfos.add(INT_TO_CHAR);
    rtTestInfos.add(AND_INT);
    rtTestInfos.add(CMPG_FLOAT);
    rtTestInfos.add(IGET);
    rtTestInfos.add(REM_FLOAT);
  }
}
