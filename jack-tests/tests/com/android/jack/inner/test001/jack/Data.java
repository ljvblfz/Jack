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

package com.android.jack.inner.test001.jack;

public class Data {
  private int i = 5;
  private int j = 7;
  private boolean b = false;
  private boolean c = true;
  private boolean d = false;

  public A getA() {
    return new A();
  }

  public int getI() {
    return i;
  }

  public int getJ() {
    return j;
  }

  public boolean getB() {
    return b;
  }

  public boolean getC() {
    return c;
  }

  public boolean getD() {
    return d;
  }

 public class A {
   public void m() {
     i++;
   }

   public void n() {
     j = i + 6;
     b = i < j;
     c = j <= i;
     d = !c;
   }
 }
}
