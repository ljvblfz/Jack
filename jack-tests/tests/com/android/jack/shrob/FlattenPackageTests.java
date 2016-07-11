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
import com.android.jack.shrob.obfuscation.NameProviderFactory;
import com.android.jack.test.category.SlowTests;
import com.android.jack.test.comparator.ComparatorMapping;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.DummyToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import javax.annotation.Nonnull;

@Category(SlowTests.class)
public class FlattenPackageTests extends AbstractTest {

  @Override
  protected void runTest(
      @Nonnull String testNumber,
      @Nonnull String flagNumber,
      @Nonnull String mappingNumber)
      throws Exception {
    File testFolder = getShrobTestRootDir(testNumber);
    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    File candidateOutputMapping = AbstractTestTools.createTempFile("mapping", ".txt");
    File refFolder = new File(testFolder, "refsFlattenPackage");
    File refOutputMapping = new File(refFolder, "expected-" + flagNumber + ".txt");
    toolchain.addProperty(NameProviderFactory.NAMEPROVIDER.getName(), "rot13");
    toolchain.addProperty(Options.METHOD_FILTER.getName(), "supported-methods");

    File proguardFlagsFile1 = new File(testFolder, "proguard.flags" + flagNumber);
    File proguardFlagsFile2 = addOptionsToFlagsFile(
        new File(
            AbstractTestTools.getTestRootDir("com.android.jack.shrob"), "keepPackageName.flags"),
        testFolder,
        " -printmapping " + candidateOutputMapping.getAbsolutePath()
            + " -flattenpackagehierarchy 'flatpackage'");

    toolchain.addProguardFlags(proguardFlagsFile1);
    toolchain.addProguardFlags(proguardFlagsFile2);

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(new File(testFolder, "jack"));

    env.setCandidateTestTools(toolchain);
    env.setReferenceTestTools(new DummyToolchain());

    // TODO(jmhenaff): Find a way to avoid this line?
    // This removes JUnit from default cp, otherwise this tests seems endless
    env.setCandidateClasspath(toolchain.getDefaultBootClasspath()[0]);

    env.runTest(new ComparatorMapping(refOutputMapping, candidateOutputMapping));
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
  public void test44_001() throws Exception {
    runTest("044", "001", "");
  }
}
