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

package com.android.jack.opcodes.sparse_switch.ref;

import com.android.jack.opcodes.sparse_switch.jm.T_sparse_switch_1;
import com.android.jack.test.DxTestCase;


public class Test_sparse_switch extends DxTestCase {

    /**
     * @title normal test
     */
    public void testN1() {
        T_sparse_switch_1 t = new T_sparse_switch_1();
        assertEquals(2, t.run(-1));

        assertEquals(-1, t.run(9));
        assertEquals(20, t.run(10));
        assertEquals(-1, t.run(11));

        assertEquals(-1, t.run(14));
        assertEquals(20, t.run(15));
        assertEquals(-1, t.run(16));
    }

    /**
     * @title check Integer.MAX_VALUE
     */
    public void testB1() {
        T_sparse_switch_1 t = new T_sparse_switch_1();
        assertEquals(-1, t.run(Integer.MAX_VALUE));
    }

    /**
     * @title check Integer.MIN_VALUE
     */
    public void testB2() {
        T_sparse_switch_1 t = new T_sparse_switch_1();
        assertEquals(-1, t.run(Integer.MIN_VALUE));
    }

}
