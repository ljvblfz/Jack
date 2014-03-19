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

import com.android.sched.util.codec.Formatter;

import java.util.Iterator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Interface representing a table.
 */
public interface Table extends Iterable<Iterable<String>> {
  @Nonnull
  public Formatter<?>[] getFormatters();

  @Nonnull
  public String getName();
  public void setName(@Nonnull String name);

  @Nonnull
  public String getDescription();
  public void setDescription(@Nonnull String description);

  @Nonnull
  public String[] getHeader();
  public void setHeader(@Nonnull String[] header);

  @Nonnull
  public Table addRow(@Nonnull DataRow row);

  @Nonnegative
  public int getColumnCount();

  @Nonnegative
  public int getRowCount();

  @Nonnull
  public Iterator<Iterable<Object>> valueIterator();
}
