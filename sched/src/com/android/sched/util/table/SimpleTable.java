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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A simple {@link Table} implementation which contains a list of {@link DataRow}.
 */
public class SimpleTable extends AbstractTable {
  @Nonnull
  private final List<DataRow> rows = new ArrayList<DataRow>();

  public SimpleTable(
      @Nonnull String name, @Nonnull String description, @Nonnull DataHeader header) {
    super(name, description, header);
  }

  @Override
  @Nonnull
  public Table addRow(@Nonnull DataRow row) {
    assert row.getColumnCount() == getColumnCount();

    rows.add(row);

    return this;
  }

  @Override
  @Nonnull
  public Iterator<Iterable<String>> iterator() {
    return new Iterator<Iterable<String>>() {
      @Nonnull
      Iterator<DataRow> iter = rows.iterator();

      @Override
      public boolean hasNext() {
        return iter.hasNext();
      }

      @Override
      @Nonnull
      public Iterable<String> next() {
        return new Iterable<String>() {
          @Nonnull
          private final DataRow row = iter.next();

          @Override
          @Nonnull
          public Iterator<String> iterator() {
            return new IteratorWithFormatter(row.iterator());
          }
        };
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
      Iterator<DataRow> iter = rows.iterator();

      @Override
      public boolean hasNext() {
        return iter.hasNext();
      }

      @Override
      @Nonnull
      public Iterable<Object> next() {
        return new Iterable<Object>() {
          @Nonnull
          private final DataRow row = iter.next();

          @Override
          @Nonnull
          public Iterator<Object> iterator() {
            return row.iterator();
          }
        };
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  @Nonnegative
  public int getRowCount() {
    return rows.size();
  }
}
