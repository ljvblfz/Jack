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

package com.android.jack.api.v01.impl;

import com.android.jack.CommandLine;
import com.android.jack.ExitStatus;
import com.android.jack.IllegalOptionsException;
import com.android.jack.Options;
import com.android.jack.api.v01.Cli01CompilationTask;
import com.android.jack.plugin.Plugin;
import com.android.sched.util.log.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

class Cli01CompilationTaskImpl extends CommandLine implements Cli01CompilationTask {


  @Nonnull
  private final PrintStream standardError;
  @Nonnull
  private final PrintStream standardOutput;

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final Options options;
  private final String[] args;

  public Cli01CompilationTaskImpl(@Nonnull Cli01ConfigImpl config, @Nonnull Options options,
      @Nonnull String[] args) {
    this.options = options;
    this.standardError = config.getStandardError();
    this.standardOutput = config.getStandardOutput();
    this.args = args;
  }

  @Override
  public int run() {

    if (args.length == 0) {
      printVersion(standardOutput);
      standardError.println("Try --help for help.");
      return ExitStatus.SUCCESS;
    }

    if (options.askForHelp()) {
      printUsage(standardOutput);
      return ExitStatus.SUCCESS;
    }

    if (options.askForPropertiesHelp()) {
      try {
        printHelpProperties(standardOutput, options);
        return ExitStatus.SUCCESS;
      } catch (IOException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        return ExitStatus.FAILURE_UNKNOWN;
      } catch (IllegalOptionsException e) {
        standardError.println(e.getMessage());
        return ExitStatus.FAILURE_USAGE;
      }
    }

    if (options.askForVersion()) {
      printVersion(System.out);
      try {
        // STOPSHIP remove call
        options.ensurePluginManager();
        for (Plugin plugin : options.getPluginManager().getPlugins()) {
          printVersion(System.out, plugin);
        }

        return ExitStatus.SUCCESS;
      } catch (IllegalOptionsException e) {
        standardError.println(e.getMessage());
        return ExitStatus.FAILURE_USAGE;
      }
    }

    return runJack(standardError, options);
  }
}
