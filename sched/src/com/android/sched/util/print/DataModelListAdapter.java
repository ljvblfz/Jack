/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.sched.util.print;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;


/**
 * Data model list which lazily converts type T to a {@link DataModel}.
 *
 */
public class DataModelListAdapter<T> implements Iterable<Object>, DataModel {
  @Nonnull
  private final List<T> rows = new ArrayList<T>();
  @Nonnull
  private final Converter<T> modeler;

  public DataModelListAdapter(@Nonnull Converter<T> modeler) {
    this.modeler = modeler;
  }

  /**
   * Interface of the converter of type T to a {@link DataModel}.
   *
   * @param T The type to convert
   */
  public static interface Converter<T> {
    @Nonnull
    public DataModel apply(@Nonnull T data);
  }

  @Nonnull
  public DataModelListAdapter<T> add(@Nonnull T data) {
    rows.add(data);

    return this;
  }

  @Nonnull
  public DataModelListAdapter<T> addAll(@Nonnull List<T> list) {
    rows.addAll(list);

    return this;
  }

  @Nonnull
  public DataModelListAdapter<T> addAll(@Nonnull T[] list) {
    for (T data : list) {
      add(data);
    }

    return this;
  }

  @Nonnull
  public DataModelListAdapter<T> addAll(@Nonnull Iterator<T> iter) {
    while (iter.hasNext()) {
      add(iter.next());
    }

    return this;
  }

  @Nonnegative
  public int getRowCount() {
    return rows.size();
  }

  @Override
  public Iterator<Object> iterator() {
    return new Iterator<Object>() {
      private final Iterator<T> iter = rows.iterator();
      @Override
      public boolean hasNext() {
        return iter.hasNext();
      }

      @Override
      public DataModel next() {
        return modeler.apply(iter.next());
      }

      @Override
      public void remove() {
        iter.remove();
      }};
  }

  @Override
  @Nonnull
  public DataView getDataView() {
    return DataViewBuilder.getList(rows.size() == 0 ? DataType.NOTHING
        : modeler.apply(rows.get(0)).getDataView().getDataType()).build();
  }
}
