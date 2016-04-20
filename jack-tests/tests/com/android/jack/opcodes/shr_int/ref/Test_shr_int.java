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

package com.android.jack.opcodes.shr_int.ref;

import com.android.jack.opcodes.shr_int.jm.T_shr_int_1;
import com.android.jack.opcodes.shr_int.jm.T_shr_int_3;
import com.android.jack.opcodes.shr_int.jm.T_shr_int_4;
import com.android.jack.test.DxTestCase;


public class Test_shr_int extends DxTestCase {

    /**
     * @title  15 >> 1
     */
    public void testN1() {
        T_shr_int_1 t = new T_shr_int_1();
        assertEquals(7, t.run(15, 1));
    }

    /**
     * @title  33 >> 2
     */
    public void testN2() {
        T_shr_int_1 t = new T_shr_int_1();
        assertEquals(8, t.run(33, 2));
    }

    /**
     * @title  -15 >> 1
     */
    public void testN3() {
        T_shr_int_1 t = new T_shr_int_1();
        assertEquals(-8, t.run(-15, 1));
    }

    /**
     * @title  Arguments = 1 & -1
     */
    public void testN4() {
        T_shr_int_1 t = new T_shr_int_1();
        assertEquals(0, t.run(1, -1));
    }

    /**
     * @title  Verify that shift distance is actually in range 0 to 32.
     */
    public void testN5() {
        T_shr_int_1 t = new T_shr_int_1();
        assertEquals(16, t.run(33, 33));
    }

    /**
     * @title  Verify that shift distance is actually in range 0 to 32.
     */
    public void testN6() {
        T_shr_int_3 t = new T_shr_int_3();
        assertEquals(16, t.run(33, 33));
    }

    /**
     * @title  Verify that shift distance is actually in range 0 to 32.
     */
    public void testN7() {
        T_shr_int_4 t = new T_shr_int_4();
        assertEquals(16, t.run(33, 33));
    }

    /**
     * @title  Arguments = 0 & -1
     */
    public void testB1() {
        T_shr_int_1 t = new T_shr_int_1();
        assertEquals(0, t.run(0, -1));
    }

    /**
     * @title  Arguments = Integer.MAX_VALUE & 1
     */
    public void testB2() {
        T_shr_int_1 t = new T_shr_int_1();
        assertEquals(0x3FFFFFFF, t.run(Integer.MAX_VALUE, 1));
    }

    /**
     * @title  Arguments = Integer.MIN_VALUE & 1
     */
    public void testB3() {
        T_shr_int_1 t = new T_shr_int_1();
        assertEquals(0xc0000000, t.run(Integer.MIN_VALUE, 1));
    }

    /**
     * @title  Arguments = 1 & 0
     */
    public void testB4() {
        T_shr_int_1 t = new T_shr_int_1();
        assertEquals(1, t.run(1, 0));
    }

}
