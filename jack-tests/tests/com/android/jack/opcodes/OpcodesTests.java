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

import com.android.jack.test.TestsProperties;
import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import javax.annotation.Nonnull;

public class OpcodesTests extends RuntimeTest {

  private static class OpcodesRuntimeInfos extends RuntimeTestInfo {

    public OpcodesRuntimeInfos(File directory, String jUnit) {
      super(directory, jUnit);
      setSrcDirName("jm");
      setRefDirName("ref");
      addReferenceExtraSources(new File(TestsProperties.getJackRootDir(),
          "jack-tests/src/com/android/jack/test/DxTestCase.java"));
    }
  }

  @Nonnull
  private OpcodesRuntimeInfos INVOKE_STATIC = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.invoke_static"),
    "com.android.jack.opcodes.invoke_static.ref.Test_invoke_static");

  @Nonnull
  private OpcodesRuntimeInfos ARRAY_LENGTH = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.array_length"),
    "com.android.jack.opcodes.array_length.ref.Test_array_length");

  @Nonnull
  private OpcodesRuntimeInfos NEG_FLOAT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.neg_float"),
    "com.android.jack.opcodes.neg_float.ref.Test_neg_float");

  @Nonnull
  private OpcodesRuntimeInfos SUB_DOUBLE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.sub_double"),
    "com.android.jack.opcodes.sub_double.ref.Test_sub_double");

  @Nonnull
  private OpcodesRuntimeInfos AGET = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aget"),
    "com.android.jack.opcodes.aget.ref.Test_aget");

  @Nonnull
  private OpcodesRuntimeInfos APUT_BYTE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aput_byte"),
    "com.android.jack.opcodes.aput_byte.ref.Test_aput_byte");

  @Nonnull
  private OpcodesRuntimeInfos OPC_NEW = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.opc_new"),
    "com.android.jack.opcodes.opc_new.ref.Test_opc_new");

  @Nonnull
  private OpcodesRuntimeInfos CMPL_DOUBLE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.cmpl_double"),
    "com.android.jack.opcodes.cmpl_double.ref.Test_cmpl_double");

  @Nonnull
  private OpcodesRuntimeInfos IF_GTZ = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_gtz"),
    "com.android.jack.opcodes.if_gtz.ref.Test_if_gtz");

  @Nonnull
  private OpcodesRuntimeInfos FLOAT_TO_LONG = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.float_to_long"),
    "com.android.jack.opcodes.float_to_long.ref.Test_float_to_long");

  @Nonnull
  private OpcodesRuntimeInfos ADD_LONG = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.add_long"),
    "com.android.jack.opcodes.add_long.ref.Test_add_long");

  @Nonnull
  private OpcodesRuntimeInfos ADD_FLOAT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.add_float"),
    "com.android.jack.opcodes.add_float.ref.Test_add_float");

  @Nonnull
  private OpcodesRuntimeInfos IPUT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.iput"),
    "com.android.jack.opcodes.iput.ref.Test_iput");

  @Nonnull
  private OpcodesRuntimeInfos DIV_FLOAT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.div_float"),
    "com.android.jack.opcodes.div_float.ref.Test_div_float");

  @Nonnull
  private OpcodesRuntimeInfos USHR_LONG = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.ushr_long"),
    "com.android.jack.opcodes.ushr_long.ref.Test_ushr_long");

  @Nonnull
  private OpcodesRuntimeInfos CONST_WIDE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.const_wide"),
    "com.android.jack.opcodes.const_wide.ref.Test_const_wide");

  @Nonnull
  private OpcodesRuntimeInfos XOR_INT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.xor_int"),
    "com.android.jack.opcodes.xor_int.ref.Test_xor_int");

  @Nonnull
  private OpcodesRuntimeInfos AGET_OBJECT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aget_object"),
    "com.android.jack.opcodes.aget_object.ref.Test_aget_object");

  @Nonnull
  private OpcodesRuntimeInfos CONST4_16 = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.const4_16"),
    "com.android.jack.opcodes.const4_16.ref.Test_const4_16");

  @Nonnull
  private OpcodesRuntimeInfos REM_LONG = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.rem_long"),
    "com.android.jack.opcodes.rem_long.ref.Test_rem_long");

  @Nonnull
  private OpcodesRuntimeInfos USHR_INT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.ushr_int"),
    "com.android.jack.opcodes.ushr_int.ref.Test_ushr_int");

  @Nonnull
  private OpcodesRuntimeInfos IF_GE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_ge"),
    "com.android.jack.opcodes.if_ge.ref.Test_if_ge");

  @Nonnull
  private OpcodesRuntimeInfos SUB_LONG = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.sub_long"),
    "com.android.jack.opcodes.sub_long.ref.Test_sub_long");

  @Nonnull
  private OpcodesRuntimeInfos FLOAT_TO_INT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.float_to_int"),
    "com.android.jack.opcodes.float_to_int.ref.Test_float_to_int");

  @Nonnull
  private OpcodesRuntimeInfos INT_TO_LONG = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.int_to_long"),
    "com.android.jack.opcodes.int_to_long.ref.Test_int_to_long");

  @Nonnull
  private OpcodesRuntimeInfos OR_LONG = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.or_long"),
    "com.android.jack.opcodes.or_long.ref.Test_or_long");

  @Nonnull
  private OpcodesRuntimeInfos IF_GEZ = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_gez"),
    "com.android.jack.opcodes.if_gez.ref.Test_if_gez");

  @Nonnull
  private OpcodesRuntimeInfos SHL_INT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.shl_int"),
    "com.android.jack.opcodes.shl_int.ref.Test_shl_int");

  @Nonnull
  private OpcodesRuntimeInfos LONG_TO_FLOAT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.long_to_float"),
    "com.android.jack.opcodes.long_to_float.ref.Test_long_to_float");

  @Nonnull
  private OpcodesRuntimeInfos DIV_INT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.div_int"),
    "com.android.jack.opcodes.div_int.ref.Test_div_int");

  @Nonnull
  private OpcodesRuntimeInfos AND_LONG = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.and_long"),
    "com.android.jack.opcodes.and_long.ref.Test_and_long");

  @Nonnull
  private OpcodesRuntimeInfos DOUBLE_TO_LONG = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.double_to_long"),
    "com.android.jack.opcodes.double_to_long.ref.Test_double_to_long");

  @Nonnull
  private OpcodesRuntimeInfos MUL_LONG = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.mul_long"),
    "com.android.jack.opcodes.mul_long.ref.Test_mul_long");

  @Nonnull
  private OpcodesRuntimeInfos DOUBLE_TO_INT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.double_to_int"),
    "com.android.jack.opcodes.double_to_int.ref.Test_double_to_int");

  @Nonnull
  private OpcodesRuntimeInfos IF_NEZ = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_nez"),
    "com.android.jack.opcodes.if_nez.ref.Test_if_nez");

  @Nonnull
  private OpcodesRuntimeInfos APUT_OBJECT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aput_object"),
    "com.android.jack.opcodes.aput_object.ref.Test_aput_object");

  @Nonnull
  private OpcodesRuntimeInfos IF_LT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_lt"),
    "com.android.jack.opcodes.if_lt.ref.Test_if_lt");

  @Nonnull
  private OpcodesRuntimeInfos INT_TO_DOUBLE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.int_to_double"),
    "com.android.jack.opcodes.int_to_double.ref.Test_int_to_double");

  @Nonnull
  private OpcodesRuntimeInfos MUL_INT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.mul_int"),
    "com.android.jack.opcodes.mul_int.ref.Test_mul_int");

  @Nonnull
  private OpcodesRuntimeInfos SPUT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.sput"),
    "com.android.jack.opcodes.sput.ref.Test_sput");

  @Nonnull
  private OpcodesRuntimeInfos SHL_LONG = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.shl_long"),
    "com.android.jack.opcodes.shl_long.ref.Test_shl_long");

  @Nonnull
  private OpcodesRuntimeInfos NEG_LONG = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.neg_long"),
    "com.android.jack.opcodes.neg_long.ref.Test_neg_long");

  @Nonnull
  private OpcodesRuntimeInfos LONG_TO_INT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.long_to_int"),
    "com.android.jack.opcodes.long_to_int.ref.Test_long_to_int");

  @Nonnull
  private OpcodesRuntimeInfos OPC_GOTO = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.opc_goto"),
    "com.android.jack.opcodes.opc_goto.ref.Test_opc_goto");

  @Nonnull
  private OpcodesRuntimeInfos INT_TO_FLOAT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.int_to_float"),
    "com.android.jack.opcodes.int_to_float.ref.Test_int_to_float");

  @Nonnull
  private OpcodesRuntimeInfos XOR_LONG = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.xor_long"),
    "com.android.jack.opcodes.xor_long.ref.Test_xor_long");

  @Nonnull
  private OpcodesRuntimeInfos MONITOR_ENTER = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.monitor_enter"),
    "com.android.jack.opcodes.monitor_enter.ref.Test_monitor_enter");

  @Nonnull
  private OpcodesRuntimeInfos IF_EQZ = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_eqz"),
    "com.android.jack.opcodes.if_eqz.ref.Test_if_eqz");

  @Nonnull
  private OpcodesRuntimeInfos INVOKE_DIRECT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.invoke_direct"),
    "com.android.jack.opcodes.invoke_direct.ref.Test_invoke_direct");

  @Nonnull
  private OpcodesRuntimeInfos CMPL_FLOAT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.cmpl_float"),
    "com.android.jack.opcodes.cmpl_float.ref.Test_cmpl_float");

  @Nonnull
  private OpcodesRuntimeInfos CHECK_CAST = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.check_cast"),
    "com.android.jack.opcodes.check_cast.ref.Test_check_cast");

  @Nonnull
  private OpcodesRuntimeInfos OPC_THROW = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.opc_throw"),
    "com.android.jack.opcodes.opc_throw.ref.Test_opc_throw");

  @Nonnull
  private OpcodesRuntimeInfos INT_TO_SHORT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.int_to_short"),
    "com.android.jack.opcodes.int_to_short.ref.Test_int_to_short");

  @Nonnull
  private OpcodesRuntimeInfos PACKED_SWITCH = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.packed_switch"),
    "com.android.jack.opcodes.packed_switch.ref.Test_packed_switch");

  @Nonnull
  private OpcodesRuntimeInfos AGET_CHAR = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aget_char"),
    "com.android.jack.opcodes.aget_char.ref.Test_aget_char");

  @Nonnull
  private OpcodesRuntimeInfos RETURN_OBJECT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.return_object"),
    "com.android.jack.opcodes.return_object.ref.Test_return_object");

  @Nonnull
  private OpcodesRuntimeInfos OPC_CONST = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.opc_const"),
    "com.android.jack.opcodes.opc_const.ref.Test_opc_const");

  @Nonnull
  private OpcodesRuntimeInfos SUB_INT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.sub_int"),
    "com.android.jack.opcodes.sub_int.ref.Test_sub_int");

  @Nonnull
  private OpcodesRuntimeInfos APUT_CHAR = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aput_char"),
    "com.android.jack.opcodes.aput_char.ref.Test_aput_char");

  @Nonnull
  private OpcodesRuntimeInfos NEG_INT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.neg_int"),
    "com.android.jack.opcodes.neg_int.ref.Test_neg_int");

  @Nonnull
  private OpcodesRuntimeInfos MUL_DOUBLE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.mul_double"),
    "com.android.jack.opcodes.mul_double.ref.Test_mul_double");

  @Nonnull
  private OpcodesRuntimeInfos DOUBLE_TO_FLOAT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.double_to_float"),
    "com.android.jack.opcodes.double_to_float.ref.Test_double_to_float");

  @Nonnull
  private OpcodesRuntimeInfos INT_TO_BYTE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.int_to_byte"),
    "com.android.jack.opcodes.int_to_byte.ref.Test_int_to_byte");

  @Nonnull
  private OpcodesRuntimeInfos IF_LE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_le"),
    "com.android.jack.opcodes.if_le.ref.Test_if_le");

  @Nonnull
  private OpcodesRuntimeInfos INVOKE_VIRTUAL = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.invoke_virtual"),
    "com.android.jack.opcodes.invoke_virtual.ref.Test_invoke_virtual");

  @Nonnull
  private OpcodesRuntimeInfos DIV_DOUBLE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.div_double"),
    "com.android.jack.opcodes.div_double.ref.Test_div_double");

  @Nonnull
  private OpcodesRuntimeInfos IF_GT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_gt"),
    "com.android.jack.opcodes.if_gt.ref.Test_if_gt");

  @Nonnull
  private OpcodesRuntimeInfos AGET_SHORT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aget_short"),
    "com.android.jack.opcodes.aget_short.ref.Test_aget_short");

  @Nonnull
  private OpcodesRuntimeInfos CONST_STRING = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.const_string"),
    "com.android.jack.opcodes.const_string.ref.Test_const_string");

  @Nonnull
  private OpcodesRuntimeInfos OR_INT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.or_int"),
    "com.android.jack.opcodes.or_int.ref.Test_or_int");

  @Nonnull
  private OpcodesRuntimeInfos REM_INT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.rem_int"),
    "com.android.jack.opcodes.rem_int.ref.Test_rem_int");

  @Nonnull
  private OpcodesRuntimeInfos REM_DOUBLE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.rem_double"),
    "com.android.jack.opcodes.rem_double.ref.Test_rem_double");

  @Nonnull
  private OpcodesRuntimeInfos LONG_TO_DOUBLE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.long_to_double"),
    "com.android.jack.opcodes.long_to_double.ref.Test_long_to_double");

  @Nonnull
  private OpcodesRuntimeInfos IF_NE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_ne"),
    "com.android.jack.opcodes.if_ne.ref.Test_if_ne");

  @Nonnull
  private OpcodesRuntimeInfos CMP_LONG = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.cmp_long"),
    "com.android.jack.opcodes.cmp_long.ref.Test_cmp_long");

  @Nonnull
  private OpcodesRuntimeInfos SGET = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.sget"),
    "com.android.jack.opcodes.sget.ref.Test_sget");

  @Nonnull
  private OpcodesRuntimeInfos AGET_WIDE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aget_wide"),
    "com.android.jack.opcodes.aget_wide.ref.Test_aget_wide");

  @Nonnull
  private OpcodesRuntimeInfos IF_LTZ = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_ltz"),
    "com.android.jack.opcodes.if_ltz.ref.Test_if_ltz");

  @Nonnull
  private OpcodesRuntimeInfos OPC_INSTANCEOF = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.opc_instanceof"),
    "com.android.jack.opcodes.opc_instanceof.ref.Test_opc_instanceof");

  @Nonnull
  private OpcodesRuntimeInfos NEG_DOUBLE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.neg_double"),
    "com.android.jack.opcodes.neg_double.ref.Test_neg_double");

  @Nonnull
  private OpcodesRuntimeInfos RETURN_WIDE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.return_wide"),
    "com.android.jack.opcodes.return_wide.ref.Test_return_wide");

  @Nonnull
  private OpcodesRuntimeInfos FLOAT_TO_DOUBLE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.float_to_double"),
    "com.android.jack.opcodes.float_to_double.ref.Test_float_to_double");

  @Nonnull
  private OpcodesRuntimeInfos APUT_SHORT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aput_short"),
    "com.android.jack.opcodes.aput_short.ref.Test_aput_short");

  @Nonnull
  private OpcodesRuntimeInfos CMPG_DOUBLE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.cmpg_double"),
    "com.android.jack.opcodes.cmpg_double.ref.Test_cmpg_double");

  @Nonnull
  private OpcodesRuntimeInfos IF_LEZ = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_lez"),
    "com.android.jack.opcodes.if_lez.ref.Test_if_lez");

  @Nonnull
  private OpcodesRuntimeInfos IF_EQ = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.if_eq"),
    "com.android.jack.opcodes.if_eq.ref.Test_if_eq");

  @Nonnull
  private OpcodesRuntimeInfos NEW_ARRAY = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.new_array"),
    "com.android.jack.opcodes.new_array.ref.Test_new_array");

  @Nonnull
  private OpcodesRuntimeInfos SHR_LONG = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.shr_long"),
    "com.android.jack.opcodes.shr_long.ref.Test_shr_long");

  @Nonnull
  private OpcodesRuntimeInfos ADD_DOUBLE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.add_double"),
    "com.android.jack.opcodes.add_double.ref.Test_add_double");

  @Nonnull
  private OpcodesRuntimeInfos DIV_LONG = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.div_long"),
    "com.android.jack.opcodes.div_long.ref.Test_div_long");

  @Nonnull
  private OpcodesRuntimeInfos SPARSE_SWITCH = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.sparse_switch"),
    "com.android.jack.opcodes.sparse_switch.ref.Test_sparse_switch");

  @Nonnull
  private OpcodesRuntimeInfos INVOKE_INTERFACE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.invoke_interface"),
    "com.android.jack.opcodes.invoke_interface.ref.Test_invoke_interface");

  @Nonnull
  private OpcodesRuntimeInfos APUT_WIDE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aput_wide"),
    "com.android.jack.opcodes.aput_wide.ref.Test_aput_wide");

  @Nonnull
  private OpcodesRuntimeInfos AGET_BYTE = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aget_byte"),
    "com.android.jack.opcodes.aget_byte.ref.Test_aget_byte");

  @Nonnull
  private OpcodesRuntimeInfos APUT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.aput"),
    "com.android.jack.opcodes.aput.ref.Test_aput");

  @Nonnull
  private OpcodesRuntimeInfos SHR_INT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.shr_int"),
    "com.android.jack.opcodes.shr_int.ref.Test_shr_int");

  @Nonnull
  private OpcodesRuntimeInfos OPC_RETURN = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.opc_return"),
    "com.android.jack.opcodes.opc_return.ref.Test_opc_return");

  @Nonnull
  private OpcodesRuntimeInfos INVOKE_SUPER = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.invoke_super"),
    "com.android.jack.opcodes.invoke_super.ref.Test_invoke_super");

  @Nonnull
  private OpcodesRuntimeInfos MUL_FLOAT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.mul_float"),
    "com.android.jack.opcodes.mul_float.ref.Test_mul_float");

  @Nonnull
  private OpcodesRuntimeInfos SUB_FLOAT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.sub_float"),
    "com.android.jack.opcodes.sub_float.ref.Test_sub_float");

  @Nonnull
  private OpcodesRuntimeInfos ADD_INT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.add_int"),
    "com.android.jack.opcodes.add_int.ref.Test_add_int");

  @Nonnull
  private OpcodesRuntimeInfos INT_TO_CHAR = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.int_to_char"),
    "com.android.jack.opcodes.int_to_char.ref.Test_int_to_char");

  @Nonnull
  private OpcodesRuntimeInfos AND_INT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.and_int"),
    "com.android.jack.opcodes.and_int.ref.Test_and_int");

  @Nonnull
  private OpcodesRuntimeInfos CMPG_FLOAT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.cmpg_float"),
    "com.android.jack.opcodes.cmpg_float.ref.Test_cmpg_float");

  @Nonnull
  private OpcodesRuntimeInfos IGET = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.iget"),
    "com.android.jack.opcodes.iget.ref.Test_iget");

  @Nonnull
  private OpcodesRuntimeInfos REM_FLOAT = new OpcodesRuntimeInfos(
    AbstractTestTools.getTestRootDir("com.android.jack.opcodes.rem_float"),
    "com.android.jack.opcodes.rem_float.ref.Test_rem_float");



  @Test
  @Category(RuntimeRegressionTest.class)
  public void invoke_static() throws Exception {
    new RuntimeTestHelper(INVOKE_STATIC)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void array_length() throws Exception {
    new RuntimeTestHelper(ARRAY_LENGTH)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void neg_float() throws Exception {
    new RuntimeTestHelper(NEG_FLOAT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void sub_double() throws Exception {
    new RuntimeTestHelper(SUB_DOUBLE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void aget() throws Exception {
    new RuntimeTestHelper(AGET)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void aput_byte() throws Exception {
    new RuntimeTestHelper(APUT_BYTE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void opc_new() throws Exception {
    new RuntimeTestHelper(OPC_NEW)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void cmpl_double() throws Exception {
    new RuntimeTestHelper(CMPL_DOUBLE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void if_gtz() throws Exception {
    new RuntimeTestHelper(IF_GTZ)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void float_to_long() throws Exception {
    new RuntimeTestHelper(FLOAT_TO_LONG)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void add_long() throws Exception {
    new RuntimeTestHelper(ADD_LONG)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void add_float() throws Exception {
    new RuntimeTestHelper(ADD_FLOAT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void iput() throws Exception {
    new RuntimeTestHelper(IPUT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void div_float() throws Exception {
    new RuntimeTestHelper(DIV_FLOAT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void ushr_long() throws Exception {
    new RuntimeTestHelper(USHR_LONG)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void const_wide() throws Exception {
    new RuntimeTestHelper(CONST_WIDE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void xor_int() throws Exception {
    new RuntimeTestHelper(XOR_INT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void aget_object() throws Exception {
    new RuntimeTestHelper(AGET_OBJECT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void const4_16() throws Exception {
    new RuntimeTestHelper(CONST4_16)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void rem_long() throws Exception {
    new RuntimeTestHelper(REM_LONG)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void ushr_int() throws Exception {
    new RuntimeTestHelper(USHR_INT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void if_ge() throws Exception {
    new RuntimeTestHelper(IF_GE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void sub_long() throws Exception {
    new RuntimeTestHelper(SUB_LONG)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void float_to_int() throws Exception {
    new RuntimeTestHelper(FLOAT_TO_INT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void int_to_long() throws Exception {
    new RuntimeTestHelper(INT_TO_LONG)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void or_long() throws Exception {
    new RuntimeTestHelper(OR_LONG)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void if_gez() throws Exception {
    new RuntimeTestHelper(IF_GEZ)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void shl_int() throws Exception {
    new RuntimeTestHelper(SHL_INT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void long_to_float() throws Exception {
    new RuntimeTestHelper(LONG_TO_FLOAT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void div_int() throws Exception {
    new RuntimeTestHelper(DIV_INT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void and_long() throws Exception {
    new RuntimeTestHelper(AND_LONG)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void double_to_long() throws Exception {
    new RuntimeTestHelper(DOUBLE_TO_LONG)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void mul_long() throws Exception {
    new RuntimeTestHelper(MUL_LONG)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void double_to_int() throws Exception {
    new RuntimeTestHelper(DOUBLE_TO_INT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void if_nez() throws Exception {
    new RuntimeTestHelper(IF_NEZ)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void aput_object() throws Exception {
    new RuntimeTestHelper(APUT_OBJECT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void if_lt() throws Exception {
    new RuntimeTestHelper(IF_LT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void int_to_double() throws Exception {
    new RuntimeTestHelper(INT_TO_DOUBLE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void mul_int() throws Exception {
    new RuntimeTestHelper(MUL_INT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void sput() throws Exception {
    new RuntimeTestHelper(SPUT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void shl_long() throws Exception {
    new RuntimeTestHelper(SHL_LONG)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void neg_long() throws Exception {
    new RuntimeTestHelper(NEG_LONG)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void long_to_int() throws Exception {
    new RuntimeTestHelper(LONG_TO_INT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void opc_goto() throws Exception {
    new RuntimeTestHelper(OPC_GOTO)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void int_to_float() throws Exception {
    new RuntimeTestHelper(INT_TO_FLOAT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void xor_long() throws Exception {
    new RuntimeTestHelper(XOR_LONG)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void monitor_enter() throws Exception {
    new RuntimeTestHelper(MONITOR_ENTER)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void if_eqz() throws Exception {
    new RuntimeTestHelper(IF_EQZ)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void invoke_direct() throws Exception {
    new RuntimeTestHelper(INVOKE_DIRECT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void cmpl_float() throws Exception {
    new RuntimeTestHelper(CMPL_FLOAT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void check_cast() throws Exception {
    new RuntimeTestHelper(CHECK_CAST)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void opc_throw() throws Exception {
    new RuntimeTestHelper(OPC_THROW)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void int_to_short() throws Exception {
    new RuntimeTestHelper(INT_TO_SHORT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void packed_switch() throws Exception {
    new RuntimeTestHelper(PACKED_SWITCH)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void aget_char() throws Exception {
    new RuntimeTestHelper(AGET_CHAR)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void return_object() throws Exception {
    new RuntimeTestHelper(RETURN_OBJECT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void opc_const() throws Exception {
    new RuntimeTestHelper(OPC_CONST)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void sub_int() throws Exception {
    new RuntimeTestHelper(SUB_INT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void aput_char() throws Exception {
    new RuntimeTestHelper(APUT_CHAR)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void neg_int() throws Exception {
    new RuntimeTestHelper(NEG_INT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void mul_double() throws Exception {
    new RuntimeTestHelper(MUL_DOUBLE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void double_to_float() throws Exception {
    new RuntimeTestHelper(DOUBLE_TO_FLOAT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void int_to_byte() throws Exception {
    new RuntimeTestHelper(INT_TO_BYTE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void if_le() throws Exception {
    new RuntimeTestHelper(IF_LE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void invoke_virtual() throws Exception {
    new RuntimeTestHelper(INVOKE_VIRTUAL)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void div_double() throws Exception {
    new RuntimeTestHelper(DIV_DOUBLE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void if_gt() throws Exception {
    new RuntimeTestHelper(IF_GT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void aget_short() throws Exception {
    new RuntimeTestHelper(AGET_SHORT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void const_string() throws Exception {
    new RuntimeTestHelper(CONST_STRING)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void or_int() throws Exception {
    new RuntimeTestHelper(OR_INT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void rem_int() throws Exception {
    new RuntimeTestHelper(REM_INT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void rem_double() throws Exception {
    new RuntimeTestHelper(REM_DOUBLE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void long_to_double() throws Exception {
    new RuntimeTestHelper(LONG_TO_DOUBLE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void if_ne() throws Exception {
    new RuntimeTestHelper(IF_NE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void cmp_long() throws Exception {
    new RuntimeTestHelper(CMP_LONG)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void sget() throws Exception {
    new RuntimeTestHelper(SGET)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void aget_wide() throws Exception {
    new RuntimeTestHelper(AGET_WIDE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void if_ltz() throws Exception {
    new RuntimeTestHelper(IF_LTZ)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void opc_instanceof() throws Exception {
    new RuntimeTestHelper(OPC_INSTANCEOF)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void neg_double() throws Exception {
    new RuntimeTestHelper(NEG_DOUBLE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void return_wide() throws Exception {
    new RuntimeTestHelper(RETURN_WIDE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void float_to_double() throws Exception {
    new RuntimeTestHelper(FLOAT_TO_DOUBLE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void aput_short() throws Exception {
    new RuntimeTestHelper(APUT_SHORT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void cmpg_double() throws Exception {
    new RuntimeTestHelper(CMPG_DOUBLE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void if_lez() throws Exception {
    new RuntimeTestHelper(IF_LEZ)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void if_eq() throws Exception {
    new RuntimeTestHelper(IF_EQ)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void new_array() throws Exception {
    new RuntimeTestHelper(NEW_ARRAY)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void shr_long() throws Exception {
    new RuntimeTestHelper(SHR_LONG)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void add_double() throws Exception {
    new RuntimeTestHelper(ADD_DOUBLE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void div_long() throws Exception {
    new RuntimeTestHelper(DIV_LONG)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void sparse_switch() throws Exception {
    new RuntimeTestHelper(SPARSE_SWITCH)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void invoke_interface() throws Exception {
    new RuntimeTestHelper(INVOKE_INTERFACE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void aput_wide() throws Exception {
    new RuntimeTestHelper(APUT_WIDE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void aget_byte() throws Exception {
    new RuntimeTestHelper(AGET_BYTE)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void aput() throws Exception {
    new RuntimeTestHelper(APUT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void shr_int() throws Exception {
    new RuntimeTestHelper(SHR_INT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void opc_return() throws Exception {
    new RuntimeTestHelper(OPC_RETURN)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void invoke_super() throws Exception {
    new RuntimeTestHelper(INVOKE_SUPER)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void mul_float() throws Exception {
    new RuntimeTestHelper(MUL_FLOAT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void sub_float() throws Exception {
    new RuntimeTestHelper(SUB_FLOAT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void add_int() throws Exception {
    new RuntimeTestHelper(ADD_INT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void int_to_char() throws Exception {
    new RuntimeTestHelper(INT_TO_CHAR)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void and_int() throws Exception {
    new RuntimeTestHelper(AND_INT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void cmpg_float() throws Exception {
    new RuntimeTestHelper(CMPG_FLOAT)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void iget() throws Exception {
    new RuntimeTestHelper(IGET)
    .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void rem_float() throws Exception {
    new RuntimeTestHelper(REM_FLOAT)
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
