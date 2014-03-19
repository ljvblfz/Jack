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

package com.android.jack.bridge.test006.jack;

import java.lang.reflect.Method;

public class Data {

  static class A {
    public Object unchanged() {
      return this;
    }
  }

  static class B extends A {
    @Override
    public B unchanged() {
      return this;
    }
  }

  public static class C extends B {
    @Override
    public C unchanged() {
      return this;
    }
  }

  public Method getMethod1() {
    Method[] methods = C.class.getMethods();
    for (Method method : methods) {
        if (method.getName().equals("unchanged") && method.getReturnType().equals(Object.class)) {
            return method;
        }
    }
    return null;
  }
}
