/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.jack.opcodes.if_eq;

import com.android.jack.opcodes.if_eq.jm.T_if_eq_1;
import com.android.jack.opcodes.if_eq.jm.T_if_eq_2;
import com.android.jack.opcodes.if_eq.jm.T_if_eq_3;
import com.android.jack.test.DxTestCase;


public class Test_if_eq extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_if_eq_1 t = new T_if_eq_1();
        String a = "a";
        String b = "b";

        /*
         * Compare with 1234 to check that in case of failed comparison
         * execution proceeds at the address following if_eq instruction
         */
        assertEquals(1234, t.run(a, b));
    }

    /**
     * @title  normal test
     */
    public void testN2() {
        T_if_eq_1 t = new T_if_eq_1();
        String a = "a";
        assertEquals(1, t.run(a, a));
    }

    public void testN3() {
      T_if_eq_2 t = new T_if_eq_2();
      String a = "a";
      assertEquals(true, t.run(a, a));
  }

    /**
     * @title  Compare with null
     */
    public void testB1() {
        T_if_eq_1 t = new T_if_eq_1();
        String a = "a";
        assertEquals(1234, t.run(null, a));
    }

    /**
     * @title  Arguments = 5, 6
     */
    public void testN1Int() {
        T_if_eq_3 t = new T_if_eq_3();
        /*
         * Compare with 1234 to check that in case of failed comparison
         * execution proceeds at the address following if_eq instruction
         */
        assertEquals(1234, t.run(5, 6));
    }

    /**
     * @title  Arguments = 0x0f0e0d0c, 0x0f0e0d0c
     */
    public void testN2Int() {
        T_if_eq_3 t = new T_if_eq_3();
        assertEquals(1, t.run(0x0f0e0d0c, 0x0f0e0d0c));
    }

    /**
     * @title  Arguments = 5, -5
     */
    public void testN3Int() {
        T_if_eq_3 t = new T_if_eq_3();
        assertEquals(1234, t.run(5, -5));
    }

    /**
     * @title  Arguments = 0x01001234, 0x1234
     */
    public void testN4Int() {
        T_if_eq_3 t = new T_if_eq_3();
        assertEquals(1234, t.run(0x01001234, 0x1234));
    }

    /**
     * @title  Arguments = Integer.MAX_VALUE, Integer.MAX_VALUE
     */
    public void testB1Int() {
        T_if_eq_3 t = new T_if_eq_3();
        assertEquals(1, t.run(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    /**
     * @title  Arguments = Integer.MIN_VALUE, Integer.MIN_VALUE
     */
    public void testB2Int() {
        T_if_eq_3 t = new T_if_eq_3();
        assertEquals(1, t.run(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    /**
     * @title  Arguments = 0, 1234567
     */
    public void testB3Int() {
        T_if_eq_3 t = new T_if_eq_3();
        assertEquals(1234, t.run(0, 1234567));
    }

    /**
     * @title  Arguments = 0, 0
     */
    public void testB4Int() {
        T_if_eq_3 t = new T_if_eq_3();
        assertEquals(1, t.run(0, 0));
    }
}
