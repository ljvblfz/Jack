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

import com.android.jack.test.toolchain.AbstractTestTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link RuntimeRunner} is used to run tests on art running on host.
 */
public class ArtRunnerHost extends HostRunner {

  public ArtRunnerHost(@Nonnull File rtEnvironmentRootDir) {
    super(rtEnvironmentRootDir);
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
    List<String> args = new ArrayList<String>();

    addStartOfCommandLine(options, classpathFiles, args);
    args.add(mainClass);

    return args;
  }

  @Nonnull
  private List<String> buildCommandLineJunit(@Nonnull String[] options,
      @CheckForNull String jUnitRunnerName, @Nonnull String[] jUnitTestClasses,
      @Nonnull File... classpathFiles) {
    List<String> args = new ArrayList<String>();

    addStartOfCommandLine(options, classpathFiles, args);

    args.add(jUnitRunnerName);

    for (String className : jUnitTestClasses) {
      args.add(className);
    }
    return args;
  }

  private void addStartOfCommandLine(@Nonnull String[] options, @Nonnull File[] classpathFiles,
      @Nonnull List<String> result) {
    result.add(rtEnvironmentRootDir.getAbsolutePath() + "/bin/art");

    for (String option : options) {
      result.add(option);
    }

    File frameworkDir = new File(rtEnvironmentRootDir, "framework");
    List<File> files = AbstractTestTools.getFiles(frameworkDir, ".jar");
    String bootClasspath = Joiner.on(File.pathSeparatorChar).join(files);

    result.add("-Xbootclasspath:" + bootClasspath);

    result.add("-classpath");

    result.add(Joiner.on(File.pathSeparatorChar).join(classpathFiles));
  }

}
