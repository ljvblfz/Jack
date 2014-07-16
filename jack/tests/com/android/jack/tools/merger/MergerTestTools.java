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

import com.android.jack.DexAnnotationsComparator;
import com.android.jack.DexComparator;
import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.backend.dex.rop.CodeItemBuilder;
import com.android.jack.util.ExecuteFile;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.vfs.Container;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

public class MergerTestTools {

  @Nonnull
  public boolean diff(@Nonnull File monoDex, @Nonnull File multiDex) throws IOException {
    ExecuteFile ef =
        new ExecuteFile("diff " + monoDex.getAbsolutePath() + " " + multiDex.getAbsolutePath());
    ef.setOut(System.out);
    ef.setErr(System.out);
    return !ef.run();
  }

  public boolean compareMonoDexWithOneDexPerType(@Nonnull File sourceFolder, boolean withDebug)
      throws Exception {
    File monoDex = TestTools.createTempFile("mono", ".dex");
    Options options = new Options();
    options.addProperty(Options.EMIT_LINE_NUMBER_DEBUG_INFO.getName(), Boolean.toString(withDebug));
    options.addProperty(ScheduleInstance.DEFAULT_RUNNER.getName(), "single-threaded");
    options.addProperty(CodeItemBuilder.FORCE_JUMBO.getName(), "true");
    TestTools
        .compileSourceToDex(options, sourceFolder, TestTools.getDefaultBootclasspathString(),
            monoDex, false /* zip */, null /* jarjarRules */, null /* flagFiles */,
            withDebug /* withDebugInfo */);

    File oneDexPerTypeMerged = buildOneDexPerType(sourceFolder, withDebug);


    new DexComparator().compare(monoDex, oneDexPerTypeMerged, false, true,
        false /* compareDebugInfoBinary */, true, 0);

    new DexAnnotationsComparator().compare(monoDex, oneDexPerTypeMerged);

    return diff(monoDex, oneDexPerTypeMerged);
  }

  public boolean compareMonoDexWithOneDexPerTypeByUsingJackFiles(@Nonnull File sourceFolder,
      boolean withDebug) throws Exception {
    File monoDex = TestTools.createTempFile("mono", ".dex");

    Options options = new Options();
    options.addProperty(Options.GENERATE_JACK_FILE.getName(), "true");
    File jackOutputFolder = TestTools.createTempDir("jackOutput","folder");
    options.addProperty(
        Options.DEX_OUTPUT_CONTAINER_TYPE.getName(), Container.DIR.toString());
    options.addProperty(Options.JACK_FILE_OUTPUT_DIR.getName(), jackOutputFolder.getAbsolutePath());
    options.addProperty(
        Options.JACK_OUTPUT_CONTAINER_TYPE.getName(), Container.DIR.toString());
    options.addProperty(Options.EMIT_LINE_NUMBER_DEBUG_INFO.getName(), Boolean.toString(withDebug));
    options.addProperty(ScheduleInstance.DEFAULT_RUNNER.getName(), "single-threaded");
    options.addProperty(CodeItemBuilder.FORCE_JUMBO.getName(), "true");
    TestTools.compileSourceToDex(options, sourceFolder, null, monoDex, false);


    File oneDexPerTypeMerged = buildOneDexPerTypeFromJack(jackOutputFolder, true);

    new DexComparator().compare(monoDex, oneDexPerTypeMerged, false, true,
        false /* compareDebugInfoBinary */, true, 0);

    new DexAnnotationsComparator().compare(monoDex, oneDexPerTypeMerged);

    return diff(monoDex, oneDexPerTypeMerged);
  }

  public File buildOneDexPerTypeFromJack(@Nonnull File sourceFolder, boolean withDebug)
      throws Exception {
    File multiDex = TestTools.createTempFile("multi", ".dex");
    File multiDexFolder = TestTools.createTempDir("multi", "dex");
    Options options = new Options();
    options.addProperty(Options.EMIT_LINE_NUMBER_DEBUG_INFO.getName(), Boolean.toString(withDebug));
    options.addProperty(ScheduleInstance.DEFAULT_RUNNER.getName(), "single-threaded");
    options.addProperty(Options.GENERATE_ONE_DEX_PER_TYPE.getName(), "true");
    options.addProperty(Options.DEX_FILE_FOLDER.getName(), multiDexFolder.getAbsolutePath());

    TestTools.compileJackToDex(options, sourceFolder, multiDex, false);

    return multiDex;
  }

  public File buildOneDexPerType(@Nonnull File sourceFolder, boolean withDebug) throws Exception {
    Options options;
    File multiDex = TestTools.createTempFile("multi", ".dex");
    File multiDexFolder = TestTools.createTempDir("multi", "dex");
    options = new Options();
    options.addProperty(Options.EMIT_LINE_NUMBER_DEBUG_INFO.getName(), Boolean.toString(withDebug));
    options.addProperty(ScheduleInstance.DEFAULT_RUNNER.getName(), "single-threaded");
    options.addProperty(Options.GENERATE_ONE_DEX_PER_TYPE.getName(), "true");
    options.addProperty(Options.DEX_FILE_FOLDER.getName(), multiDexFolder.getAbsolutePath());
    TestTools
        .compileSourceToDex(options, sourceFolder, TestTools.getDefaultBootclasspathString(),
            multiDex, false /* zip */, null /* jarjarRules */, null /* flagFiles */,
            withDebug /* withDebugInfo */);

    return multiDex;
  }
}
