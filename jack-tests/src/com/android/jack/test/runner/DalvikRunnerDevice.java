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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This {@link RuntimeRunner} is used to run tests on dalvik running on device.
 */
public class DalvikRunnerDevice extends DeviceRunner  implements DalvikRunner {

  @Nonnull
  private DalvikMode mode = DalvikMode.FAST;

  @Override
  public int runJUnit(@Nonnull String[] options, @Nonnull String jUnitRunnerName,
      @Nonnull String[] jUnitTestClasses, @Nonnull File... classpathFiles)
      throws RuntimeRunnerException {
    return runOnDevice(options, jUnitRunnerName, jUnitTestClasses,
        classpathFiles);
  }

  @Override
  public int run(@Nonnull String[] options, @Nonnull String mainClasses,
      @Nonnull File... classpathFiles) throws RuntimeRunnerException {
    return runOnDevice(options, /* jUnitRunnerName = */null, new String[] {mainClasses},
        classpathFiles);
  }

  @Override
  @Nonnull
  protected List<String> buildCommandLine(@Nonnull String[] options, @Nonnull String[] classes,
      @Nonnull File... classpathFiles) {
    List<String> commandLine = new ArrayList<String>();

    commandLine.add(rtEnvironmentRootDir.getAbsolutePath() + "/bin/dalvikvm");

    commandLine.add(mode.getArg());

    for (String option : options) {
      commandLine.add(option);
    }

    commandLine.add("-classpath");
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < classpathFiles.length; i++) {
      if (i > 0) {
        sb.append(File.pathSeparatorChar);
      }
      sb.append(classpathFiles[i].getAbsolutePath());
    }
    commandLine.add(sb.toString());

    for (String className : classes) {
      commandLine.add(className);
    }
    return commandLine;
  }

  @Override
  @Nonnull
  public DalvikRunnerDevice setMode(@Nonnull DalvikMode mode) {
    this.mode = mode;
    return this;
  }

  @Override
  @Nonnull
  protected String getRuntimeName() {
    return "DalvikVM";
  }
}
