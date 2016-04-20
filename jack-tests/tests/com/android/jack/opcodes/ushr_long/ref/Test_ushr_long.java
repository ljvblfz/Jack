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

package com.android.jack.opcodes.ushr_long.ref;

import com.android.jack.opcodes.ushr_long.jm.T_ushr_long_1;
import com.android.jack.opcodes.ushr_long.jm.T_ushr_long_3;
import com.android.jack.opcodes.ushr_long.jm.T_ushr_long_4;
import com.android.jack.opcodes.ushr_long.jm.T_ushr_long_5;
import com.android.jack.test.DxTestCase;


public class Test_ushr_long extends DxTestCase {

    /**
     * @title Arguments = 40000000000l, 3
     */
    public void testN1() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(5000000000l, t.run(40000000000l, 3));
    }

    /**
     * @title Arguments = 40000000000l, 1
     */
    public void testN2() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(20000000000l, t.run(40000000000l, 1));
    }

    /**
     * @title Arguments = -123456789l, 1
     */
    public void testN3() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(0x7FFFFFFFFC521975l, t.run(-123456789l, 1));
    }

    /**
     * @title  Arguments = 1, -1
     */
    public void testN4() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(0l, t.run(1l, -1));
    }

    /**
     * @title Arguments = 123456789l, 64
     */
    public void testN5() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(123456789l, t.run(123456789l, 64));
    }

    /**
     * @title Arguments = 123456789l, 63
     */
    public void testN6() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(0l, t.run(123456789l, 63));
    }

    /**
     * @title Arguments = 123456789l, 63
     */
    public void testN7() {
        T_ushr_long_3 t = new T_ushr_long_3();
        assertEquals(0l, t.run(123456789l, 63));
    }

    /**
     * @title Arguments = 123456789l, 63
     */
    public void testN8() {
        T_ushr_long_4 t = new T_ushr_long_4();
        assertEquals(0l, t.run(123456789, 63));
    }

    /**
     * @title Arguments = 123456789l, 63
     */
    public void testN9() {
        T_ushr_long_5 t = new T_ushr_long_5();
        assertEquals(0l, t.run(123456789l, 63));
    }

    /**
     * @title  Arguments = 0, -1
     */
    public void testB1() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(0l, t.run(0l, -1));
    }

    /**
     * @title  Arguments = Long.MAX_VALUE, 1
     */
    public void testB2() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(0x3FFFFFFFFFFFFFFFl, t.run(Long.MAX_VALUE, 1));
    }

    /**
     * @title  Arguments = Long.MIN_VALUE, 1
     */
    public void testB3() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(0x4000000000000000l, t.run(Long.MIN_VALUE, 1));
    }

    /**
     * @title  Arguments = 1, 0
     */
    public void testB4() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(1l, t.run(1l, 0));
    }

}
