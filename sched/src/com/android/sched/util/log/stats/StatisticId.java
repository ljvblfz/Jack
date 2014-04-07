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

package com.android.sched.util.log.stats;

import com.android.sched.util.config.ReflectFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

/**
 * Identification of a Statistic.
 *
 * @param <T> Type of the statistic.
 */
public class StatisticId<T extends Statistic> {
  @Nonnull
  private static Map<Class<? extends Statistic>, Statistic> dummies =
      new ConcurrentHashMap<Class<? extends Statistic>, Statistic>();
  @Nonnull
  private static Map<Class<? extends Statistic>, Class<? extends Statistic>> regulars =
      new ConcurrentHashMap<Class<? extends Statistic>, Class<? extends Statistic>>();

  @Nonnull
  private final String name;

  @Nonnull
  private final String description;

  @Nonnull
  private final ReflectFactory<T> regularFactory;

  @Nonnull
  private final ReflectFactory<T>  dummyFactory;

  public StatisticId(@Nonnull String name, @Nonnull String description,
      @Nonnull Class<? extends T> regularClass, @Nonnull Class<? extends T> dummyClass) {
    this.name = name;
    this.description = description;

    regularFactory = new ReflectFactory<T>(regularClass, false, StatisticId.class);
    dummyFactory   = new ReflectFactory<T>(dummyClass,   false, StatisticId.class);

    // Followings are deprecated, to be removed

    if (!dummies.containsKey(dummyClass)) {
      dummies.put(dummyClass, newDummyInstance());
      regulars.put(dummyClass, regularClass);
    }
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public String getDescription() {
    return description;
  }

  @Nonnull
  public T newInstance() {
    return regularFactory.create(this);
  }

  @Nonnull
  public T newDummyInstance() {
    return dummyFactory.create(this);
  }

  @Nonnull
  @Deprecated
  public static synchronized Collection<? extends Statistic> getDummies() {
    return dummies.values();
  }

  @Nonnull
  @Deprecated
  public static Class<? extends Statistic> getRegularClass(
      @Nonnull Class<? extends Statistic> dummyClass) {
    return regulars.get(dummyClass);
  }

  @Override
  @Nonnull
  public String toString() {
    return name;
  }
}
