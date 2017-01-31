/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack.java8.lambda.test044.jack;

import com.android.jack.java8.lambda.test044.lib1.I2;
import com.android.jack.java8.lambda.test044.lib1.subpkg.A2;

class B2 extends A2 {
  class C {
    int test() {
      I2 i = () -> field;
      return i.getCst() + field;
    }
  }
}