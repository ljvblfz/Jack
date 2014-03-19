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
 * Based {@link Table} implementation without row management.
 */
public abstract class AbstractTable implements Table {
  @Nonnull
  private String name;
  @Nonnull
  private String description;

  @Nonnull
  private String[] header;
  @Nonnull
  private final Formatter<?>[] formatters;

  protected AbstractTable(
      @Nonnull String name, @Nonnull String description, @Nonnull DataHeader header) {
    assert header.getHeader().length == header.getFormatters().length;

    this.name        = name;
    this.description = description;
    this.header      = header.getHeader();
    this.formatters  = header.getFormatters();
  }

  /*
   * This method returns the internal array of formatters at purposed, to be able to change some
   * formatters outside. These modifications are only local to this table and will not be reflected
   * in any other Table or DataHeader.
   */
  @Override
  @Nonnull
  public Formatter<?>[] getFormatters() {
    return formatters;
  }

  @Override
  @Nonnull
  public String getName() {
    return name;
  }

  @Override
  public void setName(@Nonnull String name) {
    this.name = name;
  }

  @Override
  @Nonnull
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(@Nonnull String description) {
    this.description = description;
  }

  @Override
  @Nonnull
  public String[] getHeader() {
    return header.clone();
  }

  @Override
  public void setHeader(@Nonnull String[] header) {
    assert header.length == getColumnCount();

    this.header = header.clone();
  }

  @Override
  @Nonnegative
  public int getColumnCount() {
    return header.length;
  }

  protected class IteratorWithFormatter implements Iterator<String> {
    @Nonnegative
    private int index = 0;
    @Nonnull
    private final Iterator<Object> iterator;

    protected IteratorWithFormatter(
        @Nonnull Iterator<Object> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public String next() {
      assert index < formatters.length;

      return ((Formatter<Object>) (formatters[index++])).formatValue(iterator.next());
    }

    @Override
    public void remove() {
      iterator.remove();
    }
  }
}
