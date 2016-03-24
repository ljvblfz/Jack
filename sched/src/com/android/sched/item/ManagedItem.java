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

import com.android.sched.util.HasDescription;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Represents an {@link Item} composed of other items.
 */
public class ManagedItem implements HasDescription {
  // Bitmap, representing this item in conjunction with @ComposedOf, inheritance or inner
  @CheckForNull
  protected long[] bitmap;

  @Nonnull
  private final ItemManager manager;

  @Nonnull
  private final String name;
  @Nonnull
  private final String description;

  @Nonnull
  private final Class<? extends Item> item;

  protected ManagedItem(@Nonnull Class<? extends Item> item, @Nonnull ItemManager manager) {
    this.manager = manager;
    this.item = item;
    this.name = Items.getName(item);

    // FINDBUGS
    String description = Items.getDescription(item);
    if (description == null) {
      throw new ItemNotConformException("Item '" + item.getCanonicalName()
          + "' must have a @" + Description.class.getSimpleName());
    }
    this.description = description;
  }

  @Nonnull
  public Class<? extends Item> getItem() {
    return item;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Override
  @Nonnull
  public String getDescription() {
    return description;
  }

  @Nonnull
  long[] getBitmap() {
    assert bitmap != null;

    return bitmap;
  }

  protected void ensureBitmap() {
    if (bitmap == null) {
      bitmap = new long[manager.getIntegersCount()];
    }
  }

  public void addComposedOf(@Nonnull ManagedItem item) {
    ensureBitmap();
    assert this.bitmap != null;

    item.addComposedOf();
    long[] bitmap = item.getBitmap();
    for (int idx = 0; idx < bitmap.length; idx++) {
      this.bitmap[idx] |= bitmap[idx];
    }
  }

  public void addComposedOf() {
    ensureBitmap();

    for (Class<? extends Item> i : Items.getComposedOf(getItem())) {
      ManagedItem mi = manager.getManagedItem(i);

      mi.addComposedOf();
      long[] bitmap = mi.getBitmap();

      // FINDBUGS
      assert this.bitmap != null;
      for (int idx = 0; idx < bitmap.length; idx++) {
        this.bitmap[idx] |= bitmap[idx];
      }
    }
  }

  @Nonnull
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("Item '");
    sb.append(getName());
    sb.append('\'');

    return new String(sb);
  }

  @Override
  public final int hashCode() {
    return item.hashCode() ^ manager.hashCode();
  }

  @Override
  public final boolean equals(@CheckForNull Object obj) {
    if (this == obj) {
      return true;
    }

    if  (!(obj instanceof ManagedItem)) {
      return false;
    }

    ManagedItem other = (ManagedItem) obj;
    return item.equals(other.item) && manager.equals(other.manager);
  }
}
