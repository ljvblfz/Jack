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
import com.android.jack.TestTools;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.sched.scheduler.ScheduleInstance;

import java.io.File;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class MergerTestTools {

  @Nonnull
  public File buildOneDexPerType(@CheckForNull String classpath, @Nonnull File sourceFolder,
      boolean withDebug) throws Exception {
    Options options;
    File multiDexFolder = TestTools.createTempDir("multi", "dex");
    File multiDex = new File(multiDexFolder, DexFileWriter.DEX_FILENAME);
    File multiDexOnTypePerTypeFolder = TestTools.createTempDir("multiOnDexPerType", "dex");
    options = new Options();
    options.addProperty(Options.EMIT_LINE_NUMBER_DEBUG_INFO.getName(), Boolean.toString(withDebug));
    options.addProperty(ScheduleInstance.DEFAULT_RUNNER.getName(), "single-threaded");
    options.addProperty(Options.INTERMEDIATE_DEX_DIR.getName(), multiDexOnTypePerTypeFolder.getAbsolutePath());
    TestTools
        .compileSourceToDex(options, sourceFolder, classpath,
            multiDexFolder, false /* zip */, null /* jarjarRules */, null /* flagFiles */,
            withDebug /* withDebugInfo */);

    return multiDex;
  }
}
