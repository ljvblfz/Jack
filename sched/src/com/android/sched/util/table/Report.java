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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A collection of {@link Table}s.
 */
public class Report implements Iterable<Table> {
  @Nonnull
  private final String name;
  @Nonnull
  private final String description;

  @Nonnull
  private final List<Table> tables = new ArrayList<Table>();

  public Report(@Nonnull String name, @Nonnull String description) {
    this.name        = name;
    this.description = description;
  }

  public Report addTable(@Nonnull Table table) {
    tables.add(table);

    return this;
  }

  @Nonnull
  public Report addTables(@Nonnull Collection<Table> tables) {
    tables.addAll(tables);

    return this;
  }

  @Override
  public Iterator<Table> iterator() {
    return tables.iterator();
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public String getDescription() {
    return description;
  }
}
