/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.unary.test001.jack;

/**
 * Unary test.
 */
public class Unary {

  public static int neg(int a) {
    return -a;
  }

  public static int bitnot(int a) {
    return ~a;
  }

  public static long neg(long a) {
    return -a;
  }

  public static long bitnot(long a) {
    return ~a;
  }

  public static float neg(float a) {
    return -a;
  }

  public static double neg(double a) {
    return -a;
  }

  public static float posNegZero() {
    return (+(-0.0f));
  }

  public static float negPosZero() {
    return (-(+0.0f));
  }

  public static float negPosNegZero() {
    return (-(+(-0.0f)));
  }

  public static float posNegPosZero() {
    return (+(-(+0.0f)));
  }

  public static float posNegUnknown(float x) {
    return (+(-(x)));
  }
}
