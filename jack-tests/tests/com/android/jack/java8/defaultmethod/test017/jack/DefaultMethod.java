/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.java8.defaultmethod.test017.jack;

interface I1 {
  default Number getNumber() {
      return new Integer(1);
  }
}

interface I2 extends I1 {
  @Override
  default Double getNumber() {
      return new Double(2.3d);
  }
}

class C implements I2 {
}

/**
 * Check that bridges are generated.
 */
public class DefaultMethod {
  private C c = new C();

  public Double test1() {
    return c.getNumber();
  }

  public Number test2() {
    return ((I1) c).getNumber();
  }
}
