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

package com.android.jack.test.toolchain;

import com.android.jack.test.util.ExecFileException;
import com.android.jack.test.util.ExecuteFile;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This {@link JackCliToolchain} uses Jack script
 */
public class JackScriptToolchain extends JackCliToolchain {

  public JackScriptToolchain(@Nonnull File prebuilt) {
    super(prebuilt);
  }

  @Override
  protected void buildJackCall(@Nonnull List<String> args) {
    args.add(AbstractTestTools.getPrebuilt("jack-script").getAbsolutePath());
  }

  @Override
  protected void run(@Nonnull List<String> cmdLine) {
    ExecuteFile exec = new ExecuteFile(cmdLine.toArray(new String[cmdLine.size()]));

    setUpEnvironment(exec);

    exec.setErr(errRedirectStream);
    exec.setOut(outRedirectStream);
    exec.setVerbose(isVerbose);

    try {
      if (exec.run() != 0) {
        throw new RuntimeException("Jack compiler exited with an error");
      }
    } catch (ExecFileException e) {
      throw new RuntimeException("An error occurred while running Jack", e);
    }
  }

  private static void setUpEnvironment(@Nonnull ExecuteFile exec) {
    String path = System.getenv("PATH");
    if (path != null) {
      exec.addEnvVar("PATH", path);
    }

    String home = System.getenv("HOME");
    if (home != null) {
      exec.addEnvVar("HOME", home);
    }

    String user = System.getenv("USER");
    if (user != null) {
      exec.addEnvVar("USER", user);
    }
  }

}

