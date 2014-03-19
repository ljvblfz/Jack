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

package com.android.jack.opcodes.const_wide;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.const_wide.jm.T_const_wide_1;
import com.android.jack.opcodes.const_wide.jm.T_const_wide_2;
import com.android.jack.opcodes.const_wide.jm.T_const_wide_3;
import com.android.jack.opcodes.const_wide.jm.T_const_wide_4;
import com.android.jack.opcodes.const_wide.jm.T_const_wide_5;
import com.android.jack.opcodes.const_wide.jm.T_const_wide_6;
import com.android.jack.opcodes.const_wide.jm.T_const_wide_7;


public class Test_const_wide extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_const_wide_1 t = new T_const_wide_1();
        long a = 20l;
        long b = 20l;
        assertEquals(a - b, t.run());
    }

    /**
     * @title normal test
     */
    public void testN2() {
        T_const_wide_2 t = new T_const_wide_2();
        long a = 20l;
        long b = 19l;
        assertEquals(a - b, t.run());
    }

    /**
     * @title normal test
     */
    public void testN3() {
        T_const_wide_3 t = new T_const_wide_3();
        double b = 1234d;
        double c = 1234d;
        double d = b - c;
        assertEquals(d, t.run());
    }

    /**
     * @title push long into stack
     */
    public void testN4() {
        T_const_wide_4 t = new T_const_wide_4();
        long a = 1234567890122l;
        long b = 1l;
        assertEquals(a + b, t.run());
    }

    /**
     * @title push double into stack
     */
    public void testN5() {
        T_const_wide_5 t = new T_const_wide_5();
        double a = 1234567890123232323232232323232323232323232323456788d;
        double b = 1d;
        assertEquals(a + b, t.run(), 0d);
    }

    public void testN6() {
        T_const_wide_6 t = new T_const_wide_6();
        assertEquals(1234567890123l, t.run());
    }

    public void testN7() {
        T_const_wide_7 t = new T_const_wide_7();
        assertEquals(9876543210123l, t.run());
    }

}
