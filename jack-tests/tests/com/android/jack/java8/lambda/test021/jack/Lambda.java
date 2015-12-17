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

package com.android.jack.java8.lambda.test021.jack;


/**
 * Test closure.
 */
public class Lambda {

  private int field;

  private static int staticField;

  public void setField(int field) {
    this.field = field;
  }

  public static void setStaticField(int staticField) {
    Lambda.staticField = staticField;
  }

  public I testClosure() {
    int local = field * 2;
    int local2 = staticField * 2;
    return () -> field + staticField + local + local2;
  }

}