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

package com.android.jack.opcodes.sub_float;

import com.android.jack.opcodes.sub_float.jm.T_sub_float_1;
import com.android.jack.opcodes.sub_float.jm.T_sub_float_3;
import com.android.jack.opcodes.sub_float.jm.T_sub_float_4;
import com.android.jack.test.DxTestCase;


public class Test_sub_float extends DxTestCase {

    /**
     * @title  Arguments = 2.7f, 3.14f
     */
    public void testN1() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(-0.44000006f, t.run(2.7f, 3.14f));
    }

    /**
     * @title  Arguments = 0, -3.14f
     */
    public void testN2() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(3.14f, t.run(0, -3.14f));
    }

    /**
     * @title Arguments = -3.14f, -2.7f
     */
    public void testN3() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(-0.44000006f, t.run(-3.14f, -2.7f));
    }

    /**
     * @title Arguments = -3.14f, -2.7f
     */
    public void testN4() {
        T_sub_float_3 t = new T_sub_float_3();
        assertEquals(-0.44000006f, t.run(-3.14f, -2.7f));
    }

    /**
     * @title Arguments = -3.14f, -2.7f
     */
    public void testN5() {
        T_sub_float_4 t = new T_sub_float_4();
        assertEquals(-311.3f, t.run(-314l, -2.7f));
    }

    /**
     * @title  Arguments = Float.MAX_VALUE, Float.NaN
     */
    public void testB1() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(Float.NaN, t.run(Float.MAX_VALUE, Float.NaN));
    }

    /**
     * @title  Arguments = Float.POSITIVE_INFINITY,
     * Float.NEGATIVE_INFINITY
     */
    public void testB2() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY));
    }

    /**
     * @title  Arguments = Float.POSITIVE_INFINITY,
     * Float.POSITIVE_INFINITY
     */
    public void testB3() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(Float.NaN, t.run(Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY));
    }

    /**
     * @title  Arguments = Float.POSITIVE_INFINITY, -2.7f
     */
    public void testB4() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Float.POSITIVE_INFINITY,
                -2.7f));
    }

    /**
     * @title  Arguments = +0, -0f
     */
    public void testB5() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(+0f, t.run(+0f, -0f));
    }

    /**
     * @title  Arguments = -0f, -0f
     */
    public void testB6() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(0f, t.run(-0f, -0f));
    }

    /**
     * @title  Arguments = +0f, +0f
     */
    public void testB7() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(+0f, t.run(+0f, +0f));
    }

    /**
     * @title  Arguments = 2.7f, 2.7f
     */
    public void testB8() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(0f, t.run(2.7f, 2.7f));
    }

    /**
     * @title  Arguments = Float.MAX_VALUE, Float.MAX_VALUE
     */
    public void testB9() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(0f, t.run(Float.MAX_VALUE, Float.MAX_VALUE));
    }

    /**
     * @title  Arguments = Float.MIN_VALUE, -1.4E-45f
     */
    public void testB10() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(0f, t.run(Float.MIN_VALUE, 1.4E-45f));
    }

    /**
     * @title  Arguments = Float.MAX_VALUE, -Float.MAX_VALUE
     */
    public void testB11() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Float.MAX_VALUE,
                -3.402823E+38F));
    }
}
