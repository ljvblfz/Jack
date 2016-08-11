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

package com.android.jack.test.runner;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * Common class for tools used to run JUnit tests or main classes.
 */
public abstract class RuntimeRunner {

  protected boolean isVerbose = false;

  public abstract int run(@Nonnull String[] options, @Nonnull String mainClasses,
      @Nonnull File... classpathFiles) throws RuntimeRunnerException;

  public abstract int runJUnit(@Nonnull String[] options, @Nonnull String jUnitRunnerName,
      @Nonnull String[] jUnitTestClasses, @Nonnull File... classpathFiles)
      throws RuntimeRunnerException;

  @Nonnull
  public RuntimeRunner setVerbose(boolean isVerbose) {
    this.isVerbose = isVerbose;
    return this;
  }

}
