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

package com.android.jack.opcodes.add_int.ref;

import com.android.jack.opcodes.add_int.jm.T_add_int_1;
import com.android.jack.opcodes.add_int.jm.T_add_int_3;
import com.android.jack.opcodes.add_int.jm.T_add_int_4;
import com.android.jack.opcodes.add_int.jm.T_add_int_5;
import com.android.jack.opcodes.add_int.jm.T_add_int_6;
import com.android.jack.opcodes.add_int.jm.T_add_int_7;
import com.android.jack.opcodes.add_int.jm.T_add_int_8;
import com.android.jack.opcodes.add_int.jm.T_add_int_9;
import com.android.jack.test.DxTestCase;
import com.android.jack.opcodes.add_int.jm.T_add_int_10;


public class Test_add_int extends DxTestCase {

    /**
     * @title Arguments = 8, 4
     */
    public void testN1() {
        T_add_int_1 t = new T_add_int_1();
        assertEquals(12, t.run(8, 4));
    }

    /**
     * @title Arguments = 0, 255
     */
    public void testN2() {
        T_add_int_1 t = new T_add_int_1();
        assertEquals(255, t.run(0, 255));
    }

    /**
     * @title Arguments = 0, -65536
     */
    public void testN3() {
        T_add_int_1 t = new T_add_int_1();
        assertEquals(-65536, t.run(0, -65536));
    }

    /**
     * @title Arguments = 0, -2147483647
     */
    public void testN4() {
        T_add_int_1 t = new T_add_int_1();
        assertEquals(-2147483647, t.run(0, -2147483647));
    }

    /**
     * @title Arguments = 0x7ffffffe, 2
     */
    public void testN5() {
        T_add_int_1 t = new T_add_int_1();
        assertEquals(-2147483648, t.run(0x7ffffffe, 2));
    }

    /**
     * @title Arguments = -1, 1
     */
    public void testN6() {
        T_add_int_1 t = new T_add_int_1();
        assertEquals(0, t.run(-1, 1));
    }

    /**
     * @title Arguments = -1, 1
     */
    public void testN7() {
        T_add_int_3 t = new T_add_int_3();
        assertEquals(0, t.run(-1, 1));
    }

    /**
     * @title Arguments = -1, 1
     */
    public void testN8() {
        T_add_int_4 t = new T_add_int_4();
        assertEquals(0, t.run(-1, 1));
    }

    /**
     * @title Arguments = 0, Integer.MAX_VALUE
     */
    public void testB1() {
        T_add_int_1 t = new T_add_int_1();
        assertEquals(Integer.MAX_VALUE, t.run(0, Integer.MAX_VALUE));
    }

    /**
     * @title Arguments = Integer.MAX_VALUE, Integer.MAX_VALUE
     */
    public void testB2() {
        T_add_int_1 t = new T_add_int_1();
        assertEquals(-2, t.run(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    /**
     * @title Arguments = Integer.MAX_VALUE, 1
     */
    public void testB3() {
        T_add_int_1 t = new T_add_int_1();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MAX_VALUE, 1));
    }

    /**
     * @title Arguments = Integer.MIN_VALUE, 1
     */
    public void testB4() {
        T_add_int_1 t = new T_add_int_1();
        assertEquals(-2147483647, t.run(Integer.MIN_VALUE, 1));
    }

    /**
     * @title Arguments = 0, 0
     */
    public void testB5() {
        T_add_int_1 t = new T_add_int_1();
        assertEquals(0, t.run(0, 0));
    }

    /**
     * @title Arguments = Integer.MIN_VALUE, Integer.MIN_VALUE
     */
    public void testB6() {
        T_add_int_1 t = new T_add_int_1();
        assertEquals(0, t.run(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    /**
     * @title  Increment by 1
     */
    public void testN1Inc() {
        T_add_int_5 t = new T_add_int_5();
        assertEquals(5, t.run(4));
    }

    /**
     * @title  Increment by -1
     */
    public void testN2Inc() {
        T_add_int_6 t = new T_add_int_6();
        assertEquals(3, t.run(4));
    }

    /**
     * @title  Increment by 63
     */
    public void testN3Inc() {
        T_add_int_7 t = new T_add_int_7();
        assertEquals(67, t.run(4));
    }

    /**
     * @title  Increment by 0
     */
    public void testB1Inc() {
        T_add_int_8 t = new T_add_int_8();
        assertEquals(Integer.MAX_VALUE, t.run(Integer.MAX_VALUE));
    }

    /**
     * @title  Increment by 0
     */
    public void testB2Inc() {
        T_add_int_8 t = new T_add_int_8();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MIN_VALUE));
    }

    /**
     * @title  Increment by 127
     */
    public void testB3Inc() {
        T_add_int_9 t = new T_add_int_9();
        assertEquals(128, t.run(1));
    }

    /**
     * @title  Increment by 127
     */
    public void testB4Inc() {
        T_add_int_9 t = new T_add_int_9();
        assertEquals(126, t.run(-1));
    }

    /**
     * @title  Increment by 127
     */
    public void testB5Inc() {
        T_add_int_9 t = new T_add_int_9();
        assertEquals(-2147483521, t.run(Integer.MIN_VALUE));
    }

    /**
     * @title  Increment by -128
     */
    public void testB6Inc() {
        T_add_int_10 t = new T_add_int_10();
        assertEquals(-127, t.run(1));
    }

    /**
     * @title  Increment by -128
     */
    public void testB7Inc() {
        T_add_int_10 t = new T_add_int_10();
        assertEquals(-128, t.run(0));
    }
}
