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

package com.android.jack.opcodes.invoke_static;

import com.android.jack.opcodes.invoke_static.jm.T_invoke_static_1;
import com.android.jack.opcodes.invoke_static.jm.T_invoke_static_12;
import com.android.jack.opcodes.invoke_static.jm.T_invoke_static_13;
import com.android.jack.opcodes.invoke_static.jm.T_invoke_static_14;
import com.android.jack.opcodes.invoke_static.jm.T_invoke_static_15;
import com.android.jack.opcodes.invoke_static.jm.T_invoke_static_18;
import com.android.jack.opcodes.invoke_static.jm.T_invoke_static_2;
import com.android.jack.opcodes.invoke_static.jm.T_invoke_static_4;
import com.android.jack.opcodes.invoke_static.jm.T_invoke_static_5;
import com.android.jack.opcodes.invoke_static.jm.T_invoke_static_6;
import com.android.jack.opcodes.invoke_static.jm.T_invoke_static_7;
import com.android.jack.opcodes.invoke_static.jm.T_invoke_static_8;
import com.android.jack.test.DxTestCase;


public class Test_invoke_static extends DxTestCase {

    /**
     * @title  Static method from library class Math
     */
    public void testN1() {
        T_invoke_static_1 t = new T_invoke_static_1();
        assertEquals(1234567, t.run());
    }

    /**
     * @title  Static method from user class
     */
    public void testN2() {
        // @uses com.android.jack.opcodes.invoke_static.jm.TestClass
        T_invoke_static_2 t = new T_invoke_static_2();
        assertEquals(777, t.run());
    }

    /**
     * @title  Check that <clinit> is called
     */
    public void testN3() {
        assertEquals(123456789l, T_invoke_static_4.run());
    }

    /**
     * @title  Check that monitor is acquired if method is synchronized
     */
    public void testN4() {
        assertTrue(T_invoke_static_12.execute());
    }

    /**
     * @title  Check that new frame is created by invoke_static and
     * arguments are passed to method
     */
    public void testN5() {
        // @uses com.android.jack.opcodes.invoke_static.jm.TestClass
        T_invoke_static_15 t = new T_invoke_static_15();
        assertTrue(t.run());
    }

    /**
     * @title  Static protected method from other class in the same package
     */
    public void testN6() {
        // @uses com.android.jack.opcodes.invoke_static.jm.TestClass
        T_invoke_static_18 t = new T_invoke_static_18();
        assertEquals(888, t.run());
    }

    /**
     * @title  attempt to call non-static method
     *
     */
    public void testE1() {
        T_invoke_static_5 t = new T_invoke_static_5();
        t.run();
    }

    /**
     * @title  Native method can't be linked
     *
     */
    public void testE2() {
        T_invoke_static_6 t = new T_invoke_static_6();
        try {
            t.run();
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError ule) {
            // expected
        }
    }

    public void testN7() {
        T_invoke_static_7 t = new T_invoke_static_7();
        t.run();
    }

    public void testN8() {
        T_invoke_static_8 t = new T_invoke_static_8();
        t.run();
    }

    public void testN9() {
        T_invoke_static_13 t = new T_invoke_static_13();
        t.run();
    }

    /**
     * @title  initialization of referenced class throws exception
     */
    public void testE7() {
        // @uses com.android.jack.opcodes.invoke_static.jm.TestClassInitError
        T_invoke_static_14 t = new T_invoke_static_14();
        try {
            t.run();
            fail("expected Error");
        } catch (Error e) {
            // expected
        }
    }

}
