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

import com.android.sched.schedulable.Schedulable;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * An unordered set of {@link Schedulable} classes.
 *
 * <p>
 * This is the structure from which you will create a {@code Request}, which in the end
 * will generate the {@code Plan}.
 */
public class SchedulableSet {
  @Nonnull
  private final SchedulableManager schedulableManager;

  @Nonnull
  private final Set<ManagedSchedulable> scheds =
      new TreeSet<ManagedSchedulable>(new SchedulableComparator());

  SchedulableSet(@Nonnull SchedulableManager manager) {
    schedulableManager = manager;
  }

  /**
   * Returns whether this {@code SchedulableSet} contains the given {@link Schedulable} class.
   *
   * @param sched the {@code Schedulable} class
   * @return true if it is contained, false otherwise
   */
  public boolean contains(@Nonnull Class<? extends Schedulable> sched) {
    for (ManagedSchedulable elt : scheds) {
      if (elt.getSchedulable() == sched) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns whether this {@code SchedulableSet} contains all the elements of the given
   * {@code SchedulableSet}.
   *
   * @param set the {@code SchedulableSet}
   * @return true if all of them are contained, false otherwise
   */
  public boolean containsAll(@Nonnull SchedulableSet set) {
    for (ManagedSchedulable elt : set.scheds) {
      if (!contains(elt.getSchedulable())) {
        return false;
      }
    }

    return true;
  }

  /**
   * Adds to this {@code SchedulableSet} the given {@link Schedulable} class.
   *
   * @param sched the {@code Schedulable} class
   */
  public void add(@Nonnull Class<? extends Schedulable> sched) {
    scheds.add(schedulableManager.getManagedSchedulable(sched));
  }

  /**
   * Adds to this {@code SchedulableSet} all the elements of the given {@code SchedulableSet}.
   *
   * @param set the {@code SchedulableSet}
   */
  public void addAll(@Nonnull SchedulableSet set) {
    scheds.addAll(set.scheds);
  }

  protected void add(@Nonnull ManagedSchedulable sched) {
    scheds.add(sched);
  }

  /**
   * Removes the given {@link Schedulable} class from this {@code SchedulableSet}.
   *
   * @param sched the {@code Schedulable} class
   */
  public void remove(@Nonnull Class<? extends Schedulable> sched) {
    for (ManagedSchedulable elt : scheds) {
      if (elt.getSchedulable() == sched) {
        scheds.remove(elt);
        return;
      }
    }
  }

  /**
   * Removes all the elements of the given {@code SchedulableSet} from this {@code SchedulableSet}.
   *
   * @param set the {@code SchedulableSet}
   */
  public void removeAll(@Nonnull SchedulableSet set) {
    for (ManagedSchedulable elt : set.scheds) {
      remove(elt.getSchedulable());
    }
  }

  /**
   * Returns the number of {@link Schedulable}s in this {@code SchedulableSet}.
   */
  @Nonnegative
  public int getSize() {
    return scheds.size();
  }

  @Nonnull
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    boolean first = true;

    sb.append('[');
    for (ManagedSchedulable sched : scheds) {
      if (!first) {
        sb.append(", ");
      } else {
        first = false;
      }

      sb.append(sched.getName());
    }
    sb.append(']');

    return new String(sb);
  }

  @Nonnull
  protected Set<ManagedSchedulable> getAll() {
    return scheds;
  }

  private static class SchedulableComparator
      implements Comparator<ManagedSchedulable>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(@CheckForNull ManagedSchedulable o1, @CheckForNull ManagedSchedulable o2) {
      assert o1 != null;
      assert o2 != null;

      return o1.getName().compareTo(o2.getName());
    }
  }
}
