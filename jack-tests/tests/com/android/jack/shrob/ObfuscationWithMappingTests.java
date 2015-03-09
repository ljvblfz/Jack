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

import com.android.jack.test.comparator.ComparatorMapping;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.DummyToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;

import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;

public class ObfuscationWithMappingTests extends AbstractTest {

  @Override
  protected void runTest(
      @Nonnull String testNumber,
      @Nonnull String flagNumber,
      @Nonnull String mappingNumber)
      throws Exception {

    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.shrob.test" + testNumber);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    File candidateOutputMapping = AbstractTestTools.createTempFile("mapping", ".txt");
    File refFolder = new File(testFolder, "refsObfuscationWithMapping");
    File refOutputMapping = new File(refFolder, "expected-" + flagNumber + ".txt");
    File inputMapping =
        new File(testFolder, "proguard.flags" + flagNumber + ".mapping" + mappingNumber);

    String extraOptions = " -printmapping " + candidateOutputMapping.getAbsolutePath();
    if (inputMapping.exists()) {
      extraOptions += " -applymapping " + inputMapping.getAbsolutePath();
    }

    File proguardFlagsFile = addOptionsToFlagsFile(
        new File(testFolder, "proguard.flags" + flagNumber),
        testFolder,
        extraOptions);

    toolchain.addProguardFlags(proguardFlagsFile);

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(new File(testFolder, "jack"));

    env.setCandidateTestTools(toolchain);
    env.setReferenceTestTools(new DummyToolchain());

    env.runTest(new ComparatorMapping(candidateOutputMapping, refOutputMapping));
  }

  @Override
  @Test
  public void test33_001() throws Exception {
    // Test 33 already has a partial mapping, it can't be used in this test suite.
  }

  @Override
  @Test
  public void test33_002() throws Exception {
    // Test 33 already has a partial mapping, it can't be used in this test suite.
  }

  @Override
  @Test
  public void test34_001() throws Exception {
    // Test 35 already has a partial mapping, it can't be used in this test suite.
  }

  @Override
  @Test
  public void test35_001() throws Exception {
    // Test 34 already has a partial mapping, it can't be used in this test suite.
  }
}
