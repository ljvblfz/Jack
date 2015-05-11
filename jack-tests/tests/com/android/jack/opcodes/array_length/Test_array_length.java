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

package com.android.jack.opcodes.array_length;

import com.android.jack.opcodes.array_length.jm.T_array_length_1;
import com.android.jack.opcodes.array_length.jm.T_array_length_2;
import com.android.jack.test.DxTestCase;


public class Test_array_length extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_array_length_1 t = new T_array_length_1();
        String[] a = new String[5];
        assertEquals(5, t.run(a));
    }

    /**
     * @title NullPointerException expected
     */
    public void testNPE1() {
        T_array_length_1 t = new T_array_length_1();
        try {
            t.run(null);
            fail("NPE expected");
        } catch (NullPointerException npe) {
            // expected
        }
    }

    /**
     * @title NullPointerException expected
     */
    public void testNPE2() {
        T_array_length_2 t = new T_array_length_2();
        try {
            t.run();
            fail("NPE expected");
        } catch (NullPointerException npe) {
            // expected
        }
    }
}
