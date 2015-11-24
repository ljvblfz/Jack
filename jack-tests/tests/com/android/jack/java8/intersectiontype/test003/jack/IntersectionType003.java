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

package com.android.jack.java8.intersectiontype.test003.jack;

interface I {
  int getValue();
}

class C1 {
}

class C2 extends C1 implements I {
  @Override
  public int getValue() {
      return 1;
  }
}

class C3 extends C2 {

}

public class IntersectionType003 {

  public static int test() {
      return (((C1 & I) new C3()).getValue());
  }
}
