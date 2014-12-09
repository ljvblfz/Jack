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
    return runJunitOnDevice(options, jUnitRunnerName, jUnitTestClasses,
        classpathFiles);
  }

  @Override
  @Nonnull
  protected List<String> buildCommandLine(@Nonnull String[] options, @Nonnull String[] mainClasses,
      @Nonnull File... classpathFiles) {
    List<String> args = new ArrayList<String>();

    args.add(rtEnvironmentRootDir.getAbsolutePath() + "/bin/dalvikvm");

    args.add(mode.getArg());

    for (String option : options) {
      args.add(option);
    }

    args.add("-classpath");
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < classpathFiles.length; i++) {
      if (i > 0) {
        sb.append(File.pathSeparatorChar);
      }
      sb.append(classpathFiles[i].getAbsolutePath());
    }
    args.add(sb.toString());

    for (String className : mainClasses) {
      args.add(className);
    }
    return args;
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
