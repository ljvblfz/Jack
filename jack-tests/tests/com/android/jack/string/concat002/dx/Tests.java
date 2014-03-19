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

package com.android.jack.string.concat002.dx;

import com.android.jack.string.concat002.jack.Data;

import org.junit.Assert;
import org.junit.Test;

public class Tests {

  @Test
  public void test1() {
    String a = "abcd";
    boolean b = false;
    Assert.assertEquals(a + b, Data.test001(a, b));
  }

  @Test
  public void test2() {
    String a = "abcd";
    byte b = 127;
    Assert.assertEquals(a + b, Data.test002(a, b));
  }

  @Test
  public void test3() {
    String a = "abcd";
    char b = 'e';
    Assert.assertEquals(a + b, Data.test003(a, b));
  }

  @Test
  public void test4() {
    String a = "abcd";
    short b = -48;
    Assert.assertEquals(a + b, Data.test004(a, b));
  }

  @Test
  public void test5() {
    String a = "abcd";
    int b = 157895;
    Assert.assertEquals(a + b, Data.test005(a, b));
  }

  @Test
  public void test6() {
    String a = "abcd";
    long b = 12345678901l;
    Assert.assertEquals(a + b, Data.test006(a, b));
  }

  @Test
  public void test7() {
    String a = "abcd";
    float b = 158.157f;
    Assert.assertEquals(a + b, Data.test007(a, b));
  }

  @Test
  public void test8() {
    String a = "abcd";
    double b = 1570.1598;
    Assert.assertEquals(a + b, Data.test008(a, b));
  }

  @Test
  public void test9() {
    boolean a = true;
    String b = "abcd";
    Assert.assertEquals(a + b, Data.test009(a, b));
  }

  @Test
  public void test10() {
    byte a = -123;
    String b = "abcd";
    Assert.assertEquals(a + b, Data.test010(a, b));
  }

  @Test
  public void test11() {
    char a = 'z';
    String b = "abcd";
    Assert.assertEquals(a + b, Data.test011(a, b));
  }

  @Test
  public void test12() {
    short a = -12783;
    String b = "abcd";
    Assert.assertEquals(a + b, Data.test012(a, b));
  }

  @Test
  public void test13() {
    int a = -12345678;
    String b = "abcd";
    Assert.assertEquals(a + b, Data.test013(a, b));
  }

  @Test
  public void test14() {
    long a = -123456789012l;
    String b = "abcd";
    Assert.assertEquals(a + b, Data.test014(a, b));
  }

  @Test
  public void test15() {
    float a = -123.12587f;
    String b = "abcd";
    Assert.assertEquals(a + b, Data.test015(a, b));
  }

  @Test
  public void test16() {
    double a = -123.125789;
    String b = "abcd";
    Assert.assertEquals(a + b, Data.test016(a, b));
  }
}
