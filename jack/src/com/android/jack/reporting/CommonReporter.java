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
import com.android.jack.frontend.java.EcjProblem;
import com.android.jack.reporting.Reportable.ProblemLevel;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.OutputStreamFile;

import org.eclipse.jdt.core.compiler.CategorizedProblem;

import java.io.PrintStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingDeque;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;


/**
 * A common implementation of {@link Reporter}.
 */
abstract class CommonReporter implements Reporter {

  @Nonnull
  private final VerbosityLevel verbosityLevel = ThreadConfig.get(Options.VERBOSITY_LEVEL);

  @Nonnull
  private final LinkedBlockingDeque<ProblemDescription> toProcess =
      new LinkedBlockingDeque<ProblemDescription>();

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
    final Thread reporterThread = new Thread(new RunReporter());
    reporterThread.start();
    Jack.getSession().getHooks().addHook(new Runnable() {
      @Override
      public void run() {
        reporterThread.interrupt();
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
    if (reportable instanceof EcjProblem) {
      assert severity == Severity.NON_FATAL;
      CategorizedProblem problem = ((EcjProblem) reportable).getProblem();
      printFilteredProblem(reportable.getDefaultProblemLevel(),
          reportable.getMessage(),
          problem.getOriginatingFileName() != null ?
              String.valueOf(problem.getOriginatingFileName()) : null,
          problem.getSourceLineNumber(),
          -1 /* endLine */,
          problem.getSourceEnd(),
          -1 /* endColumn */);
    } else {
      // default behavior
      if (severity == Severity.FATAL) {
        printFilteredProblem(ProblemLevel.ERROR, reportable.getMessage());
      } else {
        printFilteredProblem(reportable.getDefaultProblemLevel(), reportable.getMessage());
      }
    }
  }

  private void printFilteredProblem(@Nonnull ProblemLevel problemLevel, @Nonnull String message) {
    printFilteredProblem(problemLevel, message, null /* fileName */, -1, -1, -1, -1);
  }

  protected abstract void printFilteredProblem(@Nonnull ProblemLevel problemLevel,
      @Nonnull String message,
      @CheckForNull String fileName,
      int startLine,
      int endLine,
      int startColumn,
      int endColumn);

  class RunReporter implements Runnable {

    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          ProblemDescription current = toProcess.takeFirst();
          handleProblem(current.getSeverity(), current.getReportable());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
      while (!toProcess.isEmpty()) {
        ProblemDescription current = toProcess.poll();
        handleProblem(current.getSeverity(), current.getReportable());
      }
    }

  }

  static class ProblemDescription {

    @Nonnull
    private final Severity severity;

    @Nonnull
    private final Reportable reportable;

    ProblemDescription(@Nonnull Severity severity, @Nonnull Reportable reportable) {
      this.severity = severity;
      this.reportable = reportable;
    }

    @Nonnull
    public Severity getSeverity() {
      return severity;
    }

    @Nonnull
    public Reportable getReportable() {
      return reportable;
    }

  }
}
