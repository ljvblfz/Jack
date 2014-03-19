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

package com.android.jack.opcodes.return_wide;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.return_wide.jm.T_return_wide_11;
import com.android.jack.opcodes.return_wide.jm.T_return_wide_12;
import com.android.jack.opcodes.return_wide.jm.T_return_wide_13;
import com.android.jack.opcodes.return_wide.jm.T_return_wide_14;
import com.android.jack.opcodes.return_wide.jm.T_return_wide_1;
import com.android.jack.opcodes.return_wide.jm.T_return_wide_10;
import com.android.jack.opcodes.return_wide.jm.T_return_wide_2;
import com.android.jack.opcodes.return_wide.jm.T_return_wide_6;
import com.android.jack.opcodes.return_wide.jm.T_return_wide_7;
import com.android.jack.opcodes.return_wide.jm.T_return_wide_9;


public class Test_return_wide extends DxTestCase {

    /**
     * @title  simple
     */
    public void testN1() {
        T_return_wide_1 t = new T_return_wide_1();
        assertEquals(123456d, t.run());
    }

    /**
     * @title  check that frames are discarded and reinstananted correctly
     */
    public void testN2() {
        T_return_wide_6 t = new T_return_wide_6();
        assertEquals(123456d, t.run());
    }

    /**
     * @title  check that monitor is released by return_wide
     */
    public void testN3() {
        assertTrue(T_return_wide_7.execute());
    }

    /**
     * @title  simple
     */
    public void testN4() {
        T_return_wide_2 t = new T_return_wide_2();
        assertEquals(0d, t.run());
    }

    /**
     * @title  simple
     */
    public void testN5() {
        T_return_wide_10 t = new T_return_wide_10();
        assertEquals(1d, t.run());
    }


//    /**
//     * @title  Method is synchronized but thread is not monitor owner
//     */
//    public void testE1() {
//        T_return_wide_8 t = new T_return_wide_8();
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
    public void testE2() {
        T_return_wide_9 t = new T_return_wide_9();
        try {
            assertEquals(1d, t.run());
            System.out.print("dvmvfe:");
            //fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
            // expected
        }
    }

    /**
     * @title  simple
     */
    public void testN1Long() {
        T_return_wide_11 t = new T_return_wide_11();
        assertEquals(12345612345l, t.run());
    }

    /**
     * @title  check that frames are discarded and reinstananted correctly
     */
    public void testN2Long() {
        T_return_wide_12 t = new T_return_wide_12();
        assertEquals(12345612345l, t.run());
    }

    /**
     * @title  check that monitor is released by return_wide
     */
    public void testN3Long() {
        assertTrue(T_return_wide_13.execute());
    }


    /**
     * @title  Method is synchronized but thread is not monitor owner
     */
    public void testE1Long() {
        T_return_wide_14 t = new T_return_wide_14();
        assertTrue(t.run());
    }
}
