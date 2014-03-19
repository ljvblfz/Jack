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

package com.android.sched.reflections;

import com.android.sched.SchedProperties;
import com.android.sched.item.Items;
import com.android.sched.item.onlyfor.Default;
import com.android.sched.item.onlyfor.OnlyForType;
import com.android.sched.schedulable.Schedulable;
import com.android.sched.scheduler.ManagedSchedulable;
import com.android.sched.scheduler.SchedulableManager;
import com.android.sched.scheduler.SchedulableNotConformException;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.sched.ManagedDataListener;
import com.android.sched.util.sched.ManagedDataListenerFactory;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Implementation of {@link SchedulableManager} that uses the library {@code org.reflections} to
 * find all {@link Schedulable} classes in the classpath by reflection.
 */
@ImplementationName(iface = SchedulableManager.class, name = "reflections")
public class SchedulableManagerReflections extends SchedulableManager {
  @Nonnull
  private final Logger  logger = LoggerFactory.getLogger();
  @Nonnull
  private final Class<? extends OnlyForType> onlyFor = ThreadConfig.get(SchedProperties.ONLY_FOR);
  @Nonnull
  private final ManagedDataListener listener = ManagedDataListenerFactory.getManagedDataListener();
  private final boolean failedStop = ThreadConfig.get(SchedProperties.FAILED_STOP).booleanValue();

  @Override
  protected void scan () {
    if (schedulableByClass != null) {
      throw new AssertionError("SchedulableManagerReflections.scan() can not be called twice");
    }

    schedulableByClass = new HashMap<Class<? extends Schedulable>, ManagedSchedulable>();

    ReflectionManager reflectionManager = ReflectionFactory.getManager();
    for (Class<? extends Schedulable> sched :
      reflectionManager.getSubTypesOf(Schedulable.class)) {
      Class<? extends OnlyForType> onlyFor = Items.getOnlyForType(sched);

      if (onlyFor == Default.class ||
          onlyFor == this.onlyFor) {
        if (!sched.isInterface() &&
            !Modifier.isAbstract(sched.getModifiers())) {
          try {
            ManagedSchedulable is = register(sched);
            listener.notifyNewManagedSchedulable(is);

            logger.log(Level.INFO, "Register schedulable ''{0}'' ({1})",
                new Object[] {is.getName(), sched.getCanonicalName()});
          } catch (SchedulableNotConformException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            if (failedStop) {
              throw e;
            }
          }
        }
      } else {
        logger.log(Level.INFO, "Schedulable ''{0}'' ({1}) is ignored because only for {2}",
            new Object[] {Items.getName(sched), sched.getCanonicalName(), onlyFor.getSimpleName()});
      }
    }

    listener.notifyNoMoreManagedSchedulable();
  }
}
