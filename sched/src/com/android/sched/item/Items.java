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

package com.android.sched.item;

import com.android.sched.item.onlyfor.Default;
import com.android.sched.item.onlyfor.OnlyFor;
import com.android.sched.item.onlyfor.OnlyForType;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Provides utility methods to access specific annotations of {@link Item} classes.
 */
public class Items {
  @Nonnull
  private static final Class<?>[] EMPTY_COMPOSED_OF = new Class<?>[0];

  private Items() {}

  /**
   * Returns the {@link Item}s an {@code Item} has been declared composed of using the annotation
   * {@link ComposedOf}.
   */
  @SuppressWarnings("unchecked")
  @Nonnull
  public static <T> Class<? extends T>[] getComposedOf(@Nonnull Class<? extends T> item) {
    ComposedOf annotation = item.getAnnotation(ComposedOf.class);

    if (annotation != null) {
      if (annotation.value() != null) {
        return (Class<? extends T>[]) annotation.value();
      }
    }

    return (Class<? extends T>[]) EMPTY_COMPOSED_OF;
  }

  @Nonnull
  public static String getName(@Nonnull Class<? extends Item> item) {
    Name annotation = item.getAnnotation(Name.class);

    if (annotation != null) {
      if (annotation.value() != null) {
        return annotation.value();
      }
    }

    return item.getSimpleName();
  }

  @CheckForNull
  public static String getDescription(@Nonnull Class<? extends Item> item) {
    Description annotation = item.getAnnotation(Description.class);

    if (annotation != null) {
      if (annotation.value() != null) {
        return annotation.value();
      }
    }

    return null;
  }

  @Nonnull
  public static Class<? extends OnlyForType> getOnlyForType(@Nonnull Class<? extends Item> item) {
    OnlyFor ignore = item.getAnnotation(OnlyFor.class);

    if (ignore != null) {
      return ignore.value();
    } else {
      return Default.class;
    }
  }
}
