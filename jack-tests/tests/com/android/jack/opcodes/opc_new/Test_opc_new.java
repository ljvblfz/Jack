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

package com.android.jack.opcodes.opc_new;

import com.android.jack.opcodes.opc_new.jm.T_opc_new_1;
import com.android.jack.opcodes.opc_new.jm.T_opc_new_3;
import com.android.jack.opcodes.opc_new.jm.T_opc_new_9;
import com.android.jack.test.DxTestCase;


public class Test_opc_new extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_opc_new_1 t = new T_opc_new_1();
        String s = t.run();
        assertNotNull(s);
        assertEquals(0, s.compareTo("abc"));
    }

    /**
     * @title expected Error (exception during class loading)
     */
    public void testE1() {
        try {
            T_opc_new_3.run();
            fail("expected Error");
        } catch (Error e) {
            // expected
        }
    }

    /**
     * @constraint 4.8.1.18
     * @title attempt to instantiate abstract
     * class
     */
    public void testE5() {
        // @uses com.android.jack.opcodes.opc_new.jm.TestAbstractClass
        T_opc_new_9 t = new T_opc_new_9();
        try {
            t.run();
            fail("expected Error");
        } catch (Error iae) {
            // expected
        }
    }

}
