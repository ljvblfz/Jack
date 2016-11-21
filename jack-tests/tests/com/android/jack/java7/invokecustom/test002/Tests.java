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
package com.android.jack.java7.invokecustom.test002;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import com.android.jack.annotations.CalledByInvokeCustom;
import com.android.jack.annotations.LinkerFieldHandle;
import com.android.jack.annotations.MethodHandleKind;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;


public class Tests {

  public static CallSite fieldCallSite;

  static {
    try {
      MethodHandle mh_add =
          lookup().findStatic(Tests.class, "add", methodType(int.class, int.class, int.class));
      fieldCallSite = new ConstantCallSite(mh_add);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @CalledByInvokeCustom(
      fieldMethodHandle = @LinkerFieldHandle(kind = MethodHandleKind.GET_STATIC,
          enclosingType = Tests.class, name = "fieldCallSite"),
      name = "add",
      returnType = int.class,
      argumentTypes = {int.class, int.class})
  private static int add(int a, int b) {
    return a + b;
  }

  @Test
  public void test() throws Throwable {
    Assert.assertEquals(5, add(2,3));
  }
}
