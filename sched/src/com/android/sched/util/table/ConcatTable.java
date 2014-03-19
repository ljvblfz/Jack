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
 * {@link Table} implementation which concatenates two tables side by side.
 */
public class ConcatTable extends AbstractTable {
  @Nonnull
  private final Table left;
  @Nonnull
  private final Table right;

  public ConcatTable(@Nonnull String name, @Nonnull String description, @Nonnull Table left,
      @Nonnull Table right) {
    super(name, description, DataHeaderBuilder.get()
        .addHeader(left.getHeader(), left.getFormatters())
        .addHeader(right.getHeader(), right.getFormatters()).build());
    this.left  = left;
    this.right = right;
  }

  @Override
  @Nonnull
  public Iterator<Iterable<String>> iterator() {
    return new Iterator<Iterable<String>>() {
      @Nonnull
      Iterator<Iterable<Object>> iterLeft = left.valueIterator();
      @Nonnull
      Iterator<Iterable<Object>> iterRight = right.valueIterator();

      @Override
      public boolean hasNext() {
        assert iterLeft.hasNext() == iterRight.hasNext();

        return iterLeft.hasNext() && iterRight.hasNext();
      }

      @Override
      @Nonnull
      public Iterable<String> next() {
        return new Iterable<String>() {
          @Override
          public Iterator<String> iterator() {
            return new IteratorWithFormatter(
                Iterators.concat(iterLeft.next().iterator(), iterRight.next().iterator()));
          }};
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  @Nonnull
  public Iterator<Iterable<Object>> valueIterator() {
    return new Iterator<Iterable<Object>>() {
      @Nonnull
      Iterator<Iterable<Object>> iterLeft = left.valueIterator();
      @Nonnull
      Iterator<Iterable<Object>> iterRight = right.valueIterator();

      @Override
      public boolean hasNext() {
        assert iterLeft.hasNext() == iterRight.hasNext();

        return iterLeft.hasNext() && iterRight.hasNext();
      }

      @Override
      @Nonnull
      public Iterable<Object> next() {
        return new Iterable<Object>() {
          @Override
          public Iterator<Object> iterator() {
            return Iterators.concat(iterLeft.next().iterator(), iterRight.next().iterator());
          }};
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Nonnull
  public ConcatTable addLeftRow(@Nonnull DataRow row) {
    assert row.getColumnCount() == left.getColumnCount();

    left.addRow(row);

    return this;
  }

  @Nonnull
  public ConcatTable addRightRow(@Nonnull DataRow row) {
    assert row.getColumnCount() == right.getColumnCount();

    right.addRow(row);

    return this;
  }

  @Override
  @Nonnull
  public Table addRow(@Nonnull DataRow row) {
    assert row.getColumnCount() == getColumnCount();

    addLeftRow(new SubDataRow(row, left.getHeader().length));
    addRightRow(new SubDataRow(row, left.getHeader().length, right.getHeader().length));

    return this;
  }

  @Override
  @Nonnegative
  public int getRowCount() {
    assert left.getRowCount() == right.getRowCount();

    return left.getRowCount();
  }
}
