/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.optimizations.ifwithconstantsimplifier.test001;

public class NestedAssign {

  /**
   * Returns true if and only if the {@link StringProvider} provides equal strings.
   */
  public static boolean ifWithNestedAssigns(StringProvider sp) {
    String str1;
    String str2;
    if ((str1 = sp.getString1()) == null ||
        (str2 = sp.getString2()) == null ||
        (str1 != str2 && !str1.equals(str2))) {
      return false;
    }
    return true;
  }

  /**
   * Returns true if and only if all the {@link StringProvider}s provide equal strings.
   */
  public static boolean ifWithNestedAssignsInLoop(StringProvider... sps) {
    for (StringProvider sp : sps) {
      String str1;
      String str2;
      if ((str1 = sp.getString1()) == null ||
          (str2 = sp.getString2()) == null ||
          (str1 != str2 && !str1.equals(str2))) {
        return false;
      }
    }
    return true;
  }
}
