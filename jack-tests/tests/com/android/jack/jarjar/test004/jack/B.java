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

package com.android.jack.jarjar.test004.jack;

public class B {

  private com.android.jack.jarjar.test004.lib.A a;

  private int f;

  private B b;

  public B() {

  }

  private B(com.android.jack.jarjar.test004.lib.A a) {
    this.a = a;
  }

  public boolean m() {
    f = com.android.jack.jarjar.test004.lib.A.CST;
    a = new com.android.jack.jarjar.test004.lib.A();
    b = new B(new com.android.jack.jarjar.test004.lib.A());

    return new com.android.jack.jarjar.test004.lib.A().f() && f == 0 && b != null && a != null;
  }

  public String getA() {
    return com.android.jack.jarjar.test004.lib.A.class.getName();
  }

}

