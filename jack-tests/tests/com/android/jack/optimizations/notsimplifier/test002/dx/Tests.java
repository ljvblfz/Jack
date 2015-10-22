/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.optimizations.notsimplifier.test002.dx;

import com.android.jack.optimizations.notsimplifier.test002.jack.NotWithComparisonOperator;
import com.android.jack.optimizations.notsimplifier.test002.jack.NotWithEQ;
import com.android.jack.optimizations.notsimplifier.test002.jack.NotWithGT;
import com.android.jack.optimizations.notsimplifier.test002.jack.NotWithGTE;
import com.android.jack.optimizations.notsimplifier.test002.jack.NotWithLT;
import com.android.jack.optimizations.notsimplifier.test002.jack.NotWithLTE;
import com.android.jack.optimizations.notsimplifier.test002.jack.NotWithNEQ;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests about '!' simplifier optimization using comparison with bounds values or not of types.
 */
public class Tests {

  @Test
  public void test() {
    NotWithComparisonOperator[] operators = new NotWithComparisonOperator[] {
        new NotWithGT(),
        new NotWithGTE(),
        new NotWithEQ(),
        new NotWithNEQ(),
        new NotWithLT(),
        new NotWithLTE()};

    double[] doubleValues = new double[] {
        Double.NaN,
        0.0d,
        -5.0d,
        -Double.MIN_VALUE,
        Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY,
        Double.MAX_VALUE,
        5.0d};

    float[] floatValues = new float[] {
        Float.NaN,
        0.0f,
        -5.0f,
        -Float.MIN_VALUE,
        Float.NEGATIVE_INFINITY,
        Float.POSITIVE_INFINITY,
        Float.MAX_VALUE,
        5.0f};

    int[] intValues = new int[] {
        0,
        -5,
        -Integer.MIN_VALUE,
        Integer.MAX_VALUE,
        1};

    long[] longValues = new long[] {
        0l,
        -5l,
        -Long.MIN_VALUE,
        Long.MAX_VALUE,
        1l};

    byte[] byteValues = new byte[] {
        (byte) 0,
        (byte) -5,
        Byte.MIN_VALUE,
        Byte.MAX_VALUE,
        (byte) 1};

    short[] shortValues = new short[] {
        (short) 0,
        (short) -5,
        Short.MIN_VALUE,
        Short.MAX_VALUE,
        (short) 1};

    char[] charValues = new char[] {
        (char) 0,
        Character.MIN_VALUE,
        Character.MAX_VALUE};

    boolean[][] resultForDoubleAndFloat =
        new boolean[][] {
        {true, true, true, true, true, false, false, false}, // GT
        {true, false, true, true, true, false, false, false}, // GTE
        {true, false, true, true, true, true, true, true}, // EQ
        {false, true, false, false, false, false, false, false}, // NEQ
        {true, true, false, false, false, true, true, true}, // LT
        {true, false, false, false, false, true, true, true}, // LTE
        };

    boolean[][] resultForLongIntShortByte =
        new boolean[][] {
        {true, true, true, false, false}, // GT
        {false, true, true, false, false}, // GTE
        {false, true, true, true, true}, // EQ
        {true, false, false, false, false}, // NEQ
        {true, false, false, true, true}, // LT
        {false, false, false, true, true}, // LTE
        };

    boolean[][] resultForChar =
        new boolean[][] {
        {true, true, false}, // GT
        {false,false, false}, // GTE
        {false, false, true}, // EQ
        {true, true, false}, // NEQ
        {true, true, true}, // LT
        {false, false, true}, // LTE
        };

    for (int operatorIndex = 0; operatorIndex < operators.length; operatorIndex++) {
      NotWithComparisonOperator operator = operators[operatorIndex];

      for (int valueIndex = 0; valueIndex < doubleValues.length; valueIndex++) {
        Assert.assertEquals(resultForDoubleAndFloat[operatorIndex][valueIndex],
            operator.testWithDouble(doubleValues[valueIndex]));
      }

      for (int valueIndex = 0; valueIndex < floatValues.length; valueIndex++) {
        Assert.assertEquals(resultForDoubleAndFloat[operatorIndex][valueIndex],
            operator.testWithFloat(floatValues[valueIndex]));
      }

      for (int valueIndex = 0; valueIndex < intValues.length; valueIndex++) {
        Assert.assertEquals(resultForLongIntShortByte[operatorIndex][valueIndex],
            operator.testWithInt(intValues[valueIndex]));
      }

      for (int valueIndex = 0; valueIndex < longValues.length; valueIndex++) {
        Assert.assertEquals(resultForLongIntShortByte[operatorIndex][valueIndex],
            operator.testWithLong(longValues[valueIndex]));
      }

      for (int valueIndex = 0; valueIndex < byteValues.length; valueIndex++) {
        Assert.assertEquals(resultForLongIntShortByte[operatorIndex][valueIndex],
            operator.testWithByte(byteValues[valueIndex]));
      }

      for (int valueIndex = 0; valueIndex < shortValues.length; valueIndex++) {
        Assert.assertEquals(resultForLongIntShortByte[operatorIndex][valueIndex],
            operator.testWithShort(shortValues[valueIndex]));
      }

      for (int valueIndex = 0; valueIndex < charValues.length; valueIndex++) {
        Assert.assertEquals(resultForChar[operatorIndex][valueIndex],
            operator.testWithChar(charValues[valueIndex]));
      }
    }
  }
}
