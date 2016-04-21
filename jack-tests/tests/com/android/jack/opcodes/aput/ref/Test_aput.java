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

package com.android.jack.opcodes.aput.ref;

import com.android.jack.opcodes.aput.jm.T_aput_1;
import com.android.jack.opcodes.aput.jm.T_aput_2;
import com.android.jack.opcodes.aput.jm.T_aput_3;
import com.android.jack.opcodes.aput.jm.T_aput_4;
import com.android.jack.opcodes.aput.jm.T_aput_5;
import com.android.jack.opcodes.aput.jm.T_aput_6;
import com.android.jack.test.DxTestCase;


public class Test_aput extends DxTestCase {

    /**
     * @title normal test. Trying different indexes
     */
    public void testN1() {
        T_aput_1 t = new T_aput_1();
        float[] arr = new float[2];
        t.run(arr, 1, 2.7f);
        assertEquals(2.7f, arr[1]);
    }

    /**
     * @title normal test. Trying different indexes
     */
    public void testN2() {
        T_aput_1 t = new T_aput_1();
        float[] arr = new float[2];
        t.run(arr, 0, 2.7f);
        assertEquals(2.7f, arr[0]);
    }

    /**
     * @title normal test. Trying different indexes
     */
    public void testN3() {
        T_aput_2 t = new T_aput_2();
        float[] arr = new float[2];
        t.run(arr, 0, 2.7f);
        assertEquals(2.7f, arr[0]);
    }

    /**
     * @title normal test. Trying different indexes
     */
    public void testN4() {
        T_aput_3 t = new T_aput_3();
        float[] arr = new float[2];
        t.run(arr, 0, 2.7f);
        assertEquals(2.7f, arr[0]);
    }

    /**
     * @title expected ArrayIndexOutOfBoundsException
     */
    public void testE1() {
        T_aput_1 t = new T_aput_1();
        float[] arr = new float[2];
        try {
            t.run(arr, 2, 2.7f);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title expected NullPointerException
     */
    public void testE2() {
        T_aput_1 t = new T_aput_1();
        try {
            t.run(null, 2, 2.7f);
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
            // expected
        }
    }

    /**
     * @title expected ArrayIndexOutOfBoundsException
     */
    public void testE3() {
        T_aput_1 t = new T_aput_1();
        float[] arr = new float[2];
        try {
            t.run(arr, -1, 2.7f);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title Normal test/. Trying different indexes
     */
    public void testN1Int() {
        T_aput_4 t = new T_aput_4();
        int[] arr = new int[2];
        t.run(arr, 1, 100000000);
        assertEquals(100000000, arr[1]);
    }

    /**
     * @title Normal test/. Trying different indexes
     */
    public void testN2Int() {
        T_aput_4 t = new T_aput_4();
        int[] arr = new int[2];
        t.run(arr, 0, 100000000);
        assertEquals(100000000, arr[0]);
    }

    /**
     * @title Normal test/. Trying different indexes
     */
    public void testN3Int() {
        T_aput_5 t = new T_aput_5();
        int[] arr = new int[2];
        t.run(arr, 0, 100000000);
        assertEquals(100000000, arr[0]);
    }

    /**
     * @title Normal test/. Trying different indexes
     */
    public void testN4Int() {
        T_aput_6 t = new T_aput_6();
        int[] arr = new int[2];
        t.run(arr, 0, 100000000);
        assertEquals(100000000, arr[0]);
    }

    /**
     * @title expected ArrayIndexOutOfBoundsException
     */
    public void testE1Int() {
        T_aput_4 t = new T_aput_4();
        int[] arr = new int[2];
        try {
            t.run(arr, 2, 100000000);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title expected NullPointerException
     */
    public void testE2Int() {
        T_aput_4 t = new T_aput_4();
        try {
            t.run(null, 2, 100000000);
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
            // expected
        }
    }

    /**
     * @title expected ArrayIndexOutOfBoundsException
     */
    public void testE3Int() {
        T_aput_4 t = new T_aput_4();
        int[] arr = new int[2];
        try {
            t.run(arr, -1, 100000000);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }
}
