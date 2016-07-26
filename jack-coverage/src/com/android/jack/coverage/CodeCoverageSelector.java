/*
 * Copyright (C) 2015 The Android Open Source Project
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

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.formatter.SourceFormatter;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * A schedulable that selects classes that needs to be instrumented based on a given filter.
 * All classes selected for code coverage are marked with a {@link CodeCoverageMarker} marker.
 */
@Description("Filters classes for code coverage")
@Support(CodeCoverage.class)
@Constraint(need = OriginalNames.class)
@Transform(add = CodeCoverageMarker.Initialized.class)
@Protect(add = JDefinedClassOrInterface.class)
public class CodeCoverageSelector implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  private static final SourceFormatter formatter = SourceFormatter.getFormatter();

  /**
   * A {@link CoverageFilter} singleton used to cache include and exclude properties.
   */
  @Nonnull
  private final CoverageFilter filter =
      new CoverageFilter(
          ThreadConfig.get(CodeCoverage.COVERAGE_JACOCO_INCLUDES),
          ThreadConfig.get(CodeCoverage.COVERAGE_JACOCO_EXCLUDES));

  @Override
  public void run(@Nonnull JDefinedClassOrInterface t) {
    if (needsCoverage(t)) {
      t.addMarker(new CodeCoverageMarker());
    }
  }

  private boolean needsCoverage(@Nonnull JDefinedClassOrInterface declaredType) {
    if (!declaredType.isToEmit()) {
      // Do not instrument classes that will no be part of the output.
      return false;
    }
    if (declaredType instanceof JDefinedInterface) {
      // Interface are not covered.
      return false;
    }
    // Manage class filtering.
    String typeName = formatter.getName(declaredType);
    return filter.matches(typeName);
  }
}
