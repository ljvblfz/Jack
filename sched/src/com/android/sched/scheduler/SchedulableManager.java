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

package com.android.sched.scheduler;

import com.android.sched.SchedProperties;
import com.android.sched.item.Component;
import com.android.sched.item.Items;
import com.android.sched.item.onlyfor.Default;
import com.android.sched.item.onlyfor.OnlyForType;
import com.android.sched.reflections.ReflectionManager;
import com.android.sched.schedulable.AdapterSchedulable;
import com.android.sched.schedulable.ProcessorSchedulable;
import com.android.sched.schedulable.Schedulable;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.sched.ManagedDataListener;
import com.android.sched.util.sched.ManagedDataListenerFactory;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Provides utility methods to access {@link Schedulable}s.
 */
public class SchedulableManager {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();
  @Nonnull
  private final Class<? extends OnlyForType> onlyFor = ThreadConfig.get(SchedProperties.ONLY_FOR);
  @Nonnull
  private final ManagedDataListener listener = ManagedDataListenerFactory.getManagedDataListener();
  private final boolean failedStop = ThreadConfig.get(SchedProperties.FAILED_STOP).booleanValue();

  @Nonnull
  public static SchedulableManager getSchedulableManager(@Nonnull Scheduler scheduler,
      @Nonnull ReflectionManager reflectionManager) {
    return new SchedulableManager(scheduler, reflectionManager);
  }

  @Nonnull
  protected Map<Class<? extends Schedulable>, ManagedSchedulable> schedulableByClass;

  public SchedulableManager(@Nonnull Scheduler scheduler,
      @Nonnull ReflectionManager reflectionManager) {
    schedulableByClass = new HashMap<Class<? extends Schedulable>, ManagedSchedulable>();

    for (Class<? extends Schedulable> sched : reflectionManager.getSubTypesOf(Schedulable.class)) {
      Class<? extends OnlyForType> onlyFor = Items.getOnlyForType(sched);

      if (onlyFor == Default.class || onlyFor == this.onlyFor) {
        if (!sched.isInterface() && !Modifier.isAbstract(sched.getModifiers())) {
          try {
            ManagedSchedulable is = register(scheduler, sched);
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

  protected ManagedSchedulable register(@Nonnull Scheduler scheduler,
      @Nonnull Class<? extends Schedulable> sched) throws SchedulableNotConformException {
    ManagedSchedulable is = null;

    if (ProcessorSchedulable.class.isAssignableFrom(sched)) {
      @SuppressWarnings("unchecked")
      ManagedRunnable ir = new ManagedRunnable(scheduler,
          (Class<? extends ProcessorSchedulable<? extends Component>>) sched);
      is = ir;
    } else if (AdapterSchedulable.class.isAssignableFrom(sched)) {
      @SuppressWarnings("unchecked")
      ManagedVisitor iv = new ManagedVisitor(
          (Class<? extends AdapterSchedulable<? extends Component, ? extends Component>>) sched);
      is = iv;
    } else {
      throw new AssertionError();
    }

    assert schedulableByClass != null;
    for (ManagedSchedulable elt : schedulableByClass.values()) {
      if (is.getName().equals(elt.getName())) {
        throw new SchedulableNotConformException("Schedulable '" + sched.getCanonicalName()
            + "' and '" + elt.getSchedulable().getCanonicalName() + "' have the same name '"
            + elt.getName() + "'");
      }
    }

    schedulableByClass.put(sched, is);

    return is;
  }

  @CheckForNull
  public ManagedSchedulable getManagedSchedulable(
      @Nonnull Class<? extends Schedulable> schedulable) {
    return schedulableByClass.get(schedulable);
  }

  @Nonnull
  SchedulableSet getAllSchedulable(@Nonnull SchedulableManager manager) {
    SchedulableSet scheds = new SchedulableSet(manager);
    for (ManagedSchedulable sched : schedulableByClass.values()) {
      scheds.add(sched);
    }

    return scheds;
  }
}
