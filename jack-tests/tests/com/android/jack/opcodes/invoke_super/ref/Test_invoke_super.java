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

package com.android.jack.opcodes.invoke_super.ref;

import com.android.jack.opcodes.invoke_super.jm.T_invoke_super_1;
import com.android.jack.opcodes.invoke_super.jm.T_invoke_super_15;
import com.android.jack.opcodes.invoke_super.jm.T_invoke_super_18;
import com.android.jack.opcodes.invoke_super.jm.T_invoke_super_19;
import com.android.jack.opcodes.invoke_super.jm.T_invoke_super_26;
import com.android.jack.test.DxTestCase;


public class Test_invoke_super extends DxTestCase {
    /**
     * @title  Superclass' method call
     */
    public void testN1() {
        //@uses com.android.jack.opcodes.invokespecial.jm.TSuper
        T_invoke_super_1 t = new T_invoke_super_1();
        assertEquals(5, t.run());
    }

    /**
     * @title  Invoke method of superclass of superclass
     */
    public void testN3() {
        //@uses com.android.jack.opcodes.invokespecial.jm.TSuper
        //@uses com.android.jack.opcodes.invokespecial.jm.TSuper2
        T_invoke_super_15 t = new T_invoke_super_15();
        assertEquals(5, t.run());
    }

    /**
     * @title  Invoke protected method of superclass if method with the
     * same name exists in "this" class
     */
    public void testN5() {
        //@uses com.android.jack.opcodes.invokespecial.jm.TSuper
        T_invoke_super_18 t = new T_invoke_super_18();
        assertEquals(5, t.run());
    }

    /**
     * @title  Check that method's arguments are popped from stack
     */
    public void testN6() {
        //@uses com.android.jack.opcodes.invokespecial.jm.TSuper
        T_invoke_super_19 t = new T_invoke_super_19();
        assertEquals(2, t.run());
    }

    public void testN10() {
      T_invoke_super_26 t = new T_invoke_super_26();
      assertEquals(13, t.run());
    }
}
