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

package com.android.jack.inner.test004.jack;

public class Data {
  private int i = 5;
  public static int si = 8;
  public A a = new A();

  private void m() {
    i++;
  }

  private static void staticm() {
    si++;
  }

  public int getI() {
    return i;
  }

 public class A {
   public B b = new B();

   public class B {
     public void n() {
       i = 7;
       m();
       staticm();
     }
   }
 }
}
