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

package com.android.jack.optimizations.notsimplifier.test002.jack;

public class NotSimplifier {

  public static boolean smallerOrEqualTo0(double n) {
    return !(n > 0);
  }

  public static boolean smallerOrEqualTo0(float n) {
    return !(n > 0);
  }

  public static boolean smallerOrEqualTo0(long n) {
    return !(n > 0);
  }

  public static boolean smallerOrEqualTo0(int n) {
    return !(n > 0);
  }

  public static boolean smallerOrEqualTo0(byte n) {
    return !(n > 0);
  }

  public static boolean smallerOrEqualTo0(char n) {
    return !(n > 0);
  }
}
