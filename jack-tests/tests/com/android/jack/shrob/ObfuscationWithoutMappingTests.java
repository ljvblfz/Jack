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
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.shrob.spec.Flags;
import com.android.jack.test.category.KnownBugs;
import com.android.jack.test.comparator.ComparatorMapping;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.DummyToolchain;
import com.android.jack.test.toolchain.JackApiToolchain;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import javax.annotation.Nonnull;

public class ObfuscationWithoutMappingTests extends AbstractTest {



  @Override
  protected void runTest(
      @Nonnull String testNumber,
      @Nonnull String flagNumber,
      @Nonnull String mappingNumber)
      throws Exception {
    runTest(testNumber, flagNumber, mappingNumber, "rot13");
  }

  protected void runTest(
      @Nonnull String testNumber,
      @Nonnull String flagNumber,
      @Nonnull String mappingNumber,
      @Nonnull String nameProvider)
          throws Exception {

    String testPackageName = "com.android.jack.shrob.test" + testNumber;
    File testFolder = AbstractTestTools.getTestRootDir(testPackageName);

    JackApiToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    Flags flags = new Flags();
    toolchain.setShrobFlags(flags);
    GrammarActions.parse("proguard.flags" + flagNumber, testFolder.getAbsolutePath(), flags);
    File refFolder = new File(testFolder, "refsObfuscationWithoutMapping");

    toolchain.addProperty(NameProviderFactory.NAMEPROVIDER.getName(), nameProvider);
    toolchain.addProperty(Options.METHOD_FILTER.getName(), "supported-methods");

    File candidateOutputMapping = AbstractTestTools.createTempFile("mapping", ".txt");
    File refOutputMapping = new File(refFolder, "expected-" + flagNumber + ".txt");
    flags.setOutputMapping(candidateOutputMapping);
    flags.setPrintMapping(true);

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(new File(testFolder, "jack"));

    env.setCandidateTestTools(toolchain);
    env.setReferenceTestTools(new DummyToolchain());

    env.runTest(new ComparatorMapping(candidateOutputMapping, refOutputMapping));
  }

  @Override
  @Test
  @Category(KnownBugs.class)
  public void test33_001() throws Exception {
    super.test33_001();
  }

  @Override
  @Test
  @Category(KnownBugs.class)
  public void test34_001() throws Exception {
    super.test34_001();
  }

  @Override
  @Test
  @Category(KnownBugs.class)
  public void test35_001() throws Exception {
    super.test35_001();
  }

  @Test
  public void test43_001() throws Exception {
    /* Use "lower-case" name provider because a rot13 has invalid behavior on sources that would
     * allow to reproduce the problem. Using "lower-case" makes it a very fragile test anyway
     */
    runTest("043", "001", "", "lower-case");
  }
}
