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
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Builder to help building {@link DataView}.
 */
public class DataViewBuilder {
  @Nonnull
  public static DataViewBuilder getStructure() {
    return new DataViewBuilder(DataType.STRUCT);
  }

  @Nonnull
  public static DataViewBuilder getList(@Nonnull DataType type) {
    return new DataViewBuilder(DataType.LIST).addField("<unknown>", type);
  }

  @Nonnull
  private final List<String> namesList = new ArrayList<String>();
  @Nonnull
  private final List<DataType> typesList = new ArrayList<DataType>();
  @Nonnull
  private final DataType type;

  private DataViewBuilder(@Nonnull DataType type) {
    this.type = type;
  }

  @Nonnull
  public DataViewBuilder addField(@Nonnull String name, @Nonnull DataType type) {
    namesList.add(name);
    typesList.add(type);

    return this;
  }

  @Nonnull
  public DataViewBuilder addDataView(@Nonnull DataView view) {
    for (String name : view.getDataNames()) {
      this.namesList.add(name);
    }

    for (DataType type : view.getDataTypes()) {
      this.typesList.add(type);
    }

    return this;
  }

  @Nonnull
  public DataViewBuilder addFields(@Nonnull String[] names, @Nonnull DataType[] types) {
    for (String name : names) {
      this.namesList.add(name);
    }

    for (DataType type : types) {
      this.typesList.add(type);
    }

    return this;
  }

  @Nonnull
  public DataView build() {
    assert namesList.size() == typesList.size();

    return new DataHeaderImpl(namesList, typesList);
  }

  private class DataHeaderImpl implements DataView {
    @Nonnull
    private final String[] names;
    @Nonnull
    private final DataType[] types;

    public DataHeaderImpl(@Nonnull List<String> names, @Nonnull List<DataType> types) {
      this.names = names.toArray(new String[names.size()]);
      this.types = types.toArray(new DataType[types.size()]);
    }

    @Override
    @Nonnegative
    public int getDataCount() {
      return names.length;
    }

    @Override
    @Nonnull
    public String[] getDataNames() {
      return names;
    }

    @Override
    @Nonnull
    public DataType[] getDataTypes() {
      return types;
    }

    @Override
    @Nonnull
    public DataType getDataType() {
      return type;
    }
  }
}
