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

package com.android.jack.opcodes.aput_object.ref;

import com.android.jack.opcodes.aput_object.jm.T_aput_object_1;
import com.android.jack.opcodes.aput_object.jm.T_aput_object_10;
import com.android.jack.test.DxTestCase;


public class Test_aput_object extends DxTestCase {

    /**
     * @title Normal test. Trying different indexes
     */
    public void testN1() {
        T_aput_object_1 t = new T_aput_object_1();
        String[] arr = new String[2];
        t.run(arr, 0, "hello");
        assertEquals("hello", arr[0]);
    }

    /**
     * @title Normal test. Trying different indexes
     */
    public void testN2() {
        T_aput_object_1 t = new T_aput_object_1();
        String[] value = {"world", null, ""};
        String[] arr = new String[2];
        for (int i = 0; i < value.length; i++) {
            t.run(arr, 1, value[i]);
            assertEquals(value[i], arr[1]);
        }
    }

    /**
     * @title Normal test. Trying different indexes
     */
    public void testN3() {
        T_aput_object_10 t = new T_aput_object_10();
        Integer[] arr = new Integer[2];
        Integer value = new Integer(12345);
        t.run(arr, 0, value);
        assertEquals(value, arr[0]);
    }

    /**
     * @title ArrayIndexOutOfBoundsException expected
     */
    public void testE1() {
        T_aput_object_1 t = new T_aput_object_1();
        String[] arr = new String[2];
        try {
            t.run(arr, arr.length, "abc");
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title expected ArrayIndexOutOfBoundsException
     */
    public void testE2() {
        T_aput_object_1 t = new T_aput_object_1();
        String[] arr = new String[2];
        try {
            t.run(arr, -1, "abc");
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }

    /**
     * @title expected NullPointerException
     */
    public void testE3() {
        T_aput_object_1 t = new T_aput_object_1();
        String[] arr = null;
        try {
            t.run(arr, 0, "abc");
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
            // expected
        }
    }
}
