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

package com.android.jack.opcodes.return_object.ref;

import com.android.jack.opcodes.return_object.jm.T_return_object_1;
import com.android.jack.opcodes.return_object.jm.T_return_object_12;
import com.android.jack.opcodes.return_object.jm.T_return_object_13;
import com.android.jack.opcodes.return_object.jm.T_return_object_2;
import com.android.jack.opcodes.return_object.jm.T_return_object_6;
import com.android.jack.opcodes.return_object.jm.T_return_object_7;
import com.android.jack.opcodes.return_object.jm.T_return_object_9;
import com.android.jack.test.DxTestCase;


public class Test_return_object extends DxTestCase {

    /**
     * @title  simple
     */
    public void testN1() {
        T_return_object_1 t = new T_return_object_1();
        assertEquals("hello", t.run());
    }

    /**
     * @title  simple
     */
    public void testN2() {
        T_return_object_1 t = new T_return_object_1();
        assertEquals(t, t.run2());
    }

    /**
     * @title  simple
     */
    public void testN3() {
        T_return_object_1 t = new T_return_object_1();
        Integer a = 12345;
        assertEquals(a, t.run3());
    }

    /**
     * @title test for null
     */
    public void testN4() {
        T_return_object_2 t = new T_return_object_2();
        assertNull(t.run());
    }

    /**
     * @title  check that frames are discarded and reinstananted correctly
     */
    public void testN5() {
        T_return_object_6 t = new T_return_object_6();
        assertEquals("hello", t.run());
    }

    /**
     * @title  check that monitor is released by return_object
     */
    public void testN6() {
        assertTrue(T_return_object_7.execute());
    }

    /**
     * @title  assignment compatibility (TChild returned as TSuper)
     */
    public void testN7() {
        // @uses com.android.jack.opcodes.return_object.jm.TSuper
        // @uses com.android.jack.opcodes.return_object.jm.TInterface
        // @uses com.android.jack.opcodes.return_object.jm.TChild
        T_return_object_12 t = new T_return_object_12();
        assertTrue(t.run());
    }

    /**
     * @title  assignment compatibility (TChild returned as TInterface)
     */
    public void testN8() {
        // @uses com.android.jack.opcodes.return_object.jm.TInterface
        // @uses com.android.jack.opcodes.return_object.jm.TChild
        // @uses com.android.jack.opcodes.return_object.jm.TSuper
        T_return_object_13 t = new T_return_object_13();
        assertTrue(t.run());
    }

    /**
     * @title  Lock structural rule 1 is violated
     */
    public void testE2() {
        T_return_object_9 t = new T_return_object_9();
        try {
            assertEquals("abc", t.run());
            System.out.print("dvmvfe:");
            //fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
            // expected
        }
    }

    /**
     * @constraint 4.8.2.14
     * @title assignment incompatible references
     */
    @SuppressWarnings("cast")
    public void testVFE7() {
        // @uses com.android.jack.opcodes.return_object.jm.TSuper2
        // @uses com.android.jack.opcodes.return_object.Runner
        // @uses com.android.jack.opcodes.return_object.RunnerGenerator
        try {
            RunnerGenerator rg = (RunnerGenerator) Class.forName(
                    "com.android.jack.opcodes.return_object.jm.T_return_object_15").newInstance();
            Runner r = rg.run();
            assertFalse(r instanceof Runner);
            assertFalse(Runner.class.isAssignableFrom(r.getClass()));
            // only upon invocation of a concrete method,
            // a java.lang.IncompatibleClassChangeError is thrown
            r.doit();
//            fail("expected a verification exception");
        } catch (Throwable t) {
//            DxUtil.checkVerifyException(t);
        }
    }

}
