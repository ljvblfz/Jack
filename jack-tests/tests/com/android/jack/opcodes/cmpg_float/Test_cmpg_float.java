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

package com.android.jack.opcodes.cmpg_float;

import com.android.jack.DxTestCase;
import com.android.jack.opcodes.cmpg_float.jm.T_cmpg_float_1;
import com.android.jack.opcodes.cmpg_float.jm.T_cmpg_float_2;


public class Test_cmpg_float extends DxTestCase {

    /**
     * @title  Arguments = 3.14f, 2.7f
     */
    public void testN1() {
        T_cmpg_float_1 t = new T_cmpg_float_1();
        assertEquals(1, t.run(3.14f, 2.7f));
    }

    /**
     * @title  Arguments = -3.14f, 2.7f
     */
    public void testN2() {
        T_cmpg_float_1 t = new T_cmpg_float_1();
        assertEquals(-1, t.run(-3.14f, 2.7f));
    }

    /**
     * @title  Arguments = 3.14, 3.14
     */
    public void testN3() {
        T_cmpg_float_1 t = new T_cmpg_float_1();
        assertEquals(0, t.run(3.14f, 3.14f));
    }

    /**
     * @title  Arguments = 3.14, 3.14
     */
    public void testN4() {
        T_cmpg_float_2 t = new T_cmpg_float_2();
        assertEquals(false, t.run(3.14f, 3.14f));
    }

    /**
     * @title  Arguments = +0, -0
     */
    public void testB2() {
        T_cmpg_float_1 t = new T_cmpg_float_1();
        assertEquals(0, t.run(+0f, -0f));
    }

    /**
     * @title  Arguments = Float.NEGATIVE_INFINITY, Float.MIN_VALUE
     */
    public void testB3() {
        T_cmpg_float_1 t = new T_cmpg_float_1();
        assertEquals(-1, t.run(Float.NEGATIVE_INFINITY, Float.MIN_VALUE));
    }

    /**
     * @title  Arguments = Float.POSITIVE_INFINITY, Float.MAX_VALUE
     */
    public void testB4() {
        T_cmpg_float_1 t = new T_cmpg_float_1();
        assertEquals(1, t.run(Float.POSITIVE_INFINITY, Float.MAX_VALUE));
    }

    /**
     * @title  Arguments = Float.POSITIVE_INFINITY,
     * Float.NEGATIVE_INFINITY
     */
    public void testB5() {
        T_cmpg_float_1 t = new T_cmpg_float_1();
        assertEquals(1, t.run(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY));
    }
}
