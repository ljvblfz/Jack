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

package com.android.jack.jarjar.test001.jack;

public class A {

  public String getClassNameOfB() {
    return new B().getClassName();
  }

  public String starGetClassNameOfB() {
    return new com.android.jack.jarjar.test001.jack.star.B().getClassName();
  }

  public String starGetClassNameOfC() {
    return new com.android.jack.jarjar.test001.jack.star.C().getClassName();
  }

  public String starGetClassNameOfUntouchedB() {
    return new com.android.jack.jarjar.test001.jack.star.untouched.B().getClassName();
  }

  public String starGetClassNameOfUntouchedC() {
    return new com.android.jack.jarjar.test001.jack.star.untouched.C().getClassName();
  }

  public String dStarGetClassNameOfB() {
    return new com.android.jack.jarjar.test001.jack.dstar.B().getClassName();
  }

  public String dStarGetClassNameOfC() {
    return new com.android.jack.jarjar.test001.jack.dstar.C().getClassName();
  }

  public String dStarGetClassNameOfUntouchedB() {
    return new com.android.jack.jarjar.test001.jack.dstar.sub.B().getClassName();
  }

  public String dStarGetClassNameOfUntouchedC() {
    return new com.android.jack.jarjar.test001.jack.dstar.sub.C().getClassName();
  }

  public String complexGetClassNameOfB() {
    return new com.android.jack.jarjar.test001.jack.complex.one.sep.two.B().getClassName();
  }

  public String complexGetClassNameOfUntouchedB() {
    return new com.android.jack.jarjar.test001.jack.complex.one.sep.sub.two.B().getClassName();
  }

  public String testFieldRef() {
    return new B().f.getClass().getName();
  }

  public String testCtsString() {
    return "com.android.jack.jarjar.test001.jack.B";
  }

  public String testAnnot() {
    return B.class.getAnnotation(DummyAnnot.class).value();
  }
}
