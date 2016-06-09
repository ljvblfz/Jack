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
import com.android.sched.util.codec.ImplementationName;

import javax.annotation.Nonnull;

/**
 * Dummy listener for {@code ManagedDataListener}.
 */
@ImplementationName(iface = ManagedDataListener.class, name = "none")
public class DummyManagedDataListener implements ManagedDataListener {
  @Override
  public void notifyNewManagedItem(@Nonnull ManagedItem item) {
  }

  @Override
  public void notifyNoMoreManagedItem(@Nonnull Class<? extends Item> type) {
  }

  @Override
  public void notifyNewManagedSchedulable(@Nonnull ManagedSchedulable schedulable) {
  }

  @Override
  public void notifyNoMoreManagedSchedulable() {
  }

  @Override
  public void notifyNewItemManager(@Nonnull AbstractItemManager itemManager) {
  }

  @Override
  public void notifyNoMoreItemManager() {
  }
}
