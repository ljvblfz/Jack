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

package com.android.jack.annotation.test009.jack;

interface I {
  Class<?> get();
}

public class Annotation009 {

  public Annotation009() {
  }

  public Annotation009(Object o) {
  }

  public static Class<?> getLocalClass1() {
    class C {
      class D {}
    }
    return C.D.class;
  }

  public static Class<?> getLocalClass2() {
    return localClass2.get();
  }

  public static Class<?> getLocalClass3() {
    return new Annotation009().localClass3.get();
  }

  public static Class<?> getLocalClass4() {
    return localClass2.getClass();
  }

  public static Class<?> getLocalClass5() {
    return new Annotation009().localClass3.getClass();
  }

  private static I localClass2 = new I() {
    class D {}
    @Override
    public java.lang.Class<?> get() {
      return D.class;
    }
  };

  private I localClass3 = new I() {
    class D {}
    @Override
    public java.lang.Class<?> get() {
      return D.class;
    }
  };
}
