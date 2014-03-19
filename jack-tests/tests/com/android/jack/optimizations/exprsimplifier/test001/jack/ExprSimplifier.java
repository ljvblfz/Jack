/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.optimizations.exprsimplifier.test001.jack;

public class ExprSimplifier {

  public static int test001(boolean param) {
    // Check runtime when param == true is simplify to param
    if (param == true) {
      return 1;
    } else {
      return 2;
    }
  }

  public static int test002(boolean param) {
    // Check runtime when param == false is simplify to !param
    if (param == false) {
      return 1;
    } else {
      return 2;
    }
  }
}
