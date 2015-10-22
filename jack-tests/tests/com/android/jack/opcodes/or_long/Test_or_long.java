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

package com.android.jack.opcodes.or_long;

import com.android.jack.opcodes.or_long.jm.T_or_long_1;
import com.android.jack.opcodes.or_long.jm.T_or_long_3;
import com.android.jack.opcodes.or_long.jm.T_or_long_4;
import com.android.jack.opcodes.or_long.jm.T_or_long_5;
import com.android.jack.test.DxTestCase;


public class Test_or_long extends DxTestCase {

    /**
     * @title Arguments = 123456789121l, 2l
     */
    public void testN1() {
        T_or_long_1 t = new T_or_long_1();
        assertEquals(123456789123l, t.run(123456789121l, 2l));
    }

    /**
     * @title Arguments = 0xffffffffffffff8l, 0xffffffffffffff1l
     */
    public void testN2() {
        T_or_long_1 t = new T_or_long_1();
        assertEquals(0xffffffffffffff9l, t.run(0xffffffffffffff8l,
                0xffffffffffffff1l));
    }

    /**
     * @title  Arguments = 0xabcdefabcdef, -1
     */
    public void testN3() {
        T_or_long_1 t = new T_or_long_1();
        assertEquals(-1l, t.run(0xabcdefabcdefl, -1l));
    }

    /**
     * @title  Arguments = 0xabcdefabcdef, -1
     */
    public void testN4() {
        T_or_long_3 t = new T_or_long_3();
        assertEquals(-1l, t.run(0xabcdefabcdefl, -1l));
    }


    /**
     * @title  Arguments = 0xabcdefabcdef, -1
     */
    public void testN5() {
        T_or_long_4 t = new T_or_long_4();
        assertEquals(-1l, t.run(0xabcdefa, -1l));
    }


    /**
     * @title  Arguments = 0xabcdefabcdef, -1
     */
    public void testN6() {
        T_or_long_5 t = new T_or_long_5();
        assertEquals(-1l, t.run(0xabcdefabcdefl, -1l));
    }


    /**
     * @title  Arguments = 0, -1
     */
    public void testB1() {
        T_or_long_1 t = new T_or_long_1();
        assertEquals(-1l, t.run(0l, -1l));
    }

    /**
     * @title  Arguments = Long.MAX_VALUE, Long.MIN_VALUE
     */
    public void testB2() {
        T_or_long_1 t = new T_or_long_1();
        assertEquals(-1l, t.run(Long.MAX_VALUE, Long.MIN_VALUE));
    }
}
