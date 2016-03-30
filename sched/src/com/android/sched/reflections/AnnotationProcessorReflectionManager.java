/*
 * Copyright (C) 2014 The Android Open Source Project
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

import com.android.sched.build.SchedDiscover;
import com.android.sched.build.SchedDiscover.SchedData;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.NoLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Implementation of {@link ReflectionManager} that uses resources from
 * the schedlib annotation processor.
 */
public class AnnotationProcessorReflectionManager extends CommonReflectionManager
    implements ReflectionManager {
  @Nonnull
  private static final SchedDiscover thisClassLoaderData;
  @Nonnull
  private final SchedDiscover data;
  @Nonnull
  private final ClassLoader classLoader;
  @Nonnull
  private final Location location;

  static {
    thisClassLoaderData =
        getSchedDiscovery(AnnotationProcessorReflectionManager.class.getClassLoader());
  }

  @Nonnull
  private static SchedDiscover getSchedDiscovery(@Nonnull ClassLoader classLoader) {
    try {
      SchedDiscover data = new SchedDiscover();

      Enumeration<URL> enumeration = classLoader.getResources(data.getResourceName());
      while (enumeration.hasMoreElements()) {
        URL url = enumeration.nextElement();
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        try {
          data.readResource(reader);
        } finally {
          reader.close();
        }
      }

      return data;
    } catch (IOException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public AnnotationProcessorReflectionManager() {
    this(NoLocation.getInstance());
  }

  public AnnotationProcessorReflectionManager(@Nonnull Location location) {
    this(AnnotationProcessorReflectionManager.class.getClassLoader(), location);
  }

  public AnnotationProcessorReflectionManager(@Nonnull ClassLoader classLoader) {
    this(classLoader, NoLocation.getInstance());
  }

  public AnnotationProcessorReflectionManager(@Nonnull ClassLoader classLoader,
      @Nonnull Location location) {
    this.classLoader = classLoader;
    if (classLoader.equals(AnnotationProcessorReflectionManager.class.getClassLoader())) {
      this.data = thisClassLoaderData;
    } else {
      this.data = getSchedDiscovery(classLoader);
    }
    this.location = location;
  }

  @SuppressWarnings("unchecked")
  @Override
  @Nonnull
  public <T> Set<Class<? extends T>> getSubTypesOf(@Nonnull Class<T> cls) {
    assert !cls.isAssignableFrom(Annotation.class);
    return (Set<Class<? extends T>>) (Object) get(cls);
  }

  @SuppressWarnings("unchecked")
  @Override
  @Nonnull
  public <T> Set<ClassWithLocation<? extends T>> getSubTypesOfWithLocation(@Nonnull Class<T> cls) {
    assert !cls.isAssignableFrom(Annotation.class);
    return (Set<ClassWithLocation<? extends T>>) (Object) getWithLocation(cls);
  }

  @Override
  @Nonnull
  public <T extends Annotation> Set<Class<?>> getAnnotatedBy(
      @Nonnull Class<T> cls) {
    return get(cls);
  }

  @Override
  @Nonnull
  public <T extends Annotation> Set<ClassWithLocation<?>> getAnnotatedByWithLocation(
      @Nonnull Class<T> cls) {
    return getWithLocation(cls);
  }

  @Nonnull
  private Set<Class<?>> get(@Nonnull Class<?> cls) {
    Set<Class<?>> set = new HashSet<Class<?>>();

    for (SchedData element : data.get(cls.getCanonicalName())) {
      try {
        set.add(Class.forName(element.getName(), false, classLoader));
      } catch (ClassNotFoundException e) {
        throw new AssertionError(e);
      }
    }

    return set;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Nonnull
  private Set<ClassWithLocation<?>> getWithLocation(@Nonnull Class<?> cls) {
    Set<ClassWithLocation<?>> set = new HashSet<ClassWithLocation<?>>();

    for (Class<?> c : get(cls)) {
      set.add(new ClassWithLocation(c, location));
    }

    return set;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return location;
  }
}
