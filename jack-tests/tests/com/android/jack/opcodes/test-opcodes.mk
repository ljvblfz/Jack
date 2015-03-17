# Copyright (C) 2012 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

### add_double ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-add_double
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/add_double/Test_add_double.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/add_double/jm/T_add_double_1.java tests/com/android/jack/opcodes/add_double/jm/T_add_double_3.java tests/com/android/jack/opcodes/add_double/jm/T_add_double_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.add_double.Test_add_double

include $(JACK_RUN_TEST)

### float_to_long ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-float_to_long
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/float_to_long/Test_float_to_long.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/float_to_long/jm/T_float_to_long_1.java tests/com/android/jack/opcodes/float_to_long/jm/T_float_to_long_3.java tests/com/android/jack/opcodes/float_to_long/jm/T_float_to_long_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.float_to_long.Test_float_to_long

include $(JACK_RUN_TEST)

### array_length ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-array_length
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/array_length/Test_array_length.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/array_length/jm/T_array_length_1.java tests/com/android/jack/opcodes/array_length/jm/T_array_length_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.array_length.Test_array_length

include $(JACK_RUN_TEST)

### sub_double ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-sub_double
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/sub_double/Test_sub_double.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/sub_double/jm/T_sub_double_1.java tests/com/android/jack/opcodes/sub_double/jm/T_sub_double_3.java tests/com/android/jack/opcodes/sub_double/jm/T_sub_double_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.sub_double.Test_sub_double

include $(JACK_RUN_TEST)

### int_to_short ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-int_to_short
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/int_to_short/Test_int_to_short.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/int_to_short/jm/T_int_to_short_1.java tests/com/android/jack/opcodes/int_to_short/jm/T_int_to_short_3.java tests/com/android/jack/opcodes/int_to_short/jm/T_int_to_short_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.int_to_short.Test_int_to_short

include $(JACK_RUN_TEST)

### int_to_long ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-int_to_long
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/int_to_long/Test_int_to_long.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/int_to_long/jm/T_int_to_long_1.java tests/com/android/jack/opcodes/int_to_long/jm/T_int_to_long_3.java tests/com/android/jack/opcodes/int_to_long/jm/T_int_to_long_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.int_to_long.Test_int_to_long

include $(JACK_RUN_TEST)

### aput ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-aput
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/aput/Test_aput.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/aput/jm/T_aput_1.java tests/com/android/jack/opcodes/aput/jm/T_aput_2.java tests/com/android/jack/opcodes/aput/jm/T_aput_3.java tests/com/android/jack/opcodes/aput/jm/T_aput_4.java tests/com/android/jack/opcodes/aput/jm/T_aput_5.java tests/com/android/jack/opcodes/aput/jm/T_aput_6.java
JACKTEST_JUNIT := com.android.jack.opcodes.aput.Test_aput

include $(JACK_RUN_TEST)

### if_gez ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-if_gez
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/if_gez/Test_if_gez.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/if_gez/jm/T_if_gez_1.java tests/com/android/jack/opcodes/if_gez/jm/T_if_gez_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.if_gez.Test_if_gez

include $(JACK_RUN_TEST)

### check_cast ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-check_cast
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/check_cast/Test_check_cast.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/check_cast/jm/T_check_cast_1.java
JACKTEST_JUNIT := com.android.jack.opcodes.check_cast.Test_check_cast

include $(JACK_RUN_TEST)

### cmpl_double ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-cmpl_double
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/cmpl_double/Test_cmpl_double.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/cmpl_double/jm/T_cmpl_double_1.java
JACKTEST_JUNIT := com.android.jack.opcodes.cmpl_double.Test_cmpl_double

include $(JACK_RUN_TEST)

### double_to_int ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-double_to_int
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/double_to_int/Test_double_to_int.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/double_to_int/jm/T_double_to_int_1.java tests/com/android/jack/opcodes/double_to_int/jm/T_double_to_int_3.java tests/com/android/jack/opcodes/double_to_int/jm/T_double_to_int_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.double_to_int.Test_double_to_int

include $(JACK_RUN_TEST)

### int_to_byte ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-int_to_byte
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/int_to_byte/Test_int_to_byte.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/int_to_byte/jm/T_int_to_byte_1.java tests/com/android/jack/opcodes/int_to_byte/jm/T_int_to_byte_3.java tests/com/android/jack/opcodes/int_to_byte/jm/T_int_to_byte_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.int_to_byte.Test_int_to_byte

include $(JACK_RUN_TEST)

### shl_int ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-shl_int
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/shl_int/Test_shl_int.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/shl_int/jm/T_shl_int_1.java tests/com/android/jack/opcodes/shl_int/jm/T_shl_int_3.java tests/com/android/jack/opcodes/shl_int/jm/T_shl_int_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.shl_int.Test_shl_int

include $(JACK_RUN_TEST)

### long_to_double ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-long_to_double
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/long_to_double/Test_long_to_double.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/long_to_double/jm/T_long_to_double_1.java tests/com/android/jack/opcodes/long_to_double/jm/T_long_to_double_3.java tests/com/android/jack/opcodes/long_to_double/jm/T_long_to_double_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.long_to_double.Test_long_to_double

include $(JACK_RUN_TEST)

### if_ge ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-if_ge
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/if_ge/Test_if_ge.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/if_ge/jm/T_if_ge_1.java tests/com/android/jack/opcodes/if_ge/jm/T_if_ge_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.if_ge.Test_if_ge

include $(JACK_RUN_TEST)

### opc_new ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-opc_new
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/opc_new/Test_opc_new.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/opc_new/jm/TestStubs.java tests/com/android/jack/opcodes/opc_new/jm/T_opc_new_1.java tests/com/android/jack/opcodes/opc_new/jm/T_opc_new_3.java tests/com/android/jack/opcodes/opc_new/jm/T_opc_new_9.java
JACKTEST_JUNIT := com.android.jack.opcodes.opc_new.Test_opc_new

include $(JACK_RUN_TEST)

### add_float ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-add_float
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/add_float/Test_add_float.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/add_float/jm/T_add_float_1.java tests/com/android/jack/opcodes/add_float/jm/T_add_float_3.java tests/com/android/jack/opcodes/add_float/jm/T_add_float_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.add_float.Test_add_float

