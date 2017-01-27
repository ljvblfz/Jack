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

package com.android.jack.java8.parameter.test001;

import com.android.jack.java8.parameter.common.ParameterTestModifier;
import com.android.jack.java8.parameter.test001.Tests.Context.InnerWithParam;
import junit.framework.Assert;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class Tests {

  public static void m1(final int p1, int b2, Object... a) {}

  public class InnerClass {
    public class InnerInnerClass {
      public InnerInnerClass(float f) {
      }
    }
  }

  @Test
  public void testStaticMethod() throws NoSuchMethodException, SecurityException {
    Method method = Tests.class.getDeclaredMethod("m1", int.class, int.class, Object[].class);
    Assert.assertEquals(3, method.getParameters().length);

    check(method.getParameters()[0], "p1", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, ParameterTestModifier.MODIFIER_FINAL);

    check(method.getParameters()[1], "b2", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, ParameterTestModifier.MODIFIER_NONE);

    check(method.getParameters()[2], "a", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, ParameterTestModifier.MODIFIER_NONE);
  }

  @Test
  public void testInnerClass() throws NoSuchMethodException, SecurityException {
    Constructor<?> constructor = InnerClass.class.getDeclaredConstructor(Tests.class);
    Assert.assertEquals(1, constructor.getParameters().length);

    check(constructor.getParameters()[0], "this$0", /* isNamePresent= */ true,
        /* isImplicit= */ true, /* isSynthetic= */ false,
        ParameterTestModifier.MODIFIER_FINAL | ParameterTestModifier.MODIFIER_IMPLICIT);
  }

  @Test
  public void testInnerInnerClass() throws NoSuchMethodException, SecurityException {
    Constructor<?> constructor =
        InnerClass.InnerInnerClass.class.getDeclaredConstructor(InnerClass.class, float.class);
    Assert.assertEquals(2, constructor.getParameters().length);

    check(constructor.getParameters()[0], "this$1", /* isNamePresent= */ true,
        /* isImplicit= */ true, /* isSynthetic= */ false,
        ParameterTestModifier.MODIFIER_FINAL | ParameterTestModifier.MODIFIER_IMPLICIT);

    check(constructor.getParameters()[1], "f", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, ParameterTestModifier.MODIFIER_NONE);
  }

  @Test
  public void testLocalClass() throws NoSuchMethodException, SecurityException {
    class Local {
    }
    Constructor<?> constructor = Local.class.getDeclaredConstructor(Tests.class);
    Assert.assertEquals(1, constructor.getParameters().length);

    check(constructor.getParameters()[0], "this$0", /* isNamePresent= */ true,
        /* isImplicit= */ true, /* isSynthetic= */ false,
        ParameterTestModifier.MODIFIER_FINAL | ParameterTestModifier.MODIFIER_IMPLICIT);
  }

  @Test
  public void testLocalClassWithCapture() throws NoSuchMethodException, SecurityException {
    final Object o = new Object();
    @SuppressWarnings("unused")
    class Local {
      Local (int i) {
      }
      Object getObject() {
        return o;
      }
    }
    Constructor<?> constructor = Local.class.getDeclaredConstructor(Tests.class, int.class, Object.class);
    Assert.assertEquals(3, constructor.getParameters().length);

    check(constructor.getParameters()[0], "this$0", /* isNamePresent= */ true,
        /* isImplicit= */ true, /* isSynthetic= */ false,
        ParameterTestModifier.MODIFIER_FINAL | ParameterTestModifier.MODIFIER_IMPLICIT);

    check(constructor.getParameters()[1], "i", /* isNamePresent= */ true, /* isImplicit= */ false,
        /* isSynthetic= */ false, ParameterTestModifier.MODIFIER_NONE);

    check(constructor.getParameters()[2], "val$o", /* isNamePresent= */ true,
        /* isImplicit= */ false, /* isSynthetic= */ true,
        ParameterTestModifier.MODIFIER_FINAL | ParameterTestModifier.MODIFIER_SYNTHETIC);
  }

    abstract static class StaticAnonymous {
    }

    @Test
    public void testAnonymousWithStaticSuper() throws NoSuchMethodException, SecurityException {
      StaticAnonymous a = new StaticAnonymous() {};
      Constructor<?> constructor = a.getClass().getDeclaredConstructor(Tests.class);
      Assert.assertEquals(1, constructor.getParameters().length);
    check(constructor.getParameters()[0], "this$0", /* isNamePresent= */ true,
        /* isImplicit= */ true, /* isSynthetic= */ false,
        ParameterTestModifier.MODIFIER_FINAL | ParameterTestModifier.MODIFIER_IMPLICIT);
    }


  class Context{
    class InnerWithParam {
      public InnerWithParam(int i1) {
      }
    }
  }

  @Test
  public void testAnonymousWithContext() throws NoSuchMethodException, SecurityException {
    InnerWithParam iwp = new Context().new InnerWithParam(10);

    Constructor<?> constructor = iwp.getClass().getDeclaredConstructor(Context.class, int.class);
    Assert.assertEquals(2, constructor.getParameters().length);

    check(constructor.getParameters()[0], "this$1", /* isNamePresent= */ true,
        /* isImplicit= */ true, /* isSynthetic= */ false,
        ParameterTestModifier.MODIFIER_FINAL | ParameterTestModifier.MODIFIER_IMPLICIT);

    check(constructor.getParameters()[1], "i1", /* isNamePresent= */ true,
        /* isImplicit= */ false, /* isSynthetic= */ false, ParameterTestModifier.MODIFIER_NONE);
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
