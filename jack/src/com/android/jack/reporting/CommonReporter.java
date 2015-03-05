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

import com.android.jack.Options;
import com.android.jack.Options.VerbosityLevel;
import com.android.jack.frontend.java.EcjProblem;
import com.android.jack.reporting.Reportable.ProblemLevel;
import com.android.sched.util.config.ThreadConfig;

import org.eclipse.jdt.core.compiler.CategorizedProblem;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;


/**
 * A common implementation of {@link Reporter}.
 */
abstract class CommonReporter implements Reporter {

  private final VerbosityLevel verbosityLevel = ThreadConfig.get(Options.VERBOSITY_LEVEL);

  @Override
  public void report(@Nonnull Severity severity, @Nonnull Reportable reportable) {
    if (reportable instanceof EcjProblem) {
      assert severity == Severity.NON_FATAL;
      CategorizedProblem problem = ((EcjProblem) reportable).getProblem();
      printProblem(reportable.getDefaultProblemLevel(),
          reportable.getMessage(),
          String.valueOf(problem.getOriginatingFileName()),
          problem.getSourceLineNumber(),
          -1 /* endLine */,
          problem.getSourceEnd(),
          -1 /* endColumn */);
    } else {
      // default behavior
      if (severity == Severity.FATAL) {
        printProblem(ProblemLevel.ERROR, reportable.getMessage());
      } else {
        printProblem(reportable.getDefaultProblemLevel(), reportable.getMessage());
      }
    }
  }

  private void printProblem(@Nonnull ProblemLevel problemLevel, @Nonnull String message) {
    printProblem(problemLevel, message, null /* fileName */, -1, -1, -1, -1);
  }

  protected void printProblem(@Nonnull ProblemLevel problemLevel,
      @Nonnull String message,
      @CheckForNull String fileName,
      int startLine,
      int endLine,
      int startColumn,
      int endColumn) {
    switch (problemLevel) {
      case ERROR:
        printFilteredProblem(problemLevel,
            message,
            fileName,
            startLine,
            endLine,
            startColumn,
            endColumn);
        break;
      case WARNING:
        if (verbosityLevel == VerbosityLevel.TRACE || verbosityLevel == VerbosityLevel.DEBUG
            || verbosityLevel == VerbosityLevel.INFO || verbosityLevel == VerbosityLevel.WARNING) {
          printFilteredProblem(problemLevel,
              message,
              fileName,
              startLine,
              endLine,
              startColumn,
              endColumn);
        }
        break;
      case INFO:
        if (verbosityLevel == VerbosityLevel.TRACE || verbosityLevel == VerbosityLevel.DEBUG
            || verbosityLevel == VerbosityLevel.INFO) {
          printFilteredProblem(problemLevel,
              message,
              fileName,
              startLine,
              endLine,
              startColumn,
              endColumn);
        }
        break;
      default:
        throw new AssertionError();
    }
  }

  protected abstract void printFilteredProblem(@Nonnull ProblemLevel problemLevel,
      @Nonnull String message,
      @CheckForNull String fileName,
      int startLine,
      int endLine,
      int startColumn,
      int endColumn);
}
