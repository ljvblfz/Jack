/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.cast.implicit006.jack;

public class Implicit006 {

  private static final byte byteValue = 127;

  private static final short shortValue = 315;

  private static final char charValue = 't';

  private static final int intValue = 1650000;

  private static final long longValue = 0x123456789L;

  private static final float floatValue = 31.55F;

  private static final double doubleValue = 31.55D;

  public byte[] getByteArray() {
    return new byte[]{byteValue};
  }
  public char[] getCharArray() {
    return new char[]{byteValue, shortValue, charValue};
  }
  public short[] getShortArray() {
    return new short[]{byteValue, shortValue, charValue};
  }
  public int[] getIntArray() {
    return new int[]{byteValue, shortValue, charValue, intValue};
  }
  public long[] getLongArray() {
    return new long[]{byteValue, shortValue, charValue, intValue, longValue};
  }
  public float[] getFloatArray() {
    return new float[]{byteValue, shortValue, charValue, intValue, longValue, floatValue};
  }
  public double[] getDoubleArray() {
    return new double[]{byteValue, shortValue, charValue, intValue, longValue, floatValue,
        doubleValue};
  }

}
