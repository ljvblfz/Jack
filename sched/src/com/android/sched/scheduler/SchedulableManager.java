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

import com.android.sched.item.Component;
import com.android.sched.schedulable.AdapterSchedulable;
import com.android.sched.schedulable.ProcessorSchedulable;
import com.android.sched.schedulable.Schedulable;
import com.android.sched.util.codec.ImplementationSelector;
import com.android.sched.util.codec.VariableName;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.PropertyId;

import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Provides utility methods to access {@link Schedulable}s.
 */
@HasKeyId
@VariableName("algo")
public abstract class SchedulableManager {
  @Nonnull
  private static final PropertyId<SchedulableManager> SCHEDULABLE_MANAGER = PropertyId.create(
      "sched.schedulable", "Define how schedulables are discovered",
      new ImplementationSelector<SchedulableManager>(SchedulableManager.class))
      .addDefaultValue("reflections");

  @Nonnull
  public static SchedulableManager getSchedulableManager () {
    return ThreadConfig.get(SCHEDULABLE_MANAGER);
  }

  @CheckForNull
  protected Map<Class<? extends Schedulable>, ManagedSchedulable> schedulableByClass;

  protected abstract void scan();

  protected ManagedSchedulable register(@Nonnull Class<? extends Schedulable> sched)
      throws SchedulableNotConformException {
    ManagedSchedulable is = null;

    if (ProcessorSchedulable.class.isAssignableFrom(sched)) {
      @SuppressWarnings("unchecked")
      ManagedRunnable ir =
          new ManagedRunnable((Class<? extends ProcessorSchedulable<? extends Component>>) sched);
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
    if (schedulableByClass == null) {
      scan();
      assert schedulableByClass != null;
    }

    return schedulableByClass.get(schedulable);
  }

  @Nonnull
  public SchedulableSet getAllSchedulable() {
    SchedulableSet scheds = new SchedulableSet();

    if (schedulableByClass == null) {
      scan();
      assert schedulableByClass != null;
    }

    for (ManagedSchedulable sched : schedulableByClass.values()) {
      scheds.add(sched);
    }

    return scheds;
  }
}
