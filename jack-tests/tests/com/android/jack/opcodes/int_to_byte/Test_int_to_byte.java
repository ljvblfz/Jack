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

package com.android.jack.opcodes.int_to_byte;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.int_to_byte.jm.T_int_to_byte_1;
import com.android.jack.opcodes.int_to_byte.jm.T_int_to_byte_3;
import com.android.jack.opcodes.int_to_byte.jm.T_int_to_byte_4;


public class Test_int_to_byte extends DxTestCase {

    /**
     * @title  Argument = 1
     */
    public void testN1() {
        T_int_to_byte_1 t = new T_int_to_byte_1();
        assertEquals(1, t.run(1));
    }

    /**
     * @title  Argument = -1
     */
    public void testN2() {
        T_int_to_byte_1 t = new T_int_to_byte_1();
        assertEquals(-1, t.run(-1));
    }

    /**
     * @title  Argument = 16
     */
    public void testN3() {
        T_int_to_byte_1 t = new T_int_to_byte_1();
        assertEquals(16, t.run(16));
    }

    /**
     * @title  Argument = -32
     */
    public void testN4() {
        T_int_to_byte_1 t = new T_int_to_byte_1();
        assertEquals(-32, t.run(-32));
    }

    /**
     * @title  Argument = 134
     */
    public void testN5() {
        T_int_to_byte_1 t = new T_int_to_byte_1();
        assertEquals(-122, t.run(134));
    }

    /**
     * @title  Argument = -134
     */
    public void testN6() {
        T_int_to_byte_1 t = new T_int_to_byte_1();
        assertEquals(122, t.run(-134));
    }

    /**
     * @title  Argument = 1
     */
    public void testN7() {
        T_int_to_byte_3 t = new T_int_to_byte_3();
        assertEquals(1, t.run(1));
    }

    /**
     * @title  Argument = 1
     */
    public void testN8() {
        T_int_to_byte_4 t = new T_int_to_byte_4();
        assertEquals(1, t.run(1));
    }

    /**
     * @title s. Argument = 127
     */
    public void testB1() {
        T_int_to_byte_1 t = new T_int_to_byte_1();
        assertEquals(127, t.run(127));
    }

    /**
     * @title s. Argument = 128
     */
    public void testB2() {
        T_int_to_byte_1 t = new T_int_to_byte_1();
        assertEquals(-128, t.run(128));
    }

    /**
     * @title s. Argument = 0
     */
    public void testB3() {
        T_int_to_byte_1 t = new T_int_to_byte_1();
        assertEquals(0, t.run(0));
    }

    /**
     * @title s. Argument = -128
     */
    public void testB4() {
        T_int_to_byte_1 t = new T_int_to_byte_1();
        assertEquals(-128, t.run(-128));
    }

    /**
     * @title s. Argument = -129
     */
    public void testB5() {
        T_int_to_byte_1 t = new T_int_to_byte_1();
        assertEquals(127, t.run(-129));
    }

    /**
     * @title s. Argument = Integer.MAX_VALUE
     */
    public void testB6() {
        T_int_to_byte_1 t = new T_int_to_byte_1();
        assertEquals(-1, t.run(Integer.MAX_VALUE));
    }

    /**
     * @title s. Argument = Integer.MIN_VALUE
     */
    public void testB7() {
        T_int_to_byte_1 t = new T_int_to_byte_1();
        assertEquals(0, t.run(Integer.MIN_VALUE));
    }

}
