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

package com.android.jack.opcodes.sub_double;

import com.android.jack.opcodes.sub_double.jm.T_sub_double_1;
import com.android.jack.opcodes.sub_double.jm.T_sub_double_3;
import com.android.jack.opcodes.sub_double.jm.T_sub_double_4;
import com.android.jack.test.DxTestCase;


public class Test_sub_double extends DxTestCase {

    /**
     * @title  Arguments = 2.7d, 3.14d
     */
    public void testN1() {
        T_sub_double_1 t = new T_sub_double_1();
        assertEquals(-0.43999999999999995d, t.run(2.7d, 3.14d));
    }

    /**
     * @title  Arguments = 0, -3.14d
     */
    public void testN2() {
        T_sub_double_1 t = new T_sub_double_1();
        assertEquals(3.14d, t.run(0, -3.14d));
    }

    /**
     * @title Arguments = -3.14d, -2.7d
     */
    public void testN3() {
        T_sub_double_1 t = new T_sub_double_1();
        assertEquals(-0.43999999999999995d, t.run(-3.14d, -2.7d));
    }

    /**
     * @title Arguments = -3.14d, -2.7d
     */
    public void testN4() {
        T_sub_double_3 t = new T_sub_double_3();
        assertEquals(-0.4400001049041746d, t.run(-3.14f, -2.7d));
    }

    /**
     * @title Arguments = -3.14d, -2.7d
     */
    public void testN5() {
        T_sub_double_4 t = new T_sub_double_4();
        assertEquals(-311.3, t.run(-314l, -2.7d));
    }

    /**
     * @title  Arguments = Double.MAX_VALUE, Double.NaN
     */
    public void testB1() {
        T_sub_double_1 t = new T_sub_double_1();
        assertEquals(Double.NaN, t.run(Double.MAX_VALUE, Double.NaN));
    }

    /**
     * @title  Arguments = Double.POSITIVE_INFINITY,
     * Double.NEGATIVE_INFINITY
     */
    public void testB2() {
        T_sub_double_1 t = new T_sub_double_1();
        assertEquals(Double.POSITIVE_INFINITY, t.run(Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY));
    }

    /**
     * @title  Arguments = Double.POSITIVE_INFINITY,
     * Double.POSITIVE_INFINITY
     */
    public void testB3() {
        T_sub_double_1 t = new T_sub_double_1();
        assertEquals(Double.NaN, t.run(Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY));
    }

    /**
     * @title  Arguments = Double.POSITIVE_INFINITY, -2.7d
     */
    public void testB4() {
        T_sub_double_1 t = new T_sub_double_1();
        assertEquals(Double.POSITIVE_INFINITY, t.run(Double.POSITIVE_INFINITY,
                -2.7d));
    }

    /**
     * @title  Arguments = +0, -0d
     */
    public void testB5() {
        T_sub_double_1 t = new T_sub_double_1();
        assertEquals(+0d, t.run(+0d, -0d));
    }

    /**
     * @title  Arguments = -0d, -0d
     */
    public void testB6() {
        T_sub_double_1 t = new T_sub_double_1();
        assertEquals(0d, t.run(-0d, -0d));
    }

    /**
     * @title  Arguments = +0d, +0d
     */
    public void testB7() {
        T_sub_double_1 t = new T_sub_double_1();
        assertEquals(+0d, t.run(+0d, +0d));
    }

    /**
     * @title  Arguments = 2.7d, 2.7d
     */
    public void testB8() {
        T_sub_double_1 t = new T_sub_double_1();
        assertEquals(0d, t.run(2.7d, 2.7d));
    }

    /**
     * @title  Arguments = Double.MAX_VALUE, Double.MAX_VALUE
     */
    public void testB9() {
        T_sub_double_1 t = new T_sub_double_1();
        assertEquals(0d, t.run(Double.MAX_VALUE, Double.MAX_VALUE));
    }

    /**
     * @title  Arguments = Double.MIN_VALUE, 4.9E-324
     */
    public void testB10() {
        T_sub_double_1 t = new T_sub_double_1();
        assertEquals(0d, t.run(Double.MIN_VALUE, 4.9E-324));
    }
}
