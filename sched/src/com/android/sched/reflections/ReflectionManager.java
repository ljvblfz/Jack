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

package com.android.sched.reflections;

import com.android.sched.util.location.HasLocation;
import com.android.sched.util.location.Location;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * This class provides reflection utilities
 */
public interface ReflectionManager extends HasLocation {
  /**
   * A {@link Class} with a {@link Location}.
   */
  public class ClassWithLocation<T> {
    @Nonnull
    private final Class<T> cls;
    @Nonnull
    private final Location location;

    protected ClassWithLocation(@Nonnull Class<T> cls, @Nonnull Location location) {
      this.cls = cls;
      this.location = location;
    }

    @Nonnull
    public Class<T> getClazz() {
      return cls;
    }

    @Nonnull
    public Location getLocation() {
      return location;
    }

    @Override
    public final boolean equals(Object obj) {
      return obj instanceof ClassWithLocation
          && ((ClassWithLocation<?>) obj).cls.equals(cls);
    }

    @Override
    public int hashCode() {
      return cls.hashCode();
    }
  }

  /**
   * Returns a {@code Set} with all the sub-types found.
   */
  @Nonnull
  public <T> Set<Class<? extends T>> getSubTypesOf(@Nonnull Class<T> cls);

  /**
   * Returns a {@code Set} with all the sub-types found with their locations.
   */
  @Nonnull
  public <T> Set<ClassWithLocation<? extends T>> getSubTypesOfWithLocation(@Nonnull Class<T> cls);


  /**
   * Returns a {@code Set} with all the types annotated.
   */
  @Nonnull
  public <T extends Annotation> Set<Class<?>> getAnnotatedBy(@Nonnull Class<T> cls);

  /**
   * Returns a {@code Set} with all the types annotated with their locations.
   */
  @Nonnull
  public <T extends Annotation> Set<ClassWithLocation<?>> getAnnotatedByWithLocation(
      @Nonnull Class<T> cls);

  /**
   * Returns a {@code Set} with all the sup-types found.
   */
  @Nonnull
  public Set<Class<?>> getSuperTypesOf(@Nonnull Class<?> cls);
}
