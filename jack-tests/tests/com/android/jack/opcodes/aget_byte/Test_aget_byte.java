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

package com.android.jack.opcodes.aget_byte;

import com.android.jack.opcodes.aget_byte.jm.T_aget_byte_1;
import com.android.jack.test.DxTestCase;


public class Test_aget_byte extends DxTestCase {

    /**
     * @title normal test. Trying different indexes
     */
    public void testN1() {
        T_aget_byte_1 t = new T_aget_byte_1();
        byte[] arr = new byte[2];
        arr[1] = 100;
        assertEquals(100, t.run(arr, 1));
    }

    /**
     * @title normal test. Trying different indexes
     */
    public void testN2() {
        T_aget_byte_1 t = new T_aget_byte_1();
        byte[] arr = new byte[2];
        arr[0] = 100;
        assertEquals(100, t.run(arr, 0));
    }

    /**
     * @title expected ArrayIndexOutOfBoundsException
     */
    public void testE1() {
        T_aget_byte_1 t = new T_aget_byte_1();
        byte[] arr = new byte[2];
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
        T_aget_byte_1 t = new T_aget_byte_1();
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
        T_aget_byte_1 t = new T_aget_byte_1();
        byte[] arr = new byte[2];
        try {
            t.run(arr, -1);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
            // expected
        }
    }
}
