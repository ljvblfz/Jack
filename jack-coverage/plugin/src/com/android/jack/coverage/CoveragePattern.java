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

import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Represents a pattern for coverage filtering. A pattern is a fully-qualified java class name
 * that can contain wildcards '*' (replacing multiple characters) and '?' (replacing one character).
 */
public class CoveragePattern {
  @Nonnull
  private final String string;

  @Nonnull
  private final Pattern pattern;

  public CoveragePattern(@Nonnull String string) {
    this.string = string;
    StringBuilder sb = new StringBuilder();
    for (int i = 0, e = string.length(); i < e; ++i) {
      char c = string.charAt(i);
      if (c == '?' || c == '*') {
        // Prepend with '.' to replace with the corresponding regex wildcard.
        sb.append('.');
        sb.append(c);
      } else {
        sb.append(Pattern.quote(String.valueOf(c)));
      }
    }
    this.pattern = Pattern.compile(sb.toString());
  }

  /**
   *
   * @return the pattern
   */
  @Nonnull
  public String getString() {
    return string;
  }

  /**
   *
   * @return the corresponding {@link Pattern} object.
   */
  @Nonnull
  public Pattern getPattern() {
    return pattern;
  }

  @Override
  public final boolean equals(@CheckForNull Object obj) {
    if (!(obj instanceof CoveragePattern)) {
      return false;
    }
    CoveragePattern other = (CoveragePattern) obj;
    return getString().equals(other.getString());
  }

  @Override
  public final int hashCode() {
    return getString().hashCode();
  }

  @Override
  @Nonnull
  public String toString() {
    return getString();
  }

  public boolean matchesAny(String string) {
    return getPattern().matcher(string).matches();
  }
}
