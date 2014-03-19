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
import com.android.sched.item.Description;
import com.android.sched.item.Items;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Schedulable;

import javax.annotation.Nonnull;

/**
 * Represents a {@link Schedulable} with all annotations and signatures extracted.
 */
public abstract class ManagedSchedulable {
  @Nonnull
  private final Class<? extends Schedulable> schedulable;

  // @Name
  @Nonnull
  private final String name;

  // @Description
  @Nonnull
  private final String description;

  // @Synchronized
  private boolean isSynchronized = false;

  /**
   * Creates a new instance of {@link ManagedSchedulable} from a {@link Schedulable}.
   *
   * @throws SchedulableNotConformException if the structure of the {@code Schedulable} is not
   * valid.
   */
  protected ManagedSchedulable (@Nonnull Class<? extends Schedulable> schedulable)
      throws SchedulableNotConformException {
    this.schedulable = schedulable;

    name = Items.getName(schedulable);

    // FINDBUGS
    String description = Items.getDescription(schedulable);
    if (description == null) {
      throw new SchedulableNotConformException("Schedulable '" + schedulable.getCanonicalName()
          + "' must have a @" + Description.class.getSimpleName());
    }
    this.description = description;

    extractSynchronized(schedulable);
  }

  @Nonnull
  public <T> Class<? extends Schedulable> getSchedulable() {
    return this.schedulable;
  }

  public abstract boolean isVisitor();

  public abstract boolean isRunnable();

  @Nonnull
  public String getName() {
    return name;
  }

  /**
   * @return if the schedulable is synchronized
   */
  public boolean isSynchronized() {
    return isSynchronized;
  }

  /**
   * Returns the type of data this is applied to.
   */
  @Nonnull
  public abstract Class<? extends Component> getRunOn();

  @Nonnull
  public String getDescription() {
    return description;
  }

  @Nonnull
  @Override
  public abstract String toString();


  private void extractSynchronized(
      @Nonnull Class<? extends Schedulable> cls) {
    Synchronized sync = cls.getAnnotation(Synchronized.class);

    isSynchronized = sync != null;
  }
}
