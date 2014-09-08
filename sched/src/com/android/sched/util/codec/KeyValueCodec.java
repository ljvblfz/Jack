/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.sched.util.codec;

import com.android.sched.util.config.ConfigurationError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to return instance of {@code T} designed by a keyword
 *
 * @param <T> Type of the element
 */
public class KeyValueCodec<T> implements StringCodec<T> {
  private boolean ignoreCase = false;
  @Nonnull
  private final Entry<T>[] entries;
  @CheckForNull
  private List<ValueDescription> descriptions;

  public KeyValueCodec(@Nonnull Entry<T>[] entries) {
    this.entries    = Arrays.copyOf(entries, entries.length);
  }

  @Nonnull
  public KeyValueCodec<T> ignoreCase() {
    this.ignoreCase = true;

    for (int idx = 0; idx < this.entries.length; idx++) {
      this.entries[idx].key = this.entries[idx].key.toLowerCase();
    }

    return this;
  }

  @Nonnull
  public KeyValueCodec<T> sorted() {
    Arrays.sort(this.entries, new Comparator<Entry<T>>(){
      @Override
      public int compare(Entry<T> o1, Entry<T> o2) {
        return o1.key.compareToIgnoreCase(o2.key);
      }});

    return this;
  }

  @Override
  @Nonnull
  public String getUsage() {
    StringBuilder sb = new StringBuilder();

    boolean first = true;
    sb.append('{');
    for (Entry<T> entry : entries) {
      if (first) {
        first = false;
      } else {
        sb.append(',');
      }

      sb.append(entry.key);
    }
    sb.append("} (case ");
    sb.append(ignoreCase ? "insensitive" : "sensitive");
    sb.append(')');

    return sb.toString();
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    if (descriptions == null) {
      descriptions = new ArrayList<ValueDescription>(entries.length);

      for (Entry<T> entry : entries) {
        if (entry.description != null) {
          descriptions.add(new ValueDescription(entry.key, entry.description));
        }
      }
    }

    assert descriptions != null;
    return descriptions;
  }

  @Override
  @Nonnull
  public T checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    if (ignoreCase) {
      string = string.toLowerCase();
    }

    for (Entry<T> entry : entries) {
      if (entry.key.equals(string)) {
        return entry.value;
      }
    }

    throw new ParsingException(
        "The value must be " + getUsage() + " but is '" + string + "'");
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull T value)
      throws CheckingException {
    for (Entry<T> entry : entries) {
      if (entry.value.equals(value)) {
        return;
      }
    }

    Set<T> set = new HashSet<T>();
    for (Entry<T> entry : entries) {
      set.add(entry.value);
    }

    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (T data : set) {
      if (first) {
        first = false;
      } else {
        sb.append(", ");
      }

      sb.append(data);
      sb.append(" (");
      sb.append(data.getClass().getCanonicalName());
      sb.append(')');
    }

    throw new CheckingException("The value must be {" + sb.toString() + "} but is '" + value + " ("
        + value.getClass().getCanonicalName() + ")'");
  }

  @Override
  @Nonnull
  public T parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e.getMessage(), e);
    }
  }

  /**
   *
   */
  public static class Entry<T> {
    @Nonnull
    String key;
    @Nonnull
    T      value;
    @CheckForNull
    String description;

    public Entry (@Nonnull String key, @Nonnull T value) {
      this.key   = key;
      this.value = value;
    }

    public Entry (@Nonnull String key, @Nonnull T value, @CheckForNull String description) {
      this.key   = key;
      this.value = value;

      if (description != null && !description.isEmpty()) {
        this.description = description;
      }
    }
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull T value) {
    for (Entry<T> entry : entries) {
      if (entry.value == value) {
        return entry.key;
      }
    }

    throw new AssertionError();
  }
}
