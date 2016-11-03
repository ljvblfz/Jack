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

package com.android.jack.java7.invokepolymorphic.test004;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;

public class Tests {
  public MethodHandle getStaticMethodHandle(String name, Class<?> returnType, Class<?>... parameterTypes) {
    MethodHandle mh = null;
    MethodType mt = MethodType.methodType(returnType, parameterTypes);
    MethodHandles.Lookup lk = MethodHandles.lookup();

    try {
      mh = lk.findStatic(getClass(), name , mt);
    } catch (NoSuchMethodException | IllegalAccessException mhx) {
      Assert.fail();
    }

    return mh;
  }

  public MethodHandle getVirtualMethodHandle(String name, Class<?> returnType, Class<?>... parameterTypes) {
    MethodHandle mh = null;
    MethodType mt = MethodType.methodType(returnType, parameterTypes);
    MethodHandles.Lookup lk = MethodHandles.lookup();

    try {
      mh = lk.findVirtual(getClass(), name , mt);
    } catch (NoSuchMethodException | IllegalAccessException mhx) {
      Assert.fail();
    }

    return mh;
  }

  public static String getStaticDescription() {
    return "Description";
  }

  public static String getStaticDescription(int value1) {
    return "Description: " + value1;
  }

  public static String getStaticDescription(int value1, int value2) {
    return "Description: " + value1 + ", " + value2;
  }

  public static String getStaticDescription(int value1, int value2, int value3) {
    return "Description: " + value1 + ", " + value2 + ", " + value3;
  }

  public static String getStaticDescription(int value1, int value2, int value3, int value4) {
    return "Description: " + value1 + ", " + value2 + ", " + value3 + ", " + value4;
  }

  public static String getStaticDescription(int value1, int value2, int value3, int value4, int value5) {
    return "Description: " + value1 + ", " + value2 + ", " + value3 + ", " + value4 + ", " + value5;
  }

  public static String getStaticDescription(int value1, int value2, int value3, int value4, int value5,
      int value6) {
    return "Description: " + value1 + ", " + value2 + ", " + value3 + ", " + value4 + ", " + value5
        + ", " + value6;
  }

  public String getVirtualDescription() {
    return "Description";
  }

  public String getVirtualDescription(int value1) {
    return "Description: " + value1;
  }

  public String getVirtualDescription(int value1, int value2) {
    return "Description: " + value1 + ", " + value2;
  }

  public String getVirtualDescription(int value1, int value2, int value3) {
    return "Description: " + value1 + ", " + value2 + ", " + value3;
  }

  public String getVirtualDescription(int value1, int value2, int value3, int value4) {
    return "Description: " + value1 + ", " + value2 + ", " + value3 + ", " + value4;
  }

  public String getVirtualDescription(int value1, int value2, int value3, int value4,
      int value5) {
    return "Description: " + value1 + ", " + value2 + ", " + value3 + ", " + value4 + ", " + value5;
  }

  public String getVirtualDescription(int value1, int value2, int value3, int value4,
      int value5, int value6) {
    return "Description: " + value1 + ", " + value2 + ", " + value3 + ", " + value4 + ", " + value5
        + ", " + value6;
  }

  public String getVirtualMixedPrimitiveDescription(int value1, byte value2, long value3, double value4,
      short value5, char value6) {
    return "Description: " + value1 + ", " + value2 + ", " + value3 + ", " + value4 + ", " + value5
        + ", " + value6;
  }

  public String getVirtualMixedPrimitiveDescription(long value1, byte value2) {
    return "Description: " + value1 + ", " + value2;
  }

  @Test
  public void testStaticInvokeExactNoParameter() {
    try {
      MethodHandle mh = getStaticMethodHandle("getStaticDescription", String.class);
      Assert.assertEquals("Description", (String) mh.invokeExact());
    } catch (Throwable t) {
      Assert.fail();
    }
  }

  @Test
  public void testStaticInvokeExactOneParameter() {
    try {
      MethodHandle mh = getStaticMethodHandle("getStaticDescription", String.class, int.class);
      Assert.assertEquals("Description: 1", (String) mh.invokeExact(1));
    } catch (Throwable t) {
      Assert.fail();
    }
  }

  @Test
  public void testStaticInvokeExactTwoParameters() {
    try {
      MethodHandle mh = getStaticMethodHandle("getStaticDescription", String.class, int.class, int.class);
      Assert.assertEquals("Description: 1, 2", (String) mh.invokeExact(1, 2));
    } catch (Throwable t) {
      Assert.fail();
    }
  }

  @Test
  public void testStaticInvokeExactThreeParameters() {
    try {
      MethodHandle mh =
          getStaticMethodHandle("getStaticDescription", String.class, int.class, int.class, int.class);
      Assert.assertEquals("Description: 1, 2, 3", (String) mh.invokeExact(1, 2, 3));
    } catch (Throwable t) {
      Assert.fail();
    }
  }

  @Test
  public void testStaticInvokeExactFourParameters() {
    try {
      MethodHandle mh = getStaticMethodHandle("getStaticDescription", String.class, int.class, int.class,
          int.class, int.class);
      Assert.assertEquals("Description: 1, 2, 3, 4", (String) mh.invokeExact(1, 2, 3, 4));
    } catch (Throwable t) {
      Assert.fail();
    }
  }

