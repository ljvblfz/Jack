/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.coverage;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A set of patterns used for coverage filtering.
 */
public class CoverageFilterSet {
  /**
   * A set of patterns used for filtering.
   */
  @Nonnull
  private final List<CoveragePattern> patterns = new ArrayList<CoveragePattern>();

  /**
   * Constructs an empty {@link CoverageFilterSet}.
   */
  public CoverageFilterSet() {
  }

  /**
   * Constructs a {@link CoverageFilterSet} and adds the given <code>strings</code> as patterns.
   *
   * @param strings the patterns to add to the set
   */
  public CoverageFilterSet(@Nonnull String[] strings) {
    for (String string : strings) {
      addPattern(new CoveragePattern(string));
    }
  }

  /**
   * Adds a new pattern to this set unless it has previously been added.
   *
   * @param pattern a class name pattern (like foo.bar.*)
   * @throws NullPointerException if pattern == null
   */
  public void addPattern(@Nonnull CoveragePattern pattern) {
    assert pattern != null;
    if (!patterns.contains(pattern)) {
      patterns.add(pattern);
    }
  }

  @Nonnull
  public List<CoveragePattern> getPatterns() {
    return patterns;
  }

  public boolean isEmpty() {
    return patterns.size() == 0;
  }

  /**
   * Returns true if any pattern in the set matches the given string.
   *
   * @param className a class name (like java.lang.Object)
   * @return true if any pattern matches with the given string.
   */
  public boolean matchesAny(@Nonnull String className) {
    for (CoveragePattern pattern : patterns) {
      if (pattern.matchesAny(className)) {
        return true;
      }
    }
    return false;
  }
}
