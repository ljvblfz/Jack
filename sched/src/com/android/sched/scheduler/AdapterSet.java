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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * An unordered set of {@link ManagedVisitor} classes.
 */
public class AdapterSet implements Iterable<ManagedVisitor> {

  @Nonnull
  private final Set<ManagedVisitor> adapters =
      new TreeSet<ManagedVisitor>(new SchedulableComparator());

  AdapterSet() {
  }

  AdapterSet(@Nonnull AdapterSet initial) {
    adapters.addAll(initial.adapters);
  }

  void addAll(@Nonnull AdapterSet set) {
    for (ManagedVisitor ia : set.adapters) {
      add(ia);
    }
  }

  protected void add(@Nonnull ManagedVisitor sched) {
    adapters.add(sched);
  }

  @Nonnegative
  int getSize() {
    return adapters.size();
  }

  @Nonnull
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    boolean first = true;

    sb.append('[');
    for (ManagedVisitor sched : adapters) {
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
    public int compare(@CheckForNull ManagedSchedulable o1, @CheckForNull ManagedSchedulable o2) {
      assert o1 != null;
      assert o2 != null;

      return o1.getName().compareTo(o2.getName());
    }
  }

  @Nonnull
  @Override
  public Iterator<ManagedVisitor> iterator() {
    return adapters.iterator();
  }

  private static class Pair<T, S> {
    @Nonnull
    private final T first;
    @Nonnull
    private final S second;

    public Pair(@Nonnull T first, @Nonnull S second) {
      this.first = first;
      this.second = second;
    }

    @Override
    public final boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }

      if (!(obj instanceof Pair)) {
        return false;
      }

      Pair<?, ?> pair = (Pair<?, ?>) obj;
      return first.equals(pair.first) && second.equals(pair.second);
    }

    @Override
    public int hashCode() {
      return first.hashCode() ^ second.hashCode();
    }
  }

  @Nonnull
  private final Map<Pair<Class<? extends Component>, Class<? extends Component>>,
                    List<ManagedVisitor>> cache =
            new ConcurrentHashMap<Pair<Class<? extends Component>, Class<? extends Component>>,
                                  List<ManagedVisitor>>();

  @Nonnull
  public List<ManagedVisitor> getAdapter(
      @Nonnull Class<? extends Component> current, @Nonnull Class<? extends Component> after) {
    Pair<Class<? extends Component>, Class<? extends Component>> key =
        new Pair<Class<? extends Component>, Class<? extends Component>>(current, after);
    List<ManagedVisitor> list = cache.get(key);

    if (list == null) {
      Stack<ManagedVisitor> stack = new Stack<ManagedVisitor>();
      getAdapter(stack, current, after);
      list = new ArrayList<ManagedVisitor>(stack);
      cache.put(key, list);
    }

    return list;
  }

  private boolean getAdapter(@Nonnull Stack<ManagedVisitor> stack,
      @Nonnull Class<? extends Component> current, @Nonnull Class<? extends Component> after) {

    if (current == after) {
      return true;
    }
    for (ManagedVisitor adapter : adapters) {
      if (adapter.getRunOn() == current && adapter.getRunOnAfter() == after) {
        stack.push(adapter);
        return true;
      }
    }

    for (ManagedVisitor adapter : adapters) {
      if (adapter.getRunOn() == current) {
        stack.push(adapter);

        if (getAdapter(stack, adapter.getRunOnAfter(), after)) {
          return true;
        }

        stack.pop();
      }
    }

    return false;
  }


  public boolean containsAdapters(
      @Nonnull Class<? extends Component> current, @Nonnull Class<? extends Component> after) {
    if (current == after) {
      return true;
    }

    return !getAdapter(current, after).isEmpty();
  }
}
