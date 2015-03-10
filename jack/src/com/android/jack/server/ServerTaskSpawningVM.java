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

import com.android.jack.Main;
import com.android.jack.util.ExecuteFile;
import com.android.sched.util.config.cli.TokenIterator;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.file.WrongPermissionException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * {@link ServerTask} implementation.
 */
public class ServerTaskSpawningVM implements ServerTask {

  @Override
  public int run(@Nonnull PrintStream out, @Nonnull PrintStream err, @Nonnull File pwd,
      @Nonnull TokenIterator args) {
    String jackJarPath = getJackJarPath();
    if (jackJarPath == null) {
      return ServerExitStatus.FAILURE_JACK_JAR_NOT_FOUND;
    }

    List<String> commandLineArgs;
    try {
      commandLineArgs = buildArgs(jackJarPath, args);
    } catch (Exception e) {
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
      return ServerExitStatus.FAILURE_UNKNOWN;
    }
  }

  @Nonnull
  private List<String> buildArgs(@Nonnull String jackJarPath, @Nonnull TokenIterator args)
      throws NoSuchElementException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileOrDirectoryException,
      CannotReadException {

    List<String> commandLineArgs = new ArrayList<String>();
    commandLineArgs.add("java");
    commandLineArgs.add("-jar");
    commandLineArgs.add(jackJarPath);

    while (args.hasNext()) {
      commandLineArgs.add(args.next());
    }

    return commandLineArgs;
  }

  @CheckForNull
  private String getJackJarPath() {
    CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
    if (codeSource != null) {
      URL location = codeSource.getLocation();
      try {
        if (location != null && location.toString().endsWith(".jar")) {
          return location.toURI().getPath();
        }
      } catch (URISyntaxException e) {
        // Fails to locate jack jar file, return null at the end of method
      }
    }
    return null;
  }
}
