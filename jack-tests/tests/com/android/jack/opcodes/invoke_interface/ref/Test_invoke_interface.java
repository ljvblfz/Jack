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

package com.android.jack.opcodes.invoke_interface.ref;

import com.android.jack.opcodes.invoke_interface.jm.ITestImpl;
import com.android.jack.opcodes.invoke_interface.jm.T_invoke_interface_1;
import com.android.jack.opcodes.invoke_interface.jm.T_invoke_interface_12;
import com.android.jack.opcodes.invoke_interface.jm.T_invoke_interface_14;
import com.android.jack.opcodes.invoke_interface.jm.T_invoke_interface_19;
import com.android.jack.opcodes.invoke_interface.jm.T_invoke_interface_2;
import com.android.jack.test.DxTestCase;


public class Test_invoke_interface extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_invoke_interface_1 t = new T_invoke_interface_1();
        assertEquals(0, t.run("aa", "aa"));
        assertEquals(-1, t.run("aa", "bb"));
        assertEquals(1, t.run("bb", "aa"));
    }

    /**
     * @title  Check that new frame is created by invoke_interface and
     * arguments are passed to method
     */
    public void testN2() {
        //@uses com.android.jack.opcodes.invoke_interface.jm.ITest
        //@uses com.android.jack.opcodes.invoke_interface.jm.ITestImpl
        T_invoke_interface_14 t = new T_invoke_interface_14();
        ITestImpl impl = new ITestImpl();
        assertEquals(1, t.run(impl));
    }

    /**
     * @title  Check that monitor is acquired if method is synchronized
     */
    public void testN3() {
        //@uses com.android.jack.opcodes.invoke_interface.jm.ITest
        assertTrue(T_invoke_interface_19.execute());
    }

    /**
     * @title expected NullPointerException
     */
    public void testE3() {
        //@uses com.android.jack.opcodes.invoke_interface.jm.ITest
        try {
            new T_invoke_interface_2(null);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }
    }

    /**
     * @title  Native method can't be linked
     */
    public void testE5() {
        //@uses com.android.jack.opcodes.invoke_interface.jm.ITest
        //@uses com.android.jack.opcodes.invoke_interface.jm.ITestImpl
        T_invoke_interface_12 t = new T_invoke_interface_12();
        ITestImpl impl = new ITestImpl();
        try {
            t.run(impl);
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
            // expected
        }
    }
}
