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
import com.android.jack.TestTools;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.dex.MultiDexLegacy;
import com.android.jack.preprocessor.PreProcessor;

import org.junit.BeforeClass;

import java.io.File;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Tests for verifying that tracing for MultiDex does not disturb shrinking.
 */
public class ShrinkMultiDexTest extends AbstractTest {

  private static ProguardFlags dontObfuscateFlagFile =
      new ProguardFlags(TestTools.getJackTestFolder("shrob"), "dontobfuscate.flags");

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Override
  protected void runTest(
      @CheckForNull File[] bootclasspath,
      @CheckForNull File[] classpath,
      @Nonnull String testNumber,
      @Nonnull String flagNumber,
      @Nonnull String mappingNumber)
      throws Exception {
    String testName = "shrob/test" + testNumber;
    File testFolder = TestTools.getJackTestFolder(testName);
    ProguardFlags[] proguardflagsFiles = new ProguardFlags[] {
        dontObfuscateFlagFile,
        new ProguardFlags(TestTools.getJackTestFolder("shrob"),"keepAllAttributes.flags"),
        new ProguardFlags(testFolder, "proguard.flags" + flagNumber)};
    File refFolder = new File(testFolder, "refsShrinking");
    Options options = new Options();
    options.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");
    options.addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
    options.addProperty(PreProcessor.ENABLE.getName(), "true");
    options.addProperty(PreProcessor.FILE.getName(), new File(TestTools.getJackTestFolder("shrob"),
        "legacyMainDexClasses.jpp").getAbsolutePath());
    TestTools.checkListingWhenMultiDex(options,
        bootclasspath,
        classpath,
        TestTools.getJackTestsWithJackFolder(testName),
        proguardflagsFiles,
        new File(refFolder, "expected-" + flagNumber + ".txt"));
  }

}
