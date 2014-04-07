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

import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.config.id.KeyId;
import com.android.sched.util.config.id.ObjectId;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.log.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Debug implementation of a fully built {@code Config}. This class check recursivity.
 */
class ConfigDebug extends ConfigImpl {
  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final Map<KeyId<?, ?>, String> dropCauses;

  @Nonnull
  private final ThreadLocal<Stack<KeyId<?, ?>>> keyIds = new ThreadLocal<Stack<KeyId<?, ?>>>() {
      @Override
      protected Stack<KeyId<?, ?>> initialValue() {
        return new Stack<KeyId<?, ?>>();
      }
  };

  ConfigDebug(@Nonnull CodecContext context,
      @Nonnull Map<PropertyId<?>, PropertyId<?>.Value> values,
      @Nonnull Map<KeyId<?, ?>, Object> instances,
      @Nonnull Map<KeyId<?, ?>, String> dropCauses) {
    super(context, values, instances);

    this.dropCauses = new HashMap<KeyId<?, ?>, String>(dropCauses);
  }

  @Override
  @Nonnull
  public synchronized <T> T get(@Nonnull PropertyId<T> propertyId) {
    return get((KeyId<T, ?>) propertyId);
  }

  @Override
  @Nonnull
  public synchronized <T> T get(@Nonnull ObjectId<T> objectId) {
    return get((KeyId<T, ?>) objectId);
  }

  @Override
  @Nonnull
  public <T, S> T get(@Nonnull KeyId<T, S> keyId) {
    Stack<KeyId<?, ?>> localKeyIds = keyIds.get();

    if (localKeyIds.contains(keyId)) {
      logger.log(Level.SEVERE, "Recursivity during getting configuration:");

      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
      localKeyIds.push(keyId);

      // 1 to skip getStackTrace()
      StackTraceElement marker = stackTrace[1];
      for (int i = 1; i < stackTrace.length; i++) {
        StackTraceElement trace = stackTrace[i];

        if (trace.getClassName().equals(marker.getClassName())
            && trace.getMethodName().equals(marker.getMethodName())
            && trace.getFileName().equals(marker.getFileName())) {
          logger.log(Level.SEVERE, "  get ''{0}'' at:", localKeyIds.pop().getName());
        }

        logger.log(Level.SEVERE, "    {0}", trace);
      }

      throw new AssertionError(
          "Recursivity during creation of '" + keyId.getName() + "' (see log)");
    }

    localKeyIds.push(keyId);
    try {
      try {
        return super.get(keyId);
      } catch (ConfigurationError e) {
        String cause = dropCauses.get(keyId);
        if (cause != null) {
          StringBuilder sb = new StringBuilder();

          sb.append(e.getMessage());
          sb.append(" (dropped because ");
          sb.append(dropCauses.get(keyId));
          sb.append(')');

          throw new ConfigurationError(sb.toString(), e);
        } else {
          throw e;
        }
      }
    } finally {
      localKeyIds.pop();
    }
  }
}
