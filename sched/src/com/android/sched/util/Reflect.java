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

package com.android.sched.util;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Provides reflection utilities.
 */
public class Reflect {

  /**
   * Returns an array with all the interfaces implemented by a class.
   * <p>It does the same as {@link Class#getGenericInterfaces()} but also returns the interfaces
   * indirectly implemented.
   */
  @Nonnull
  public static Type[] getAllGenericInSuperClassOrInterface(@Nonnull Class<?> cls) {
    Set<Type> set = new HashSet<Type>();

    getAllGenericInSuperClassOrInterface(set, cls);

    return set.toArray(new Type[set.size()]);
  }

  private static void getAllGenericInSuperClassOrInterface(
      @Nonnull Set<Type> list, @Nonnull Class<?> cls) {
    Type[] array = cls.getGenericInterfaces();
    for (Type type : array) {
      list.add(type);
      if (type instanceof Class<?>) {
        getAllGenericInSuperClassOrInterface(list, (Class<?>) type);
      }
    }

    Type supClass = cls.getGenericSuperclass();
    list.add(supClass);
    if (supClass instanceof Class<?>) {
      getAllGenericInSuperClassOrInterface(list, (Class<?>) supClass);
    }
  }

  private Reflect() {}
}
