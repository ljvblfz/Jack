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

package com.android.jack.java8.parameter.test002;

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

  enum Number {
    ONE, TWO;
  }

  @Test
  public void testEnumValueOf() throws NoSuchMethodException, SecurityException {
    Method method = Number.class.getDeclaredMethod("valueOf", String.class);
    Assert.assertEquals(1, method.getParameters().length);

    check(method.getParameters()[0], "name", /* isNamePresent= */ true, /* isImplicit= */ true,
        /* isSynthetic= */ false, MODIFIER_IMPLICIT);
  }

  @Test
  public void testEnumConstructor() throws NoSuchMethodException, SecurityException {
    Constructor<?> constructor = Number.class.getDeclaredConstructor(String.class, int.class);
    Assert.assertEquals(2, constructor.getParameters().length);

    check(constructor.getParameters()[0], "enum$name", /* isNamePresent= */ true,
        /* isImplicit= */ false, /* isSynthetic= */ true, MODIFIER_SYNTHETIC);

    check(constructor.getParameters()[1], "enum$ordinal", /* isNamePresent= */ true,
        /* isImplicit= */ false, /* isSynthetic= */ true, MODIFIER_SYNTHETIC);
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
