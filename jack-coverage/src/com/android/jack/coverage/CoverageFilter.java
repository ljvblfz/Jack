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

import javax.annotation.Nonnull;

/**
 * A filter used for coverage that manage class inclusion/exclusion.
 */
public class CoverageFilter {
  /**
   * The packages that are excluded from code coverage by default.
   */
  // package visible for testing.
  static final String[] EXCLUDED_PACKAGES = {
      "org.jacoco.*", // JaCoCo
      "com.vladium.emma.*" // EMMA
  };

  /**
   * Set of classes included in code coverage instrumentation.
   */
  @Nonnull
  private final CoverageFilterSet includes;

  /**
   * Set of classes excluded from code coverage instrumentation.
   */
  @Nonnull
  private final CoverageFilterSet excludes;

  public CoverageFilter(@Nonnull CoverageFilterSet includes, @Nonnull CoverageFilterSet excludes) {
    this.includes = includes.makeCopy();
    this.excludes = excludes.makeCopy();
    for (String packageName : EXCLUDED_PACKAGES) {
      this.excludes.addPattern(new CoveragePattern(packageName));
    }
  }

  public CoverageFilter() {
    this(new CoverageFilterSet(), new CoverageFilterSet());
  }

  /**
   * Indicates whether code coverage must be applied to the class identified by the given name.
   *
   * @param className a fully-qualified class name
   * @return true if the given class name is included but not excluded from coverage.
   */
  public boolean matches(@Nonnull String className) {
    if (excludes.matchesAny(className)) {
      // At least one pattern excludes it.
      return false;
    }
    if (includes.isEmpty()) {
      // By default, we include everything.
      return true;
    } else {
      return includes.matchesAny(className);
    }
  }
}
