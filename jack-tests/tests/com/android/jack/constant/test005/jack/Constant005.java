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

package com.android.jack.constant.test005.jack;

/**
 * Test checking that there is only seven constant defined into the generated code.
 */
public class Constant005 {

  public int sum(int a, int b) {
    return a + b;
  }

  public int test() {
    int a = 1;
    int b = 2;
    int c = 3;
    int d = 4;
    int e = 5;
    int f = 6;
    int g = 7;
    int sum1 = sum (a,a);
    int sum2 = sum (b,b);
    int sum3 = sum (c,c);
    int sum4 = sum (d,d);
    int sum5 = sum (e,e);
    int sum6 = sum (f,f);
    int sum7 = sum (g,g);
    return sum1 + sum2 + sum3 + sum4 + sum5 + sum6 + sum7;
  }
}