include $(JACK_RUN_TEST)

### cmp_long ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-cmp_long
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/cmp_long/Test_cmp_long.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/cmp_long/jm/T_cmp_long_1.java tests/com/android/jack/opcodes/cmp_long/jm/T_cmp_long_3.java tests/com/android/jack/opcodes/cmp_long/jm/T_cmp_long_4.java tests/com/android/jack/opcodes/cmp_long/jm/T_cmp_long_5.java
JACKTEST_JUNIT := com.android.jack.opcodes.cmp_long.Test_cmp_long

include $(JACK_RUN_TEST)

### opc_instanceof ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-opc_instanceof
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/opc_instanceof/Test_opc_instanceof.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/opc_instanceof/jm/TestStubs.java tests/com/android/jack/opcodes/opc_instanceof/jm/T_opc_instanceof_1.java
JACKTEST_JUNIT := com.android.jack.opcodes.opc_instanceof.Test_opc_instanceof

include $(JACK_RUN_TEST)

### int_to_char ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-int_to_char
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/int_to_char/Test_int_to_char.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/int_to_char/jm/T_int_to_char_1.java tests/com/android/jack/opcodes/int_to_char/jm/T_int_to_char_3.java tests/com/android/jack/opcodes/int_to_char/jm/T_int_to_char_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.int_to_char.Test_int_to_char

include $(JACK_RUN_TEST)

### aget_short ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-aget_short
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/aget_short/Test_aget_short.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/aget_short/jm/T_aget_short_1.java tests/com/android/jack/opcodes/aget_short/jm/T_aget_short_4.java tests/com/android/jack/opcodes/aget_short/jm/T_aget_short_5.java
JACKTEST_JUNIT := com.android.jack.opcodes.aget_short.Test_aget_short

include $(JACK_RUN_TEST)

### and_int ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-and_int
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/and_int/Test_and_int.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/and_int/jm/T_and_int_1.java tests/com/android/jack/opcodes/and_int/jm/T_and_int_3.java tests/com/android/jack/opcodes/and_int/jm/T_and_int_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.and_int.Test_and_int

include $(JACK_RUN_TEST)

### if_eqz ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-if_eqz
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/if_eqz/Test_if_eqz.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/if_eqz/jm/T_if_eqz_1.java tests/com/android/jack/opcodes/if_eqz/jm/T_if_eqz_2.java tests/com/android/jack/opcodes/if_eqz/jm/T_if_eqz_3.java tests/com/android/jack/opcodes/if_eqz/jm/T_if_eqz_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.if_eqz.Test_if_eqz

include $(JACK_RUN_TEST)

### opc_goto ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-opc_goto
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/opc_goto/Test_opc_goto.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/opc_goto/jm/T_opc_goto_1.java tests/com/android/jack/opcodes/opc_goto/jm/T_opc_goto_2.java tests/com/android/jack/opcodes/opc_goto/jm/T_opc_goto_5.java
JACKTEST_JUNIT := com.android.jack.opcodes.opc_goto.Test_opc_goto

include $(JACK_RUN_TEST)

### if_gtz ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-if_gtz
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/if_gtz/Test_if_gtz.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/if_gtz/jm/T_if_gtz_1.java tests/com/android/jack/opcodes/if_gtz/jm/T_if_gtz_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.if_gtz.Test_if_gtz

include $(JACK_RUN_TEST)

### or_int ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-or_int
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/or_int/Test_or_int.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/or_int/jm/T_or_int_1.java tests/com/android/jack/opcodes/or_int/jm/T_or_int_3.java tests/com/android/jack/opcodes/or_int/jm/T_or_int_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.or_int.Test_or_int

include $(JACK_RUN_TEST)

### int_to_double ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-int_to_double
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/int_to_double/Test_int_to_double.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/int_to_double/jm/T_int_to_double_1.java tests/com/android/jack/opcodes/int_to_double/jm/T_int_to_double_3.java tests/com/android/jack/opcodes/int_to_double/jm/T_int_to_double_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.int_to_double.Test_int_to_double

include $(JACK_RUN_TEST)

### xor_long ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-xor_long
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/xor_long/Test_xor_long.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/xor_long/jm/T_xor_long_1.java tests/com/android/jack/opcodes/xor_long/jm/T_xor_long_3.java tests/com/android/jack/opcodes/xor_long/jm/T_xor_long_4.java tests/com/android/jack/opcodes/xor_long/jm/T_xor_long_5.java
JACKTEST_JUNIT := com.android.jack.opcodes.xor_long.Test_xor_long

include $(JACK_RUN_TEST)

### rem_int ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-rem_int
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/rem_int/Test_rem_int.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/rem_int/jm/T_rem_int_1.java tests/com/android/jack/opcodes/rem_int/jm/T_rem_int_3.java tests/com/android/jack/opcodes/rem_int/jm/T_rem_int_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.rem_int.Test_rem_int

include $(JACK_RUN_TEST)

### opc_throw ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-opc_throw
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/opc_throw/Test_opc_throw.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/opc_throw/jm/T_opc_throw_12.java tests/com/android/jack/opcodes/opc_throw/jm/T_opc_throw_1.java tests/com/android/jack/opcodes/opc_throw/jm/T_opc_throw_2.java tests/com/android/jack/opcodes/opc_throw/jm/T_opc_throw_8.java
JACKTEST_JUNIT := com.android.jack.opcodes.opc_throw.Test_opc_throw

include $(JACK_RUN_TEST)

### shr_long ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-shr_long
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/shr_long/Test_shr_long.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/shr_long/jm/T_shr_long_1.java tests/com/android/jack/opcodes/shr_long/jm/T_shr_long_3.java tests/com/android/jack/opcodes/shr_long/jm/T_shr_long_4.java tests/com/android/jack/opcodes/shr_long/jm/T_shr_long_5.java
JACKTEST_JUNIT := com.android.jack.opcodes.shr_long.Test_shr_long

include $(JACK_RUN_TEST)

### cmpg_double ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-cmpg_double
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/cmpg_double/Test_cmpg_double.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/cmpg_double/jm/T_cmpg_double_1.java tests/com/android/jack/opcodes/cmpg_double/jm/T_cmpg_double_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.cmpg_double.Test_cmpg_double

