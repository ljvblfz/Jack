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

package com.android.jack.opcodes.int_to_long;

import com.android.jack.opcodes.int_to_long.jm.T_int_to_long_1;
import com.android.jack.opcodes.int_to_long.jm.T_int_to_long_3;
import com.android.jack.opcodes.int_to_long.jm.T_int_to_long_4;
import com.android.jack.test.DxTestCase;


public class Test_int_to_long extends DxTestCase {

    /**
     * @title Argument = 123456
     */
    public void testN1() {
        T_int_to_long_1 t = new T_int_to_long_1();
        assertEquals(123456l, t.run(123456));
    }

    /**
     * @title  Argument = 1
     */
    public void testN2() {
        T_int_to_long_1 t = new T_int_to_long_1();
        assertEquals(1l, t.run(1));
    }

    /**
     * @title  Argument = -1
     */
    public void testN3() {
        T_int_to_long_1 t = new T_int_to_long_1();
        assertEquals(-1l, t.run(-1));
    }

    /**
     * @title  Argument = -1
     */
    public void testN4() {
        T_int_to_long_3 t = new T_int_to_long_3();
        assertEquals(-1l, t.run(-1));
    }

    /**
     * @title  Argument = -1
     */
    public void testN5() {
        T_int_to_long_4 t = new T_int_to_long_4();
        assertEquals(-1l, t.run(-1));
    }

    /**
     * @title  Argument = 0
     */
    public void testB1() {
        T_int_to_long_1 t = new T_int_to_long_1();
        assertEquals(0l, t.run(0));
    }

    /**
     * @title  Argument = Integer.MAX_VALUE
     */
    public void testB2() {
        T_int_to_long_1 t = new T_int_to_long_1();
        assertEquals(2147483647l, t.run(Integer.MAX_VALUE));
    }

    /**
     * @title  Argument = Integer.MIN_VALUE
     */
    public void testB3() {
        T_int_to_long_1 t = new T_int_to_long_1();
        assertEquals(-2147483648l, t.run(Integer.MIN_VALUE));
    }

}
