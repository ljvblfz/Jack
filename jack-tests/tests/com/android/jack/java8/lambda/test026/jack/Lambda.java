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

package com.android.jack.java8.lambda.test026.jack;


public class Lambda {

  private int call(I1 lambda) {
    return lambda.m();
  }

  private String call(I2 lambda) {
    return lambda.m();
  }

  public String useI1() {
    return Integer.toString(call(() -> 1));
  }

  public String useI2() {
    return call(() -> "2");
  }

}