include $(JACK_RUN_TEST)

### mul_double ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-mul_double
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/mul_double/Test_mul_double.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/mul_double/jm/T_mul_double_1.java tests/com/android/jack/opcodes/mul_double/jm/T_mul_double_3.java tests/com/android/jack/opcodes/mul_double/jm/T_mul_double_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.mul_double.Test_mul_double

include $(JACK_RUN_TEST)

### cmpg_float ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-cmpg_float
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/cmpg_float/Test_cmpg_float.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/cmpg_float/jm/T_cmpg_float_1.java tests/com/android/jack/opcodes/cmpg_float/jm/T_cmpg_float_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.cmpg_float.Test_cmpg_float

include $(JACK_RUN_TEST)

### mul_int ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-mul_int
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/mul_int/Test_mul_int.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/mul_int/jm/T_mul_int_1.java tests/com/android/jack/opcodes/mul_int/jm/T_mul_int_3.java tests/com/android/jack/opcodes/mul_int/jm/T_mul_int_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.mul_int.Test_mul_int

include $(JACK_RUN_TEST)

### neg_float ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-neg_float
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/neg_float/Test_neg_float.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/neg_float/jm/T_neg_float_1.java
JACKTEST_JUNIT := com.android.jack.opcodes.neg_float.Test_neg_float

include $(JACK_RUN_TEST)

### div_double ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-div_double
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/div_double/Test_div_double.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/div_double/jm/T_div_double_1.java tests/com/android/jack/opcodes/div_double/jm/T_div_double_3.java tests/com/android/jack/opcodes/div_double/jm/T_div_double_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.div_double.Test_div_double

include $(JACK_RUN_TEST)

### monitor_enter ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-monitor_enter
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/monitor_enter/Test_monitor_enter.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/monitor_enter/jm/T_monitor_enter_1.java tests/com/android/jack/opcodes/monitor_enter/jm/T_monitor_enter_2.java tests/com/android/jack/opcodes/monitor_enter/jm/T_monitor_enter_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.monitor_enter.Test_monitor_enter

include $(JACK_RUN_TEST)

### and_long ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-and_long
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/and_long/Test_and_long.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/and_long/jm/T_and_long_1.java tests/com/android/jack/opcodes/and_long/jm/T_and_long_3.java tests/com/android/jack/opcodes/and_long/jm/T_and_long_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.and_long.Test_and_long

include $(JACK_RUN_TEST)

### if_ltz ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-if_ltz
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/if_ltz/Test_if_ltz.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/if_ltz/jm/T_if_ltz_1.java tests/com/android/jack/opcodes/if_ltz/jm/T_if_ltz_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.if_ltz.Test_if_ltz

include $(JACK_RUN_TEST)

### sparse_switch ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-sparse_switch
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/sparse_switch/Test_sparse_switch.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/sparse_switch/jm/T_sparse_switch_1.java
JACKTEST_JUNIT := com.android.jack.opcodes.sparse_switch.Test_sparse_switch

include $(JACK_RUN_TEST)

### sget ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-sget
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/sget/TestStubs.java tests/com/android/jack/opcodes/sget/Test_sget.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/sget/jm/StubInitError.java tests/com/android/jack/opcodes/sget/jm/T_sget_11.java tests/com/android/jack/opcodes/sget/jm/T_sget_1.java tests/com/android/jack/opcodes/sget/jm/T_sget_2.java tests/com/android/jack/opcodes/sget/jm/T_sget_4.java tests/com/android/jack/opcodes/sget/jm/T_sget_5.java tests/com/android/jack/opcodes/sget/jm/T_sget_9.java
JACKTEST_JUNIT := com.android.jack.opcodes.sget.Test_sget

include $(JACK_RUN_TEST)

### ushr_long ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-ushr_long
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/ushr_long/Test_ushr_long.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/ushr_long/jm/T_ushr_long_1.java tests/com/android/jack/opcodes/ushr_long/jm/T_ushr_long_3.java tests/com/android/jack/opcodes/ushr_long/jm/T_ushr_long_4.java tests/com/android/jack/opcodes/ushr_long/jm/T_ushr_long_5.java
JACKTEST_JUNIT := com.android.jack.opcodes.ushr_long.Test_ushr_long

include $(JACK_RUN_TEST)

### const_wide ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-const_wide
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/const_wide/Test_const_wide.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/const_wide/jm/T_const_wide_1.java tests/com/android/jack/opcodes/const_wide/jm/T_const_wide_2.java tests/com/android/jack/opcodes/const_wide/jm/T_const_wide_3.java tests/com/android/jack/opcodes/const_wide/jm/T_const_wide_4.java tests/com/android/jack/opcodes/const_wide/jm/T_const_wide_5.java tests/com/android/jack/opcodes/const_wide/jm/T_const_wide_6.java tests/com/android/jack/opcodes/const_wide/jm/T_const_wide_7.java
JACKTEST_JUNIT := com.android.jack.opcodes.const_wide.Test_const_wide

include $(JACK_RUN_TEST)

### aget ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-aget
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/aget/Test_aget.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/aget/jm/T_aget_1.java tests/com/android/jack/opcodes/aget/jm/T_aget_2.java tests/com/android/jack/opcodes/aget/jm/T_aget_3.java tests/com/android/jack/opcodes/aget/jm/T_aget_4.java tests/com/android/jack/opcodes/aget/jm/T_aget_5.java tests/com/android/jack/opcodes/aget/jm/T_aget_6.java
JACKTEST_JUNIT := com.android.jack.opcodes.aget.Test_aget

include $(JACK_RUN_TEST)

### iput ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-iput
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/iput/Test_iput.java tests/com/android/jack/opcodes/iput/TIput.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/iput/jm/TSuper.java tests/com/android/jack/opcodes/iput/jm/T_iput_12.java tests/com/android/jack/opcodes/iput/jm/T_iput_14.java tests/com/android/jack/opcodes/iput/jm/T_iput_16.java tests/com/android/jack/opcodes/iput/jm/T_iput_18.java tests/com/android/jack/opcodes/iput/jm/T_iput_1.java tests/com/android/jack/opcodes/iput/jm/T_iput_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.iput.Test_iput

