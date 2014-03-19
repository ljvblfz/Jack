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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * An unordered set of {@link ManagedRunnable} classes.
 */
public class RunnerSet implements Iterable<ManagedRunnable> {
  @Nonnull
  private final Set<ManagedRunnable> runners =
      new TreeSet<ManagedRunnable>(new SchedulableComparator());

  RunnerSet() {
  }

  RunnerSet(@Nonnull RunnerSet initial) {
    runners.addAll(initial.runners);
  }

  public void addAll(@Nonnull RunnerSet set) {
    runners.addAll(set.runners);
  }

  protected void add(@Nonnull ManagedRunnable sched) {
    runners.add(sched);
  }

  @Nonnegative
  public int getSize() {
    return runners.size();
  }

  @Nonnull
  public Set<ManagedRunnable> getAll() {
    return runners;
  }

  @Nonnull
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    boolean first = true;

    sb.append('[');
    for (ManagedRunnable sched : runners) {
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

  private static class SchedulableComparator
      implements Comparator<ManagedSchedulable>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(@CheckForNull ManagedSchedulable o1, @CheckForNull  ManagedSchedulable o2) {
      assert o1 != null;
      assert o2 != null;

      return o1.getName().compareTo(o2.getName());
    }
  }

  public void removeAll(@Nonnull List<ManagedRunnable> list) {
    runners.removeAll(list);
  }

  public void remove(@Nonnull ManagedRunnable runner) {
    runners.remove(runner);
  }

  @Nonnull
  @Override
  public Iterator<ManagedRunnable> iterator() {
    return runners.iterator();
  }
}
