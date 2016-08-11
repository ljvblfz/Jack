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

package com.android.jack.optimizations.lambdas.test002.jack;

import java.io.IOException;

interface I0 {
  String foo() throws AssertionError;
}

interface I1 {
  String foo() throws ClassNotFoundException;
}

interface I2 {
  String foo() throws Exception;
}

interface I3 extends I2 {
  String foo() throws IOException;
}

interface I4 extends I2 {
  String foo() throws AssertionError;
}

interface I5 {
  String foo() throws Throwable;
}

interface I6 extends I5 {
  String foo() throws Exception;
}

public class A {
  String s0(I0 i) {
    try { return i.foo(); }
    catch (Throwable e) {return e.getMessage();}
  }

  String s1(I1 i) throws Throwable {
    try { return i.foo(); }
    catch (Throwable e) {return e.getMessage();}
  }

  String s2(I2 i) throws Throwable {
    try { return i.foo(); }
    catch (Throwable e) {return e.getMessage();}
  }

  String s3(I3 i) throws Throwable {
    try { return i.foo(); }
    catch (Throwable e) {return e.getMessage();}
  }

  String s4(I4 i) {
    try { return i.foo(); }
    catch (Throwable e) {return e.getMessage();}
  }

  String s5(I5 i) throws Throwable {
    try { return i.foo(); }
    catch (Throwable e) {return e.getMessage();}
  }

  String s6(I6 i) throws Throwable {
    try { return i.foo(); }
    catch (Throwable e) {return e.getMessage();}
  }

  public String testException() throws Throwable {
    return
        s0(() -> {throw new AssertionError("{assertion#1}");}) +
        s1(() -> {throw new ClassNotFoundException("{classnotfound}");}) +
        s2(() -> {throw new Exception("{exception#1}");}) +
        s3(() -> {throw new IOException("{ioexception}");}) +
        s4(() -> {throw new AssertionError("{assertion#2}");}) +
        s5(() -> {throw new Exception("{exception#2}");}) +
        s6(() -> {throw new Exception("{exception#3}");}) ;
  }
}