include $(JACK_RUN_TEST)

### sput ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-sput
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/sput/TestStubs.java tests/com/android/jack/opcodes/sput/Test_sput.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/sput/jm/StubInitError.java tests/com/android/jack/opcodes/sput/jm/T_sput_13.java tests/com/android/jack/opcodes/sput/jm/T_sput_14.java tests/com/android/jack/opcodes/sput/jm/T_sput_16.java tests/com/android/jack/opcodes/sput/jm/T_sput_18.java tests/com/android/jack/opcodes/sput/jm/T_sput_1.java tests/com/android/jack/opcodes/sput/jm/T_sput_2.java tests/com/android/jack/opcodes/sput/jm/T_sput_6.java
JACKTEST_JUNIT := com.android.jack.opcodes.sput.Test_sput

include $(JACK_RUN_TEST)

### return_object ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-return_object
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/return_object/Test_return_object.java tests/com/android/jack/opcodes/return_object/Runner.java tests/com/android/jack/opcodes/return_object/RunnerGenerator.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/return_object/jm/T_return_object_12.java tests/com/android/jack/opcodes/return_object/jm/T_return_object_13.java tests/com/android/jack/opcodes/return_object/jm/T_return_object_15.java tests/com/android/jack/opcodes/return_object/jm/T_return_object_1.java tests/com/android/jack/opcodes/return_object/jm/T_return_object_2.java tests/com/android/jack/opcodes/return_object/jm/T_return_object_3.java tests/com/android/jack/opcodes/return_object/jm/T_return_object_6.java tests/com/android/jack/opcodes/return_object/jm/T_return_object_7.java tests/com/android/jack/opcodes/return_object/jm/T_return_object_9.java tests/com/android/jack/opcodes/return_object/jm/TInterface.java
JACKTEST_JUNIT := com.android.jack.opcodes.return_object.Test_return_object

include $(JACK_RUN_TEST)

### invoke_virtual ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-invoke_virtual
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/invoke_virtual/TProtected.java tests/com/android/jack/opcodes/invoke_virtual/Test_invoke_virtual.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/invoke_virtual/jm/ATest.java tests/com/android/jack/opcodes/invoke_virtual/jm/T_invoke_virtual_13.java tests/com/android/jack/opcodes/invoke_virtual/jm/T_invoke_virtual_14.java tests/com/android/jack/opcodes/invoke_virtual/jm/T_invoke_virtual_17.java tests/com/android/jack/opcodes/invoke_virtual/jm/T_invoke_virtual_19.java tests/com/android/jack/opcodes/invoke_virtual/jm/T_invoke_virtual_1.java tests/com/android/jack/opcodes/invoke_virtual/jm/T_invoke_virtual_22.java tests/com/android/jack/opcodes/invoke_virtual/jm/T_invoke_virtual_23.java tests/com/android/jack/opcodes/invoke_virtual/jm/T_invoke_virtual_2.java tests/com/android/jack/opcodes/invoke_virtual/jm/T_invoke_virtual_4.java tests/com/android/jack/opcodes/invoke_virtual/jm/T_invoke_virtual_7.java tests/com/android/jack/opcodes/invoke_virtual/jm/TPlain.java tests/com/android/jack/opcodes/invoke_virtual/jm/TSuper2.java tests/com/android/jack/opcodes/invoke_virtual/jm/TSuper.java
JACKTEST_JUNIT := com.android.jack.opcodes.invoke_virtual.Test_invoke_virtual

include $(JACK_RUN_TEST)

### aget_object ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-aget_object
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/aget_object/Test_aget_object.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/aget_object/jm/T_aget_object_1.java tests/com/android/jack/opcodes/aget_object/jm/T_aget_object_4.java tests/com/android/jack/opcodes/aget_object/jm/T_aget_object_5.java tests/com/android/jack/opcodes/aget_object/jm/T_aget_object_6.java tests/com/android/jack/opcodes/aget_object/jm/T_aget_object_7.java tests/com/android/jack/opcodes/aget_object/jm/T_aget_object_8.java tests/com/android/jack/opcodes/aget_object/jm/T_aget_object_9.java
JACKTEST_JUNIT := com.android.jack.opcodes.aget_object.Test_aget_object

include $(JACK_RUN_TEST)

### neg_double ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-neg_double
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/neg_double/Test_neg_double.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/neg_double/jm/T_neg_double_1.java
JACKTEST_JUNIT := com.android.jack.opcodes.neg_double.Test_neg_double

include $(JACK_RUN_TEST)

### int_to_float ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-int_to_float
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/int_to_float/Test_int_to_float.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/int_to_float/jm/T_int_to_float_1.java tests/com/android/jack/opcodes/int_to_float/jm/T_int_to_float_3.java tests/com/android/jack/opcodes/int_to_float/jm/T_int_to_float_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.int_to_float.Test_int_to_float

include $(JACK_RUN_TEST)

### invoke_static ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-invoke_static
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/invoke_static/TestStubs.java tests/com/android/jack/opcodes/invoke_static/Test_invoke_static.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/invoke_static/jm/TestClassAbstract.java tests/com/android/jack/opcodes/invoke_static/jm/TestClassInitError.java tests/com/android/jack/opcodes/invoke_static/jm/TestClass.java tests/com/android/jack/opcodes/invoke_static/jm/T_invoke_static_12.java tests/com/android/jack/opcodes/invoke_static/jm/T_invoke_static_13.java tests/com/android/jack/opcodes/invoke_static/jm/T_invoke_static_14.java tests/com/android/jack/opcodes/invoke_static/jm/T_invoke_static_15.java tests/com/android/jack/opcodes/invoke_static/jm/T_invoke_static_18.java tests/com/android/jack/opcodes/invoke_static/jm/T_invoke_static_1.java tests/com/android/jack/opcodes/invoke_static/jm/T_invoke_static_20.java tests/com/android/jack/opcodes/invoke_static/jm/T_invoke_static_2.java tests/com/android/jack/opcodes/invoke_static/jm/T_invoke_static_4.java tests/com/android/jack/opcodes/invoke_static/jm/T_invoke_static_5.java tests/com/android/jack/opcodes/invoke_static/jm/T_invoke_static_6.java tests/com/android/jack/opcodes/invoke_static/jm/T_invoke_static_7.java tests/com/android/jack/opcodes/invoke_static/jm/T_invoke_static_8.java
JACKTEST_JUNIT := com.android.jack.opcodes.invoke_static.Test_invoke_static

