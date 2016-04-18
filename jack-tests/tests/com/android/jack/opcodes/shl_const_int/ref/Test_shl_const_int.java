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

package com.android.jack.opcodes.shl_const_int.ref;

import com.android.jack.opcodes.shl_const_int.jm.T_shl_const_int_1;
import com.android.jack.test.DxTestCase;

public class Test_shl_const_int extends DxTestCase {

  /**
   * @title 1 << 0
   */
  public void testN0() {
    T_shl_const_int_1 t = new T_shl_const_int_1();
    assertEquals(1 << 0, t.shift_0(1));
  }

  /**
   * @title 1 << 1
   */
  public void testN1() {
    T_shl_const_int_1 t = new T_shl_const_int_1();
    assertEquals(1 << 1, t.shift_1(1));
  }

  /**
   * @title 1 << 5
   */
  public void testN2() {
    T_shl_const_int_1 t = new T_shl_const_int_1();
    assertEquals(1 << 5, t.shift_5(1));
  }

  /**
   * @title 1 << 31
   */
  public void testN3() {
    T_shl_const_int_1 t = new T_shl_const_int_1();
    assertEquals(1 << 31, t.shift_31(1));
  }


  /**
   * @title 1 << 32
   */
  public void testN4() {
    T_shl_const_int_1 t = new T_shl_const_int_1();
    assertEquals(1 << 32, t.shift_32(1));
  }

  /**
   * @title 1 << 741
   */
  public void testN5() {
    T_shl_const_int_1 t = new T_shl_const_int_1();
    assertEquals(1 << 741, t.shift_741(1));
  }
}
