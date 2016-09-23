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

package com.android.jack.java7.invokepolymorphic.test003;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Check that call to invoke polymorphic method is always detected by Jack even if the return type
 * is not java.lang.Object.
 */
public class Tests {
  public MethodHandle getCompute() {
    MethodHandle mh = null;
    MethodType mt = MethodType.methodType(String.class, Integer.class, int.class, String.class);
    MethodHandles.Lookup lk = MethodHandles.lookup();

    try {
      mh = lk.findVirtual(getClass(), "compute", mt);
    } catch (NoSuchMethodException | IllegalAccessException mhx) {
      throw (AssertionError) new AssertionError().initCause(mhx);
    }

    return mh;
  }

  public String compute(Integer i1, int i2, String s) {
    return (i1 == null ? "N" : "!N") + "-" + i2 + "-" + s;
  }

  @Test
  public void test() {
    MethodHandle mh = getCompute();
    try {
      Assert.assertEquals("N-1-string", (String) mh.invoke(this, null, 1, "string"));
    } catch (Throwable t) {
      Assert.fail();
    }
  }
}
