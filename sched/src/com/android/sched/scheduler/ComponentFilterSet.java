/*
 * Copyright (C) 2016 The Android Open Source Project
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
import com.android.sched.item.ComposedOf;
import com.android.sched.item.AbstractItemManager;
import com.android.sched.item.ItemSet;
import com.android.sched.schedulable.ComponentFilter;

import javax.annotation.Nonnull;

/**
 * A set of {@link ComponentFilter} classes.
 * <p>
 * When adding an {@code ComponentFilter} composed of several {@code ComponentFilter}s (using the
 * annotation {@link ComposedOf}), all the {@code ComponentFilter}s are added to the
 * {@code ComponentFilterSet}.
 */
public class ComponentFilterSet extends ItemSet<ComponentFilter<? extends Component>> {
  public ComponentFilterSet(@Nonnull ComponentFilterSet initial) {
    super(initial);
  }

  public ComponentFilterSet(@Nonnull AbstractItemManager manager) {
    super(manager);
  }

  @Nonnull
  @Override
  public ComponentFilterSet clone() {
    return (ComponentFilterSet) super.clone();
  }

  @Nonnull
  public ComponentFilterSet getIntersection(@Nonnull ComponentFilterSet set) {
    ComponentFilterSet inter = new ComponentFilterSet(set);
    computeIntersection(inter, set);

    return inter;
  }
}
