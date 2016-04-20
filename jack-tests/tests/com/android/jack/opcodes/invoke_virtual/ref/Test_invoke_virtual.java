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

package com.android.jack.opcodes.invoke_virtual.ref;

import com.android.jack.opcodes.invoke_virtual.jm.T_invoke_virtual_1;
import com.android.jack.opcodes.invoke_virtual.jm.T_invoke_virtual_13;
import com.android.jack.opcodes.invoke_virtual.jm.T_invoke_virtual_14;
import com.android.jack.opcodes.invoke_virtual.jm.T_invoke_virtual_17;
import com.android.jack.opcodes.invoke_virtual.jm.T_invoke_virtual_19;
import com.android.jack.opcodes.invoke_virtual.jm.T_invoke_virtual_2;
import com.android.jack.opcodes.invoke_virtual.jm.T_invoke_virtual_22;
import com.android.jack.opcodes.invoke_virtual.jm.T_invoke_virtual_23;
import com.android.jack.opcodes.invoke_virtual.jm.T_invoke_virtual_4;
import com.android.jack.opcodes.invoke_virtual.jm.T_invoke_virtual_7;
import com.android.jack.test.DxTestCase;


public class Test_invoke_virtual extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_invoke_virtual_1 t = new T_invoke_virtual_1();
        int a = 1;
        String sa = "a" + a;
        String sb = "a1";
        assertTrue(t.run(sa, sb));
        assertFalse(t.run(this, sa));
        assertFalse(t.run(sb, this));
    }

    /**
     * @title  Check that monitor is acquired if method is synchronized
     */
    public void testN2() {
        assertTrue(T_invoke_virtual_2.execute());
    }

    /**
     * @title  Invoke protected method of superclass
     */
    public void testN3() {
        // @uses com.android.jack.opcodes.invoke_virtual.jm.TSuper
        T_invoke_virtual_7 t = new T_invoke_virtual_7();
        assertEquals(5, t.run());
    }

    /**
     * @title  Private method call
     */
    public void testN4() {
        T_invoke_virtual_13 t = new T_invoke_virtual_13();
        assertEquals(345, t.run());
    }

    /**
     * @title  Check that new frame is created by invoke_virtual and
     * arguments are passed to method
     */
    public void testN5() {
        // @uses com.android.jack.opcodes.invoke_virtual.jm.TSuper
        T_invoke_virtual_14 t = new T_invoke_virtual_14();
        assertTrue(t.run());
    }

    /**
     * @title  Recursion of method lookup procedure
     */
    public void testN6() {
        // @uses com.android.jack.opcodes.invoke_virtual.jm.TSuper
        T_invoke_virtual_17 t = new T_invoke_virtual_17();
        assertEquals(5, t.run());
    }

    /**
     * @title  Recursion of method lookup procedure
     */
    public void testN7() {
        // @uses com.android.jack.opcodes.invoke_virtual.jm.TSuper
        T_invoke_virtual_19 t = new T_invoke_virtual_19();
        assertEquals(3, t.run());
    }

    public void testN8() {
        T_invoke_virtual_22 t = new T_invoke_virtual_22();
        assertEquals(1, t.run());
    }

    public void testN9() {
        T_invoke_virtual_23 t = new T_invoke_virtual_23();
        assertEquals(13, t.run());
    }

    /**
     * @title
     */
    public void testE1() {
        T_invoke_virtual_1 t = new T_invoke_virtual_1();
        String s = "s";
        try {
            t.run(null, s);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }
    }

    /**
     * @title  Native method can't be linked
     */
    public void testE2() {
        T_invoke_virtual_4 t = new T_invoke_virtual_4();
        try {
            t.run();
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError ule) {
            // expected
        }
    }
}
