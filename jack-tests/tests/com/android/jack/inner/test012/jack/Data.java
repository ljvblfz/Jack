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

package com.android.jack.inner.test012.jack;

import com.android.jack.inner.test012.lib.Data2;

public class Data extends Data2 {
  B b = new B();

  public int m() {
    return f;
  }

  public B getB() {
    return b;
  }

  public class B extends Data2 {
    A a = new A();

    public A getA() {
      return a;
    }

    public class A {
      public int setf(int value, byte increment) {
        return b.setf(value, increment);
      }
    }
  }
}