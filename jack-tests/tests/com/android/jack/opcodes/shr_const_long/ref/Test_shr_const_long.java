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

package com.android.jack.opcodes.shr_const_long.ref;

import com.android.jack.opcodes.shr_const_long.jm.T_shr_const_long_1;
import com.android.jack.test.DxTestCase;

public class Test_shr_const_long extends DxTestCase {

  /**
   * @title 0b10000000000000000000000000000000 >> 0
   */
  public void testN0() {
    T_shr_const_long_1 t = new T_shr_const_long_1();
    assertEquals(Long.MIN_VALUE >> 0, t.shift_0(Long.MIN_VALUE));
  }

  /**
   * @title 0b10000000000000000000000000000000 >> 1
   */
  public void testN1() {
    T_shr_const_long_1 t = new T_shr_const_long_1();
    assertEquals(Long.MIN_VALUE >> 1, t.shift_1(Long.MIN_VALUE));
  }

  /**
   * @title 0b10000000000000000000000000000000 >> 5
   */
  public void testN2() {
    T_shr_const_long_1 t = new T_shr_const_long_1();
    assertEquals(Long.MIN_VALUE >> 5, t.shift_5(Long.MIN_VALUE));
  }

  /**
   * @title 0b10000000000000000000000000000000 >> 63
   */
  public void testN3() {
    T_shr_const_long_1 t = new T_shr_const_long_1();
    assertEquals(Long.MIN_VALUE >> 63, t.shift_63(Long.MIN_VALUE));
  }


  /**
   * @title 0b10000000000000000000000000000000 >> 64
   */
  public void testN4() {
    T_shr_const_long_1 t = new T_shr_const_long_1();
    assertEquals(Long.MIN_VALUE >> 64, t.shift_64(Long.MIN_VALUE));
  }

  /**
   * @title 0b10000000000000000000000000000000 >> 65
   */
  public void testN5() {
    T_shr_const_long_1 t = new T_shr_const_long_1();
    assertEquals(Long.MIN_VALUE >> 65, t.shift_65(Long.MIN_VALUE));
  }

  /**
   * @title 0b10000000000000000000000000000000 >> 7410
   */
  public void testN6() {
    T_shr_const_long_1 t = new T_shr_const_long_1();
    assertEquals(Long.MIN_VALUE >> 7410, t.shift_7410(Long.MIN_VALUE));
  }
}
