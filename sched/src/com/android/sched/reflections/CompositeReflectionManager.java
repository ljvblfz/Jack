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

import com.android.sched.util.location.Location;
import com.android.sched.util.location.NoLocation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Implementation of {@link ReflectionManager} that uses a list of ReflectionManagers, and try to
 * fulfill requests by trying each of them until one succeeds.
 */
public class CompositeReflectionManager extends CommonReflectionManager
    implements ReflectionManager {
  @Nonnull
  private final List<ReflectionManager> reflectionManagers;

  public CompositeReflectionManager() {
    this.reflectionManagers = new ArrayList<ReflectionManager>();
  }

  public CompositeReflectionManager(@Nonnull ReflectionManager reflectionManager) {
    this.reflectionManagers = new ArrayList<ReflectionManager>();
    this.reflectionManagers.add(reflectionManager);
  }

  public CompositeReflectionManager(@Nonnull ReflectionManager[] reflectionManagers) {
    this.reflectionManagers = Arrays.asList(reflectionManagers);
  }

  public CompositeReflectionManager(@Nonnull List<ReflectionManager> reflectionManagers) {
    this.reflectionManagers = reflectionManagers;
  }

  @Nonnull
  public synchronized CompositeReflectionManager addReflectionManager(
      @Nonnull ReflectionManager reflectionManager) {
    reflectionManagers.add(reflectionManager);

    return this;
  }

  @Override
  @Nonnull
  public synchronized <T> Set<Class<? extends T>> getSubTypesOf(@Nonnull Class<T> cls) {
    Set<Class<? extends T>> set = new HashSet<Class<? extends T>>();

    for (ReflectionManager reflectionManager : reflectionManagers) {
      set.addAll(reflectionManager.getSubTypesOf(cls));
    }

    return set;
  }

  @Override
  @Nonnull
  public synchronized <T> Set<ClassWithLocation<? extends T>> getSubTypesOfWithLocation(
      @Nonnull Class<T> cls) {
    Set<ClassWithLocation<? extends T>> set = new HashSet<ClassWithLocation<? extends T>>();

    for (ReflectionManager reflectionManager : reflectionManagers) {
      set.addAll(reflectionManager.getSubTypesOfWithLocation(cls));
    }

    return set;
  }

  @Override
  @Nonnull
  public <T extends Annotation> Set<Class<?>> getAnnotatedBy(@Nonnull Class<T> cls) {
    Set<Class<?>> set = new HashSet<Class<?>>();

    for (ReflectionManager reflectionManager : reflectionManagers) {
      set.addAll(reflectionManager.getAnnotatedBy(cls));
    }

    return set;
  }

  @Override
  @Nonnull
  public <T extends Annotation> Set<ClassWithLocation<?>> getAnnotatedByWithLocation(
      @Nonnull Class<T> cls) {
    Set<ClassWithLocation<?>> set = new HashSet<ClassWithLocation<?>>();

    for (ReflectionManager reflectionManager : reflectionManagers) {
      set.addAll(reflectionManager.getAnnotatedByWithLocation(cls));
    }

    return set;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return NoLocation.getInstance();
  }
}
