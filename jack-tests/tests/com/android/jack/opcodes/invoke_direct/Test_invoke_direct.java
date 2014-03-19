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

package com.android.jack.opcodes.invoke_direct;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.invoke_direct.jm.T_invoke_direct_2;
import com.android.jack.opcodes.invoke_direct.jm.T_invoke_direct_21;



public class Test_invoke_direct extends DxTestCase {

    /**
     * @title  private method call
     */
    public void testN2() {
        T_invoke_direct_2 t = new T_invoke_direct_2();
        assertEquals(345, t.run());
    }

    /**
     * @title  Check that new frame is created by invokespecial
     */
    public void testN7() {
        //@uses com.android.jack.opcodes.invokespecial.jm.TSuper
        T_invoke_direct_21 t = new T_invoke_direct_21();
        assertEquals(1, t.run());
    }
}
