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

package com.android.jack.opcodes.neg_long.ref;

import com.android.jack.opcodes.neg_long.jm.T_neg_long_1;
import com.android.jack.opcodes.neg_long.jm.T_neg_long_2;
import com.android.jack.opcodes.neg_long.jm.T_neg_long_4;
import com.android.jack.opcodes.neg_long.jm.T_neg_long_5;
import com.android.jack.opcodes.neg_long.jm.T_neg_long_6;
import com.android.jack.test.DxTestCase;


public class Test_neg_long extends DxTestCase {

    /**
     * @title Argument = 123123123272432432l
     */
    public void testN1() {
        T_neg_long_1 t = new T_neg_long_1();
        assertEquals(-123123123272432432l, t.run(123123123272432432l));
    }

    /**
     * @title  Argument = 1
     */
    public void testN2() {
        T_neg_long_1 t = new T_neg_long_1();
        assertEquals(-1l, t.run(1l));
    }

    /**
     * @title  Argument = -1
     */
    public void testN3() {
        T_neg_long_1 t = new T_neg_long_1();
        assertEquals(1l, t.run(-1l));
    }

    /**
     * @title  Check that -x == (~x + 1)
     */
    public void testN4() {
        T_neg_long_2 t = new T_neg_long_2();
        assertTrue(t.run(123123123272432432l));
    }

    /**
     * @title  Argument = -1
     */
    public void testN5() {
        T_neg_long_4 t = new T_neg_long_4();
        assertEquals(1l, t.run(-1l));
    }

    /**
     * @title  Argument = -1
     */
    public void testN6() {
        T_neg_long_5 t = new T_neg_long_5();
        assertEquals(1l, t.run(-1));
    }

    /**
     * @title  Argument = -1
     */
    public void testN7() {
        T_neg_long_6 t = new T_neg_long_6();
        assertEquals(1l, t.run(-1l));
    }

    /**
     * @title  Argument = 0
     */
    public void testB1() {
        T_neg_long_1 t = new T_neg_long_1();
        assertEquals(0, t.run(0));
    }

    /**
     * @title  Argument = Long.MAX_VALUE
     */
    public void testB2() {
        T_neg_long_1 t = new T_neg_long_1();
        assertEquals(-9223372036854775807L, t.run(Long.MAX_VALUE));
    }

    /**
     * @title  Argument = Long.MIN_VALUE
     */
    public void testB3() {
        T_neg_long_1 t = new T_neg_long_1();
        assertEquals(-9223372036854775808L, t.run(Long.MIN_VALUE));
    }

}
