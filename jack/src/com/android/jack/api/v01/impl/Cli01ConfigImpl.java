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

import com.android.jack.IllegalOptionsException;
import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.api.v01.Cli01CompilationTask;
import com.android.jack.api.v01.Cli01Config;
import com.android.jack.api.v01.ConfigurationException;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.cli.TokenIterator;
import com.android.sched.util.location.NoLocation;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A configuration implementation for CLI level 01 of the Jack compiler.
 */
public class Cli01ConfigImpl implements Cli01Config {

  @Nonnull
  private PrintStream standardOutput = System.out;
  @Nonnull
  private PrintStream standardError = System.err;
  @Nonnull
  private File workingDirectory = new File(".");

  @Override
  @Nonnull
  public Cli01CompilationTask getTask(@Nonnull String[] args) throws ConfigurationException {
    Options options = new Options();
    options.setWorkingDirectory(workingDirectory);
    options.setStandardError(standardError);
    options.setStandardOutput(standardOutput);
    RunnableHooks configHooks = new RunnableHooks();
    try {
      TokenIterator iterator = new TokenIterator(new NoLocation(), args);
      iterator = iterator.withFileRelativeTo(workingDirectory);
      List<String> list = new ArrayList<String>();
      while (iterator.hasNext()) {
        list.add(iterator.next());
      }
      CmdLineParser parser =
          new CmdLineParser(options, ParserProperties.defaults().withUsageWidth(100));

      parser.parseArgument(list);
      parser.stopOptionParsing();

      Jack.check(options, configHooks);
    } catch (com.android.sched.util.config.ConfigurationException e) {
      throw new ConfigurationException(e.getMessage(), e);
    } catch (IllegalOptionsException e) {
      throw new ConfigurationException(e.getMessage(), e);
    } catch (CmdLineException e) {
      throw new ConfigurationException(e.getMessage(), e);
    } catch (IOException e) {
      throw new ConfigurationException(e.getMessage(), e);
    }

    return new Cli01CompilationTaskImpl(this, options, args);
  }

  @Override
  public void setStandardError(@Nonnull PrintStream standardError) {
    this.standardError = standardError;
  }

  @Nonnull
  public PrintStream getStandardError() {
    return standardError;
  }

  @Override
  public void setStandardOutput(@Nonnull PrintStream standardOutput) {
    this.standardOutput = standardOutput;
  }

  @Nonnull
  public PrintStream getStandardOutput() {
    return standardOutput;
  }

  @Override
  public void setWorkingDirectory(@Nonnull File workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  @Nonnull
  public File getWorkingDirectory() {
    return workingDirectory;
  }
}
