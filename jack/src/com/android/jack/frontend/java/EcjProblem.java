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

import com.android.jack.reporting.Reportable;

import org.eclipse.jdt.core.compiler.CategorizedProblem;

import javax.annotation.Nonnull;

/**
 * A {@link Reportable} that contains ECJ {@link CategorizedProblem}s.
 */
public class EcjProblem implements Reportable {

  private static final int isClassPathCorrectId = 0x01000000 + 324;

  @Nonnull
  private final CategorizedProblem problem;

  public EcjProblem(@Nonnull CategorizedProblem problem) {
    this.problem = problem;
  }

  @Nonnull
  public CategorizedProblem getProblem() {
    return problem;
  }

  @Override
  @Nonnull
  public ProblemLevel getDefaultProblemLevel() {
    return problem.isError() ? ProblemLevel.ERROR : ProblemLevel.WARNING;
  }

  @Override
  @Nonnull
  public String getMessage() {
    String message = null;
    if (problem.getID() == isClassPathCorrectId) {
      assert problem.getArguments().length == 1;
      message = "The type " + problem.getArguments()[0]
          + " cannot be found in source files, imported jack libs or the classpath";
    } else {
      message = problem.getMessage();
    }
    return message;
  }

}
