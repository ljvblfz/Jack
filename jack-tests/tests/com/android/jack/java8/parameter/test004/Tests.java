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

package com.android.jack.java8.parameter.test004;


import junit.framework.Assert;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

public class Tests {

  public static final int MODIFIER_NONE = 0;
  public static final int MODIFIER_FINAL = 16;
  public static final int MODIFIER_SYNTHETIC = 4096;
  public static final int MODIFIER_IMPLICIT = 32768;


  abstract class InstanceAnonymous {
    public InstanceAnonymous(int value) {
    }
  }

  @Test
  public void testAnonymousWithInstanceSuper() throws NoSuchMethodException, SecurityException {
    InstanceAnonymous ia = new InstanceAnonymous(0) {
    };

    // Could be a constructor with Test.class, int.class
    Constructor<?> constructor = ia.getClass().getDeclaredConstructor(Tests.class, Tests.class, int.class);
    Assert.assertEquals(3, constructor.getParameters().length);

    check(constructor.getParameters()[0], "this$0", /* isNamePresent= */ true, /* isImplicit= */ true,
      /* isSynthetic= */ false, MODIFIER_FINAL | MODIFIER_IMPLICIT);

    // Could be this$1 rather than this$0_1
    check(constructor.getParameters()[1], "this$0_1", /* isNamePresent= */ true, /* isImplicit= */ true,
        /* isSynthetic= */ false, MODIFIER_FINAL | MODIFIER_IMPLICIT);

    // Could be value rather than $anonymous0
    check(constructor.getParameters()[2], "$anonymous0", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, MODIFIER_NONE);
  }

  @Test
  public void testAnonymousWithInstanceSuperLocal() throws NoSuchMethodException, SecurityException {
    abstract class InstanceAnonymous {
      public InstanceAnonymous(int value) {
      }
    }

    InstanceAnonymous ia = new InstanceAnonymous(0) {
    };

    // Could be a constructor with Test.class, int.class
    Constructor<?> constructor = ia.getClass().getDeclaredConstructor(Tests.class, Tests.class, int.class);
    Assert.assertEquals(3, constructor.getParameters().length);

    check(constructor.getParameters()[0], "this$0", /* isNamePresent= */ true, /* isImplicit= */ false,
      /* isSynthetic= */ true, MODIFIER_FINAL | MODIFIER_SYNTHETIC);

    // Could be this$1 rather than this$0_1
    check(constructor.getParameters()[1], "this$0_1", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ true, MODIFIER_FINAL | MODIFIER_SYNTHETIC);

    // Could be value rather than $anonymous0
    check(constructor.getParameters()[2], "$anonymous0", /* isNamePresent= */ true, /* isImplicit= */ false,
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
