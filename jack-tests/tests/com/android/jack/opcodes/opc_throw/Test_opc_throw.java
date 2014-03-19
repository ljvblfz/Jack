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

package com.android.jack.opcodes.opc_throw;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.opc_throw.jm.T_opc_throw_1;
import com.android.jack.opcodes.opc_throw.jm.T_opc_throw_12;
import com.android.jack.opcodes.opc_throw.jm.T_opc_throw_2;
import com.android.jack.opcodes.opc_throw.jm.T_opc_throw_8;


public class Test_opc_throw extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_opc_throw_1 t = new T_opc_throw_1();
        try {
            t.run();
            fail("must throw a RuntimeException");
        } catch (RuntimeException re) {
            // expected
        }
    }

    /**
     * @title  Throwing of the objectref on the class Throwable
     */
    public void testN2() {
        T_opc_throw_2 t = new T_opc_throw_2();
        try {
            t.run();
            fail("must throw a Throwable");
        } catch (Throwable e) {
            // expected
        }
    }

    /**
     * @title  Throwing of the objectref on the subclass of Throwable
     */
    public void testN3() {
        T_opc_throw_8 t = new T_opc_throw_8();
        try {
            t.run();
            fail("must throw a Error");
        } catch (Error e) {
            // expected
        }
    }

    /**
     * @title  Nearest matching catch must be executed in case of exception
     */
    public void testN4() {
        T_opc_throw_12 t = new T_opc_throw_12();
        assertTrue(t.run());
    }

}
