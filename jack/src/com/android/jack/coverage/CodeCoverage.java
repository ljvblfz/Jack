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

import com.android.jack.library.DumpInLibrary;
import com.android.sched.item.Description;
import com.android.sched.item.Feature;
import com.android.sched.util.codec.OutputStreamCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.OutputStreamFile;

import javax.annotation.Nonnull;

/**
 * A {@link Feature} to generate code coverage.
 */
@Description("Support code coverage")
@HasKeyId
public class CodeCoverage implements Feature {

  @Nonnull
  public static final BooleanPropertyId CODE_COVERVAGE = BooleanPropertyId
      .create("jack.coverage", "Enable code coverage")
      .addDefaultValue(Boolean.FALSE).addCategory(DumpInLibrary.class);

  @Nonnull
  public static final PropertyId<OutputStreamFile> COVERAGE_METADATA_FILE = PropertyId.create(
      "jack.coverage.metadata.file", "File where the coverage metadata will be emitted",
      new OutputStreamCodec(Existence.MAY_EXIST).allowStandardOutputOrError())
      .requiredIf(CODE_COVERVAGE.getValue().isTrue());

  @Nonnull
  public static final PropertyId<JacocoPackage> COVERAGE_JACOCO_PACKAGE_NAME =
      PropertyId.create(
              "jack.coverage.jacoco.package",
              "The name of the JaCoCo package containing the classes that manage instrumentation.",
              new JacocoPackage.Codec())
          .requiredIf(CODE_COVERVAGE.getValue().isTrue())
          .addDefaultValue(new JacocoPackage(""));

  @Nonnull
  public static final PropertyId<CoverageFilterSet> COVERAGE_JACOCO_INCLUDES =
      PropertyId
          .create(
              "jack.coverage.jacoco.include",
              "Class names included in the code coverage instrumentation",
              new CoverageFilterSetCodec())
          .addDefaultValue(new CoverageFilterSet())
          .requiredIf(CODE_COVERVAGE.getValue().isTrue())
          .addCategory(DumpInLibrary.class);

  @Nonnull
  public static final PropertyId<CoverageFilterSet> COVERAGE_JACOCO_EXCLUDES =
      PropertyId
          .create(
              "jack.coverage.jacoco.exclude",
              "Class names excluded from the code coverage instrumentation",
              new CoverageFilterSetCodec())
          .addDefaultValue(new CoverageFilterSet())
          .requiredIf(CODE_COVERVAGE.getValue().isTrue())
          .addCategory(DumpInLibrary.class);
}

