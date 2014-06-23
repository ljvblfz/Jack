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

package com.android.sched.vfs;

import com.google.common.base.Splitter;

import javax.annotation.Nonnull;

/**
 * A path relative to a VFS. Two instances of {@link VPath} can be compared regardless of the
 * separator used as initialization.
 */
public final class VPath {

  @Nonnull
  private final CharSequence path;

  private final char separator;

  /**
   * Creates an instance of VFS-relative path. The {@link CharSequence} is evaluated lazily at each
   * usage. The separator must not be contained twice consecutively, nor be at the start or the
   * beginning of the path.
   * @param path the relative path
   * @param separator the separator used as file separator in the path
   */
  public VPath(@Nonnull CharSequence path, char separator) {
    this.path = path;
    this.separator = separator;
    assert isValidPath();
  }

  private boolean isValidPath() {
    String toString = path.toString();
    String stringSeparator = String.valueOf(separator);
    String doubleSeparator = stringSeparator + separator;
    if (toString.contains(doubleSeparator)) {
      return false;
    }
    if (toString.startsWith(stringSeparator)) {
      return false;
    }
    if (toString.endsWith(stringSeparator)) {
      return false;
    }
    return true;
  }

  /**
   * Returns the VFS-relative path as an {@link Iterable} of path elements.
   */
  @Nonnull
  public Iterable<String> split() {
    Splitter splitter = Splitter.on(separator).omitEmptyStrings();
    return splitter.split(path);
  }

  /**
   * Returns the VFS-relative path using the given separator.
   * @param separator the file separator wanted
   * @return the relative path
   */
  @Nonnull
  public String getPathAsString(char separator) {
    return path.toString().replace(this.separator, separator);
  }

  @Override
  public final boolean equals(Object obj) {
    if (!(obj instanceof VPath)) {
      return false;
    }
    return getInternalPath().equals(((VPath) obj).getInternalPath());
  }

  @Override
  public final int hashCode() {
    return getInternalPath().hashCode();
  }

  @Nonnull
  private String getInternalPath() {
    return path.toString().replace(separator, '/');
  }

  @Nonnull
  public String getLastPathElement() {
    String toString = path.toString();
    return toString.substring(toString.lastIndexOf(separator) + 1);
  }
}