include $(JACK_RUN_TEST)

### rem_long ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-rem_long
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/rem_long/Test_rem_long.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/rem_long/jm/T_rem_long_1.java tests/com/android/jack/opcodes/rem_long/jm/T_rem_long_3.java tests/com/android/jack/opcodes/rem_long/jm/T_rem_long_4.java tests/com/android/jack/opcodes/rem_long/jm/T_rem_long_5.java
JACKTEST_JUNIT := com.android.jack.opcodes.rem_long.Test_rem_long

include $(JACK_RUN_TEST)

### aget_wide ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-aget_wide
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/aget_wide/Test_aget_wide.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/aget_wide/jm/T_aget_wide_1.java tests/com/android/jack/opcodes/aget_wide/jm/T_aget_wide_2.java tests/com/android/jack/opcodes/aget_wide/jm/T_aget_wide_3.java tests/com/android/jack/opcodes/aget_wide/jm/T_aget_wide_4.java tests/com/android/jack/opcodes/aget_wide/jm/T_aget_wide_5.java
JACKTEST_JUNIT := com.android.jack.opcodes.aget_wide.Test_aget_wide

include $(JACK_RUN_TEST)

### float_to_double ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-float_to_double
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/float_to_double/Test_float_to_double.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/float_to_double/jm/T_float_to_double_1.java tests/com/android/jack/opcodes/float_to_double/jm/T_float_to_double_3.java tests/com/android/jack/opcodes/float_to_double/jm/T_float_to_double_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.float_to_double.Test_float_to_double

include $(JACK_RUN_TEST)

### if_lt ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-if_lt
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/if_lt/Test_if_lt.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/if_lt/jm/T_if_lt_1.java tests/com/android/jack/opcodes/if_lt/jm/T_if_lt_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.if_lt.Test_if_lt

include $(JACK_RUN_TEST)

### double_to_float ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-double_to_float
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/double_to_float/Test_double_to_float.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/double_to_float/jm/T_double_to_float_1.java tests/com/android/jack/opcodes/double_to_float/jm/T_double_to_float_3.java tests/com/android/jack/opcodes/double_to_float/jm/T_double_to_float_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.double_to_float.Test_double_to_float

include $(JACK_RUN_TEST)

### double_to_long ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-double_to_long
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/double_to_long/Test_double_to_long.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/double_to_long/jm/T_double_to_long_1.java tests/com/android/jack/opcodes/double_to_long/jm/T_double_to_long_3.java tests/com/android/jack/opcodes/double_to_long/jm/T_double_to_long_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.double_to_long.Test_double_to_long

include $(JACK_RUN_TEST)

### return_wide ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-return_wide
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/return_wide/Test_return_wide.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/return_wide/jm/T_return_wide_1.java tests/com/android/jack/opcodes/return_wide/jm/T_return_wide_2.java tests/com/android/jack/opcodes/return_wide/jm/T_return_wide_6.java tests/com/android/jack/opcodes/return_wide/jm/T_return_wide_7.java tests/com/android/jack/opcodes/return_wide/jm/T_return_wide_8.java tests/com/android/jack/opcodes/return_wide/jm/T_return_wide_9.java tests/com/android/jack/opcodes/return_wide/jm/T_return_wide_10.java tests/com/android/jack/opcodes/return_wide/jm/T_return_wide_11.java tests/com/android/jack/opcodes/return_wide/jm/T_return_wide_12.java tests/com/android/jack/opcodes/return_wide/jm/T_return_wide_13.java tests/com/android/jack/opcodes/return_wide/jm/T_return_wide_14.java
JACKTEST_JUNIT := com.android.jack.opcodes.return_wide.Test_return_wide

include $(JACK_RUN_TEST)

### add_long ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-add_long
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/add_long/Test_add_long.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/add_long/jm/T_add_long_1.java tests/com/android/jack/opcodes/add_long/jm/T_add_long_3.java tests/com/android/jack/opcodes/add_long/jm/T_add_long_4.java tests/com/android/jack/opcodes/add_long/jm/T_add_long_5.java
JACKTEST_JUNIT := com.android.jack.opcodes.add_long.Test_add_long

include $(JACK_RUN_TEST)

### sub_long ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-sub_long
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/sub_long/Test_sub_long.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/sub_long/jm/T_sub_long_1.java tests/com/android/jack/opcodes/sub_long/jm/T_sub_long_3.java tests/com/android/jack/opcodes/sub_long/jm/T_sub_long_4.java tests/com/android/jack/opcodes/sub_long/jm/T_sub_long_5.java
JACKTEST_JUNIT := com.android.jack.opcodes.sub_long.Test_sub_long

include $(JACK_RUN_TEST)

### shl_long ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-shl_long
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/shl_long/Test_shl_long.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/shl_long/jm/T_shl_long_1.java tests/com/android/jack/opcodes/shl_long/jm/T_shl_long_3.java tests/com/android/jack/opcodes/shl_long/jm/T_shl_long_4.java tests/com/android/jack/opcodes/shl_long/jm/T_shl_long_5.java
JACKTEST_JUNIT := com.android.jack.opcodes.shl_long.Test_shl_long

include $(JACK_RUN_TEST)

### aput_byte ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-aput_byte
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/aput_byte/Test_aput_byte.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/aput_byte/jm/T_aput_byte_1.java
JACKTEST_JUNIT := com.android.jack.opcodes.aput_byte.Test_aput_byte

include $(JACK_RUN_TEST)

### aget_char ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-aget_char
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/aget_char/Test_aget_char.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/aget_char/jm/T_aget_char_1.java
JACKTEST_JUNIT := com.android.jack.opcodes.aget_char.Test_aget_char

include $(JACK_RUN_TEST)

