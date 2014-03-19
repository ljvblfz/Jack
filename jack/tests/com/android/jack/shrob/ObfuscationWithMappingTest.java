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
import com.android.jack.TestTools;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.shrob.spec.Flags;

import org.junit.Test;

import java.io.File;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class ObfuscationWithMappingTest extends AbstractTest {

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
    Flags flags = new Flags();
    GrammarActions.parse("proguard.flags" + flagNumber, testFolder.getAbsolutePath(), flags);
    File candidateOutputMapping = TestTools.createTempFile("mapping", ".txt");
    File refFolder = new File(testFolder, "refsObfuscationWithMapping");
    File refOutputMapping = new File(refFolder, "expected-" + flagNumber + ".txt");
    flags.setOutputMapping(candidateOutputMapping);
    flags.setPrintMapping(true);
    File inputMapping =
        new File(testFolder, "proguard.flags" + flagNumber + ".mapping" + mappingNumber);
    if (inputMapping.exists()) {
      flags.setObfuscationMapping(inputMapping);
    }
    TestTools.runWithFlags(new Options(),
        bootclasspath,
        classpath,
        TestTools.getJackTestsWithJackFolder(testName),
        flags);
    ListingComparator.compare(refOutputMapping, candidateOutputMapping);
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
