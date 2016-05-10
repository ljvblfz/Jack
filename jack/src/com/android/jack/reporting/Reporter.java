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

package com.android.jack.reporting;

import com.android.jack.config.id.Arzon;
import com.android.jack.config.id.Brest;
import com.android.jack.config.id.Carnac;
import com.android.jack.reporting.Reportable.ProblemLevel;
import com.android.sched.util.codec.EnumCodec;
import com.android.sched.util.codec.ListCodec;
import com.android.sched.util.codec.OutputStreamCodec;
import com.android.sched.util.codec.PairCodec;
import com.android.sched.util.codec.PairCodec.Pair;
import com.android.sched.util.codec.PairListToMapCodecConverter;
import com.android.sched.util.codec.VariableName;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.ImplementationPropertyId;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.config.id.PropertyId.ShutdownRunnable;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.OutputStreamFile;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * A tool that allows to report {@link Reportable} objects.
 */
@HasKeyId
@VariableName("reporter")
public interface Reporter {

  /**
   * Whether the {@link Reportable} object is fatal or not.
   */
  public static enum Severity {
    FATAL, NON_FATAL
  }

  @Nonnull
  public static final ImplementationPropertyId<Reporter> REPORTER = ImplementationPropertyId
      .create("jack.reporter", "Define which reporter will be used", Reporter.class)
      .addDefaultValue("default").addCategory(Arzon.class);

  @Nonnull
  public static final PropertyId<OutputStreamFile> REPORTER_OUTPUT_STREAM = PropertyId.create(
      "jack.reporter.file", "File where the reporter will write",
      new OutputStreamCodec(Existence.MAY_EXIST).allowStandardOutputOrError())
      .addDefaultValue("--").requiredIf(REPORTER.getClazz().isImplementedBy(DefaultReporter.class)
          .or(REPORTER.getClazz().isImplementedBy(SdkReporter.class))).addCategory(Brest.class);

  @Nonnull
  public static final PropertyId<Map<ProblemLevel, OutputStreamFile>>
                                                                  REPORTER_OUTPUT_STREAM_BY_LEVEL =
      PropertyId
          .create(
              "jack.reporter.level.file",
              "File where the reporter will write by level",
              new PairListToMapCodecConverter<ProblemLevel, OutputStreamFile>(
                  new ListCodec<Pair<ProblemLevel, OutputStreamFile>>(
                      new PairCodec<ProblemLevel, OutputStreamFile>(new EnumCodec<ProblemLevel>(
                          ProblemLevel.class, ProblemLevel.values()).ignoreCase(),
                          new OutputStreamCodec(Existence.MAY_EXIST).allowStandardOutputOrError())
                          .on("=")).setMin(0)))
          .addDefaultValue(Collections.<ProblemLevel, OutputStreamFile>emptyMap())
          .addCategory(Carnac.class)
          .setShutdownHook(new ShutdownRunnable<Map<ProblemLevel, OutputStreamFile>>() {
            @Override
            public void run(@Nonnull Map<ProblemLevel, OutputStreamFile> map) {
              for (OutputStreamFile osf : map.values()) {
                osf.getPrintStream().close();
              }
            }
          });

  public void report(@Nonnull Severity severity, @Nonnull Reportable reportable);
}
