/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.assertion.test001.jack;

public class Data {

  public static void throwAssert() {
    assert false;
  }

  public static void throwAssert(boolean b) {
    assert b : "boolean is false";
  }

  public static void throwAssertObject(Object o) {
    assert false : o;
  }

  public static void throwAssert(String message, int a) {
    assert a == 0 : message;
  }
}