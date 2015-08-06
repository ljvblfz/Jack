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

package com.android.jill.api.v01.impl;

import com.android.jill.Main;
import com.android.jill.Options;
import com.android.jill.api.v01.Cli01Config;
import com.android.jill.api.v01.Cli01TranslationTask;
import com.android.jill.api.v01.ConfigurationException;

import org.kohsuke.args4j.CmdLineException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class provides the version 01 implementation of Jill CLI.
 */
public class Cli01ConfigImpl implements Cli01Config {

  @CheckForNull
  private PrintStream standardError;
  @CheckForNull
  private File workingDirectory;

  @Override
  @Nonnull
  public Cli01TranslationTask getTask(@Nonnull String[] args) throws ConfigurationException {
    try {
      Options options = Main.getOptions(args);
      options.setStandardError(standardError);
      options.setWorkingDirectory(workingDirectory);
      return new Cli01TranslationTaskImpl(options);
    } catch (CmdLineException e) {
      throw new ConfigurationException(e.getMessage(), e);
    } catch (IOException e) {
      throw new ConfigurationException(e.getMessage(), e);
    }
  }

  private static class Cli01TranslationTaskImpl extends Main implements Cli01TranslationTask {

    @Nonnull
    private final Options options;

    public Cli01TranslationTaskImpl(@Nonnull Options options) {
      this.options = options;
    }

    @Override
    public int run() {
      PrintStream err = options.getStandardError();
      if (err == null) {
        err = System.err;
      }
      return runJill(err, options);
    }

  }

  @Override
  public void setStandardError(@Nonnull PrintStream standardError) {
    this.standardError = standardError;
  }

  @Override
  public void setStandardOutput(@Nonnull PrintStream standardOutput) {
  }

  @Override
  public void setWorkingDirectory(@Nonnull File workingDirectory) {
    this.workingDirectory = workingDirectory;
  }
}

