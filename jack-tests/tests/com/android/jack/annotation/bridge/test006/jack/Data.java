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

package com.android.jack.annotation.bridge.test006.jack;

public class Data {

  static class A {
    public Object unchanged(Object arg) {
      return this;
    }
  }

  static class B extends A {
    @Anno
    @Override
    public B unchanged(@Anno Object arg) {
      return this;
    }
  }

  public static class C extends B {
    @Anno
    @Override
    public C unchanged(@Anno Object arg) {
      return this;
    }
  }
}
