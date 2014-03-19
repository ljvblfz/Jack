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

package com.android.jack.cast.useless001.jack;



public class Data {

  class A {
    private int f = 1;

    public int getInt() {
      return 1;
    }

    private int getIntPrivate() {
      return 1;
    }

    public int getInt3(A a) {
      return 1;
    }
  }

  class B extends A {
    private int f = 2;

    @Override
    public int getInt() {
      return 2;
    }

    private int getIntPrivate() {
      return 2;
    }

    public int getInt3(B b) {
      return 2;
    }
  }

  public int getValue1() {
    B b = new B();
    return (((A)b).getInt());
  }

  public int getValue2() {
    B b = new B();
    return (((A)b).getIntPrivate() + b.getIntPrivate());
  }

  public int getValue3() {
    B b = new B();
    return (((A)b).f + b.f);
  }

  public int getValue4() {
    B b = new B();
    return (b.getInt3(new B()) + ((A)b).getInt3(new B()));
  }

  public int getValue5(Object o) {
    return ((Object[]) o).length;
  }
}
