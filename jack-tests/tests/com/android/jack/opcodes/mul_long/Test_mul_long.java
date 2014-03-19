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

package com.android.jack.opcodes.mul_long;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.mul_long.jm.T_mul_long_1;
import com.android.jack.opcodes.mul_long.jm.T_mul_long_4;
import com.android.jack.opcodes.mul_long.jm.T_mul_long_5;
import com.android.jack.opcodes.mul_long.jm.T_mul_long_6;


public class Test_mul_long extends DxTestCase {

    /**
     * @title Arguments = 222000000000l, 5000000000l
     */
    public void testN1() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(3195355577426903040l, t.run(222000000000l, 5000000000l));
    }

    /**
     * @title Arguments = -123456789l, 123456789l
     */
    public void testN2() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(-15241578750190521l, t.run(-123456789l, 123456789l));
    }

    /**
     * @title Arguments = -123456789l, -123456789l
     */
    public void testN3() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(15241578750190521l, t.run(-123456789l, -123456789l));
    }

    /**
     * @title Arguments = -123456789l, -123456789l
     */
    public void testN4() {
        T_mul_long_4 t = new T_mul_long_4();
        assertEquals(15241578750190520l, t.run(-123456789l, -123456789l));
    }

    /**
     * @title Arguments = -123456789l, -123456789l
     */
    public void testN5() {
        T_mul_long_5 t = new T_mul_long_5();
        assertEquals(152345677626l, t.run(-123456789l, -1234));
    }

    /**
     * @title Arguments = -123456789l, -123456789l
     */
    public void testN6() {
        T_mul_long_6 t = new T_mul_long_6();
        assertEquals(15241579434344448l, t.run(-123456789l, -123456789l));
    }

    /**
     * @title Arguments = 0, Long.MAX_VALUE
     */
    public void testB1() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(0, t.run(0, Long.MAX_VALUE));
    }
    /**
     * @title Arguments = Long.MAX_VALUE, 1
     */
    public void testB2() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(9223372036854775807L, t.run(Long.MAX_VALUE, 1));
    }
    /**
     * @title Arguments = Long.MIN_VALUE, 1
     */
    public void testB3() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(-9223372036854775808L, t.run(Long.MIN_VALUE, 1));
    }
    /**
     * @title Arguments = Long.MAX_VALUE, Long.MIN_VALUE
     */
    public void testB4() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(-9223372036854775808L, t.run(Long.MAX_VALUE,
                Long.MIN_VALUE));
    }
    /**
     * @title Arguments = 0, 0
     */
    public void testB5() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(0, t.run(0, 0));
    }
    /**
     * @title Arguments = Long.MAX_VALUE, -1
     */
    public void testB6() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(-9223372036854775807L, t.run(Long.MAX_VALUE, -1));
    }
    /**
     * @title Arguments = Long.MIN_VALUE, -1
     */
    public void testB7() {
        T_mul_long_1 t = new T_mul_long_1();
        assertEquals(-9223372036854775808L, t.run(Long.MIN_VALUE, -1));
    }
}
