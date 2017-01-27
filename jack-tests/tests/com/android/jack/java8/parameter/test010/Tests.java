/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack.java8.parameter.test010;

import com.android.jack.java8.parameter.common.ParameterTestModifier;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class Tests {

  public static void m1(int a, double d, final float f, final long l) {
  }

  public void m2(final int a, double d, final float f, long l) {
  }

  @Test
  public void testStaticMethod() throws NoSuchMethodException, SecurityException {
    Method method =
        Tests.class.getDeclaredMethod("m1", int.class, double.class, float.class, long.class);
    Parameter[] parameters = method.getParameters();

    Assert.assertEquals(4, parameters.length);

    check(parameters[0], "a", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, ParameterTestModifier.MODIFIER_NONE);

    check(parameters[1], "d", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, ParameterTestModifier.MODIFIER_NONE);

    check(parameters[2], "f", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, ParameterTestModifier.MODIFIER_FINAL);

    check(parameters[3], "l", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, ParameterTestModifier.MODIFIER_FINAL);
  }

  @Test
  public void testIsntanceMethod() throws NoSuchMethodException, SecurityException {
    Method method =
        Tests.class.getDeclaredMethod("m2", int.class, double.class, float.class, long.class);
    Parameter[] parameters = method.getParameters();

    Assert.assertEquals(4, parameters.length);

    check(parameters[0], "a", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, ParameterTestModifier.MODIFIER_FINAL);

    check(parameters[1], "d", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, ParameterTestModifier.MODIFIER_NONE);

    check(parameters[2], "f", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, ParameterTestModifier.MODIFIER_FINAL);

    check(parameters[3], "l", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, ParameterTestModifier.MODIFIER_NONE);
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
