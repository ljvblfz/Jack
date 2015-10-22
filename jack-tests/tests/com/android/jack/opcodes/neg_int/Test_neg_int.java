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

package com.android.jack.opcodes.neg_int;

import com.android.jack.opcodes.neg_int.jm.T_neg_int_1;
import com.android.jack.opcodes.neg_int.jm.T_neg_int_2;
import com.android.jack.test.DxTestCase;


public class Test_neg_int extends DxTestCase {

    /**
     * @title  Argument = 1
     */
    public void testN1() {
        T_neg_int_1 t = new T_neg_int_1();
        assertEquals(-1, t.run(1));
    }

    /**
     * @title  Argument = -1
     */
    public void testN2() {
        T_neg_int_1 t = new T_neg_int_1();
        assertEquals(1, t.run(-1));
    }

    /**
     * @title  Argument = 32768
     */
    public void testN3() {
        T_neg_int_1 t = new T_neg_int_1();
        assertEquals(-32768, t.run(32768));
    }

    /**
     * @title  Argument = 0
     */
    public void testN4() {
        T_neg_int_1 t = new T_neg_int_1();
        assertEquals(0, t.run(0));
    }

    /**
     * @title  Check that -x == (~x + 1)
     */
    public void testN5() {
        T_neg_int_2 t = new T_neg_int_2();
        assertTrue(t.run(12345));
    }

    /**
     * @title  Check that -x == (~x + 1)
     */
    public void testN6() {
        T_neg_int_2 t = new T_neg_int_2();
        assertTrue(t.run(12345));
    }


    /**
     * @title  Argument = Integer.MAX_VALUE
     */
    public void testB1() {
        T_neg_int_1 t = new T_neg_int_1();
        assertEquals(0x80000001, t.run(Integer.MAX_VALUE));
    }

    /**
     * @title  Argument = Integer.MIN_VALUE
     */
    public void testB2() {
        T_neg_int_1 t = new T_neg_int_1();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MIN_VALUE));
    }

}
