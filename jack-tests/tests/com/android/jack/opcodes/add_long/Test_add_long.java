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

package com.android.jack.opcodes.add_long;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.add_long.jm.T_add_long_1;
import com.android.jack.opcodes.add_long.jm.T_add_long_3;
import com.android.jack.opcodes.add_long.jm.T_add_long_4;
import com.android.jack.opcodes.add_long.jm.T_add_long_5;


public class Test_add_long extends DxTestCase {

    /**
     * @title Arguments = 12345678l, 87654321l
     */
    public void testN1() {
        T_add_long_1 t = new T_add_long_1();
        assertEquals(99999999l, t.run(12345678l, 87654321l));
    }

    /**
     * @title Arguments = 0l, 87654321l
     */
    public void testN2() {
        T_add_long_1 t = new T_add_long_1();
        assertEquals(87654321l, t.run(0l, 87654321l));
    }

    /**
     * @title Arguments = -12345678l, 0l
     */
    public void testN3() {
        T_add_long_1 t = new T_add_long_1();
        assertEquals(-12345678l, t.run(-12345678l, 0l));
    }

    /**
     * @title  Arguments: 0 + Long.MAX_VALUE
     */
    public void testB1() {
        T_add_long_1 t = new T_add_long_1();
        assertEquals(9223372036854775807L, t.run(0l, Long.MAX_VALUE));
    }

    /**
     * @title  Arguments: 0 + Long.MIN_VALUE
     */
    public void testB2() {
        T_add_long_1 t = new T_add_long_1();
        assertEquals(-9223372036854775808L, t.run(0l, Long.MIN_VALUE));
    }

    /**
     * @title  Arguments: 0 + 0
     */
    public void testB3() {
        T_add_long_1 t = new T_add_long_1();
        assertEquals(0l, t.run(0l, 0l));
    }

    /**
     * @title  Arguments: Long.MAX_VALUE + Long.MAX_VALUE
     */
    public void testB4() {
        T_add_long_1 t = new T_add_long_1();
        assertEquals(-2, t.run(Long.MAX_VALUE, Long.MAX_VALUE));
    }

    /**
     * @title  Arguments: Long.MAX_VALUE + Long.MIN_VALUE
     */
    public void testB5() {
        T_add_long_1 t = new T_add_long_1();
        assertEquals(-1l, t.run(Long.MAX_VALUE, Long.MIN_VALUE));
    }

    /**
     * @title  Arguments: Long.MIN_VALUE + Long.MIN_VALUE
     */
    public void testB6() {
        T_add_long_1 t = new T_add_long_1();
        assertEquals(0l, t.run(Long.MIN_VALUE, Long.MIN_VALUE));
    }

    /**
     * @title  Arguments: Long.MIN_VALUE + 1
     */
    public void testB7() {
        T_add_long_1 t = new T_add_long_1();
        assertEquals(-9223372036854775807l, t.run(Long.MIN_VALUE, 1l));
    }

    /**
     * @title  Arguments: Long.MAX_VALUE + 1
     */
    public void testB8() {
        T_add_long_1 t = new T_add_long_1();
        assertEquals(-9223372036854775808l, t.run(Long.MAX_VALUE, 1l));
    }

    /**
     * @title  Arguments: Long.MAX_VALUE + 1
     */
    public void testB9() {
        T_add_long_3 t = new T_add_long_3();
        assertEquals(9223372036854775807l, t.run(Long.MAX_VALUE, 1l));
    }


    /**
     * @title  Arguments: Long.MAX_VALUE + 1
     */
    public void testB10() {
        T_add_long_4 t = new T_add_long_4();
        assertEquals(-9223372036854775808l, t.run(Long.MAX_VALUE, 1));
    }


    /**
     * @title  Arguments: Long.MAX_VALUE + 1
     */
    public void testB11() {
        T_add_long_5 t = new T_add_long_5();
        assertEquals(9223372036854775807l, t.run(Long.MAX_VALUE, 1l));
    }

}
