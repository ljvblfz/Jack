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

import com.android.sched.util.log.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.annotation.Nonnull;

/**
 * Implementation of {@link ReflectionManager} that uses a list of ReflectionManagers, and try to
 * fulfill requests by trying each of them until one succeeds.
 */
public class CompositeReflectionManager extends CommonReflectionManager
    implements ReflectionManager {
  @Nonnull
  private final List<ReflectionManager> reflectionManagers;

  public CompositeReflectionManager(@Nonnull List<ReflectionManager> reflectionManagers) {
    this.reflectionManagers = reflectionManagers;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.android.sched.reflections.ReflectionManager#getSubTypesOf(java.lang.Class)
   */
  @Override
  @Nonnull
  public <T> Set<Class<? extends T>> getSubTypesOf(@Nonnull Class<T> cls) {
    Set<Class<? extends T>> result = null;
    for (int i = 0; i < reflectionManagers.size(); i++) {
      try {
        result = reflectionManagers.get(i).getSubTypesOf(cls);
        break;
      } catch (ReflectionException e) {
        // try next manager
      }
    }

    if (result == null) {
      LoggerFactory.getLogger().log(Level.SEVERE, "Failed to getSubtypes of {0}", cls.getName());
      throw new ReflectionException("Failed to getSubtypes of " + cls.getName());
    }

    return result;
  }

}
