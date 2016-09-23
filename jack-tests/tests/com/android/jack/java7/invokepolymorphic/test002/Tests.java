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

package com.android.jack.java7.invokepolymorphic.test002;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class Tests {
  public MethodHandle getCompute() {
    MethodHandle mh = null;
    MethodType mt = MethodType.methodType(String.class, byte.class, char.class, short.class,
        float.class, double.class, long.class, Integer.class, int.class, String.class);
    MethodHandles.Lookup lk = MethodHandles.lookup();

    try {
      mh = lk.findVirtual(getClass(), "compute", mt);
    } catch (NoSuchMethodException | IllegalAccessException mhx) {
      throw (AssertionError) new AssertionError().initCause(mhx);
    }

    return mh;
  }

  public String compute(byte b, char c, short s, float f, double d, long l, Integer i1, int i2, String str) {
    return b + "-" + c + "-" + s + "-" + f + "-" + d + "-" + l + "-" + (i1 == null ? "N" : "!N")
        + "-" + i2 + "-" + str;
  }

  @Test
  public void test() {
    MethodHandle mh = getCompute();
    try {
      Assert.assertEquals("2-a--1-1.1-2.24-12345678-N-1-string", mh.invoke(this, (byte) 2, 'a',
          (short) 0xFFFF, 1.1f, 2.24d, 12345678L, null, 1, "string"));
    } catch (Throwable t) {
      t.printStackTrace();
      Assert.fail();
    }
  }
}