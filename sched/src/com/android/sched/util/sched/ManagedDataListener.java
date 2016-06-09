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

package com.android.sched.util.sched;

import com.android.sched.item.AbstractItemManager;
import com.android.sched.item.Item;
import com.android.sched.item.ManagedItem;
import com.android.sched.scheduler.ManagedSchedulable;
import com.android.sched.util.codec.VariableName;

import javax.annotation.Nonnull;

/**
 * Should be implemented by an object that wants to receive notifications about
 * {@code Schedulable}s, {@code Tag}s, {@code Marker}s, {@code Component}s, {@code Feature}s and
 * {@code Production}s in the system.
 */
@VariableName("listener")
public interface ManagedDataListener {
  public void notifyNewItemManager(@Nonnull AbstractItemManager itemManager);
  public void notifyNoMoreItemManager();

  public void notifyNewManagedItem(@Nonnull ManagedItem item);
  public void notifyNoMoreManagedItem(@Nonnull Class<? extends Item> type);

  public void notifyNewManagedSchedulable(@Nonnull ManagedSchedulable schedulable);
  public void notifyNoMoreManagedSchedulable();
}
