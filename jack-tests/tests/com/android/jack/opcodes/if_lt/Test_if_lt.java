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

package com.android.jack.opcodes.if_lt;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.if_lt.jm.T_if_lt_1;
import com.android.jack.opcodes.if_lt.jm.T_if_lt_2;


public class Test_if_lt extends DxTestCase {

    /**
     * @title  Arguments = 5, 6
     */
    public void testN1() {
        T_if_lt_1 t = new T_if_lt_1();
        assertEquals(1, t.run(5, 6));
    }

    /**
     * @title  Arguments = 0x0f0e0d0c, 0x0f0e0d0c
     */
    public void testN2() {
        T_if_lt_1 t = new T_if_lt_1();
        /*
         * Compare with 1234 to check that in case of failed comparison
         * execution proceeds at the address following if_lt instruction
         */
        assertEquals(1234, t.run(0x0f0e0d0c, 0x0f0e0d0c));
    }

    /**
     * @title  Arguments = 5, -5
     */
    public void testN3() {
        T_if_lt_1 t = new T_if_lt_1();
        assertEquals(1234, t.run(5, -5));
    }

    /**
     * @title  Arguments = 0x0f0e0d0d, 0x0f0e0d0c
     */
    public void testN4() {
        T_if_lt_1 t = new T_if_lt_1();
        assertEquals(1234, t.run(0x0f0e0d0d, 0x0f0e0d0c));
    }

    /**
     * @title  Arguments = 0x1234, 0x01001234
     */
    public void testN5() {
        T_if_lt_1 t = new T_if_lt_1();
        assertEquals(1, t.run(0x1234, 0x01001234));
    }

    /**
     * @title  Arguments = -5, 5
     */
    public void testN6() {
        T_if_lt_1 t = new T_if_lt_1();
        assertEquals(1, t.run(-5, 5));
    }

    /**
     * @title  Arguments = -5, 5
     */
    public void testN7() {
        T_if_lt_2 t = new T_if_lt_2();
        assertEquals(true, t.run(-5, 5));
    }

    /**
     * @title  Arguments = Integer.MAX_VALUE, Integer.MAX_VALUE
     */
    public void testB1() {
        T_if_lt_1 t = new T_if_lt_1();
        assertEquals(1234, t.run(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    /**
     * @title  Arguments = Integer.MIN_VALUE, Integer.MIN_VALUE
     */
    public void testB2() {
        T_if_lt_1 t = new T_if_lt_1();
        assertEquals(1234, t.run(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    /**
     * @title  Arguments = 1234567, 0
     */
    public void testB3() {
        T_if_lt_1 t = new T_if_lt_1();
        assertEquals(1234, t.run(1234567, 0));
    }

    /**
     * @title  Arguments = 0, 1234567
     */
    public void testB4() {
        T_if_lt_1 t = new T_if_lt_1();
        assertEquals(1, t.run(0, 1234567));
    }

    /**
     * @title  Arguments = 0, 0
     */
    public void testB5() {
        T_if_lt_1 t = new T_if_lt_1();
        assertEquals(1234, t.run(0, 0));
    }

}
