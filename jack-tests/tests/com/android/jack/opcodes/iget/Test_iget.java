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

package com.android.jack.opcodes.iget;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.iget.jm.T_iget_1;
import com.android.jack.opcodes.iget.jm.T_iget_11;
import com.android.jack.opcodes.iget.jm.T_iget_15;
import com.android.jack.opcodes.iget.jm.T_iget_2;
import com.android.jack.opcodes.iget.jm.T_iget_4;
import com.android.jack.opcodes.iget.jm.T_iget_5;
import com.android.jack.opcodes.iget.jm.T_iget_6;


public class Test_iget extends DxTestCase {
    private int TestStubField = 123;
    protected int TestStubFieldP = 0;

    private int privateInt = 456;

    /**
     * @title  type - int
     */
    public void testN1() {
        T_iget_1 t = new T_iget_1();
        assertEquals(35, t.run());
    }

    /**
     * @title  type - double
     */
    public void testN2() {
        T_iget_2 t = new T_iget_2();
        assertEquals(123d, t.run());
    }

    /**
     * @title  access protected field from subclass
     */
    public void testN3() {
     // @uses com.android.jack.opcodes.iget.jm.T_iget_1
        T_iget_11 t = new T_iget_11();
        assertEquals(10, t.run());
    }

    public void testN4() {
         T_iget_4 t = new T_iget_4();
         assertEquals(5, t.run());
    }

    public void testN5() {
      T_iget_15 t = new T_iget_15();
      assertNull(t.run());
    }

    /**
     * @title  attempt to access static field
     */
    public void testN6() {
      T_iget_5 t = new T_iget_5();
      assertEquals(5, t.run());
    }

    /**
     * @title  attempt to access of non-accessible private field
     */
    public void testN7() {
       T_iget_6 t = new T_iget_6();
       int res = t.run();
       assertEquals(-99, t.run());
    }

}