  @Test
  public void testStaticInvokeExactFiveParameters() {
    try {
      MethodHandle mh = getStaticMethodHandle("getStaticDescription", String.class, int.class, int.class,
          int.class, int.class, int.class);
      Assert.assertEquals("Description: 1, 2, 3, 4, 5", (String) mh.invokeExact(1, 2, 3, 4, 5));
    } catch (Throwable t) {
      Assert.fail();
    }
  }

  @Test
  public void testStaticInvokeExactSixParameters() {
    try {
      MethodHandle mh = getStaticMethodHandle("getStaticDescription", String.class, int.class, int.class,
          int.class, int.class, int.class, int.class);
      Assert.assertEquals("Description: 1, 2, 3, 4, 5, 6",
          (String) mh.invokeExact(1, 2, 3, 4, 5, 6));
    } catch (Throwable t) {
      Assert.fail();
    }
  }

  @Test
  public void testVirtualInvokeExactNoParameter() {
    try {
      MethodHandle mh = getVirtualMethodHandle("getVirtualDescription", String.class);
      Assert.assertEquals("Description", (String) mh.invokeExact(this));
    } catch (Throwable t) {
      Assert.fail();
    }
  }

  @Test
  public void testVirtualInvokeExactOneParameter() {
    try {
      MethodHandle mh = getVirtualMethodHandle("getVirtualDescription", String.class, int.class);
      Assert.assertEquals("Description: 1", (String) mh.invokeExact(this, 1));
    } catch (Throwable t) {
      Assert.fail();
    }
  }

  @Test
  public void testVirtualInvokeExactTwoParameters() {
    try {
      MethodHandle mh = getVirtualMethodHandle("getVirtualDescription", String.class, int.class, int.class);
      Assert.assertEquals("Description: 1, 2", (String) mh.invokeExact(this, 1, 2));
    } catch (Throwable t) {
      Assert.fail();
    }
  }

  @Test
  public void testVirtualInvokeExactThreeParameters() {
    try {
      MethodHandle mh =
          getVirtualMethodHandle("getVirtualDescription", String.class, int.class, int.class, int.class);
      Assert.assertEquals("Description: 1, 2, 3", (String) mh.invokeExact(this, 1, 2, 3));
    } catch (Throwable t) {
      Assert.fail();
    }
  }

  @Test
  public void testVirtualInvokeExactFourParameters() {
    try {
      MethodHandle mh = getVirtualMethodHandle("getVirtualDescription", String.class, int.class, int.class,
          int.class, int.class);
      Assert.assertEquals("Description: 1, 2, 3, 4", (String) mh.invokeExact(this, 1, 2, 3, 4));
    } catch (Throwable t) {
      Assert.fail();
    }
  }

  @Test
  public void testVirtualInvokeExactFiveParameters() {
    try {
      MethodHandle mh = getVirtualMethodHandle("getVirtualDescription", String.class, int.class, int.class,
          int.class, int.class, int.class);
      Assert.assertEquals("Description: 1, 2, 3, 4, 5", (String) mh.invokeExact(this, 1, 2, 3, 4, 5));
    } catch (Throwable t) {
      Assert.fail();
    }
  }

  @Test
  public void testVirtualInvokeExactSixParameters() {
    try {
      MethodHandle mh = getVirtualMethodHandle("getVirtualDescription", String.class, int.class, int.class,
          int.class, int.class, int.class, int.class);
      Assert.assertEquals("Description: 1, 2, 3, 4, 5, 6",
          (String) mh.invokeExact(this, 1, 2, 3, 4, 5, 6));
    } catch (Throwable t) {
      Assert.fail();
    }
  }

  @Test
  public void testVirtualInvokeExactWithTwoMixedPrimitiveParameters() {
    try {
      MethodHandle mh = getVirtualMethodHandle("getVirtualMixedPrimitiveDescription", String.class,
          long.class, byte.class);
      Assert.assertEquals("Description: 123456789, -128",
          (String) mh.invokeExact(this, 123456789L, (byte) 128));
    } catch (Throwable t) {
      t.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testVirtualInvokeExactWithSixMixedPrimitiveParameters() {
    try {
      MethodHandle mh = getVirtualMethodHandle("getVirtualMixedPrimitiveDescription", String.class,
          int.class, byte.class, long.class, double.class, short.class, char.class);
      Assert.assertEquals("Description: 1, 127, 123456789, 4.5, 1024, c",
          (String) mh.invokeExact(this, 1, (byte) 127, 123456789L, 4.5, (short) 1024, 'c'));
    } catch (Throwable t) {
      Assert.fail();
    }
  }

  @Test
  public void testStaticInvokeExactWrongMethodException() {
    try {
      MethodHandle mh = getStaticMethodHandle("getStaticDescription", String.class);
      Assert.assertEquals("Description", mh.invokeExact());
      Assert.fail();
    } catch (WrongMethodTypeException e) {
      // OK
    } catch (Throwable e) {
      Assert.fail();
    }
  }
}
