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

import com.android.jack.DexAnnotationsComparator;
import com.android.jack.DexComparator;
import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.category.RedundantTests;
import com.android.jack.category.SlowTests;
import com.android.jack.config.id.JavaVersionPropertyId.JavaVersion;
import com.android.jack.test.comparator.ComparatorDex;
import com.android.jack.test.helper.CheckDexStructureTestHelper;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

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
    SOURCELIST = TestTools.getTargetLibSourcelist("core-libart");
  }

  @Test
  @Category(RedundantTests.class)
  public void compileCore() throws Exception {
    File outDexFolder = AbstractTestTools.createTempDir();
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addProperty(Options.JAVA_SOURCE_VERSION.getName(), JavaVersion.JAVA_7.toString());
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(
        outDexFolder,
        /* zipFile = */ false,
        SOURCELIST);
  }

  @Test
  public void compareLibCoreStructure() throws Exception {
    SourceToDexComparisonTestHelper helper = new CheckDexStructureTestHelper(SOURCELIST);
    helper.setCandidateClasspath(/* empty */);
    helper.setReferenceClasspath(/* empty */);
    helper.setSourceLevel(SourceLevel.JAVA_7);

    ComparatorDex comparator = helper.createDexFileComparator();
    comparator.setCompareDebugInfoBinary(false);
    comparator.setCompareInstructionNumber(false);
    comparator.setInstructionNumberTolerance(0.1f);

    helper.runTest(comparator);
  }

  @Test
  @Category(SlowTests.class)
  public void compileCoreWithJackAndDex() throws Exception {
    File coreDexFolderFromJava = AbstractTestTools.createTempDir();
    File coreDexFromJava = new File(coreDexFolderFromJava, DexFileWriter.DEX_FILENAME);

    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    File outputFile = AbstractTestTools.createTempDir();
    toolchain.setSourceLevel(SourceLevel.JAVA_7);

    toolchain.setIncrementalFolder(outputFile);

    toolchain.srcToExe(
        coreDexFolderFromJava,
        /* zipFile = */ false,
        SOURCELIST);

    File coreDexFolderFromJack = AbstractTestTools.createTempDir();
    File coreDexFromJack = new File(coreDexFolderFromJack, DexFileWriter.DEX_FILENAME);
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.libToExe(outputFile, coreDexFolderFromJack, /* zipFile = */ false);

    // Compare dex files structures and number of instructions
    new DexComparator(false /* withDebugInfo */, false /* strict */,
        false /* compareDebugInfoBinary */, true /* compareInstructionNumber */, 0).compare(
        coreDexFromJava, coreDexFromJack);
    new DexAnnotationsComparator().compare(coreDexFromJava, coreDexFromJack);
  }
}
