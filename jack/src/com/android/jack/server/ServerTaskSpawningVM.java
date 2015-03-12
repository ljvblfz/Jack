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

package com.android.jack.server;

import com.android.jack.util.ExecuteFile;
import com.android.sched.util.config.cli.TokenIterator;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.InputStreamFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.file.WrongPermissionException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * {@link ServerTask} implementation that launches compilations with Jack into a spawned VM.
 */
public class ServerTaskSpawningVM implements ServerTask {

  private static class MissingEnvException extends Exception {
    private static final long serialVersionUID = 1L;
    @Nonnull
    private final String missingVariable;

    public MissingEnvException(@Nonnull String missingVariable) {
      this.missingVariable = missingVariable;
    }

    @Nonnull
    @Override
    public String getMessage() {
      return "Environment variable '" + missingVariable + "' is undefined";
    }
  }
  @Nonnull
  private static Logger logger = Logger.getLogger(ServerTaskSpawningVM.class.getSimpleName());

  @Override
  public int run(@Nonnull PrintStream out, @Nonnull PrintStream err, @Nonnull File pwd,
      @Nonnull TokenIterator args) {
    List<String> commandLineArgs;
    try {
      commandLineArgs = buildArgs(args);
    } catch (NoSuchFileException e) {
      return ServerExitStatus.FAILURE_JACK_JAR_NOT_FOUND;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Failed to build command line", e);
      return ServerExitStatus.FAILURE_USAGE;
    }

    ExecuteFile exec = new ExecuteFile(commandLineArgs.toArray(new String[commandLineArgs.size()]));
    exec.setErr(err);
    exec.setOut(out);

    try {
      exec.setWorkingDir(pwd, /* create */false);
    } catch (IOException e) {
      // It means that pwd is not a directory
      return ServerExitStatus.FAILURE_USAGE;
    }

    try {
      return exec.run();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Failed to run command " + commandLineArgs, e);

      return ServerExitStatus.FAILURE_UNKNOWN;
    }
  }

  @Nonnull
  private List<String> buildArgs(@Nonnull TokenIterator args)
      throws NoSuchElementException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileOrDirectoryException,
      CannotReadException, MissingEnvException {

    List<String> commandLineArgs = new ArrayList<String>();
    String vmCommand = System.getenv("JACK_VM_COMMAND");
    if (vmCommand == null) {
      throw new MissingEnvException("JACK_VM_COMMAND");
    }
    String jackJarPath = System.getenv("JACK_JAR");
    if (jackJarPath == null) {
      throw new MissingEnvException("JACK_JAR");
    }
      new InputStreamFile(jackJarPath);

    StreamTokenizer iter = getCommandLineTokenizer(vmCommand);
    try {
      while (iter.nextToken() != StreamTokenizer.TT_EOF) {
        commandLineArgs.add(iter.sval);
      }
    } catch (IOException e) {
      throw new AssertionError();
    }
    commandLineArgs.add("-jar");
    commandLineArgs.add(jackJarPath);

    while (args.hasNext()) {
      commandLineArgs.add(args.next());
    }

    return commandLineArgs;
  }

  @Nonnull
  private static StreamTokenizer getCommandLineTokenizer(@Nonnull String command) {
    StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(command));

    tokenizer.resetSyntax();
    tokenizer.wordChars(0, 255);
    tokenizer.whitespaceChars(' ', ' ');
    tokenizer.whitespaceChars('\t', '\t');
    tokenizer.whitespaceChars('\n', '\n');
    tokenizer.whitespaceChars('\r', '\r');
    tokenizer.quoteChar('\'');
    tokenizer.quoteChar('\"');
    tokenizer.eolIsSignificant(false);
    tokenizer.slashSlashComments(false);
    tokenizer.slashStarComments(false);
    tokenizer.lowerCaseMode(false);

    return tokenizer;
  }
}
