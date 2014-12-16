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

package com.android.jack.tools.merger;

import com.android.jack.Options;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiToolchain;
import com.android.sched.scheduler.ScheduleInstance;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class MergerTestTools {

  @Nonnull
  protected File buildOneDexPerType(@Nonnull File sourceFolder,
      boolean withDebug, @CheckForNull OutputStream out, @CheckForNull OutputStream err) throws Exception {
    JackApiToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    String classpath = AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath());
    try {
      File multiDexFolder = AbstractTestTools.createTempDir();
      File multiDex = new File(multiDexFolder, DexFileWriter.DEX_FILENAME);
      File internalJackLibraryOutput = AbstractTestTools.createTempDir();

      toolchain.addProperty(Options.EMIT_LINE_NUMBER_DEBUG_INFO.getName(),
          Boolean.toString(withDebug));
      toolchain.addProperty(ScheduleInstance.DEFAULT_RUNNER.getName(), "single-threaded");
      toolchain.addProperty(Options.LIBRARY_OUTPUT_DIR.getName(),
      internalJackLibraryOutput.getAbsolutePath());

      if (out != null) {
        toolchain.setOutputStream(out);
      }
      if (err != null) {
        toolchain.setErrorStream(err);
      }

      toolchain.srcToExe(classpath, multiDexFolder, /* zipFile = */ false, sourceFolder);

      return multiDex;

    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

}
