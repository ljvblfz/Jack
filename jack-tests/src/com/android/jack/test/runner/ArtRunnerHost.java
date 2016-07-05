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

import com.google.common.base.Joiner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link RuntimeRunner} is used to run tests on art running on host.
 */
public class ArtRunnerHost extends HostRunner {

  private boolean isDebugMode = false;

  public ArtRunnerHost(@Nonnull File rtEnvironmentRootDir) {
    super(rtEnvironmentRootDir);
  }

  public ArtRunnerHost setDebugMode(boolean isDebugMode) {
    this.isDebugMode = isDebugMode;
    return this;
  }

  @Override
  public int runJUnit(@Nonnull String[] options, @Nonnull String jUnitRunnerName,
      @Nonnull String[] jUnitTestClasses, @Nonnull File... classpathFiles)
      throws RuntimeRunnerException {
    return runOnHost(
        buildCommandLineJunit(options, jUnitRunnerName, jUnitTestClasses, classpathFiles),
        "ANDROID_HOST_OUT");
  }

  @Override
  public int run(@Nonnull String[] options, @Nonnull String mainClass,
      @Nonnull File... classpathFiles) throws RuntimeRunnerException {
    return runOnHost(buildCommandLine(options, mainClass, classpathFiles),
        "ANDROID_HOST_OUT");
  }

  @Nonnull
  private List<String> buildCommandLine(@Nonnull String[] options, @Nonnull String mainClass,
      @Nonnull File... classpathFiles) {
    List<String> commandLine = new ArrayList<String>();

    addStartOfCommandLine(options, classpathFiles, commandLine);
    commandLine.add(mainClass);

    return commandLine;
  }

  @Nonnull
  private List<String> buildCommandLineJunit(@Nonnull String[] options,
      @CheckForNull String jUnitRunnerName, @Nonnull String[] jUnitTestClasses,
      @Nonnull File... classpathFiles) {
    List<String> commandLine = new ArrayList<String>();

    addStartOfCommandLine(options, classpathFiles, commandLine);

    commandLine.add(jUnitRunnerName);

    for (String className : jUnitTestClasses) {
      commandLine.add(className);
    }
    return commandLine;
  }

  protected void addStartOfCommandLine(@Nonnull String[] options, @Nonnull File[] classpathFiles,
      @Nonnull List<String> commandLine) {
    commandLine.add(rtEnvironmentRootDir.getAbsolutePath() + "/bin/art");

    if (isDebugMode) {
      commandLine.add("-d");
    }

    commandLine.add("-Xcompiler-option");
    commandLine.add("--abort-on-hard-verifier-error");

    for (String option : options) {
      commandLine.add(option);
    }

    File frameworkDir = new File(rtEnvironmentRootDir, "framework");
    File coreImage = new File(frameworkDir, "core.art");
    commandLine.add("-Ximage:" + coreImage.getAbsolutePath());

    commandLine.add("-classpath");

    commandLine.add(Joiner.on(File.pathSeparatorChar).join(classpathFiles));
  }

}
