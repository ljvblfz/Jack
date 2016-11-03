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

package com.android.jack.java7.invokepolymorphic.test005;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class Tests {
  public static void testDelegate_allTypes(boolean z, char a, short b, int c, long d, float e,
      double f, String g, Object h) {
    Assert.assertFalse(z);
    Assert.assertEquals('h', a);
    Assert.assertEquals(56, b);
    Assert.assertEquals(72, c);
    Assert.assertEquals(2147483689L, d);
    Assert.assertEquals(0.56f, e, 0.0);
    Assert.assertEquals(100.0, f, 0.0);
    Assert.assertEquals("hello", g);
    Assert.assertEquals("goodbye", h);
  }

  @Test
  public void testInvokeExactWithAllTypes() throws Throwable {
    MethodHandle specialFunctionHandle =
        MethodHandles.lookup()
            .findStatic(Tests.class, "testDelegate_allTypes",
                MethodType
                    .methodType(void.class,
                        new Class<?>[] {boolean.class, char.class, short.class, int.class,
                                        long.class, float.class, double.class, String.class, Object.class}));

    specialFunctionHandle.invokeExact(false, 'h', (short) 56, 72, Integer.MAX_VALUE + 42l, 0.56f,
        100.0d, "hello", (Object) "goodbye");
  }
}
