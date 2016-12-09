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
package com.android.jack.java7.invokecustom.test001;

import static java.lang.invoke.MethodHandles.lookup;

import com.android.jack.annotations.CalledByInvokeCustom;
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
          argumentTypes = {MethodHandles.Lookup.class, String.class, MethodType.class}),
      name = "add",
      returnType = int.class,
      argumentTypes = {int.class, int.class})
  private static int add(int a, int b) {
    return a + b;
  }

  @SuppressWarnings("unused")
  private static CallSite linkerMethod(MethodHandles.Lookup caller, String name,
      MethodType methodType) throws Throwable {
    MethodHandle mh_add = lookup().findStatic(Tests.class, name, methodType);
    return new ConstantCallSite(mh_add);
  }

  @Test
  public void test() throws Throwable {
    Assert.assertEquals(5, Tests.add(2,3));
  }
}
