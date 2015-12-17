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

package com.android.jack.java8.lambda.test029.jack;

interface I {
  int m(int a, int b);
}

public class Lambda {

  public I[] t = {(a, b) -> a + b, (a, b) -> a * b};

  public int add(int a, int b) {
    return t[0].m(a, b);
  }

  public int mul(int a,int b) {
    return t[1].m(a, b);
  }

  public int add2(int a, int b) {
    return ((I) ((x, y) -> t[0].m(x, y))).m(a, b);
  }

  public int mul2(int a,int b) {
    return ((I) ((x, y) -> t[1].m(x, y))).m(a, b);
  }
}
