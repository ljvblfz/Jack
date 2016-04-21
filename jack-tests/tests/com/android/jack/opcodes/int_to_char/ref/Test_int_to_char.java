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

package com.android.jack.opcodes.int_to_char.ref;

import com.android.jack.opcodes.int_to_char.jm.T_int_to_char_1;
import com.android.jack.opcodes.int_to_char.jm.T_int_to_char_3;
import com.android.jack.opcodes.int_to_char.jm.T_int_to_char_4;
import com.android.jack.test.DxTestCase;


public class Test_int_to_char extends DxTestCase {

    /**
     * @title  Argument = 65
     */
    public void testN1() {
        T_int_to_char_1 t = new T_int_to_char_1();
        assertEquals('A', t.run(65));
    }

    /**
     * @title  Argument = 65537
     */
    public void testN2() {
        T_int_to_char_1 t = new T_int_to_char_1();
        assertEquals('\u0001', t.run(65537));
    }

    /**
     * @title  Argument = -2
     */
    public void testN3() {
        T_int_to_char_1 t = new T_int_to_char_1();
        assertEquals('\ufffe', t.run(-2));
    }

    /**
     * @title  Argument = 0x110000
     */
    public void testN4() {
        T_int_to_char_1 t = new T_int_to_char_1();
        assertEquals('\u0000', t.run(0x110000));
    }

    /**
     * @title  Argument = 65
     */
    public void testN5() {
        T_int_to_char_3 t = new T_int_to_char_3();
        assertEquals('A', t.run(65));
    }

    /**
     * @title  Argument = 65
     */
    public void testN6() {
        T_int_to_char_4 t = new T_int_to_char_4();
        assertEquals('A', t.run(65));
    }

    /**
     * @title  Argument = 0
     */
    public void testB1() {
        T_int_to_char_1 t = new T_int_to_char_1();
        assertEquals('\u0000', t.run(0));
    }

    /**
     * @title  Argument = 65535
     */
    public void testB2() {
        T_int_to_char_1 t = new T_int_to_char_1();
        assertEquals('\uffff', t.run(65535));
    }

    /**
     * @title  Argument = Integer.MAX_VALUE
     */
    public void testB3() {
        T_int_to_char_1 t = new T_int_to_char_1();
        assertEquals('\uffff', t.run(Integer.MAX_VALUE));
    }

    /**
     * @title  Argument = Integer.MIN_VALUE
     */
    public void testB4() {
        T_int_to_char_1 t = new T_int_to_char_1();
        assertEquals('\u0000', t.run(Integer.MIN_VALUE));
    }


}
