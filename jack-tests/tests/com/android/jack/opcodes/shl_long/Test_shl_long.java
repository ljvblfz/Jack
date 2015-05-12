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

package com.android.jack.opcodes.shl_long;

import com.android.jack.opcodes.shl_long.jm.T_shl_long_1;
import com.android.jack.opcodes.shl_long.jm.T_shl_long_3;
import com.android.jack.opcodes.shl_long.jm.T_shl_long_4;
import com.android.jack.opcodes.shl_long.jm.T_shl_long_5;
import com.android.jack.test.DxTestCase;


public class Test_shl_long extends DxTestCase {

    /**
     * @title Arguments = 5000000000l, 3
     */
    public void testN1() {
        T_shl_long_1 t = new T_shl_long_1();
        assertEquals(40000000000l, t.run(5000000000l, 3));
    }

    /**
     * @title Arguments = 5000000000l, 1
     */
    public void testN2() {
        T_shl_long_1 t = new T_shl_long_1();
        assertEquals(10000000000l, t.run(5000000000l, 1));
    }

    /**
     * @title Arguments = -5000000000l, 1
     */
    public void testN3() {
        T_shl_long_1 t = new T_shl_long_1();
        assertEquals(-10000000000l, t.run(-5000000000l, 1));
    }

    /**
     * @title  Arguments = 1, -1
     */
    public void testN4() {
        T_shl_long_1 t = new T_shl_long_1();
        assertEquals(0x8000000000000000l, t.run(1l, -1));
    }

    /**
     * @title  Verify that shift distance is actually in range 0 to 64.
     */
    public void testN5() {
        T_shl_long_1 t = new T_shl_long_1();
        assertEquals(130l, t.run(65l, 65));
    }

    /**
     * @title  Verify that shift distance is actually in range 0 to 64.
     */
    public void testN6() {
        T_shl_long_3 t = new T_shl_long_3();
        assertEquals(130l, t.run(65l, 65));
    }

    /**
     * @title  Verify that shift distance is actually in range 0 to 64.
     */
    public void testN7() {
        T_shl_long_4 t = new T_shl_long_4();
        assertEquals(130l, t.run(65, 65));
    }

    /**
     * @title  Verify that shift distance is actually in range 0 to 64.
     */
    public void testN8() {
        T_shl_long_5 t = new T_shl_long_5();
        assertEquals(130l, t.run(65l, 65));
    }

    /**
     * @title  Arguments = 0, -1
     */
    public void testB1() {
        T_shl_long_1 t = new T_shl_long_1();
        assertEquals(0, t.run(0, -1));
    }

    /**
     * @title  Arguments = 1, 0
     */
    public void testB2() {
        T_shl_long_1 t = new T_shl_long_1();
        assertEquals(1, t.run(1, 0));
    }

    /**
     * @title  Arguments = Long.MAX_VALUE, 1
     */
    public void testB3() {
        T_shl_long_1 t = new T_shl_long_1();
        assertEquals(0xfffffffe, t.run(Long.MAX_VALUE, 1));
    }

    /**
     * @title  Arguments = Long.MIN_VALUE, 1
     */
    public void testB4() {
        T_shl_long_1 t = new T_shl_long_1();
        assertEquals(0l, t.run(Long.MIN_VALUE, 1));
    }

}
