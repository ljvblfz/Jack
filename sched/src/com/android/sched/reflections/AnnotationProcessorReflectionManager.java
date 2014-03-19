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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
  private static final SchedDiscover data = new SchedDiscover();

  static {
    try {
      Enumeration<URL> enumeration = AnnotationProcessorReflectionManager.class.getClassLoader()
          .getResources(data.getResourceName());

      while (enumeration.hasMoreElements()) {
        URL url = enumeration.nextElement();

        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        try {

          data.readResource(reader);

        } finally {
          reader.close();
        }
      }
    } catch (IOException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  @Nonnull
  public <T> Set<Class<? extends T>> getSubTypesOf(@Nonnull Class<T> cls) {
    Set<Class<? extends T>> set = new HashSet<Class<? extends T>>();

    for (SchedData element : data.get(cls.getCanonicalName())) {
      try {
        set.add(((Class<? extends T>) Class.forName(element.getName())));
      } catch (ClassNotFoundException e) {
        throw new AssertionError(e);
      }
    }

    return set;
  }
}
