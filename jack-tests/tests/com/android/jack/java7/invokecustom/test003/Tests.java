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
package com.android.jack.java7.invokecustom.test003;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import com.android.jack.annotations.CalledByInvokeCustom;
import com.android.jack.annotations.LinkerMethodHandle;
import com.android.jack.annotations.MethodHandleKind;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;

public class Tests {

  public static CallSite fieldCallSite;

  @CalledByInvokeCustom(
      invokeMethodHandle = @LinkerMethodHandle(kind = MethodHandleKind.INVOKE_STATIC,
          enclosingType = Tests.class, name = "linkerMethod"),
      name = "add",
      returnType = int.class,
      argumentTypes = {int.class, int.class, int.class, int.class, int.class, int.class})
  private static int add(int a, int b, int c, int d, int e, int f) {
    return a + b + c + d + e + f;
  }

  @SuppressWarnings("unused")
  private static CallSite linkerMethod() throws Throwable {
    MethodHandle mh_add = lookup().findStatic(Tests.class, "add",
        methodType(int.class, int.class, int.class, int.class, int.class, int.class, int.class));
    return new ConstantCallSite(mh_add);
  }

  @Test
  public void test() throws Throwable {
    Assert.assertEquals(21, add(1, 2, 3, 4, 5, 6));
  }
}
