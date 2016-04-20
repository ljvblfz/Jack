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

package com.android.jack.opcodes.sput.ref;

import com.android.jack.opcodes.sput.jm.T_sput_1;
import com.android.jack.opcodes.sput.jm.T_sput_13;
import com.android.jack.opcodes.sput.jm.T_sput_14;
import com.android.jack.opcodes.sput.jm.T_sput_16;
import com.android.jack.opcodes.sput.jm.T_sput_18;
import com.android.jack.opcodes.sput.jm.T_sput_2;
import com.android.jack.opcodes.sput.jm.T_sput_6;
import com.android.jack.test.DxTestCase;


public class Test_sput extends DxTestCase {

    /**
     * @title  type - int
     */
    public void testN1() {
        T_sput_1 t = new T_sput_1();
        assertEquals(0, T_sput_1.st_i1);
        t.run();
        assertEquals(1000000, T_sput_1.st_i1);
    }

    /**
     * @title  type - double
     */
    public void testN2() {
        T_sput_2 t = new T_sput_2();
        assertEquals(0d, T_sput_2.st_d1);
        t.run();
        assertEquals(1000000d, T_sput_2.st_d1);
    }

    /**
     * @title  modification of protected field from subclass
     */
    public void testN4() {
        // @uses com.android.jack.opcodes.sput.jm.T_sput_1
        T_sput_14 t = new T_sput_14();
        assertEquals(0, T_sput_14.getProtectedField());
        t.run();
        assertEquals(1000000, T_sput_14.getProtectedField());
    }

    /**
     * @title  assignment compatible references
     */
    public void testN5() {
        T_sput_16 t = new T_sput_16();
        assertNull(T_sput_16.o);
        t.run();
        assertEquals("", (String) T_sput_16.o);
    }

    /**
     * @title  modification of protected field from subclass
     */
    public void testN6() {
        T_sput_6 t = new T_sput_6();
        assertEquals(null, T_sput_6.s);
        try {
          t.run();
          fail ("ClassCastException expected");
        }
        catch (ClassCastException e) {
          // expected
        }
        assertEquals(null, T_sput_6.s);
    }

    /**
     * @title  modification of protected field from subclass
     */
    public void testN7() {
        T_sput_18 t = new T_sput_18();
        assertEquals(0, T_sput_18.v);
        t.run();
        assertEquals(3, T_sput_18.v);
    }

    /**
     * @title  initialization of referenced class throws exception
     */
    public void testE6() {
        // @uses com.android.jack.opcodes.sput.jm.StubInitError
        T_sput_13 t = new T_sput_13();
        try {
            t.run();
            fail("expected Error");
        } catch (Error e) {
            // expected
        }
    }

}
