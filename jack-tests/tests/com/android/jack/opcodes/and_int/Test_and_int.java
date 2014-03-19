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

package com.android.jack.opcodes.and_int;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.and_int.jm.T_and_int_1;
import com.android.jack.opcodes.and_int.jm.T_and_int_3;
import com.android.jack.opcodes.and_int.jm.T_and_int_4;


public class Test_and_int extends DxTestCase {

    /**
     * @title Arguments = 15, 8
     */
    public void testN1() {
        T_and_int_1 t = new T_and_int_1();
        assertEquals(8, t.run(15, 8));
    }

    /**
     * @title Arguments = 0xfffffff8, 0xfffffff1
     */
    public void testN2() {
        T_and_int_1 t = new T_and_int_1();
        assertEquals(0xfffffff0, t.run(0xfffffff8, 0xfffffff1));
    }

    /**
     * @title  Arguments = 0xcafe & -1
     */
    public void testN3() {
        T_and_int_1 t = new T_and_int_1();
        assertEquals(0xcafe, t.run(0xcafe, -1));
    }

    /**
     * @title  Arguments = 0xcafe & -1
     */
    public void testN4() {
        T_and_int_3 t = new T_and_int_3();
        assertEquals(0xcafe, t.run(0xcafe, -1));
    }

    /**
     * @title  Arguments = 0xcafe & -1
     */
    public void testN5() {
        T_and_int_4 t = new T_and_int_4();
        assertEquals(0xcafe, t.run(0xcafe, -1));
    }

    /**
     * @title  Arguments = 0 & -1
     */
    public void testB1() {
        T_and_int_1 t = new T_and_int_1();
        assertEquals(0, t.run(0, -1));
    }

    /**
     * @title  Arguments = Integer.MAX_VALUE & Integer.MIN_VALUE
     */
    public void testB2() {
        T_and_int_1 t = new T_and_int_1();
        assertEquals(0, t.run(Integer.MAX_VALUE, Integer.MIN_VALUE));
    }

}
