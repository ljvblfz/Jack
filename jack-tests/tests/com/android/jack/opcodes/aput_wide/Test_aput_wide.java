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

package com.android.jack.opcodes.aput_wide;

import com.android.jack.opcodes.aput_wide.jm.T_aput_wide_1;
import com.android.jack.opcodes.aput_wide.jm.T_aput_wide_2;
import com.android.jack.opcodes.aput_wide.jm.T_aput_wide_3;
import com.android.jack.opcodes.aput_wide.jm.T_aput_wide_4;
import com.android.jack.test.DxTestCase;


public class Test_aput_wide extends DxTestCase {

    /**
     * @title normal test. Trying different indexes
     */
    public void testN1() {
        T_aput_wide_1 t = new T_aput_wide_1();
        double[] arr = new double[2];
        t.run(arr, 1, 2.7d);
        assertEquals(2.7d, arr[1]);
    }

    /**
     * @title normal test. Trying different indexes
     */
    public void testN2() {
        T_aput_wide_1 t = new T_aput_wide_1();
        double[] arr = new double[2];
        t.run(arr, 0, 2.7d);
        assertEquals(2.7d, arr[0]);
    }

    /**
     * @title expected ArrayIndexOutOfBoundsException
     */
    public void testE1() {
        T_aput_wide_1 t = new T_aput_wide_1();
        double[] arr = new double[2];
        try {
            t.run(arr, 2, 2.7d);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title expected NullPointerException
     */
    public void testE2() {
        T_aput_wide_1 t = new T_aput_wide_1();
        try {
            t.run(null, 2, 2.7d);
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
            // expected
        }
    }

    /**
     * @title expected ArrayIndexOutOfBoundsException
     */
    public void testE3() {
        T_aput_wide_1 t = new T_aput_wide_1();
        double[] arr = new double[2];
        try {
            t.run(arr, -1, 2.7d);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title normal test. trying different indexes
     */
    public void testN1Long() {
        T_aput_wide_2 t = new T_aput_wide_2();
        long[] arr = new long[2];
        t.run(arr, 1, 100000000000l);
        assertEquals(100000000000l, arr[1]);
    }

    /**
     * @title normal test. trying different indexes
     */
    public void testN2Long() {
        T_aput_wide_2 t = new T_aput_wide_2();
        long[] arr = new long[2];
        t.run(arr, 0, 100000000000l);
        assertEquals(100000000000l, arr[0]);
    }

    /**
     * @title normal test. trying different indexes
     */
    public void testN3Long() {
        T_aput_wide_3 t = new T_aput_wide_3();
        long[] arr = new long[2];
        t.run(arr, 0, 100000000000l);
        assertEquals(100000000000l, arr[0]);
    }

    /**
     * @title normal test. trying different indexes
     */
    public void testN4Long() {
        T_aput_wide_4 t = new T_aput_wide_4();
        long[] arr = new long[2];
        t.run(arr, 0, 1000);
        assertEquals(1000, arr[0]);
    }
    /**
     * @title  Exception - ArrayIndexOutOfBoundsException
     */
    public void testE1Long() {
        T_aput_wide_2 t = new T_aput_wide_2();
        long[] arr = new long[2];
        try {
            t.run(arr, 2, 100000000000l);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title  Exception - NullPointerException
     */
    public void testE2Long() {
        T_aput_wide_2 t = new T_aput_wide_2();
        try {
            t.run(null, 1, 100000000000l);
            fail("expected NullPointerException");
        } catch (NullPointerException np) {
            // expected
        }
    }

    /**
     * @title  Exception - ArrayIndexOutOfBoundsException
     */
    public void testE3Long() {
        T_aput_wide_2 t = new T_aput_wide_2();
        long[] arr = new long[2];
        try {
            t.run(arr, -1, 100000000000l);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }
}
