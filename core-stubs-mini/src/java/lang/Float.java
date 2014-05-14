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

package java.lang;

public final class Float extends java.lang.Number implements java.lang.Comparable<java.lang.Float> {
  public Float(float value) {
    throw new RuntimeException("Stub!");
  }

  public Float(double value) {
    throw new RuntimeException("Stub!");
  }

  public Float(java.lang.String string) throws java.lang.NumberFormatException {
    throw new RuntimeException("Stub!");
  }

  public int compareTo(java.lang.Float object) {
    throw new RuntimeException("Stub!");
  }

  public byte byteValue() {
    throw new RuntimeException("Stub!");
  }

  public double doubleValue() {
    throw new RuntimeException("Stub!");
  }

  public boolean equals(java.lang.Object object) {
    throw new RuntimeException("Stub!");
  }

  public static native int floatToIntBits(float value);

  public static native int floatToRawIntBits(float value);

  public float floatValue() {
    throw new RuntimeException("Stub!");
  }

  public int hashCode() {
    throw new RuntimeException("Stub!");
  }

  public static native float intBitsToFloat(int bits);

  public int intValue() {
    throw new RuntimeException("Stub!");
  }

  public boolean isInfinite() {
    throw new RuntimeException("Stub!");
  }

  public static boolean isInfinite(float f) {
    throw new RuntimeException("Stub!");
  }

  public boolean isNaN() {
    throw new RuntimeException("Stub!");
  }

  public static boolean isNaN(float f) {
    throw new RuntimeException("Stub!");
  }

  public long longValue() {
    throw new RuntimeException("Stub!");
  }

  public static float parseFloat(java.lang.String string) throws java.lang.NumberFormatException {
    throw new RuntimeException("Stub!");
  }

  public short shortValue() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String toString() {
    throw new RuntimeException("Stub!");
  }

  public static java.lang.String toString(float f) {
    throw new RuntimeException("Stub!");
  }

  public static java.lang.Float valueOf(java.lang.String string)
      throws java.lang.NumberFormatException {
    throw new RuntimeException("Stub!");
  }

  public static int compare(float float1, float float2) {
    throw new RuntimeException("Stub!");
  }

  public static java.lang.Float valueOf(float f) {
    throw new RuntimeException("Stub!");
  }

  public static java.lang.String toHexString(float f) {
    throw new RuntimeException("Stub!");
  }

  public static final float MAX_VALUE = 3.4028235E38f;
  public static final float MIN_VALUE = 1.4E-45f;
  public static final float NaN = (0.0f / 0.0f);
  public static final float POSITIVE_INFINITY = (1.0f / 0.0f);
  public static final float NEGATIVE_INFINITY = (-1.0f / 0.0f);
  public static final float MIN_NORMAL = 1.17549435E-38f;
  public static final int MAX_EXPONENT = 127;
  public static final int MIN_EXPONENT = -126;
  @java.lang.SuppressWarnings(value = {"unchecked"})
  public static final java.lang.Class<java.lang.Float> TYPE;
  public static final int SIZE = 32;
  static {
    TYPE = null;
  }
}
