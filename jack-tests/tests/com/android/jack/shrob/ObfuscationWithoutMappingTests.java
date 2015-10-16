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

import com.android.jack.Options;
import com.android.jack.shrob.obfuscation.MappingApplier;
import com.android.jack.shrob.obfuscation.NameProviderFactory;
import com.android.jack.test.comparator.ComparatorMapping;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.DummyToolchain;
import com.android.jack.test.toolchain.IncrementalToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;

import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;

public class ObfuscationWithoutMappingTests extends AbstractTest {



  @Override
  protected void runTest(
      @Nonnull String testNumber,
      @Nonnull String flagNumber,
      @Nonnull String mappingNumber)
      throws Exception {

    String testPackageName = "com.android.jack.shrob.test" + testNumber;
    File testFolder = AbstractTestTools.getTestRootDir(testPackageName);

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    File refFolder = new File(testFolder, "refsObfuscationWithoutMapping");

    toolchain.addProperty(NameProviderFactory.NAMEPROVIDER.getName(), "rot13");
    toolchain.addProperty(Options.METHOD_FILTER.getName(), "supported-methods");

    File candidateOutputMapping = AbstractTestTools.createTempFile("mapping", ".txt");
    File refOutputMapping = new File(refFolder, "expected-" + flagNumber + ".txt");

    File proguardFlagsFile = addOptionsToFlagsFile(
        new File(testFolder, "proguard.flags" + flagNumber),
        testFolder,
        " -printmapping " + candidateOutputMapping.getAbsolutePath());

    toolchain.addProguardFlags(proguardFlagsFile);

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(new File(testFolder, "jack"));

    env.setCandidateTestTools(toolchain);
    env.setReferenceTestTools(new DummyToolchain());

    env.runTest(new ComparatorMapping(refOutputMapping, candidateOutputMapping));
  }

  @Override
  @Test
  @KnownIssue
  public void test33_001() throws Exception {
    super.test33_001();
  }

  @Override
  @Test
  @KnownIssue
  public void test34_001() throws Exception {
    super.test34_001();
  }

  @Override
  @Test
  @KnownIssue
  public void test35_001() throws Exception {
    super.test35_001();
  }

  @Override
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void test44_001() throws Exception {
    super.test44_001();
  }

  @Override
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void test5_001() throws Exception {
    super.test5_001();
  }

  @Override
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void test15_001() throws Exception {
    super.test15_001();
  }

  @Test
  public void test047() throws Exception {
    String testPackageName = "com.android.jack.shrob.test047";
    File testFolder = AbstractTestTools.getTestRootDir(testPackageName);

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    File refFolder = new File(testFolder, "refsObfuscationWithoutMapping");

    toolchain.addProperty(NameProviderFactory.NAMEPROVIDER.getName(), "rot13");
    toolchain.addProperty(Options.METHOD_FILTER.getName(), "supported-methods");
    // Only difference with other tests: allows mapping collision
    toolchain.addProperty(MappingApplier.COLLISION_POLICY.getName(), "ignore");

    File candidateOutputMapping = AbstractTestTools.createTempFile("mapping", ".txt");
    File refOutputMapping = new File(refFolder, "expected-001.txt");

    File proguardFlagsFile = addOptionsToFlagsFile(
        new File(testFolder, "proguard.flags001"),
        testFolder,
        " -printmapping " + candidateOutputMapping.getAbsolutePath());

    toolchain.addProguardFlags(proguardFlagsFile);

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(new File(testFolder, "jack"));

    env.setCandidateTestTools(toolchain);
    env.setReferenceTestTools(new DummyToolchain());

    env.runTest(new ComparatorMapping(refOutputMapping, candidateOutputMapping));
  }
}
