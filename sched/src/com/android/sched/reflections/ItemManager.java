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
import com.android.sched.item.AbstractItemManager;
import com.android.sched.item.Item;
import com.android.sched.item.ItemSet;
import com.android.sched.item.Items;
import com.android.sched.item.ManagedItem;
import com.android.sched.item.onlyfor.Default;
import com.android.sched.item.onlyfor.OnlyForType;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.sched.ManagedDataListener;
import com.android.sched.util.sched.ManagedDataListenerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Implementation of {@link AbstractItemManager} that uses the library {@code org.reflections} to
 * access subtypes of the considered {@link Item} type by reflection.
 */
public class ItemManager extends AbstractItemManager {
  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final Class<? extends OnlyForType> onlyFor = ThreadConfig.get(SchedProperties.ONLY_FOR);
  @Nonnull
  private final ManagedDataListener listener = ManagedDataListenerFactory.getManagedDataListener();

  @Nonnull
  private final Class<? extends Item> type;

  public ItemManager(@Nonnull ReflectionManager reflectionManager,
      @Nonnull Class<? extends Item> type) {
    this.type = type;
    scan(reflectionManager);
  }

  @Nonnull
  @Override
  public Class<? extends Item> getType() {
    return type;
  }

  private void scan(@Nonnull ReflectionManager reflectionManager) {
    // Discover all items
    for (Class<? extends Item> item : reflectionManager.getSubTypesOf(type)) {

      if (!isToIgnore(item)) {
        ManagedItem ii = registerItem(item);
        logger.log(Level.INFO, "Register {0} ({1})", new Object[] {ii, item.getCanonicalName()});
      } else {
        logger.log(Level.INFO, "Item ''{0}'' ({1}) is ignored because only for {2}",
            new Object[] {Items.getName(item), item.getCanonicalName(), onlyFor.getSimpleName()});
      }
    }

    logger.log(Level.INFO, "Register {0} item(s) in {1} integer(s)",
        new Object[] {Integer.valueOf(getItemsCount()), Integer.valueOf(getIntegersCount())});

    for (ManagedItem item : map.values()) {
      Class<? extends Item> cls = item.getItem();

      for (Class<?> sup : reflectionManager.getSuperTypesOf(cls)) {
        ManagedItem managedSup = map.get(sup);
        if (managedSup != null) {
          managedSup.addComposedOf(item);
        }
      }
    }

    // Add missing @ComposedOf
    for (ManagedItem item : map.values()) {
      item.addComposedOf();
    }

    // Notify & log items
    for (ManagedItem item : map.values()) {
      listener.notifyNewManagedItem(item);

      if (logger.isLoggable(Level.FINER)) {
        ItemSet<Item> set = new ItemSet<Item>(this);
        set.add(item.getItem());
        logger.log(Level.FINER, "Item {0} is {1}", new Object[] {item, set});
      }
    }

    listener.notifyNoMoreManagedItem(type);
  }

  private boolean isToIgnore(Class<? extends Item> item) {
    Class<? extends OnlyForType> onlyFor = Items.getOnlyForType(item);
    return onlyFor != Default.class && onlyFor != this.onlyFor;
  }
}
