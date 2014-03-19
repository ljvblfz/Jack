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

package com.android.jack.invoke.test001.dx;

import com.android.jack.invoke.test001.jack.InvokeClone;
import com.android.jack.invoke.test001.jack.InvokeDirect;
import com.android.jack.invoke.test001.jack.InvokeInterface;
import com.android.jack.invoke.test001.jack.InvokeInterfaceComputationImpl;
import com.android.jack.invoke.test001.jack.InvokeStatic;
import com.android.jack.invoke.test001.jack.InvokeSuper;
import com.android.jack.invoke.test001.jack.InvokeVirtual;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests about invoke static.
 */
public class Tests {

  @Test
  public void testInvokeStatic() {
    Assert.assertEquals(1 + 2, InvokeStatic.invoke001(1, 2));
    Assert.assertEquals(123456789 + 9876543210L, InvokeStatic.invoke002(123456789, 9876543210L));
  }

  @Test
  public void testInvokeDirect() {
    Assert.assertEquals(1 + 2, new InvokeDirect().invoke001(1, 2));
    Assert.assertEquals(123456789 + 9876543210L,
        new InvokeDirect().invoke002(123456789, 9876543210L));
  }

  @Test
  public void testInvokeVirtual() {
    Assert.assertEquals(1 + 2, new InvokeVirtual().invoke001(1, 2));
    Assert.assertEquals(123456789 + 9876543210L,
        new InvokeVirtual().invoke002(123456789, 9876543210L));
  }

  @Test
  public void testInvokeInterfaces() {
    Assert.assertEquals(1 + 2,
        new InvokeInterface().invoke001(new InvokeInterfaceComputationImpl(), 1, 2));
    Assert.assertEquals(123456789 + 9876543210L, new InvokeInterface().invoke002(
        new InvokeInterfaceComputationImpl(), 123456789, 9876543210L));
  }

  @Test
  public void testInvokeSuper() {
    InvokeSuper invokeSuper = new InvokeSuper();
    Assert.assertEquals(111, invokeSuper.get1());
    Assert.assertEquals(112, invokeSuper.get2());
    Assert.assertEquals(123, invokeSuper.get3());
    Assert.assertEquals(24, invokeSuper.get4());
    Assert.assertEquals(124, invokeSuper.get5());
  }

  @Test
  public void testInvokeClone() {
    Assert.assertEquals(3, InvokeClone.getArray()[2]);
  }
}
