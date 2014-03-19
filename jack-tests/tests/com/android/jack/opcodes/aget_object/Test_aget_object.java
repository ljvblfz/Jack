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

package com.android.jack.opcodes.aget_object;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.aget_object.jm.T_aget_object_1;
import com.android.jack.opcodes.aget_object.jm.T_aget_object_4;
import com.android.jack.opcodes.aget_object.jm.T_aget_object_5;
import com.android.jack.opcodes.aget_object.jm.T_aget_object_6;
import com.android.jack.opcodes.aget_object.jm.T_aget_object_7;
import com.android.jack.opcodes.aget_object.jm.T_aget_object_8;
import com.android.jack.opcodes.aget_object.jm.T_aget_object_9;


public class Test_aget_object extends DxTestCase {

    /**
     * @title Normal test. Trying different indexes
     */
    public void testN1() {
        T_aget_object_1 t = new T_aget_object_1();
        String[] arr = new String[] {"a", "b"};
        assertEquals("a", t.run(arr, 0));
    }

    /**
     * @title Normal test. Trying different indexes
     */
    public void testN2() {
        T_aget_object_1 t = new T_aget_object_1();
        String[] arr = new String[] {"a", "b"};
        assertEquals("b", t.run(arr, 1));
    }

    /**
     * @title ArrayIndexOutOfBoundsException expected
     */
    public void testE1() {
        T_aget_object_1 t = new T_aget_object_1();
        String[] arr = new String[] {"a", "b"};
        try {
            t.run(arr, 2);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            // expected
        }
    }

    /**
     * @title Negative index. ArrayIndexOutOfBoundsException expected
     */
    public void testE2() {
        T_aget_object_1 t = new T_aget_object_1();
        String[] arr = new String[] {"a", "b"};
        try {
            t.run(arr, -1);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            // expected
        }
    }

    /**
     * @title NullPointerException expected
     */
    public void testE3() {
        T_aget_object_1 t = new T_aget_object_1();
        String[] arr = null;
        try {
            t.run(arr, 0);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - array, double
     */
    public void testN3() {
      T_aget_object_4 t = new T_aget_object_4();
      String[] arr = new String[] {"a", "b"};
      assertEquals("a", t.run(arr, 0));
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - array, long
     */
    public void testN4() {
      T_aget_object_5 t = new T_aget_object_5();
      String[] arr = new String[] {"a", "b"};
      assertEquals("a", t.run(arr, 0));
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - Object, int
     */
    public void testN5() {
      T_aget_object_6 t = new T_aget_object_6();
      String[] arr = new String[] {"a", "b"};
      assertEquals("a", t.run(null, arr, 0));
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - float[], int
     */
    public void testN6() {
      T_aget_object_7 t = new T_aget_object_7();
      String[] arr = new String[] {"a", "b"};
      assertEquals("a", t.run(null, arr, 0));
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - long[], int
     */
    public void testN7() {
      T_aget_object_8 t = new T_aget_object_8();
      String[] arr = new String[] {"a", "b"};
      assertEquals("a", t.run(null, arr, 0));
    }

    /**
     * @constraint 4.8.2.1
     * @title types of arguments - array, reference
     */
    public void testN8() {
      T_aget_object_9 t = new T_aget_object_9();
      String[] arr = new String[] {"a", "b"};
      assertEquals("a", t.run(arr, 0));
    }

}
