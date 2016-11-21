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
package com.android.jack.java7.invokecustom.test004;

import static java.lang.invoke.MethodHandles.lookup;

import com.android.jack.annotations.CalledByInvokeCustom;
import com.android.jack.annotations.Constant;
import com.android.jack.annotations.LinkerMethodHandle;
import com.android.jack.annotations.MethodHandleKind;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class Tests {

  public static CallSite fieldCallSite;

  @CalledByInvokeCustom(
      invokeMethodHandle = @LinkerMethodHandle(kind = MethodHandleKind.INVOKE_STATIC,
          enclosingType = Tests.class,
          name = "linkerMethod",
          argumentTypes = {MethodHandles.Lookup.class, String.class, MethodType.class,
                           boolean.class, byte.class, char.class, short.class, int.class,
                           float.class, double.class, String.class, Class.class, long.class}),
      methodHandleExtraArgs = {@Constant(booleanValue = true), @Constant(byteValue = 1),
                         @Constant(charValue = 'a'), @Constant(shortValue = 1024),
                         @Constant(intValue = 1), @Constant(floatValue = 11.1f),
                         @Constant(doubleValue = 2.2), @Constant(stringValue = "Hello"),
                         @Constant(classValue = Tests.class), @Constant(longValue = 123456789L)},
      name = "add",
      returnType = int.class,
      argumentTypes = {int.class, int.class})
  private static int add(int a, int b) {
    return a + b;
  }

  @SuppressWarnings("unused")
  private static CallSite linkerMethod(MethodHandles.Lookup caller, String name,
      MethodType methodType, boolean v1, byte v2, char v3, short v4, int v5, float v6, double v7,
      String v8, Class<?> v9, long v10) throws Throwable {
    Assert.assertTrue(v1);
    Assert.assertEquals(1, v2);
    Assert.assertEquals('a', v3);
    Assert.assertEquals(1024, v4);
    Assert.assertEquals(1, v5);
    Assert.assertEquals(11.1f, v6, 0);
    Assert.assertEquals(2.2, v7, 0);
    Assert.assertEquals("Hello", v8);
    Assert.assertEquals(Tests.class, v9);
    Assert.assertEquals(123456789L, v10);
    MethodHandle mh_add = lookup().findStatic(Tests.class, name, methodType);
    return new ConstantCallSite(mh_add);
  }

  @Test
  public void test() throws Throwable {
    Assert.assertEquals(5, add(2,3));
  }
}
