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

package com.android.jack.optimizations.modifiers.test002.jack;

import java.util.AbstractList;
import java.util.ArrayList;

class PrePreBase {
  int a = 0;
  private static Object bar() {
    return null;
  }
  public <T extends AbstractList<String>, V> T g(String t) {
    return null;
  }
}

class PreBase extends PrePreBase {
  private Object foo() {
    return null;
  }
}

class Base extends PreBase {
  public Object foo() {
    return null;
  }
  public Object bar() {
    return null;
  }
  public ArrayList g(String t) {
    return null;
  }
}

class D1 extends Base implements Inter {
  public Base foo() {
    return null;
  }
  public Object bar() {
    return null;
  }
}

class D2 extends D1 {
  public D1 foo() {
    return null;
  }
  public Object bar() {
    return null;
  }
}

interface Inter {
  Base foo();
}

public abstract class A {
  private static void touch(Class clazz) {
  }
  public static void touch() {
    touch(Base.class);
    touch(D1.class);
    touch(D2.class);
    touch(Inter.class);
    touch(PreBase.class);
    touch(PrePreBase.class);
  }
}