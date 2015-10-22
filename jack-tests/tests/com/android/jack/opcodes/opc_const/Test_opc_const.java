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

package com.android.jack.opcodes.opc_const;

import com.android.jack.opcodes.opc_const.jm.T_opc_const_1;
import com.android.jack.opcodes.opc_const.jm.T_opc_const_2;
import com.android.jack.test.DxTestCase;


public class Test_opc_const extends DxTestCase {

    /**
     * @title push float into stack
     */
    public void testN1() {
        T_opc_const_1 t = new T_opc_const_1();
        float a = 1.5f;
        float b = 0.04f;
        assertEquals(a + b, t.run(), 0f);
        assertEquals(1.54f, t.run(), 0f);
    }

    /**
     * @title push int into stack
     */
    public void testN2() {
        T_opc_const_2 t = new T_opc_const_2();
        int a = 1000000000;
        int b = 1000000000;
        assertEquals(a + b, t.run());
    }
}
