/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.sched.util.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import javax.annotation.Nonnull;

/**
 * A class capable of providing instances of type {@code T} according to an implementation class.
 *
 * @param <T>
 */
public class ReflectDefaultCtorFactory<T> implements DefaultFactory<T> {
  @Nonnull
  private final Constructor<? extends T> ctor;
  @Nonnull
  private final Class<? extends T> cls;

  public ReflectDefaultCtorFactory(
      @Nonnull Class<? extends T> cls, boolean respectAccessibility) {
    assert checkAssertion(cls);

    this.cls = cls;
    try {
      ctor = cls.getDeclaredConstructor();
      if (!respectAccessibility) {
        ctor.setAccessible(true);
      }
    } catch (NoSuchMethodException e) {
      throw new AssertionError("Default constructor is not found in '" + cls.getName() + '\'');
    }
  }

  private boolean checkAssertion(@Nonnull Class<? extends T> cls) {
    if (cls.isInterface()) {
      throw new AssertionError(
          "Type '" + cls.getName() + "' is an interface");
    }
    if (Modifier.isAbstract(cls.getModifiers())) {
      throw new AssertionError(
          "Type '" + cls.getName() + "' is an abstract class");
    }

    return true;
  }

  @Override
  @Nonnull
  public T create() {
    try {
      return ctor.newInstance();
    } catch (InstantiationException e) {
      // See checkAssertion
      throw new AssertionError();
    } catch (IllegalAccessException e) {
      throw new AssertionError("Constructor '" + ctor + "' is not accessible");
    } catch (InvocationTargetException e) {
      // Exception throw during instantiation
      throw new RuntimeException(e.getCause());
    }
  }

  @Override
  @Nonnull
  public Class<? extends T> getInstanciatedClass() {
    return cls;
  }
}
