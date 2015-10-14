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
 * Data model list.
 */
public class DataModelList implements Iterable<Object>, DataModel {
  @Nonnull
  private final List<DataModel> rows = new ArrayList<DataModel>();

  @Nonnull
  public DataModelList add(@Nonnull DataModel data) {
    rows.add(data);

    return this;
  }

  @Nonnull
  public DataModelList addAll(@Nonnull List<DataModel> list) {
    rows.addAll(list);

    return this;
  }

  @Nonnull
  public DataModelList addAll(@Nonnull DataModel[] list) {
    for (DataModel data : list) {
      add(data);
    }

    return this;
  }

  @Nonnull
  public DataModelList addAll(@Nonnull Iterator<DataModel> iter) {
    while (iter.hasNext()) {
      add(iter.next());
    }

    return this;
  }

  @Nonnegative
  public int getRowCount() {
    return rows.size();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Object> iterator() {
    return (Iterator<Object>) ((List<?>) rows).iterator();
  }

  @Override
  @Nonnull
  public DataView getDataView() {
    return DataViewBuilder
        .getList(rows.size() == 0 ? DataType.NOTHING : rows.get(0).getDataView().getDataType())
        .build();
  }
}
