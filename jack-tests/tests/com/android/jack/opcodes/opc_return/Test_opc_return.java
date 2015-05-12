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

package com.android.jack.opcodes.opc_return;

import com.android.jack.opcodes.opc_return.jm.T_opc_return_11;
import com.android.jack.opcodes.opc_return.jm.T_opc_return_12;
import com.android.jack.opcodes.opc_return.jm.T_opc_return_13;
import com.android.jack.opcodes.opc_return.jm.T_opc_return_14;
import com.android.jack.opcodes.opc_return.jm.T_opc_return_15;
import com.android.jack.opcodes.opc_return.jm.T_opc_return_1;
import com.android.jack.opcodes.opc_return.jm.T_opc_return_10;
import com.android.jack.opcodes.opc_return.jm.T_opc_return_2;
import com.android.jack.opcodes.opc_return.jm.T_opc_return_3;
import com.android.jack.opcodes.opc_return.jm.T_opc_return_4;
import com.android.jack.opcodes.opc_return.jm.T_opc_return_5;
import com.android.jack.opcodes.opc_return.jm.T_opc_return_6;
import com.android.jack.opcodes.opc_return.jm.T_opc_return_7;
import com.android.jack.opcodes.opc_return.jm.T_opc_return_8;
import com.android.jack.test.DxTestCase;


public class Test_opc_return extends DxTestCase {

    /**
     * @title  check that frames are discarded and reinstananted correctly
     */
    public void testN1() {
        T_opc_return_1 t = new T_opc_return_1();
        assertEquals(123456, t.run());
    }

    /**
     * @title  check that monitor is released by return
     */
    public void testN2() {
        assertTrue(T_opc_return_2.execute());
    }


    public void testN3() {
        T_opc_return_3 t = new T_opc_return_3();
        assertTrue(t.run());
    }

    /**
     * @title  Lock structural rule 1 is violated
     */
    public void testE2() {
        T_opc_return_4 t = new T_opc_return_4();
        try {
            t.run();
            System.out.print("dvmvfe:");
            //fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
            // expected
        }
    }

    /**
     * @title  simple
     */
    public void testN1Float() {
        T_opc_return_5 t = new T_opc_return_5();
        assertEquals(123456f, t.run());
    }

    /**
     * @title  check that frames are discarded and reinstananted correctly
     */
    public void testN2Float() {
        T_opc_return_7 t = new T_opc_return_7();
        assertEquals(123456f, t.run());
    }

    /**
     * @title  check that monitor is released by return
     */
    public void testN3Float() {
        assertTrue(T_opc_return_8.execute());
    }

    /**
     * @title  check that monitor is released by return
     */
    public void testN4Float() {
        T_opc_return_6 t = new T_opc_return_6();
        assertEquals(0f, t.run());
    }

//    /**
//     * @title  Method is synchronized but thread is not monitor owner
//     */
//    public void testE1() {
//        T_opc_return_9 t = new T_opc_return_9();
//        try {
//            assertTrue(t.run());
//            fail("expected IllegalMonitorStateException");
//        } catch (IllegalMonitorStateException imse) {
//            // expected
//        }
//    }

    /**
     * @title  Lock structural rule 1 is violated
     */
    public void testE2Float() {
        T_opc_return_10 t = new T_opc_return_10();
        try {
            assertEquals(1f, t.run());
            System.out.print("dvmvfe:");
            //fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
            // expected
        }
    }

    /**
     * @title  simple
     */
    public void testN1Int() {
        T_opc_return_11 t = new T_opc_return_11();
        assertEquals(123456, t.run());
    }

    /**
     * @title  check that frames are discarded and reinstananted correctly
     */
    public void testN2Int() {
        T_opc_return_12 t = new T_opc_return_12();
        assertEquals(123456, t.run());
    }

    /**
     * @title  check that monitor is released by return-int
     */
    public void testN3Int() {
        assertTrue(T_opc_return_13.execute());
    }

    /**
     * @title  Method is synchronized but thread is not monitor owner
     */
    public void testE1Int() {
        T_opc_return_14 t = new T_opc_return_14();
        assertTrue(t.run());
    }

    /**
     * @title  Lock structural rule 1 is violated
     */
    public void testE2Int() {
        T_opc_return_15 t = new T_opc_return_15();
        try {
            assertEquals(1, t.run());
            System.out.print("dvmvfe:");
            //fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
            // expected
        }
    }

}
