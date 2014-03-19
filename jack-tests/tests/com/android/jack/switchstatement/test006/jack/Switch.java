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

package com.android.jack.switchstatement.test006.jack;

public class Switch {

  public static class A {
    public int value;
  }

  public static class B {
    public static int value;
    static {
      int i = 0;
      if (i != 1) {
        throw new NullPointerException();
      }
    }
  }

  public static int switch001(A a) {
    switch (a.value) {
      default:
        return 3;
    }
  }

  public static int switch002() {
    switch (B.value) {
      default:
        return 3;
    }
  }

  @SuppressWarnings({"boxing", "cast"})
  public static int switch003(A a) {
    switch ((Integer) a.value) {
      default:
        return 3;
    }
  }

  public static int switch004(int i) {
    int [] array = {1, 2};
    switch (array[i++]) {
      default:
        return i;
    }
  }

}
