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

import com.android.jack.test.category.SlowTests;
import com.android.jack.test.comparator.ComparatorSeeds;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.DummyToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;

import org.junit.experimental.categories.Category;

import java.io.File;

import javax.annotation.Nonnull;

@Category(SlowTests.class)
public class SeedTests extends AbstractTest {

  @Override
  protected void runTest(
      @Nonnull String testNumber,
      @Nonnull String flagNumber,
      @Nonnull String mappingNumber)
      throws Exception {

    File testFolder = getShrobTestRootDir(testNumber);
    File refFolder = new File(testFolder, "refsSeed");
    File candidateOutputSeeds = AbstractTestTools.createTempFile("seeds", ".txt");
    File refOutputSeeds = new File(refFolder, "expected-" + flagNumber + ".txt");

    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);

    File seedsFile = addOptionsToFlagsFile(
        new File(testFolder, "proguard.flags" + flagNumber),
        testFolder,
        " -dontshrink -dontobfuscate -printseeds " + candidateOutputSeeds.getAbsolutePath());

    toolchain.addProguardFlags(seedsFile);

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(new File(testFolder, "jack"));

    env.setCandidateTestTools(toolchain);
    env.setReferenceTestTools(new DummyToolchain());

    env.runTest(new ComparatorSeeds(refOutputSeeds, candidateOutputSeeds));
  }
}
