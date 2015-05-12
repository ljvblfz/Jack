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

package com.android.jack.opcodes.sget;

import com.android.jack.opcodes.sget.jm.T_sget_1;
import com.android.jack.opcodes.sget.jm.T_sget_11;
import com.android.jack.opcodes.sget.jm.T_sget_2;
import com.android.jack.opcodes.sget.jm.T_sget_5;
import com.android.jack.opcodes.sget.jm.T_sget_9;
import com.android.jack.test.DxTestCase;


public class Test_sget extends DxTestCase {

    /**
     * @title  type - int
     */
    public void testN1() {
        T_sget_1 t = new T_sget_1();
        assertEquals(35, t.run());
    }

    /**
     * @title  type - double
     */
    public void testN2() {
        T_sget_2 t = new T_sget_2();
        assertEquals(123d, t.run());
    }

    /**
     * @title  access protected field from subclass
     */
    public void testN3() {
        // @uses com.android.jack.opcodes.sget.jm.T_sget_1
        T_sget_11 t = new T_sget_11();
        assertEquals(10, t.run());
    }

    /**
     * @title  attempt to access non-static field
     */
    public void testN4() {
        T_sget_5 t = new T_sget_5();
        assertEquals(5, t.run());
    }

    /**
     * @title  initialization of referenced class throws exception
     */
    public void testE6() {
        // @uses com.android.jack.opcodes.sget.jm.StubInitError
        T_sget_9 t = new T_sget_9();
        try {
            t.run();
            fail("expected Error");
        } catch (Error e) {
            // expected
        }
    }
}
