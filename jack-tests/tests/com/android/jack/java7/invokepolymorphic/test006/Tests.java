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

package com.android.jack.java7.invokepolymorphic.test006;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;

public class Tests {
  public MethodHandle getCompute() {
    MethodHandle mh = null;
    MethodType mt = MethodType.methodType(String.class, Integer.class);
    MethodHandles.Lookup lk = MethodHandles.lookup();

    try {
      mh = lk.findVirtual(getClass(), "compute", mt);
    } catch (NoSuchMethodException | IllegalAccessException mhx) {
      throw (AssertionError) new AssertionError().initCause(mhx);
    }

    return mh;
  }

  public String compute(Integer i1) {
    return (i1 == null ? "N" : "!N");
  }

  @Test
  public void test() {
    MethodHandle mh = getCompute();
    try {
      Assert.assertEquals("N", mh.invoke(this, null));
    } catch (Throwable t) {
      Assert.fail();
    }
    try {
      Assert.assertEquals("N", mh.invoke(this, (Integer) null));
    } catch (Throwable t) {
      Assert.fail();
    }
    try {
      Assert.assertEquals("!N", mh.invoke(this, 1));
    } catch (Throwable t) {
      Assert.fail();
    }
    try {
      Assert.assertEquals("!N", mh.invoke(this, new Integer(1)));
    } catch (Throwable t) {
      Assert.fail();
    }
    try {
      Assert.assertEquals("N", mh.invokeExact(this, (Integer) null));
      Assert.fail();
    } catch (WrongMethodTypeException e) {
      // Ok
    } catch (Throwable t) {
      Assert.fail();
    }
    try {
      Assert.assertEquals("N", (String) mh.invokeExact(this, (Integer) null));
    } catch (Throwable t) {
      Assert.fail();
    }
    try {
      Assert.assertEquals("N", (String) mh.invokeExact(this, null));
      Assert.fail();
    } catch (WrongMethodTypeException e) {
      // Ok
    } catch (Throwable t) {
      t.printStackTrace();
      Assert.fail();
    }
  }
}
