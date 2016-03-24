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

package com.android.sched.item;


import com.android.sched.util.codec.VariableName;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.ReflectFactoryPropertyId;
import com.android.sched.util.sched.ManagedDataListener;
import com.android.sched.util.sched.ManagedDataListenerFactory;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Provides utilities needed by an {@link ItemSet}.
 */
@HasKeyId
@VariableName("algo")
public abstract class ItemManager {
  @Nonnull
  private static final ReflectFactoryPropertyId<ItemManager> ITEM_MANAGER =
      ReflectFactoryPropertyId.create(
          "sched.item", "Define how items are discovered", ItemManager.class)
          .addDefaultValue("reflections").addArgType(Class.class);

  @Nonnull
  public static ItemManager createItemManager(@Nonnull Class<? extends Item> type) {
    return ThreadConfig.get(ITEM_MANAGER).create(type);
  }

  @Nonnull
  private final ManagedDataListener listener = ManagedDataListenerFactory.getManagedDataListener();

  @Nonnull
  protected Map<Class<? extends Item>, ManagedItem> map =
      new ConcurrentHashMap<Class<? extends Item>, ManagedItem>();

  @Nonnegative
  private int currentNumIntegers = 0;
  @Nonnegative
  private int currentNumBits = 0;
  @Nonnegative
  private int itemsCount = 0;

  protected ItemManager() {
    listener.notifyNewItemManager(this);
  }

  /**
   * @return the number of {@code Item}
   */
  @Nonnegative
  public synchronized int getItemsCount() {
    return itemsCount;
  }

  @Nonnull
  public abstract Class<? extends Item> getType();

  /**
   * @return the number of Integers (e.g. long here) needed to encode the set
   */
  @Nonnegative
  public synchronized int getIntegersCount() {
      return currentNumIntegers + 1;
  }

  @Nonnull
  ManagedItem getManagedItem(@Nonnull Class<? extends Item> cls) {
    ManagedItem item = map.get(cls);

    if (item == null) {
      throw new ItemNotRegisteredError(cls);
    }

    return item;
  }

  @Nonnull
  ManagedItem getManagedItem(@Nonnegative int posInteger, @Nonnegative int posBit) {
    for (ManagedItem managedItem : map.values()) {
      if (managedItem instanceof ManagedConcreteItem) {
        ManagedConcreteItem concreteItem = (ManagedConcreteItem) managedItem;

        if (concreteItem.getPosBit() == posBit && concreteItem.getPosInteger() == posInteger) {
          return concreteItem;
        }
      }
    }

    throw new NoSuchElementException("No such item with id <" + posInteger + ", " + posBit
        + "> for type " + getType().getCanonicalName());
  }

  @Nonnull
  protected Collection<ManagedItem> getManagedItems() {
    return map.values();
  }

  @Nonnull
  protected ManagedItem registerItem (@Nonnull Class<? extends Item> item) {
    ManagedItem mi;
    int posNumIntegers;
    int posNumBits;

    if (Items.getComposedOf(item).length == 0 &&
        !Modifier.isAbstract(item.getModifiers())) {
      // Not a ComposedOf, neither a abstract class affect position in bitmap
      synchronized (this) {
        itemsCount++;
        posNumIntegers = currentNumIntegers;
        posNumBits = currentNumBits++;

        if (currentNumBits == Long.SIZE) {
          currentNumBits = 0;
          currentNumIntegers++;
        }
      }

      mi = new ManagedConcreteItem(item, this, posNumIntegers, posNumBits);
    } else {
      mi = new ManagedItem(item, this);
    }

    map.put(item, mi);

    return mi;
  }

  @Nonnull
  protected void registerManagedItem (@Nonnull ManagedItem mi) {
    int posNumIntegers;
    int posNumBits;

    if (mi instanceof ManagedConcreteItem) {
      // Not a ComposedOf, neither a abstract class affect position in bitmap
      synchronized (this) {
        itemsCount++;
        posNumIntegers = currentNumIntegers;
        posNumBits = currentNumBits++;

        if (currentNumBits == Long.SIZE) {
          currentNumBits = 0;
          currentNumIntegers++;
        }

        ((ManagedConcreteItem) mi).setPosition(posNumIntegers, posNumBits);
      }
    }

    map.put(mi.getItem(), mi);
  }
}
