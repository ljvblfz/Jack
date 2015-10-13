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

import com.google.common.base.Joiner;
import com.google.common.collect.Ordering;

import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.log.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A set of {@link Item} classes.
 * <p>When adding an {@code Item} composed of several {@code Item}s (using the annotation
 * {@link ComposedOf}), all the {@code Item}s are added to the {@code ItemSet}.
 *
 * @param <T> a type of {@code Item}
 */
@HasKeyId
public class ItemSet<T extends Item> implements Cloneable, Iterable<Class<? extends T>> {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final ItemManager manager;
  @Nonnull
  private long[] bitmap;

  public ItemSet(@Nonnull ItemManager manager) {
    this.manager = manager;
    this.bitmap = new long[manager.getIntegersCount()];
  }

  public ItemSet(@Nonnull ItemSet<T> initial) {
    this(initial.manager);
    addAll(initial);
  }

  public boolean contains(@Nonnull Class<? extends T> item) {
    return containsAll(manager.getManagedItem(item).getBitmap());
  }

  public boolean containsAll(@Nonnull ItemSet<T> set) {
    return containsAll(set.bitmap);
  }

  private boolean containsAll(@Nonnull long[] bitmap) {
    assert this.bitmap.length == bitmap.length;

    for (int i = 0; i < this.bitmap.length; i++) {
      if ((this.bitmap[i] & bitmap[i]) != bitmap[i]) {
        return false;
      }
    }

    return true;
  }

  public boolean containsOne(@Nonnull ItemSet<T> set) {
    return containsOne(set.bitmap);
  }

  private boolean containsOne(@Nonnull long[] bitmap) {
    assert this.bitmap.length == bitmap.length;

    for (int i = 0; i < this.bitmap.length; i++) {
      if ((this.bitmap[i] & bitmap[i]) != 0) {
        return true;
      }
    }

    return false;
  }

  public boolean containsNone(@Nonnull ItemSet<T> set) {
    return containsNone(set.bitmap);
  }

  private boolean containsNone(long[] bitmap) {
    assert this.bitmap.length == bitmap.length;

    for (int i = 0; i < this.bitmap.length; i++) {
      if ((this.bitmap[i] & bitmap[i]) != 0) {
        return false;
      }
    }

    return true;
  }

  protected void computeIntersection(@Nonnull ItemSet<T> inter, @Nonnull ItemSet<T> set) {
    inter.bitmap = intersection(set.bitmap);
  }

