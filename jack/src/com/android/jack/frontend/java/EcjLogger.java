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

package com.android.jack.frontend.java;

import com.android.jack.reporting.Reporter;
import com.android.jack.reporting.Reporter.Severity;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.batch.Main.Logger;

import java.io.PrintWriter;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * ECJ {@link Logger} for Jack.
 */
public class EcjLogger extends Logger {

  @Nonnull
  private final JackBatchCompiler jackBatchCompiler;

  @CheckForNull
  private Reporter reporter;

  public EcjLogger(@Nonnull Main main, @Nonnull PrintWriter out, @Nonnull PrintWriter err,
      @Nonnull JackBatchCompiler jackBatchCompiler) {
    super(main, out, err);
    this.jackBatchCompiler = jackBatchCompiler;
  }

  @Override
  public int logProblems(@Nonnull CategorizedProblem[] problems, @Nonnull char[] unitSource,
      @Nonnull Main currentMain) {
    return report(problems, currentMain);
  }

  private int report(@Nonnull CategorizedProblem[] problems, @Nonnull Main currentMain) {
    if (reporter == null) {
      // lazy because the Reporter is not yet available when the EcjLogger is instantiated.
      reporter = jackBatchCompiler.getReporter();
    }

    int numErrors = 0;
    for (CategorizedProblem problem : problems) {
      if (problem != null) {
        reporter.report(Severity.NON_FATAL, new EcjProblem(problem));
        currentMain.globalProblemsCount++;
        if (problem.isError()) {
          currentMain.globalErrorsCount++;
          numErrors++;
        } else if (problem.isWarning()) {
          currentMain.globalWarningsCount++;
        }
      }
    }
    return numErrors;
  }


  @Override
  public void loggingExtraProblems(@Nonnull Main currentMain) {
    List<CategorizedProblem> extras = jackBatchCompiler.getExtraProblems();
    if (extras != null) {
      report(extras.toArray(new CategorizedProblem[extras.size()]), currentMain);
    }
  }
}