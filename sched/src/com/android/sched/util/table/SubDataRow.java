/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.sched.util.table;

import com.google.common.collect.Iterators;

import java.util.Iterator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A {@link DataRow} implementation which takes a subset of another {@link DataRow}.
 */
public class SubDataRow implements DataRow {
  @Nonnull
  private final DataRow row;
  @Nonnegative
  private final int from;
  @Nonnegative
  private final int size;

  public SubDataRow(@Nonnull DataRow row, @Nonnegative int size) {
    this.row = row;
    this.size = size;
    this.from = 0;
  }

  public SubDataRow(@Nonnull DataRow row, @Nonnegative int from, @Nonnegative int size) {
    this.row = row;
    this.size = size;
    this.from = from;
  }

  @Override
  @Nonnegative
  public int getColumnCount() {
    return size;
  }

  @Override
  @Nonnull
  public Iterator<Object> iterator() {
    Iterator<Object> iter = row.iterator();

    Iterators.advance(iter, from);
    return Iterators.limit(iter, size);
  }
}
