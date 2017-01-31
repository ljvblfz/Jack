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

package com.android.jack.java8.lambda.test044.lib1;

import com.android.jack.java8.lambda.test044.lib2.A1;
import com.android.jack.java8.lambda.test044.lib2.A3;
import com.android.jack.java8.lambda.test044.lib2.I1;

public class B1 {

  public I1 m1() {
    return new A1().m();
  }

  public I1 m3() {
    return new A3().m();
  }

  public I2 m4() {
    return new A4().m();
  }

  public I2 m5() {
    return new A5().m();
  }
}
