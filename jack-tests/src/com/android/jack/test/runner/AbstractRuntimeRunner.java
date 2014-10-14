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
import java.io.OutputStream;
import java.io.PrintStream;

import javax.annotation.Nonnull;


/**
 * This {@link RuntimeRunner} can have its outputs redirected in user defined streams.
 */
public abstract class AbstractRuntimeRunner extends RuntimeRunner {

  @Nonnull
  protected PrintStream outRedirectStream = System.out;
  @Nonnull
  protected PrintStream errRedirectStream = System.err;

  protected AbstractRuntimeRunner(@Nonnull File rtEnvRootDir) {
    super(rtEnvRootDir);
  }

  @Override
  public abstract int run(@Nonnull String[] options, @Nonnull String[] mainClasses,
      @Nonnull File... classpathFiles) throws RuntimeRunnerException;

  @Nonnull
  public final AbstractRuntimeRunner setOutputStream(@Nonnull OutputStream outputStream) {
    if (outRedirectStream != null) {
      outRedirectStream.close();
    }
    outRedirectStream = new PrintStream(outputStream);
    return this;
  }

  @Nonnull
  public final AbstractRuntimeRunner setErrorStream(@Nonnull OutputStream errorStream) {
    if (errRedirectStream != null) {
      errRedirectStream.close();
    }
    errRedirectStream = new PrintStream(errorStream);
    return this;
  }

  @Nonnull
  public PrintStream getOutStream() {
    return outRedirectStream;
  }

  @Nonnull
  public PrintStream getErrStream() {
    return errRedirectStream;
  }

}
