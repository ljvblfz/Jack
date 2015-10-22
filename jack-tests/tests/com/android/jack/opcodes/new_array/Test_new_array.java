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

package com.android.jack.opcodes.new_array;

import com.android.jack.opcodes.new_array.jm.T_new_array_1;
import com.android.jack.opcodes.new_array.jm.T_new_array_2;
import com.android.jack.opcodes.new_array.jm.T_new_array_4;
import com.android.jack.opcodes.new_array.jm.T_new_array_5;
import com.android.jack.opcodes.new_array.jm.T_new_array_6;
import com.android.jack.test.DxTestCase;


public class Test_new_array extends DxTestCase {

    /**
     * @title Test for Object
     */
    public void testN1() {
        T_new_array_1 t = new T_new_array_1();

        Object[] arr = t.run(10);
        assertNotNull(arr);
        assertEquals(10, arr.length);
        for (int i = 0; i < 10; i++)
            assertNull(arr[i]);
    }

    /**
     * @title Test for String
     */
    public void testN2() {
        T_new_array_1 t = new T_new_array_1();

        String[] arr2 = t.run2(5);
        assertNotNull(arr2);
        assertEquals(5, arr2.length);
        for (int i = 0; i < 5; i++)
            assertNull(arr2[i]);
    }

    /**
     * @title Test for Integer
     */
    public void testN3() {
        T_new_array_1 t = new T_new_array_1();

        Integer[] arr3 = t.run3(15);
        assertNotNull(arr3);
        assertEquals(15, arr3.length);
        for (int i = 0; i < 15; i++)
            assertNull(arr3[i]);
    }

    /**
     * @title if count is zero, no subsequent dimensions allocated
     */
    public void testE1() {
        T_new_array_1 t = new T_new_array_1();
        Object[] res = t.run(0);
        try {
            Object s = res[0];
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException ae) {
            // expected
        }
    }

    /**
     * @title expected NegativeArraySizeException
     */
    public void testE2() {
        T_new_array_1 t = new T_new_array_1();
        try {
            t.run(-2);
            fail("expected NegativeArraySizeException");
        } catch (NegativeArraySizeException nase) {
            // expected
        }
    }

    /**
     * @title normal test
     */
    public void testN1MultiDim() {
        T_new_array_2 t = new T_new_array_2();
        String[][][] res = t.run(2, 5, 4);

        assertEquals(2, res.length);

        // check default initialization
        for (int i = 0; i < 2; i++) {
            assertEquals(5, res[i].length);

            for (int j = 0; j < 5; j++) {
                assertEquals(4, res[i][j].length);

                for (int k = 0; j < 4; j++) {
                    assertNull(res[i][j][k]);
                }
            }
        }
    }

    /**
     * @title  if count is zero, no subsequent dimensions allocated
     */
    public void testN2MultiDim() {
        T_new_array_2 t = new T_new_array_2();
        String[][][] res = t.run(2, 0, 4);

        try {
            String s = res[2][0][0];
            fail("expected ArrayIndexOutOfBoundsException");
            fail("dummy for s "+s);
        } catch (ArrayIndexOutOfBoundsException ae) {
            // expected
        }
    }

    public void testN3MultiDim() {
        T_new_array_4 t = new T_new_array_4();
        String[][][] res = t.run(2, 1, 4);

        if (res.length != 2) fail("incorrect multiarray length");
        if (res[0].length != 1) fail("incorrect array length");

        try {
            int i = res[0][0].length;
            fail("expected NullPointerException");
            fail("dummy for i "+i);
        } catch (NullPointerException npe) {
            // expected
        }
    }

    /**
     * @title expected NegativeArraySizeException
     */
    public void testE1MultiDim() {
        T_new_array_2 t = new T_new_array_2();
        try {
            t.run(2, -5, 3);
            fail("expected NegativeArraySizeException");
        } catch (NegativeArraySizeException nase) {
            // expected
        }
    }

    /**
     * @title  Array of ints
     */
    public void testN1Simple() {
        T_new_array_5 t = new T_new_array_5();
        int[] r = t.run(10);
        int l = r.length;
        assertEquals(10, l);

        // check default initialization
        for (int i = 0; i < l; i++) {
            assertEquals(0, r[i]);
        }

    }

    /**
     * @title  Array of floats
     */
    public void testN2Simple() {
        T_new_array_6 t = new T_new_array_6();
        float[] r = t.run(10);
        int l = r.length;
        assertEquals(10, l);

        // check default initialization
        for (int i = 0; i < l; i++) {
            assertEquals(0f, r[i]);
        }
    }

    /**
     * @title expected NegativeArraySizeException
     */
    public void testE1Simple() {
        T_new_array_6 t = new T_new_array_6();
        try {
            t.run(-1);
            fail("expected NegativeArraySizeException");
        } catch (NegativeArraySizeException nase) {
            // expected
        }
    }

    /**
     * @title  Array size = 0
     */
    public void testB1Simple() {
        T_new_array_5 t = new T_new_array_5();
        int[] r = t.run(0);
        assertNotNull(r);
        assertEquals(0, r.length);
    }

}
