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
import com.android.jack.category.KnownBugs;
import com.android.jack.category.SlowTests;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.shrob.spec.Flags;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

@Category(SlowTests.class)
public class SeedTest extends AbstractTest {

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
    flags.setShrink(false);
    flags.setObfuscate(false);
    GrammarActions.parse("proguard.flags" + flagNumber, testFolder.getAbsolutePath(), flags);
    File refFolder = new File(testFolder, "refsSeed");
    File candidateOutputSeeds = TestTools.createTempFile("seeds", ".txt");
    File refOutputSeeds = new File(refFolder, "expected-" + flagNumber + ".txt");
    flags.setSeedsFile(candidateOutputSeeds);
    flags.setPrintSeeds(true);

    Options jackOptions = new Options();
    TestTools.runWithFlags(jackOptions, bootclasspath, classpath,
        TestTools.getJackTestsWithJackFolder(testName), flags);
    SeedsComparator.compare(refOutputSeeds, candidateOutputSeeds);
  }

  @Override
  @Test
  @Category(KnownBugs.class)
  public void test19_001() throws Exception {
    super.test19_001();
  }

  @Override
  @Test
  @Category(KnownBugs.class)
  public void test22_001() throws Exception {
    super.test22_001();
  }

  @Override
  @Test
  @Category(KnownBugs.class)
  public void test30_001() throws Exception {
    super.test30_001();
  }

  @Override
  @Test
  @Category(KnownBugs.class)
  public void test31_001() throws Exception {
    super.test31_001();
  }
}