### const4_16 ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-const4_16
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/const4_16/Test_const4_16.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_1.java tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_2.java tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_3.java tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_4.java tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_5.java tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_6.java tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_7.java tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_8.java tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_9.java tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_10.java tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_11.java tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_12.java tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_13.java tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_14.java tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_15.java tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_16.java tests/com/android/jack/opcodes/const4_16/jm/T_const4_16_17.java
JACKTEST_JUNIT := com.android.jack.opcodes.const4_16.Test_const4_16

include $(JACK_RUN_TEST)

### sub_int ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-sub_int
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/sub_int/Test_sub_int.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/sub_int/jm/T_sub_int_1.java tests/com/android/jack/opcodes/sub_int/jm/T_sub_int_3.java tests/com/android/jack/opcodes/sub_int/jm/T_sub_int_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.sub_int.Test_sub_int

include $(JACK_RUN_TEST)

### neg_long ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-neg_long
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/neg_long/Test_neg_long.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/neg_long/jm/T_neg_long_1.java tests/com/android/jack/opcodes/neg_long/jm/T_neg_long_2.java tests/com/android/jack/opcodes/neg_long/jm/T_neg_long_4.java tests/com/android/jack/opcodes/neg_long/jm/T_neg_long_5.java tests/com/android/jack/opcodes/neg_long/jm/T_neg_long_6.java
JACKTEST_JUNIT := com.android.jack.opcodes.neg_long.Test_neg_long

include $(JACK_RUN_TEST)

### opc_return ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-opc_return
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/opc_return/Test_opc_return.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/opc_return/jm/T_opc_return_1.java tests/com/android/jack/opcodes/opc_return/jm/T_opc_return_2.java tests/com/android/jack/opcodes/opc_return/jm/T_opc_return_3.java tests/com/android/jack/opcodes/opc_return/jm/T_opc_return_4.java tests/com/android/jack/opcodes/opc_return/jm/T_opc_return_5.java tests/com/android/jack/opcodes/opc_return/jm/T_opc_return_6.java tests/com/android/jack/opcodes/opc_return/jm/T_opc_return_7.java tests/com/android/jack/opcodes/opc_return/jm/T_opc_return_8.java tests/com/android/jack/opcodes/opc_return/jm/T_opc_return_9.java tests/com/android/jack/opcodes/opc_return/jm/T_opc_return_10.java tests/com/android/jack/opcodes/opc_return/jm/T_opc_return_11.java tests/com/android/jack/opcodes/opc_return/jm/T_opc_return_12.java tests/com/android/jack/opcodes/opc_return/jm/T_opc_return_13.java tests/com/android/jack/opcodes/opc_return/jm/T_opc_return_14.java tests/com/android/jack/opcodes/opc_return/jm/T_opc_return_15.java
JACKTEST_JUNIT := com.android.jack.opcodes.opc_return.Test_opc_return

include $(JACK_RUN_TEST)

### long_to_float ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-long_to_float
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/long_to_float/Test_long_to_float.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/long_to_float/jm/T_long_to_float_1.java tests/com/android/jack/opcodes/long_to_float/jm/T_long_to_float_3.java tests/com/android/jack/opcodes/long_to_float/jm/T_long_to_float_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.long_to_float.Test_long_to_float

include $(JACK_RUN_TEST)

### if_le ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-if_le
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/if_le/Test_if_le.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/if_le/jm/T_if_le_1.java tests/com/android/jack/opcodes/if_le/jm/T_if_le_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.if_le.Test_if_le

include $(JACK_RUN_TEST)

### aget_byte ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-aget_byte
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/aget_byte/Test_aget_byte.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/aget_byte/jm/T_aget_byte_1.java
JACKTEST_JUNIT := com.android.jack.opcodes.aget_byte.Test_aget_byte

include $(JACK_RUN_TEST)

### iget ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-iget
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/iget/Test_iget.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/iget/jm/TestStubs.java tests/com/android/jack/opcodes/iget/jm/T_iget_11.java tests/com/android/jack/opcodes/iget/jm/T_iget_15.java tests/com/android/jack/opcodes/iget/jm/T_iget_1.java tests/com/android/jack/opcodes/iget/jm/T_iget_2.java tests/com/android/jack/opcodes/iget/jm/T_iget_4.java tests/com/android/jack/opcodes/iget/jm/T_iget_5.java tests/com/android/jack/opcodes/iget/jm/T_iget_6.java
JACKTEST_JUNIT := com.android.jack.opcodes.iget.Test_iget

include $(JACK_RUN_TEST)

### aput_char ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-aput_char
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/aput_char/Test_aput_char.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/aput_char/jm/T_aput_char_1.java tests/com/android/jack/opcodes/aput_char/jm/T_aput_char_4.java tests/com/android/jack/opcodes/aput_char/jm/T_aput_char_5.java
JACKTEST_JUNIT := com.android.jack.opcodes.aput_char.Test_aput_char

include $(JACK_RUN_TEST)

### div_float ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-div_float
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/div_float/Test_div_float.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/div_float/jm/T_div_float_1.java tests/com/android/jack/opcodes/div_float/jm/T_div_float_3.java tests/com/android/jack/opcodes/div_float/jm/T_div_float_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.div_float.Test_div_float

include $(JACK_RUN_TEST)

### if_nez ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-if_nez
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/if_nez/Test_if_nez.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/if_nez/jm/T_if_nez_1.java tests/com/android/jack/opcodes/if_nez/jm/T_if_nez_2.java tests/com/android/jack/opcodes/if_nez/jm/T_if_nez_3.java tests/com/android/jack/opcodes/if_nez/jm/T_if_nez_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.if_nez.Test_if_nez

include $(JACK_RUN_TEST)

### div_int ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-div_int
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/div_int/Test_div_int.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/div_int/jm/T_div_int_1.java tests/com/android/jack/opcodes/div_int/jm/T_div_int_3.java tests/com/android/jack/opcodes/div_int/jm/T_div_int_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.div_int.Test_div_int

include $(JACK_RUN_TEST)

### float_to_int ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-float_to_int
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/float_to_int/Test_float_to_int.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/float_to_int/jm/T_float_to_int_1.java tests/com/android/jack/opcodes/float_to_int/jm/T_float_to_int_3.java tests/com/android/jack/opcodes/float_to_int/jm/T_float_to_int_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.float_to_int.Test_float_to_int

include $(JACK_RUN_TEST)

