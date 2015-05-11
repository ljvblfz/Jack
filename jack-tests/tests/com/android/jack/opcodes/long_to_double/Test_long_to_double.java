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

package com.android.jack.opcodes.long_to_double;

import com.android.jack.opcodes.long_to_double.jm.T_long_to_double_1;
import com.android.jack.opcodes.long_to_double.jm.T_long_to_double_3;
import com.android.jack.opcodes.long_to_double.jm.T_long_to_double_4;
import com.android.jack.test.DxTestCase;


public class Test_long_to_double extends DxTestCase {

    /**
     * @title  Argument = 50000000000
     */
    public void testN1() {
        T_long_to_double_1 t = new T_long_to_double_1();
        assertEquals(5.0E10d, t.run(50000000000l), 0d);
    }

    /**
     * @title  Argument = 1
     */
    public void testN2() {
        T_long_to_double_1 t = new T_long_to_double_1();
        assertEquals(1d, t.run(1l), 0d);
    }

    /**
     * @title  Argument = -1
     */
    public void testN3() {
        T_long_to_double_1 t = new T_long_to_double_1();
        assertEquals(-1d, t.run(-1l), 0d);
    }

    /**
     * @title  Argument = -1
     */
    public void testN4() {
        T_long_to_double_3 t = new T_long_to_double_3();
        assertEquals(-1d, t.run(-1l), 0d);
    }

    /**
     * @title  Argument = -1
     */
    public void testN5() {
        T_long_to_double_4 t = new T_long_to_double_4();
        assertEquals(-1d, t.run(-1), 0d);
    }

    /**
     * @title  Argument = Long.MAX_VALUE
     */
    public void testB1() {
        T_long_to_double_1 t = new T_long_to_double_1();
        assertEquals(9.223372036854776E18d, t.run(Long.MAX_VALUE), 0d);
    }

    /**
     * @title  Argument = Long.MIN_VALUE
     */
    public void testB2() {
        T_long_to_double_1 t = new T_long_to_double_1();
        assertEquals(-9.223372036854776E18, t.run(Long.MIN_VALUE), 0d);
    }

    /**
     * @title  Argument = 0
     */
    public void testB3() {
        T_long_to_double_1 t = new T_long_to_double_1();
        assertEquals(0d, t.run(0), 0d);
    }

}
