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

package com.android.jack.test.toolchain;

import com.android.jack.test.util.ExecFileException;
import com.android.jack.test.util.ExecuteFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This {@link JillBasedToolchain} uses legacy java compiler as a frontend.
 */
public class LegacyJillToolchain extends JillBasedToolchain {

  @Nonnull
  private File jillPrebuilt;

  public LegacyJillToolchain(@Nonnull File refCompilerPrebuilt, @Nonnull File jillPrebuilt,
      @Nonnull File jackPrebuilt, @Nonnull File jarjarPrebuilt, @Nonnull File proguardPrebuilt) {
    super(jackPrebuilt, refCompilerPrebuilt, jarjarPrebuilt, proguardPrebuilt);
    this.jillPrebuilt = jillPrebuilt;
  }

  @Override
  protected void executeJill(@Nonnull File in, @Nonnull File out) {
    boolean assertEnable = false;
    assert true == (assertEnable = true);

    List<String> commandLine = new ArrayList<String>();
    commandLine.add("java");
    commandLine.add(assertEnable ? "-ea" : "-da");
    commandLine.add("-jar");
    commandLine.add(jillPrebuilt.getAbsolutePath());
    if (isVerbose) {
      commandLine.add("--verbose");
    }

    commandLine.add(in.getAbsolutePath());
    commandLine.add("--output");
    commandLine.add(out.getAbsolutePath());

    ExecuteFile execFile = new ExecuteFile(commandLine.toArray(new String[commandLine.size()]));
    execFile.inheritEnvironment();
    execFile.setOut(outRedirectStream);
    execFile.setErr(errRedirectStream);
    execFile.setVerbose(isVerbose);

    try {
      if (execFile.run() != 0) {
        throw new RuntimeException("Jill exited with an error");
      }
    } catch (ExecFileException e) {
      throw new RuntimeException("An error occurred while running Jill", e);
    }
  }
}
