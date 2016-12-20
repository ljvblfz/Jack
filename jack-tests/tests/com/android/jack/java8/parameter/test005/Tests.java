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

package com.android.jack.java8.parameter.test005;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class Tests {

  public static final int MODIFIER_NONE = 0;
  public static final int MODIFIER_FINAL = 16;
  public static final int MODIFIER_SYNTHETIC = 4096;
  public static final int MODIFIER_IMPLICIT = 32768;

  public static int m1(int p1, int p2, int p3) {
    return p1 + p2 + p3;
  }


  @Test
  public void testStaticMethod() throws NoSuchMethodException, SecurityException {
    Method method = Tests.class.getDeclaredMethod("m1", int.class, int.class, int.class);
    Assert.assertEquals(3, method.getParameters().length);

    check(method.getParameters()[0], "p1", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, MODIFIER_NONE);

    check(method.getParameters()[1], "p2", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, MODIFIER_NONE);

    check(method.getParameters()[2], "p3", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, MODIFIER_NONE);
  }

  private void check(Parameter parameter, String name, boolean isNamePresent, boolean isImplicit,
      boolean isSynthetic, int modifier) {
    Assert.assertEquals(name, parameter.getName());
    Assert.assertEquals(isNamePresent, parameter.isNamePresent());
    Assert.assertEquals(isSynthetic, parameter.isSynthetic());
    Assert.assertEquals(isImplicit, parameter.isImplicit());
    Assert.assertEquals(modifier, parameter.getModifiers());
  }
}
