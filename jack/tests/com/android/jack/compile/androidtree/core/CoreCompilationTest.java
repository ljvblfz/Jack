/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.compile.androidtree.core;

import com.android.jack.DexComparator;
import com.android.jack.JarJarRules;
import com.android.jack.Options;
import com.android.jack.ProguardFlags;
import com.android.jack.TestTools;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.category.RedundantTests;
import com.android.jack.category.SlowTests;
import com.android.sched.vfs.Container;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

@Ignore("Tree")
public class CoreCompilationTest {

  private static File SOURCELIST;

  @BeforeClass
  public static void setUpClass() {
    CoreCompilationTest.class.getClassLoader().setDefaultAssertionStatus(true);
    SOURCELIST = TestTools.getTargetLibSourcelist("core");
  }

  @Test
  @Category(RedundantTests.class)
  public void compileCore() throws Exception {
    File out = TestTools.createTempFile("core", ".dex");
    TestTools.compileSourceToDex(new Options(), SOURCELIST, null, out, false);
  }

  @Test
  public void compareLibCoreStructure() throws Exception {
    TestTools.checkStructure(null, null, SOURCELIST,
        false /*withDebugInfo*/, false /*compareInstructionNumber*/, 0.1f, (JarJarRules) null,
        (ProguardFlags[]) null);
  }

  @Test
  @Category(SlowTests.class)
  public void compileCoreWithJackAndDex() throws Exception {
    File coreDexFolderFromJava = TestTools.createTempDir("coreFromJava", "dex");
    File coreDexFromJava = new File(coreDexFolderFromJava, DexFileWriter.DEX_FILENAME);

    Options options = new Options();
    options.addProperty(Options.GENERATE_JACK_FILE.getName(), "true");
    File outputFile = new File("/tmp/jackIncrementalOutput");
    options.addProperty(
        Options.DEX_OUTPUT_CONTAINER_TYPE.getName(), Container.DIR.toString());
    options.addProperty(Options.JACK_FILE_OUTPUT_DIR.getName(), outputFile.getAbsolutePath());
    options.addProperty(
        Options.JACK_OUTPUT_CONTAINER_TYPE.getName(), Container.DIR.toString());
    TestTools.compileSourceToDex(options, SOURCELIST, null, coreDexFolderFromJava, false);

    File coreDexFolderFromJack = TestTools.createTempDir("coreFromJack", "dex");
    File coreDexFromJack = new File(coreDexFolderFromJack, DexFileWriter.DEX_FILENAME);
    TestTools.compileJackToDex(new Options(), outputFile, coreDexFolderFromJack,
        false);

    // Compare dex files structures and number of instructions
    new DexComparator(false /* withDebugInfo */, false /* strict */,
        false /* compareDebugInfoBinary */, true /* compareInstructionNumber */, 0).compare(
        coreDexFromJava, coreDexFromJack);
  }
}
