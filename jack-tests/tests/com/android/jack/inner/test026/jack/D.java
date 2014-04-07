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

package com.android.jack.inner.test026.jack;

import com.android.jack.inner.test026.jack.pkg.C;

public class D extends C {
  public static int Dvalue = new D().f();
  public int Evalue = getE();
  public int Fvalue = getF();
  public static int Gvalue = new G().f();
  public static int G2value = new G().getE();
  public static int G3value = new G().getF();

  int f() {
    return m();
  }

  class E {
    int f() {
      return D.super.m();
    }
  }

  class F {
    int f() {
      return m();
    }
  }

  public int getE() {
    return new E().f();
  }

  public int getF() {
    return new F().f();
  }
}

class G extends D {
  @Override
  protected int m() {
    return 2;
  }

  @Override
  int f() {
    return m();
  }
}
