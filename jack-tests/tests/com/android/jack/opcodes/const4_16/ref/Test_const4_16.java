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

package com.android.jack.opcodes.const4_16.ref;

import com.android.jack.opcodes.const4_16.jm.T_const4_16_1;
import com.android.jack.opcodes.const4_16.jm.T_const4_16_10;
import com.android.jack.opcodes.const4_16.jm.T_const4_16_11;
import com.android.jack.opcodes.const4_16.jm.T_const4_16_12;
import com.android.jack.opcodes.const4_16.jm.T_const4_16_13;
import com.android.jack.opcodes.const4_16.jm.T_const4_16_14;
import com.android.jack.opcodes.const4_16.jm.T_const4_16_15;
import com.android.jack.opcodes.const4_16.jm.T_const4_16_16;
import com.android.jack.opcodes.const4_16.jm.T_const4_16_2;
import com.android.jack.opcodes.const4_16.jm.T_const4_16_3;
import com.android.jack.opcodes.const4_16.jm.T_const4_16_4;
import com.android.jack.opcodes.const4_16.jm.T_const4_16_5;
import com.android.jack.opcodes.const4_16.jm.T_const4_16_6;
import com.android.jack.opcodes.const4_16.jm.T_const4_16_7;
import com.android.jack.opcodes.const4_16.jm.T_const4_16_8;
import com.android.jack.opcodes.const4_16.jm.T_const4_16_9;
import com.android.jack.test.DxTestCase;
import com.android.jack.opcodes.const4_16.jm.T_const4_16_17;


public class Test_const4_16 extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_const4_16_1 t = new T_const4_16_1();
        assertEquals(100, t.run());
    }

    /**
     * @title normal test
     */
    public void testB1() {
        T_const4_16_2 t = new T_const4_16_2();
        assertEquals(0, t.run());
    }

    /**
     * @title normal test
     */
    public void testB2() {
        T_const4_16_3 t = new T_const4_16_3();
        assertEquals(-1, t.run());
    }

    /**
     * @title normal test
     */
    public void testB3() {
        T_const4_16_4 t = new T_const4_16_4();
        assertEquals(1, t.run());
    }

    /**
     * @title const/16 -13570
     */
    public void testN2() {
        T_const4_16_5 t = new T_const4_16_5();
        assertEquals(-13570, t.run());
    }

    /**
     * @title const/4 0
     */
    public void testB4() {
      T_const4_16_6 t = new T_const4_16_6();
        assertEquals(0, t.run());
    }

    /**
     * @title const/4 - 1
     */
    public void testB5() {
      T_const4_16_7 t = new T_const4_16_7();
        assertEquals(-1, t.run());
    }

    /**
     * @title normal test
     */
    public void testN3() {
        T_const4_16_8 t = new T_const4_16_8();
        int b = 1234;
        int c = 1234;
        int d = b - c;
        assertEquals(d, t.run());
    }

    /**
     * @title normal test
     */
    public void testN4() {
        T_const4_16_9 t = new T_const4_16_9();
        int b = 1235;
        int c = 1234;
        int d = b - c;
        assertEquals(d, t.run());
    }

    /**
     * @title normal test
     */
    public void testN5() {
        T_const4_16_10 t = new T_const4_16_10();
        int b = 1236;
        int c = 1234;
        int d = b - c;
        assertEquals(d, t.run());
    }

    /**
     * @title normal test
     */
    public void testN6() {
        T_const4_16_11 t = new T_const4_16_11();
        int b = 1237;
        int c = 1234;
        int d = b - c;
        assertEquals(d, t.run());
    }

    /**
     * @title normal test
     */
    public void testN7() {
        T_const4_16_12 t = new T_const4_16_12();
        int b = 1238;
        int c = 1234;
        int d = b - c;
        assertEquals(d, t.run());
    }

    /**
     * @title normal test
     */
    public void testN8() {
        T_const4_16_13 t = new T_const4_16_13();
        int b = 1239;
        int c = 1234;
        int d = b - c;
        assertEquals(d, t.run());
    }

    /**
     * @title normal test
     */
    public void testN9() {
        T_const4_16_14 t = new T_const4_16_14();
        int b = 1233;
        int c = 1234;
        int d = b - c;
        assertEquals(d, t.run());
    }

    /**
     * @title normal test
     */
    public void testN1Float_0() {
        T_const4_16_15 t = new T_const4_16_15();
        float b = 1234f;
        float c = 1234f;
        float d = b - c;
        assertEquals(d, t.run());
    }

    /**
     * @title normal test
     */
    public void testN1Float_1() {
        T_const4_16_16 t = new T_const4_16_16();
        float b = 1235f;
        float c = 1234f;
        float d = b - c;
        assertEquals(d, t.run());
    }

    /**
     * @title normal test
     */
    public void testN1Float_2() {
        T_const4_16_17 t = new T_const4_16_17();
        float b = 1236f;
        float c = 1234f;
        float d = b - c;
        assertEquals(d, t.run());
    }
}
