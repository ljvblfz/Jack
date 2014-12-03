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

package com.android.jack.shrob;

import com.android.jack.Main;
import com.android.jack.Options;
import com.android.jack.ProguardFlags;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.dex.MultiDexLegacy;
import com.android.jack.preprocessor.PreProcessor;
import com.android.jack.shrob.shrink.ShrinkStructurePrinter;
import com.android.jack.test.comparator.ComparatorMapping;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.DummyToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;

import org.junit.BeforeClass;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * Tests for verifying that tracing for MultiDex does not disturb shrinking.
 */
public class ShrinkMultiDexTests extends AbstractTest {

  private static File shrobTestsDir =
      AbstractTestTools.getTestRootDir("com.android.jack.shrob");

  private static ProguardFlags dontObfuscateFlagFile =
      new ProguardFlags(shrobTestsDir, "dontobfuscate.flags");

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Override
  protected void runTest(
      @Nonnull String testNumber,
      @Nonnull String flagNumber,
      @Nonnull String mappingNumber)
      throws Exception {
    File testFolder = new File(shrobTestsDir, "test" + testNumber);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);

    File refFolder = new File(testFolder, "refsShrinking");
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");
    toolchain.addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
    toolchain.addProperty(PreProcessor.ENABLE.getName(), "true");
    toolchain.addProperty(PreProcessor.FILE.getName(),
        new File(shrobTestsDir, "legacyMainDexClasses.jpp").getAbsolutePath());

    File candidateNodeListing = AbstractTestTools.createTempFile("nodeListing", ".txt");
    toolchain.addProperty(ShrinkStructurePrinter.STRUCTURE_PRINTING.getName(), "true");
    toolchain.addProperty(ShrinkStructurePrinter.STRUCTURE_PRINTING_FILE.getName(),
        candidateNodeListing.getPath());
    toolchain.addProperty(Options.METHOD_FILTER.getName(), "supported-methods");
    toolchain.disableDxOptimizations();

    File out = AbstractTestTools.createTempFile("checklisting", ".zip");

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(new File(testFolder, "jack"));
    env.setCandidateTestTools(toolchain);
    env.setReferenceTestTools(new DummyToolchain());
    env.setWithDebugInfo(true);
    env.setProguardFlags(dontObfuscateFlagFile,
        new ProguardFlags(shrobTestsDir, "keepAllAttributes.flags"),
        new ProguardFlags(testFolder, "proguard.flags" + flagNumber));

    env.runTest(new ComparatorMapping(new File(refFolder, "expected-" + flagNumber + ".txt"),
        candidateNodeListing));
  }

}
