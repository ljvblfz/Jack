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

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.Options.VerbosityLevel;
import com.android.jack.ir.HasSourceInfo;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.reporting.Reportable.ProblemLevel;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.OutputStreamFile;
import com.android.sched.util.log.ThreadWithTracer;

import java.io.PrintStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;


/**
 * A common implementation of {@link Reporter}.
 */
abstract class CommonReporter implements Reporter {
  @Nonnull
  private static final Logger logger = Logger.getLogger(CommonReporter.class.getName());

  @Nonnull
  private final VerbosityLevel verbosityLevel = ThreadConfig.get(Options.VERBOSITY_LEVEL);

  @Nonnull
  private final LinkedBlockingDeque<Problem> toProcess =
      new LinkedBlockingDeque<Problem>();

  @Nonnull
  protected final PrintStream streamByDefault = ThreadConfig.get(REPORTER_OUTPUT_STREAM)
      .getPrintStream();

  @Nonnull
  protected final Map<ProblemLevel, PrintStream> streamByLevel =
      new EnumMap<ProblemLevel, PrintStream>(ProblemLevel.class);

  @Nonnull
  protected final PrintStream reporterStream =
      ThreadConfig.get(REPORTER_OUTPUT_STREAM).getPrintStream();

  protected CommonReporter() {
    for (final Entry<ProblemLevel, OutputStreamFile> entry : ThreadConfig.get(
        Reporter.REPORTER_OUTPUT_STREAM_BY_LEVEL).entrySet()) {
      streamByLevel.put(entry.getKey(), entry.getValue().getPrintStream());
    }
    final Thread reporterThread = new ThreadWithTracer(new RunReporter(), "Jack reporter");
    reporterThread.start();
    Jack.getSession().getHooks().addHook(new Runnable() {
      @Override
      public void run() {
        toProcess.add(ReportingDone.INSTANCE);
        try {
          reporterThread.join();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    });
  }

  @Override
  public void report(@Nonnull Severity severity, @Nonnull Reportable reportable) {
    if (severity == Severity.FATAL
        || reportable.getDefaultProblemLevel().isVisibleWith(verbosityLevel)) {
      toProcess.add(new ProblemDescription(severity, reportable));
    }
  }

  private void handleProblem(@Nonnull Severity severity, @Nonnull Reportable reportable) {
    ProblemLevel problemLevel;
    if (severity == Severity.FATAL) {
      problemLevel = ProblemLevel.ERROR;
    } else {
      problemLevel = reportable.getDefaultProblemLevel();
    }

    if (reportable instanceof HasSourceInfo) {
      printFilteredProblem(problemLevel,
          reportable.getMessage(), ((HasSourceInfo) reportable).getSourceInfo());
    } else {
      printFilteredProblem(problemLevel, reportable.getMessage());
    }
  }

  private void printFilteredProblem(@Nonnull ProblemLevel problemLevel, @Nonnull String message) {
    printFilteredProblem(problemLevel, message, SourceInfo.UNKNOWN);
  }

  protected abstract void printFilteredProblem(@Nonnull ProblemLevel problemLevel,
      @Nonnull String message, @Nonnull SourceInfo sourceInfo);

  class RunReporter implements Runnable {

    @Override
    public void run() {
      try {
        Problem current;
        while ((current = toProcess.takeFirst()) != ReportingDone.INSTANCE) {
          handleProblem(current.getSeverity(), current.getReportable());
        }
      } catch (InterruptedException e) {
        logger.log(Level.FINE, "Reporter thread '" + Thread.currentThread().getName()
            + "' was interrupted");
        Thread.currentThread().interrupt();
      }
    }

  }

  private static interface Problem {
    @Nonnull
    Severity getSeverity();

    @Nonnull
    Reportable getReportable();
  }

  private static class ProblemDescription implements Problem {

    @Nonnull
    private final Severity severity;

    @Nonnull
    private final Reportable reportable;

    ProblemDescription(@Nonnull Severity severity, @Nonnull Reportable reportable) {
      this.severity = severity;
      this.reportable = reportable;
    }

    @Override
    @Nonnull
    public Severity getSeverity() {
      return severity;
    }

    @Override
    @Nonnull
    public Reportable getReportable() {
      return reportable;
    }

  }

  /**
   * Fake Problem injected in queue as a last message indicating the end of the reporting.
   */
  private static class ReportingDone implements Problem {
    @Nonnull
    public static final ReportingDone INSTANCE = new ReportingDone();

    private ReportingDone() {
    }
    @Override
    public Severity getSeverity() {
      throw new AssertionError();
    }

    @Override
    public Reportable getReportable() {
      throw new AssertionError();
    }
  }
}
