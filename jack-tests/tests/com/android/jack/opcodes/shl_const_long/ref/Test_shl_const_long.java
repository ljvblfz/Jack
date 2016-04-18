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

package com.android.jack.opcodes.shl_const_long.ref;

import com.android.jack.opcodes.shl_const_long.jm.T_shl_const_long_1;
import com.android.jack.test.DxTestCase;

public class Test_shl_const_long extends DxTestCase {

  /**
   * @title 1l << 0
   */
  public void testN0() {
    T_shl_const_long_1 t = new T_shl_const_long_1();
    assertEquals(1l << 0, t.shift_0(1));
  }

  /**
   * @title 1l << 1
   */
  public void testN1() {
    T_shl_const_long_1 t = new T_shl_const_long_1();
    assertEquals(1l << 1, t.shift_1(1));
  }

  /**
   * @title 1l << 5
   */
  public void testN2() {
    T_shl_const_long_1 t = new T_shl_const_long_1();
    assertEquals(1l << 5, t.shift_5(1));
  }

  /**
   * @title 1l << 63
   */
  public void testN3() {
    T_shl_const_long_1 t = new T_shl_const_long_1();
    assertEquals(1l << 63, t.shift_63(1));
  }


  /**
   * @title 1l << 64
   */
  public void testN4() {
    T_shl_const_long_1 t = new T_shl_const_long_1();
    assertEquals(1l << 64, t.shift_64(1));
  }

  /**
   * @title 1l << 65
   */
  public void testN5() {
    T_shl_const_long_1 t = new T_shl_const_long_1();
    assertEquals(1l << 65, t.shift_65(1));
  }

  /**
   * @title 1l << 7410
   */
  public void testN6() {
    T_shl_const_long_1 t = new T_shl_const_long_1();
    assertEquals(1l << 7410, t.shift_7410(1));
  }
}
