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

package com.android.sched.util;

import com.google.common.collect.Lists;

import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.ObjectId;
import com.android.sched.util.log.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * A class to add, remove and execute hooks. Each instance of this class is registered as a runtime
 * shutdown hook ({@link Runtime#addShutdownHook}) but can be managed by the user in order to
 * trigger hooks at another level than {@link Runtime#addShutdownHook(Thread)}.
 *
 * Also declare a general purpose thread local {@code Config} shutdown hooks.
 */
@HasKeyId
public class RunnableHooks {
  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();
  @Nonnull
  private static final ObjectId<RunnableHooks> SHUTDOWN_HOOKS =
      new ObjectId<RunnableHooks>("sched.internal.shutdown", RunnableHooks.class);

  @Nonnull
  private final List<Runnable> hooks = new ArrayList<Runnable>();

  public synchronized void addHook(@Nonnull Runnable hook) {
    assert !hooks.contains(hook);

    hooks.add(hook);
  }

  public synchronized void removeHook(@Nonnull Runnable hook) {
    assert hooks.contains(hook);

    hooks.remove(hook);
  }

  public synchronized void runHooks() {
    Throwable current = null;

    for (Runnable hook : Lists.reverse(hooks)) {
      try {
        hook.run();
      } catch (Error | RuntimeException e) {
        logger.log(Level.SEVERE, "Uncaught exception during RunnableHook", e);
        if (current == null) {
          // Throw only the first one
          current = e;
        }
      }
    }

    hooks.clear();
    if (current != null) {
      if (current instanceof Error) {
        throw (Error) current;
      } else if (current instanceof RuntimeException)  {
        throw (RuntimeException) current;
      } else {
        throw new AssertionError(current);
      }
    }
  }

  /**
   * @return general purpose thread local {@code Config} shutdown hooks.
   */
  @Nonnull
  public static RunnableHooks getShutdownHooks() {
    return ThreadConfig.get(SHUTDOWN_HOOKS);
  }
}