### long_to_int ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-long_to_int
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/long_to_int/Test_long_to_int.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/long_to_int/jm/T_long_to_int_1.java tests/com/android/jack/opcodes/long_to_int/jm/T_long_to_int_3.java tests/com/android/jack/opcodes/long_to_int/jm/T_long_to_int_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.long_to_int.Test_long_to_int

include $(JACK_RUN_TEST)

### cmpl_float ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-cmpl_float
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/cmpl_float/Test_cmpl_float.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/cmpl_float/jm/T_cmpl_float_1.java tests/com/android/jack/opcodes/cmpl_float/jm/T_cmpl_float_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.cmpl_float.Test_cmpl_float

include $(JACK_RUN_TEST)

### shr_int ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-shr_int
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/shr_int/Test_shr_int.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/shr_int/jm/T_shr_int_1.java tests/com/android/jack/opcodes/shr_int/jm/T_shr_int_3.java tests/com/android/jack/opcodes/shr_int/jm/T_shr_int_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.shr_int.Test_shr_int

include $(JACK_RUN_TEST)

### if_eq ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-if_eq
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/if_eq/Test_if_eq.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/if_eq/jm/T_if_eq_1.java tests/com/android/jack/opcodes/if_eq/jm/T_if_eq_2.java tests/com/android/jack/opcodes/if_eq/jm/T_if_eq_3.java
JACKTEST_JUNIT := com.android.jack.opcodes.if_eq.Test_if_eq

include $(JACK_RUN_TEST)

### add_int ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-add_int
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/add_int/Test_add_int.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/add_int/jm/T_add_int_1.java tests/com/android/jack/opcodes/add_int/jm/T_add_int_3.java tests/com/android/jack/opcodes/add_int/jm/T_add_int_4.java tests/com/android/jack/opcodes/add_int/jm/T_add_int_5.java tests/com/android/jack/opcodes/add_int/jm/T_add_int_6.java tests/com/android/jack/opcodes/add_int/jm/T_add_int_7.java tests/com/android/jack/opcodes/add_int/jm/T_add_int_8.java tests/com/android/jack/opcodes/add_int/jm/T_add_int_9.java tests/com/android/jack/opcodes/add_int/jm/T_add_int_10.java
JACKTEST_JUNIT := com.android.jack.opcodes.add_int.Test_add_int

include $(JACK_RUN_TEST)

### or_long ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-or_long
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/or_long/Test_or_long.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/or_long/jm/T_or_long_1.java tests/com/android/jack/opcodes/or_long/jm/T_or_long_3.java tests/com/android/jack/opcodes/or_long/jm/T_or_long_4.java tests/com/android/jack/opcodes/or_long/jm/T_or_long_5.java
JACKTEST_JUNIT := com.android.jack.opcodes.or_long.Test_or_long

include $(JACK_RUN_TEST)

### mul_long ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-mul_long
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/mul_long/Test_mul_long.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/mul_long/jm/T_mul_long_1.java tests/com/android/jack/opcodes/mul_long/jm/T_mul_long_4.java tests/com/android/jack/opcodes/mul_long/jm/T_mul_long_5.java tests/com/android/jack/opcodes/mul_long/jm/T_mul_long_6.java
JACKTEST_JUNIT := com.android.jack.opcodes.mul_long.Test_mul_long

include $(JACK_RUN_TEST)

### if_lez ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-if_lez
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/if_lez/Test_if_lez.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/if_lez/jm/T_if_lez_1.java tests/com/android/jack/opcodes/if_lez/jm/T_if_lez_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.if_lez.Test_if_lez

include $(JACK_RUN_TEST)

### sub_float ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-sub_float
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/sub_float/Test_sub_float.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/sub_float/jm/T_sub_float_1.java tests/com/android/jack/opcodes/sub_float/jm/T_sub_float_3.java tests/com/android/jack/opcodes/sub_float/jm/T_sub_float_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.sub_float.Test_sub_float

include $(JACK_RUN_TEST)

### rem_double ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-rem_double
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/rem_double/Test_rem_double.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/rem_double/jm/T_rem_double_1.java tests/com/android/jack/opcodes/rem_double/jm/T_rem_double_3.java tests/com/android/jack/opcodes/rem_double/jm/T_rem_double_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.rem_double.Test_rem_double

include $(JACK_RUN_TEST)

### neg_int ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-neg_int
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/neg_int/Test_neg_int.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/neg_int/jm/T_neg_int_1.java tests/com/android/jack/opcodes/neg_int/jm/T_neg_int_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.neg_int.Test_neg_int

include $(JACK_RUN_TEST)

### aput_wide ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-aput_wide
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/aput_wide/Test_aput_wide.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/aput_wide/jm/T_aput_wide_1.java tests/com/android/jack/opcodes/aput_wide/jm/T_aput_wide_2.java tests/com/android/jack/opcodes/aput_wide/jm/T_aput_wide_3.java tests/com/android/jack/opcodes/aput_wide/jm/T_aput_wide_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.aput_wide.Test_aput_wide

include $(JACK_RUN_TEST)

### div_long ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-div_long
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/div_long/Test_div_long.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/div_long/jm/T_div_long_1.java tests/com/android/jack/opcodes/div_long/jm/T_div_long_4.java tests/com/android/jack/opcodes/div_long/jm/T_div_long_5.java tests/com/android/jack/opcodes/div_long/jm/T_div_long_6.java
JACKTEST_JUNIT := com.android.jack.opcodes.div_long.Test_div_long

include $(JACK_RUN_TEST)

### xor_int ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-xor_int
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/xor_int/Test_xor_int.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/xor_int/jm/T_xor_int_1.java tests/com/android/jack/opcodes/xor_int/jm/T_xor_int_3.java tests/com/android/jack/opcodes/xor_int/jm/T_xor_int_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.xor_int.Test_xor_int

include $(JACK_RUN_TEST)

### aput_object ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-aput_object
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/aput_object/Test_aput_object.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/aput_object/jm/T_aput_object_10.java tests/com/android/jack/opcodes/aput_object/jm/T_aput_object_1.java
JACKTEST_JUNIT := com.android.jack.opcodes.aput_object.Test_aput_object

include $(JACK_RUN_TEST)

