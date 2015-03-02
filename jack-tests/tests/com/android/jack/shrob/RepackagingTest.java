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
import com.android.jack.test.comparator.ComparatorMapping;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.DummyToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;

import java.io.File;

import javax.annotation.Nonnull;

public class RepackagingTest extends AbstractTest {

  @Override
  protected void runTest(
      @Nonnull String testNumber,
      @Nonnull String flagNumber,
      @Nonnull String mappingNumber)
      throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.shrob.test" + testNumber);
    Flags flags = new Flags();

    GrammarActions.parse("proguard.flags" + flagNumber, testFolder.getAbsolutePath(), flags);
    flags.setPackageForRenamedClasses("");
    File candidateOutputMapping = AbstractTestTools.createTempFile("mapping", ".txt");
    File refFolder = new File(testFolder, "refsRepackageClasses");
    File refOutputMapping = new File(refFolder, "expected-" + flagNumber + ".txt");
    flags.setOutputMapping(candidateOutputMapping);
    flags.setPrintMapping(true);

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.setShrobFlags(flags);
    toolchain.addProperty(NameProviderFactory.NAMEPROVIDER.getName(), "rot13");
    toolchain.addProperty(Options.METHOD_FILTER.getName(), "supported-methods");

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(new File(testFolder, "jack"));

    env.setCandidateTestTools(toolchain);
    env.setReferenceTestTools(new DummyToolchain());

    env.runTest(new ComparatorMapping(candidateOutputMapping, refOutputMapping));
  }
}
