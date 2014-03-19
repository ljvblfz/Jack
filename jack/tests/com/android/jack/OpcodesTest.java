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

package com.android.jack;
import org.junit.BeforeClass;
import org.junit.Test;
/**
 * JUnit test for compilation of arithmetic tests.
 */
public class OpcodesTest {
  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testCompile_add_double() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("add_double")));
  }

  @Test
  public void testCompile_float_to_long() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("float_to_long")));
  }

  @Test
  public void testCompile_array_length() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("array_length")));
  }

  @Test
  public void testCompile_sub_double() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("sub_double")));
  }

  @Test
  public void testCompile_int_to_short() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("int_to_short")));
  }

  @Test
  public void testCompile_aput() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("aput")));
  }

  @Test
  public void testCompile_if_gez() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("if_gez")));
  }

  @Test
  public void testCompile_check_cast() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("check_cast")));
  }

  @Test
  public void testCompile_cmpl_double() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("cmpl_double")));
  }

  @Test
  public void testCompile_double_to_int() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("double_to_int")));
  }

  @Test
  public void testCompile_int_to_byte() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("int_to_byte")));
  }

  @Test
  public void testCompile_shl_int() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("shl_int")));
  }

  @Test
  public void testCompile_long_to_double() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("long_to_double")));
  }

  @Test
  public void testCompile_if_ge() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("if_ge")));
  }

  @Test
  public void testCompile_opc_new() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("opc_new")));
  }

  @Test
  public void testCompile_add_float() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("add_float")));
  }

  @Test
  public void testCompile_cmp_long() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("cmp_long")));
  }

  @Test
  public void testCompile_opc_instanceof() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("opc_instanceof")));
  }

  @Test
  public void testCompile_int_to_char() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("int_to_char")));
  }

  @Test
  public void testCompile_aget_short() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("aget_short")));
  }

  @Test
  public void testCompile_and_int() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("and_int")));
  }

  @Test
  public void testCompile_if_eqz() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("if_eqz")));
  }

  @Test
  public void testCompile_opc_goto() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("opc_goto")));
  }

  @Test
  public void testCompile_if_gtz() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("if_gtz")));
  }

  @Test
  public void testCompile_or_int() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("or_int")));
  }

  @Test
  public void testCompile_int_to_double() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("int_to_double")));
  }

  @Test
  public void testCompile_xor_long() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("xor_long")));
  }

  @Test
  public void testCompile_rem_int() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("rem_int")));
  }

  @Test
  public void testCompile_opc_throw() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("opc_throw")));
  }

  @Test
  public void testCompile_shr_long() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("shr_long")));
  }

  @Test
  public void testCompile_cmpg_double() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("cmpg_double")));
  }

  @Test
  public void testCompile_mul_double() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("mul_double")));
  }

  @Test
  public void testCompile_cmpg_float() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("cmpg_float")));
  }

  @Test
  public void testCompile_mul_int() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("mul_int")));
  }

  @Test
  public void testCompile_neg_float() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("neg_float")));
  }

  @Test
  public void testCompile_div_double() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("div_double")));
  }

  @Test
  public void testCompile_monitor_enter() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("monitor_enter")));
  }

  @Test
  public void testCompile_and_long() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("and_long")));
  }

  @Test
  public void testCompile_if_ltz() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("if_ltz")));
  }

  @Test
  public void testCompile_sparse_switch() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("sparse_switch")));
  }

  @Test
  public void testCompile_sget() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("sget")));
  }

  @Test
  public void testCompile_ushr_long() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("ushr_long")));
  }

  @Test
  public void testCompile_const_wide() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("const_wide")));
  }

  @Test
  public void testCompile_aget() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("aget")));
  }

  @Test
  public void testCompile_iput() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("iput")));
  }

  @Test
  public void testCompile_sput() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("sput")));
  }

  @Test
  public void testCompile_return_object() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("return_object")));
  }

  @Test
  public void testCompile_invoke_virtual() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("invoke_virtual")));
  }

  @Test
  public void testCompile_aget_object() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("aget_object")));
  }

  @Test
  public void testCompile_neg_double() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("neg_double")));
  }

  @Test
  public void testCompile_int_to_float() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("int_to_float")));
  }

  @Test
  public void testCompile_invoke_static() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("invoke_static")));
  }

  @Test
  public void testCompile_rem_long() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("rem_long")));
  }

  @Test
  public void testCompile_aget_wide() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("aget_wide")));
  }

  @Test
  public void testCompile_float_to_double() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("float_to_double")));
  }

  @Test
  public void testCompile_if_lt() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("if_lt")));
  }

  @Test
  public void testCompile_double_to_float() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("double_to_float")));
  }

  @Test
  public void testCompile_double_to_long() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("double_to_long")));
  }

  @Test
  public void testCompile_return_wide() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("return_wide")));
  }

  @Test
  public void testCompile_add_long() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("add_long")));
  }

  @Test
  public void testCompile_shl_long() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("shl_long")));
  }

  @Test
  public void testCompile_aput_byte() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("aput_byte")));
  }

  @Test
  public void testCompile_aget_char() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("aget_char")));
  }

  @Test
  public void testCompile_const4_16() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("const4_16")));
  }

  @Test
  public void testCompile_sub_int() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("sub_int")));
  }

  @Test
  public void testCompile_neg_long() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("neg_long")));
  }

  @Test
  public void testCompile_opc_return() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("opc_return")));
  }

  @Test
  public void testCompile_sub_long() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("sub_long")));
  }

  @Test
  public void testCompile_long_to_float() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("long_to_float")));
  }

  @Test
  public void testCompile_if_le() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("if_le")));
  }

  @Test
  public void testCompile_aget_byte() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("aget_byte")));
  }

  @Test
  public void testCompile_iget() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("iget")));
  }

  @Test
  public void testCompile_aput_char() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("aput_char")));
  }

  @Test
  public void testCompile_int_to_long() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("int_to_long")));
  }

  @Test
  public void testCompile_div_float() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("div_float")));
  }

  @Test
  public void testCompile_if_nez() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("if_nez")));
  }

  @Test
  public void testCompile_div_int() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("div_int")));
  }

  @Test
  public void testCompile_float_to_int() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("float_to_int")));
  }

  @Test
  public void testCompile_long_to_int() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("long_to_int")));
  }

  @Test
  public void testCompile_cmpl_float() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("cmpl_float")));
  }

  @Test
  public void testCompile_shr_int() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("shr_int")));
  }

  @Test
  public void testCompile_if_eq() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("if_eq")));
  }

  @Test
  public void testCompile_add_int() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("add_int")));
  }

  @Test
  public void testCompile_or_long() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("or_long")));
  }

  @Test
  public void testCompile_mul_long() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("mul_long")));
  }

  @Test
  public void testCompile_if_lez() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("if_lez")));
  }

  @Test
  public void testCompile_sub_float() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("sub_float")));
  }

  @Test
  public void testCompile_rem_double() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("rem_double")));
  }

  @Test
  public void testCompile_neg_int() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("neg_int")));
  }

  @Test
  public void testCompile_aput_wide() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("aput_wide")));
  }

  @Test
  public void testCompile_div_long() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("div_long")));
  }

  @Test
  public void testCompile_xor_int() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("xor_int")));
  }

  @Test
  public void testCompile_aput_object() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("aput_object")));
  }

  @Test
  public void testCompile_new_array() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("new_array")));
  }

  @Test
  public void testCompile_aput_short() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("aput_short")));
  }

  @Test
  public void testCompile_invoke_direct() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("invoke_direct")));
  }

  @Test
  public void testCompile_invoke_super() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("invoke_super")));
  }

  @Test
  public void testCompile_if_ne() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("if_ne")));
  }

  @Test
  public void testCompile_rem_float() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("rem_float")));
  }

  @Test
  public void testCompile_ushr_int() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("ushr_int")));
  }

  @Test
  public void testCompile_invoke_interface() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("invoke_interface")));
  }

  @Test
  public void testCompile_mul_float() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("mul_float")));
  }

  @Test
  public void testCompile_if_gt() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("if_gt")));
  }

  @Test
  public void testCompile_packed_switch() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("packed_switch")));
  }

  @Test
  public void testCompile_const_string() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("const_string")));
  }

  @Test
  public void testCompile_opc_const() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools.getOpcodeTestFolder("opc_const")));
  }
}
