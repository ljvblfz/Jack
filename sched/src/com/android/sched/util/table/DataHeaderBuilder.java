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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Builder to help building {@link DataHeader}.
 */
public class DataHeaderBuilder {
  @Nonnull
  private final List<String> header = new ArrayList<String>();
  @Nonnull
  private final List<Formatter<?>> formatters = new ArrayList<Formatter<?>>();

  @Nonnull
  public static DataHeaderBuilder get() {
    return new DataHeaderBuilder();
  }

  private DataHeaderBuilder() {
  }

  @Nonnull
  public DataHeaderBuilder addColumn(@Nonnull String name, @Nonnull Formatter<?> formatter) {
    header.add(name);
    formatters.add(formatter);

    return this;
  }

  @Nonnull
  public DataHeaderBuilder addHeader(@Nonnull DataHeader header) {
    for (String name : header.getHeader()) {
      this.header.add(name);
    }

    for (Formatter<?> formatter : header.getFormatters()) {
      this.formatters.add(formatter);
    }

    return this;
  }

  @Nonnull
  public DataHeaderBuilder addHeader(@Nonnull String[] header, @Nonnull Formatter<?>[] formatters) {
    for (String name : header) {
      this.header.add(name);
    }

    for (Formatter<?> formatter : formatters) {
      this.formatters.add(formatter);
    }

    return this;
  }

  @Nonnull
  public DataHeader build() {
    assert header.size() == formatters.size();

    return new DataHeader() {
      @Nonnull
      private final String[] headers =
          DataHeaderBuilder.this.header.toArray(new String[DataHeaderBuilder.this.header.size()]);
      @Nonnull
      private final Formatter<?>[] formaters = DataHeaderBuilder.this.formatters.toArray(
          new Formatter<?>[DataHeaderBuilder.this.formatters.size()]);

      @Override
      @Nonnegative
      public int getColumnCount() {
        return headers.length;
      }

      @Override
      @Nonnull
      public String[] getHeader() {
        return headers;
      }

      @Override
      @Nonnull
      public Formatter<?>[] getFormatters() {
        return formaters;
      }
    };
  }
}
