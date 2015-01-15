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
import com.google.common.collect.Iterators;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A path relative to a VFS. Two instances of {@link VPath} can be compared regardless of the
 * separator used as initialization.
 */
public final class VPath implements Cloneable {

  @Nonnull
  public static final VPath ROOT = new VPath("", '/');

  private static final char INTERNAL_SEPARATOR = '/';

  @Nonnull
  private static final Splitter splitter = Splitter.on(INTERNAL_SEPARATOR).omitEmptyStrings();

  @Nonnull
  ArrayList<VPathFragment> pathFragments;

  /**
   * Creates an instance of VFS-relative path. The {@link CharSequence} is evaluated lazily at each
   * usage. The separator must not be contained twice consecutively, nor be at the start or the
   * beginning of the path.
   * @param path the relative path
   * @param separator the separator used as file separator in the path
   */
  public VPath(@Nonnull CharSequence path, char separator) {
    pathFragments = new ArrayList<VPathFragment>(1);
    VPathFragment pe = new VPathFragment(path, separator);
    assert pe.isValidPath();
    pathFragments.add(pe);
  }

  private VPath(ArrayList<VPathFragment> pathFragments) {
    this.pathFragments = pathFragments;
  }

  /**
   * Inserts a path before an existing path. The resulting path is evaluated lazily at each usage.
   * There is an implicit separator between the prepended path and the existing path.
   * @param path the path to insert before the existing path
   * @return the current path
   */
  public VPath prependPath(@Nonnull VPath path) {
    pathFragments.add(0, new VPathFragment(String.valueOf(INTERNAL_SEPARATOR), INTERNAL_SEPARATOR));
    pathFragments.addAll(0, path.getPathFragments());

    return this;
  }

  /**
   * Inserts a path after an existing path. The resulting path is evaluated lazily at each usage.
   * There is an implicit separator between the existing path and the appended path.
   * @param path the path to insert after the existing path
   * @return the current path
   */
  public VPath appendPath(@Nonnull VPath path) {
    pathFragments.add(new VPathFragment(String.valueOf(INTERNAL_SEPARATOR), INTERNAL_SEPARATOR));
    pathFragments.addAll(path.getPathFragments());

    return this;
  }

  /**
   * Adds a suffix to the existing path. The resulting path is evaluated lazily at each usage.
   * It may be identical or different from the previous path.
   * No implicit separator will be added between the suffix and the previous path.
   * @param suffix the suffix to add to the path
   * @return the current path
   */
  public VPath addSuffix(@Nonnull CharSequence suffix) {
    VPathFragment pe = new VPathFragment(suffix, INTERNAL_SEPARATOR);
    assert pe.isValidSuffix();
    pathFragments.add(pe);

    return this;
  }

  @Override
  public VPath clone() {
    // no need to clone path fragments, they should be immutable
    @SuppressWarnings("unchecked")
    ArrayList<VPathFragment> clonedList = (ArrayList<VPathFragment>) pathFragments.clone();
    return new VPath(clonedList);
  }

  /**
   * Returns the VFS-relative path as an {@link Iterable} of path elements.
   */
  @Nonnull
  public Iterable<String> split() {
    return splitter.split(getInternalPath());
  }

  /**
   * Returns the VFS-relative path using the given separator.
   * @param separator the file separator wanted
   * @return the relative path
   */
  @Nonnull
  public String getPathAsString(char separator) {
    StringBuffer buffer = new StringBuffer();
    for (VPathFragment pathElement : pathFragments) {
      buffer.append(pathElement.getPathElementAsString(separator));
    }
    return buffer.toString();
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
  private List<VPathFragment> getPathFragments() {
    return pathFragments;
  }

  @Nonnull
  private String getInternalPath() {
    return getPathAsString(INTERNAL_SEPARATOR);
  }

  /**
   * A portion of path that should be immutable.
   */
  static class VPathFragment {
    @Nonnull
    private final CharSequence path;

    private final char separator;

    public VPathFragment(@Nonnull CharSequence path, char separator) {
      this.path = path;
      this.separator = separator;
    }

    @Nonnull
    public String getPathElementAsString(char separator) {
      return path.toString().replace(this.separator, separator);
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

    private boolean isValidSuffix() {
      return !path.toString().contains(String.valueOf(separator));
    }
  }

  /**
   * @return the last name of a path.
   */
  @Nonnull
  public String getLastName() {
    return Iterators.getLast(split().iterator(), "");
  }
}
