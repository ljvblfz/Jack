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

package com.android.jack.java8.lambda.test027.jack;

interface I {
  String getField(A a);
}

public class Lambda {

  private A field = new A("A3");

  public String test1() {
    I i = (x) -> x.f;
    return i.getField(new A("A1"));
  }

  public String test2() {
    A a = new A("A2");
    I i = (x) -> a.f;
    return i.getField(new A("A"));
  }

  public String test3() {
    I i = (x) -> x.f;
    return i.getField(field);
  }
}
