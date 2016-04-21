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

package com.android.jack.opcodes.aget.ref;

import com.android.jack.opcodes.aget.jm.T_aget_1;
import com.android.jack.opcodes.aget.jm.T_aget_2;
import com.android.jack.opcodes.aget.jm.T_aget_3;
import com.android.jack.opcodes.aget.jm.T_aget_4;
import com.android.jack.opcodes.aget.jm.T_aget_5;
import com.android.jack.opcodes.aget.jm.T_aget_6;
import com.android.jack.test.DxTestCase;


public class Test_aget extends DxTestCase {

    /**
     * @title normal test. Trying different indexes
     */
    public void testN1() {
        T_aget_1 t = new T_aget_1();
        float[] arr = new float[2];
        arr[1] = 3.1415f;
        assertEquals(3.1415f, t.run(arr, 1));
    }

    /**
     * @title normal test. Trying different indexes
     */
    public void testN2() {
        T_aget_1 t = new T_aget_1();
        float[] arr = new float[2];
        arr[0] = 3.1415f;
        assertEquals(3.1415f, t.run(arr, 0));
    }

    /**
     * @title normal test. Trying different indexes
     */
    public void testN3() {
        T_aget_2 t = new T_aget_2();
        float[] arr = new float[2];
        arr[0] = 3.1415f;
        assertEquals(3.1415f, t.run(arr, 0));
    }

    /**
     * @title normal test. Trying different indexes
     */
    public void testN4() {
        T_aget_3 t = new T_aget_3();
        float[] arr = new float[2];
        arr[0] = 3.1415f;
        assertEquals(3.1415f, t.run(arr, 0));
    }

    /**
     * @title expected ArrayIndexOutOfBoundsException
     */
    public void testE1() {
        T_aget_1 t = new T_aget_1();
        float[] arr = new float[2];
        try {
            t.run(arr, 2);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title expected NullPointerException
     */
    public void testE2() {
        T_aget_1 t = new T_aget_1();
        try {
            t.run(null, 2);
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
            // expected
        }
    }

    /**
     * @title expected ArrayIndexOutOfBoundsException
     */
    public void testE3() {
        T_aget_1 t = new T_aget_1();
        float[] arr = new float[2];
        try {
            t.run(arr, -1);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title Normal test/. Trying different indexes
     */
    public void testN1Int() {
        T_aget_4 t = new T_aget_4();
        int[] arr = new int[2];
        arr[1] = 100000000;
        assertEquals(100000000, t.run(arr, 1));
    }

    /**
     * @title Normal test/. Trying different indexes
     */
    public void testN2Int() {
        T_aget_4 t = new T_aget_4();
        int[] arr = new int[2];
        arr[0] = 100000000;
        assertEquals(100000000, t.run(arr, 0));
    }

    /**
     * @title Normal test/. Trying different indexes
     */
    public void testN3Int() {
        T_aget_5 t = new T_aget_5();
        int[] arr = new int[2];
        arr[0] = 100000000;
        assertEquals(100000000, t.run(arr, 0));
    }

    /**
     * @title Normal test/. Trying different indexes
     */
    public void testN4Int() {
        T_aget_6 t = new T_aget_6();
        int[] arr = new int[2];
        arr[0] = 100000000;
        assertEquals(100000000, t.run(arr, 0));
    }

    /**
     * @title expected ArrayIndexOutOfBoundsException
     */
    public void testE1Int() {
        T_aget_4 t = new T_aget_4();
        int[] arr = new int[2];
        try {
            t.run(arr, 2);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title expected NullPointerException
     */
    public void testE2Int() {
        T_aget_4 t = new T_aget_4();
        try {
            t.run(null, 2);
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
            // expected
        }
    }

    /**
     * @title expected ArrayIndexOutOfBoundsException
     */
    public void testE3Int() {
        T_aget_4 t = new T_aget_4();
        int[] arr = new int[2];
        try {
            t.run(arr, -1);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }
}