### new_array ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-new_array
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/new_array/Test_new_array.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/new_array/jm/T_new_array_1.java tests/com/android/jack/opcodes/new_array/jm/T_new_array_2.java tests/com/android/jack/opcodes/new_array/jm/T_new_array_3.java tests/com/android/jack/opcodes/new_array/jm/T_new_array_4.java tests/com/android/jack/opcodes/new_array/jm/T_new_array_5.java tests/com/android/jack/opcodes/new_array/jm/T_new_array_6.java
JACKTEST_JUNIT := com.android.jack.opcodes.new_array.Test_new_array

include $(JACK_RUN_TEST)

### aput_short ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-aput_short
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/aput_short/Test_aput_short.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/aput_short/jm/T_aput_short_1.java tests/com/android/jack/opcodes/aput_short/jm/T_aput_short_4.java tests/com/android/jack/opcodes/aput_short/jm/T_aput_short_5.java
JACKTEST_JUNIT := com.android.jack.opcodes.aput_short.Test_aput_short

include $(JACK_RUN_TEST)


### if_ne ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-if_ne
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/if_ne/Test_if_ne.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/if_ne/jm/T_if_ne_1.java tests/com/android/jack/opcodes/if_ne/jm/T_if_ne_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.if_ne.Test_if_ne

include $(JACK_RUN_TEST)

### rem_float ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-rem_float
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/rem_float/Test_rem_float.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/rem_float/jm/T_rem_float_1.java tests/com/android/jack/opcodes/rem_float/jm/T_rem_float_3.java tests/com/android/jack/opcodes/rem_float/jm/T_rem_float_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.rem_float.Test_rem_float

include $(JACK_RUN_TEST)

### ushr_int ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-ushr_int
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/ushr_int/Test_ushr_int.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/ushr_int/jm/T_ushr_int_1.java tests/com/android/jack/opcodes/ushr_int/jm/T_ushr_int_3.java tests/com/android/jack/opcodes/ushr_int/jm/T_ushr_int_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.ushr_int.Test_ushr_int

include $(JACK_RUN_TEST)

### invoke_interface ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-invoke_interface
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/invoke_interface/Test_invoke_interface.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/invoke_interface/jm/ITestImplAbstract.java tests/com/android/jack/opcodes/invoke_interface/jm/ITestImpl.java tests/com/android/jack/opcodes/invoke_interface/jm/ITest.java tests/com/android/jack/opcodes/invoke_interface/jm/T_invoke_interface_12.java tests/com/android/jack/opcodes/invoke_interface/jm/T_invoke_interface_14.java tests/com/android/jack/opcodes/invoke_interface/jm/T_invoke_interface_17.java tests/com/android/jack/opcodes/invoke_interface/jm/T_invoke_interface_19.java tests/com/android/jack/opcodes/invoke_interface/jm/T_invoke_interface_1.java tests/com/android/jack/opcodes/invoke_interface/jm/T_invoke_interface_23.java tests/com/android/jack/opcodes/invoke_interface/jm/T_invoke_interface_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.invoke_interface.Test_invoke_interface

include $(JACK_RUN_TEST)

### mul_float ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-mul_float
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/mul_float/Test_mul_float.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/mul_float/jm/T_mul_float_1.java tests/com/android/jack/opcodes/mul_float/jm/T_mul_float_3.java tests/com/android/jack/opcodes/mul_float/jm/T_mul_float_4.java
JACKTEST_JUNIT := com.android.jack.opcodes.mul_float.Test_mul_float

include $(JACK_RUN_TEST)

### if_gt ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-if_gt
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/if_gt/Test_if_gt.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/if_gt/jm/T_if_gt_1.java tests/com/android/jack/opcodes/if_gt/jm/T_if_gt_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.if_gt.Test_if_gt

include $(JACK_RUN_TEST)

### packed_switch ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-packed_switch
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/packed_switch/Test_packed_switch.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/packed_switch/jm/T_packed_switch_1.java
JACKTEST_JUNIT := com.android.jack.opcodes.packed_switch.Test_packed_switch

include $(JACK_RUN_TEST)

### const_string ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-const_string
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/const_string/Test_const_string.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/const_string/jm/T_const_string_1.java
JACKTEST_JUNIT := com.android.jack.opcodes.const_string.Test_const_string

include $(JACK_RUN_TEST)

### opc_const ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-opc_const
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/opc_const/Test_opc_const.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/opc_const/jm/T_opc_const_1.java tests/com/android/jack/opcodes/opc_const/jm/T_opc_const_2.java
JACKTEST_JUNIT := com.android.jack.opcodes.opc_const.Test_opc_const

include $(JACK_RUN_TEST)

### invoke_super ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-invoke_super
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/invoke_super/Test_invoke_super.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/invoke_super/jm/T_invoke_super_1.java tests/com/android/jack/opcodes/invoke_super/jm/T_invoke_super_14.java tests/com/android/jack/opcodes/invoke_super/jm/T_invoke_super_15.java tests/com/android/jack/opcodes/invoke_super/jm/T_invoke_super_18.java tests/com/android/jack/opcodes/invoke_super/jm/T_invoke_super_19.java tests/com/android/jack/opcodes/invoke_super/jm/T_invoke_super_26.java tests/com/android/jack/opcodes/invoke_super/jm/TSuper2.java tests/com/android/jack/opcodes/invoke_super/jm/TSuper.java
JACKTEST_JUNIT := com.android.jack.opcodes.invoke_super.Test_invoke_super

include $(JACK_RUN_TEST)

### invoke_direct ###

include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := opcode-invoke_direct
JACKTEST_WITHDX_SRC := tests/com/android/jack/opcodes/invoke_direct/Test_invoke_direct.java src/com/android/jack/DxTestCase.java
JACKTEST_WITHJACK_SRC := tests/com/android/jack/opcodes/invoke_direct/jm/T_invoke_direct_2.java tests/com/android/jack/opcodes/invoke_direct/jm/T_invoke_direct_21.java tests/com/android/jack/opcodes/invoke_direct/jm/TSuper.java
JACKTEST_JUNIT := com.android.jack.opcodes.invoke_direct.Test_invoke_direct

include $(JACK_RUN_TEST)

