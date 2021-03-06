/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.opcodes.ushr_const_int.ref;

import com.android.jack.opcodes.ushr_const_int.jm.T_ushr_const_int_1;
import com.android.jack.test.DxTestCase;

public class Test_ushr_const_int extends DxTestCase {

  /**
   * @title 0b10000000000000000000000000000000 >>> 0
   */
  public void testN0() {
    T_ushr_const_int_1 t = new T_ushr_const_int_1();
    assertEquals(Integer.MIN_VALUE >>> 0, t.shift_0(Integer.MIN_VALUE));
  }

  /**
   * @title 0b10000000000000000000000000000000 >>> 1
   */
  public void testN1() {
    T_ushr_const_int_1 t = new T_ushr_const_int_1();
    assertEquals(Integer.MIN_VALUE >>> 1, t.shift_1(Integer.MIN_VALUE));
  }

  /**
   * @title 0b10000000000000000000000000000000 >>> 5
   */
  public void testN2() {
    T_ushr_const_int_1 t = new T_ushr_const_int_1();
    assertEquals(Integer.MIN_VALUE >>> 5, t.shift_5(Integer.MIN_VALUE));
  }

  /**
   * @title 0b10000000000000000000000000000000 >>> 31
   */
  public void testN3() {
    T_ushr_const_int_1 t = new T_ushr_const_int_1();
    assertEquals(Integer.MIN_VALUE >>> 31, t.shift_31(Integer.MIN_VALUE));
  }


  /**
   * @title 0b10000000000000000000000000000000 >>> 32
   */
  public void testN4() {
    T_ushr_const_int_1 t = new T_ushr_const_int_1();
    assertEquals(Integer.MIN_VALUE >>> 32, t.shift_32(Integer.MIN_VALUE));
  }

  /**
   * @title 0b10000000000000000000000000000000 >>> 33
   */
  public void testN5() {
    T_ushr_const_int_1 t = new T_ushr_const_int_1();
    assertEquals(Integer.MIN_VALUE >>> 741, t.shift_741(Integer.MIN_VALUE));
  }
}