  @Nonnull
  private long[] intersection(@Nonnull long[] bitmap) {
    assert this.bitmap.length == bitmap.length;

    long[] res = new long[manager.getIntegersCount()];

    for (int i = 0; i < this.bitmap.length; i++) {
      res[i] = this.bitmap[i] & bitmap[i];
    }

    return res;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(@CheckForNull Object obj) {
    if (obj == this) {
      return true;
    }

    if (obj instanceof ItemSet<?>) {
      ItemSet<T> set = (ItemSet<T>) obj;

      return equals(set.bitmap);
    } else {
      return false;
    }
  }

  private boolean equals(@Nonnull long[] bitmap) {
    assert this.bitmap.length == bitmap.length;

    for (int i = 0; i < this.bitmap.length; i++) {
      if (this.bitmap[i] != bitmap[i]) {
        return false;
      }
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 0;

    for (long element : this.bitmap) {
      hashCode = hashCode ^ (int) (element & 0xFFFFFFFF) ^ (int) (element >> 32);
    }

    return hashCode;
  }

  public void add(@Nonnull Class<? extends T> item) {
    add(manager.getManagedItem(item).getBitmap());
  }

  private void add(@Nonnull long[] bitmap) {
    assert this.bitmap.length == bitmap.length;

    for (int i = 0; i < this.bitmap.length; i++) {
      this.bitmap[i] |= bitmap[i];
    }
  }

  public void addAll(@Nonnull ItemSet<T> set) {
    add(set.bitmap);
  }

  public void remove(@Nonnull Class<? extends T> item) {
    remove(manager.getManagedItem(item).getBitmap());
  }

  private void remove(@Nonnull long[] bitmap) {
    assert this.bitmap.length == bitmap.length;

    for (int i = 0; i < this.bitmap.length; i++) {
      this.bitmap[i] &= ~bitmap[i];
    }
  }

  public void removeAll(@Nonnull ItemSet<T> set) {
    remove(set.bitmap);
  }

  public void clear() {
    for (int i = 0; i < this.bitmap.length; i++) {
      this.bitmap[i] = 0;
    }
  }

  @Nonnegative
  public int getSize() {
    return getSize(bitmap);
  }

  @Nonnegative
  private int getSize(@Nonnull long[] bitmap) {
    int size = 0;

    for (long mask : bitmap) {

      while (mask != 0) {
        if ((mask & 0x1) == 0x1) {
          size++;
        }
        mask = mask >>> 1;
      }
    }

    return size;
  }

  public boolean isEmpty() {
    for (long element : this.bitmap) {
      if (element != 0) {
        return false;
      }
    }

    return true;
  }

  @Nonnull
  public static final BooleanPropertyId COMPACT_TOSTRING = BooleanPropertyId.create(
      "sched.itemset.compact", "Define if item sets are displayed compacted")
      .addDefaultValue("false");

  @Override
  @Nonnull
  public String toString() {
    if (ThreadConfig.get(COMPACT_TOSTRING).booleanValue()) {
      return toStringCompact();
    } else {
      return toStringRaw();
    }
  }

  @Nonnull
  public String toStringRaw() {
    List<String> names = new ArrayList<String>();

    ItemIterator<Item> iter = new ItemIterator<Item>(this);
    while (iter.hasNext()) {
      names.add(iter.next().getName());
    }

    StringBuilder sb = new StringBuilder();
    sb.append('[');
    Joiner.on(", ")
        .appendTo(sb, Ordering.from(String.CASE_INSENSITIVE_ORDER).immutableSortedCopy(names));
    sb.append(']');

    return sb.toString();
  }

  @Nonnull
  public String toStringCompact() {
    List<String> names = new ArrayList<String>();

    for (Class<? extends T> item : getCompactSet()) {
      names.add(Items.getName(item));
    }

    StringBuilder sb = new StringBuilder();
    sb.append('[');
    Joiner.on(", ")
        .appendTo(sb, Ordering.from(String.CASE_INSENSITIVE_ORDER).immutableSortedCopy(names));
    sb.append(']');

    return sb.toString();
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public Set<Class<? extends T>> getCompactSet() {
    ItemSet<T> items = new ItemSet<T>(this);
    Set<Class<? extends T>> set = new HashSet<Class<? extends T>>();
    ManagedItem bestItem = null;
    int best = 0;

    while (!items.isEmpty()) {
      for (ManagedItem item : manager.getManagedItems()) {
        assert item.bitmap != null;

        if (items.containsAll(item.bitmap)) {
          int size = getSize(item.bitmap);

          if (size > best) {
            best = size;
            bestItem = item;
          }
        }
      }

      if (bestItem != null) {
        set.add((Class<? extends T>) bestItem.getItem());

        assert bestItem.bitmap != null;
        items.remove(bestItem.bitmap);
        best = 0;
        bestItem = null;
      } else {
        throw new AssertionError();
      }
    }

    return set;
  }

  @Nonnull
  @Override
  @SuppressWarnings("unchecked")
  public ItemSet<T> clone() {
    try {
      ItemSet<T> cloned = (ItemSet<T>) super.clone();

      cloned.bitmap = this.bitmap.clone();

      return cloned;
    } catch (CloneNotSupportedException e) {
      logger.log(Level.SEVERE, "Programm can not be here", e);
      throw new AssertionError(e);
    }
  }

  @Nonnull
  @Override
  public Iterator<Class<? extends T>> iterator() {
    return new ItemIterator<T>(this);
  }

  private class ItemIterator<T extends Item> implements Iterator<Class<? extends T>> {
    private int ptrIntegers;
    private int ptrBits;
    private long mask;
    private final ItemSet<? extends T> set;

    ItemIterator(@Nonnull ItemSet<? extends T> set) {
      this.ptrIntegers = 0;
      this.ptrBits = 0;
      this.set = set;

      this.mask = set.bitmap[0];
    }

    @Override
    public boolean hasNext() {
      while (mask == 0) {
        ptrIntegers++;
        if (ptrIntegers < set.bitmap.length) {
          mask = set.bitmap[ptrIntegers];
          ptrBits = 0;
        } else {
          return false;
        }
      }

      while ((mask & 0x1) == 0x0) {
        ptrBits++;
        mask = mask >>> 1;
      }

      return true;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends T> next() {
      mask = mask >>> 1;

      return (Class<? extends T>) set.manager.getManagedItem(ptrIntegers, ptrBits++).getItem();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
