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

package com.android.jack.java8.lambda.test034.jack;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Lambda with reflect API
 */
public class Tests {

  @Test
  public void test001() throws Exception {
    Callable<String> c = () -> "Hello";
    Assert.assertEquals(2, c.getClass().getDeclaredMethods().length);
    Assert.assertEquals(Object.class, c.getClass().getMethod("call").getReturnType());
  }

  interface Condition<T> {
    boolean check(T arg);
  }

  @Test
  public void test002() throws Exception {
    Condition<String> c = String::isEmpty;
    Assert.assertEquals(2, c.getClass().getDeclaredMethods().length);
    Method m = c.getClass().getMethod("check", Object.class);
    Assert.assertEquals(boolean.class, m.getReturnType());
    Assert.assertFalse(m.isSynthetic());
    Assert.assertFalse(m.isBridge());
  }

  @Test
  public void test003() throws Exception {
    Runnable r = () -> System.out.println("Hello");
    Method m = r.getClass().getMethod("run");
    Assert.assertSame(r.getClass(), m.getDeclaringClass());
    Assert.assertFalse(m.isSynthetic());
  }
}
