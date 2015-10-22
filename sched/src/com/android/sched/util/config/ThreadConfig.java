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

import com.android.sched.util.config.id.ObjectId;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;
import com.android.sched.util.log.tracer.TracerEventType;

import javax.annotation.Nonnull;

/**
 * This class handles a configuration that is local to current thread and its children.
 */
public class ThreadConfig {
  @Nonnull
  public static final StatisticId<Counter> TLS_READ = new StatisticId<Counter>(
      "sched.config.tls.read", "Reading TLS to get current config",
      CounterImpl.class, Counter.class);

  @Nonnull
  private static final Config unitializedConfig = new UninitializedConfig();

  @Nonnull
  protected static final InheritableThreadLocal<Config> threadLocalConfig =
      new InheritableThreadLocal<Config>() {
        @Override
        protected Config initialValue() {
          return unitializedConfig;
        }
      };

  @Nonnull
  public static <T> T get(@Nonnull PropertyId<T> propertyId) {
    Config config = threadLocalConfig.get();
    updateStatistic(config);

    return config.get(propertyId);
  }

  @Nonnull
  public static <T> T get(@Nonnull ObjectId<T> objectId) {
    Config config = threadLocalConfig.get();
    updateStatistic(config);

    return config.get(objectId);
  }

  private static void updateStatistic(@Nonnull Config config) {
    Tracer tracer = ((InternalConfig) config).<Tracer> getObjectIfAny(TracerFactory.TRACER);

    if (tracer != null && tracer.getCurrentEventType() != TracerEventType.NOEVENT) {
      tracer.getStatistic(TLS_READ).incValue();
    }
  }

  @Nonnull
  public static Config getConfig() {
    return threadLocalConfig.get();
  }

  public static void setConfig(@Nonnull Config config) {
    assert config != null;

    threadLocalConfig.set(config);
  }

  public static void unsetConfig() {
    threadLocalConfig.remove();
  }
}
