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

package com.android.jack.opcodes.long_to_int;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.long_to_int.jm.T_long_to_int_1;
import com.android.jack.opcodes.long_to_int.jm.T_long_to_int_3;
import com.android.jack.opcodes.long_to_int.jm.T_long_to_int_4;


public class Test_long_to_int extends DxTestCase {

    /**
     * @title  Argument = 0xAAAAFFEEDDCCl
     */
    public void testN1() {
        T_long_to_int_1 t = new T_long_to_int_1();
        assertEquals(0xFFEEDDCC, t.run(0xAAAAFFEEDDCCl));
    }

    /**
     * @title  Argument = -123456789
     */
    public void testN2() {
        T_long_to_int_1 t = new T_long_to_int_1();
        assertEquals(-123456789, t.run(-123456789l));
    }

    /**
     * @title  Argument = 1
     */
    public void testN3() {
        T_long_to_int_1 t = new T_long_to_int_1();
        assertEquals(1, t.run(1l));
    }

    /**
     * @title  Argument = -1
     */
    public void testN4() {
        T_long_to_int_1 t = new T_long_to_int_1();
        assertEquals(-1, t.run(-1l));
    }

    /**
     * @title  Argument = -1
     */
    public void testN5() {
        T_long_to_int_3 t = new T_long_to_int_3();
        assertEquals(-1, t.run(-1l));
    }

    /**
     * @title  Argument = -1
     */
    public void testN6() {
        T_long_to_int_4 t = new T_long_to_int_4();
        assertEquals(-1, t.run(-1l));
    }

    /**
     * @title  Argument = Long.MAX_VALUE
     */
    public void testB1() {
        T_long_to_int_1 t = new T_long_to_int_1();
        assertEquals(-1, t.run(Long.MAX_VALUE));
    }

    /**
     * @title  Argument = Long.MIN_VALUE
     */
    public void testB2() {
        T_long_to_int_1 t = new T_long_to_int_1();
        assertEquals(0, t.run(Long.MIN_VALUE));
    }

    /**
     * @title  Argument = 0
     */
    public void testB3() {
        T_long_to_int_1 t = new T_long_to_int_1();
        assertEquals(0, t.run(0l));
    }

}
