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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents a concrete {@link Item}.
 */
public class ManagedConcreteItem extends ManagedItem {
  //
  // Position in the bitmap of the original item (without @ComposedOf, inheritance or inner)
  //
  private int posInteger = -1;
  private int posBit = -1;

  public ManagedConcreteItem(@Nonnull Class<? extends Item> item, @Nonnull ItemManager manager) {
    super(item, manager);
  }

  public ManagedConcreteItem(@Nonnull Class<? extends Item> item, @Nonnull ItemManager manager,
      @Nonnegative int posInteger, @Nonnegative int posBits) {
    super(item, manager);

    this.posInteger = posInteger;
    this.posBit = posBits;
  }

  public void setPosition(@Nonnegative int posInteger, @Nonnegative int posBits) {
    this.posInteger = posInteger;
    this.posBit = posBits;
  }

  @Override
  protected void ensureBitmap() {
    if (bitmap == null) {
      super.ensureBitmap();
      assert bitmap != null;
      bitmap[posInteger] = 1L << posBit;
    }
  }

  /**
   * @return the posInteger
   */
  @Nonnegative
  public int getPosInteger() {
    assert posInteger >= 0;

    return posInteger;
  }

  /**
   * @return the posBit
   */
  @Nonnegative
  public int getPosBit() {
    assert posBit >= 0;

    return posBit;
  }

  @Nonnull
  @Override
  public String toString() {
    assert posInteger >= 0;
    assert posBit >= 0;

    StringBuilder sb = new StringBuilder();

    sb.append("Item '");
    sb.append(getName());
    sb.append("' id <");
    sb.append(posInteger);
    sb.append(", ");
    sb.append(posBit);
    sb.append('>');

    return new String(sb);
  }
}
