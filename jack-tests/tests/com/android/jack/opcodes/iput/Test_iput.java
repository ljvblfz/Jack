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

package com.android.jack.opcodes.iput;

import com.android.jack.opcodes.iput.jm.T_iput_1;
import com.android.jack.opcodes.iput.jm.T_iput_12;
import com.android.jack.opcodes.iput.jm.T_iput_14;
import com.android.jack.opcodes.iput.jm.T_iput_16;
import com.android.jack.opcodes.iput.jm.T_iput_18;
import com.android.jack.opcodes.iput.jm.T_iput_2;
import com.android.jack.test.DxTestCase;


public class Test_iput extends DxTestCase {

    /**
     * @title  type - int
     */
    public void testN1() {
        T_iput_1 t = new T_iput_1();
        assertEquals(0, t.st_i1);
        t.run();
        assertEquals(1000000, t.st_i1);
    }

    /**
     * @title  type - double
     */
    public void testN2() {
        T_iput_2 t = new T_iput_2();
        assertEquals(0d, t.st_d1);
        t.run();
        assertEquals(1000000d, t.st_d1);
    }

    /**
     * @title  modification of final field
     */
    public void testN3() {
        T_iput_12 t = new T_iput_12();
        assertEquals(0, t.st_i1);
        t.run();
        assertEquals(1000000, t.st_i1);
    }

    /**
     * @title  modification of protected field from subclass
     */
    public void testN4() {
        // @uses com.android.jack.opcodes.iput.jm.T_iput_1
        T_iput_14 t = new T_iput_14();
        assertEquals(0, t.getProtectedField());
        t.run();
        assertEquals(1000000, t.getProtectedField());
    }

    /**
     * @title  assignment compatible object references
     */
    public void testN5() {
        // @uses com.android.jack.opcodes.iput.jm.TChild
        // @uses com.android.jack.opcodes.iput.jm.TSuper
        T_iput_18 t = new T_iput_18();
        assertEquals(0, t.run().compareTo("xyz"));
    }

    /**
     * @title  assignment compatible values
     */
    public void testN6() {
        T_iput_16 t = new T_iput_16();
        assertNull(t.o);
        t.run();
        assertEquals("", (String) t.o);
    }
}
