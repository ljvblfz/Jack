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

package com.android.jack.opcodes.div_double.ref;

import com.android.jack.opcodes.div_double.jm.T_div_double_1;
import com.android.jack.opcodes.div_double.jm.T_div_double_3;
import com.android.jack.opcodes.div_double.jm.T_div_double_4;
import com.android.jack.test.DxTestCase;


public class Test_div_double extends DxTestCase {

    /**
     * @title Arguments = 2.7d, 3.14d
     */
    public void testN1() {

        T_div_double_1 t = new T_div_double_1();
        assertEquals(0.8598726114649682d, t.run(2.7d, 3.14d));
    }

    /**
     * @title  Dividend = 0
     */
    public void testN2() {
        T_div_double_1 t = new T_div_double_1();
        assertEquals(0d, t.run(0, 3.14d));
    }

    /**
     * @title  Dividend is negative
     */
    public void testN3() {

        T_div_double_1 t = new T_div_double_1();
        assertEquals(-1.162962962962963d, t.run(-3.14d, 2.7d));
    }

    /**
     * @title  Dividend is negative
     */
    public void testN4() {

        T_div_double_1 t = new T_div_double_1();
        assertEquals(-1.162962962962963d, t.run(-3.14d, 2.7d));
    }

    /**
     * @title  Dividend is negative
     */
    public void testN5() {

        T_div_double_3 t = new T_div_double_3();
        assertEquals(-1.162963001816361d, t.run(-3.14f, 2.7d));
    }

    /**
     * @title  Dividend is negative
     */
    public void testN6() {

        T_div_double_4 t = new T_div_double_4();
        assertEquals(-116.29629629629629d, t.run(-314l, 2.7d));
    }

    /**
     * @title  Arguments = Double.POSITIVE_INFINITY,
     * Double.NEGATIVE_INFINITY
     */
    public void testB2() {
        T_div_double_1 t = new T_div_double_1();
        assertEquals(Double.NaN, t.run(Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY));
    }

    /**
     * @title  Arguments = Double.POSITIVE_INFINITY, -2.7d
     */
    public void testB3() {
        T_div_double_1 t = new T_div_double_1();
        assertEquals(Double.NEGATIVE_INFINITY, t.run(Double.POSITIVE_INFINITY,
                -2.7d));
    }

    /**
     * @title  Arguments = -2.7d, Double.NEGATIVE_INFINITY
     */
    public void testB4() {
        T_div_double_1 t = new T_div_double_1();
        assertEquals(0d, t.run(-2.7d, Double.NEGATIVE_INFINITY));
    }

    /**
     * @title  Arguments = 0, 0
     */
    public void testB5() {
        T_div_double_1 t = new T_div_double_1();
        assertEquals(Double.NaN, t.run(0, 0));
    }

    /**
     * @title  Arguments = 0, -2.7
     */
    public void testB6() {
        T_div_double_1 t = new T_div_double_1();
        assertEquals(-0d, t.run(0, -2.7d));
    }

    /**
     * @title  Arguments = -2.7, 0
     */
    public void testB7() {
        T_div_double_1 t = new T_div_double_1();
        assertEquals(Double.NEGATIVE_INFINITY, t.run(-2.7d, 0));
    }

    /**
     * @title  Arguments = 1, Double.MAX_VALUE
     */
    public void testB8() {
        T_div_double_1 t = new T_div_double_1();
        assertEquals(Double.POSITIVE_INFINITY, t.run(1, Double.MIN_VALUE));
    }

    /**
     * @title  Arguments = Double.MAX_VALUE, -1E-9f
     */
    public void testB9() {
        T_div_double_1 t = new T_div_double_1();
        assertEquals(Double.NEGATIVE_INFINITY, t.run(Double.MAX_VALUE, -1E-9f));
    }
}
