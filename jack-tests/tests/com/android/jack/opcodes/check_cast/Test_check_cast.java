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

package com.android.jack.opcodes.check_cast;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.check_cast.jm.T_check_cast_1;


public class Test_check_cast extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_check_cast_1 t = new T_check_cast_1();
        String s = "";
        assertEquals(s, t.run(s));
    }

    /**
     * @title check null value
     */
    public void testN2() {
        T_check_cast_1 t = new T_check_cast_1();
        assertNull(t.run(null));
    }

    /**
     * @title expected ClassCastException
     */
    public void testE1() {
        T_check_cast_1 t = new T_check_cast_1();
        try {
            t.run(this);
            fail("expected ClassCastException");
        } catch (ClassCastException iae) {
            // expected
        }
    }
}
