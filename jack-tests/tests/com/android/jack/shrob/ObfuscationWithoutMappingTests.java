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
import com.android.jack.category.KnownBugs;
import com.android.jack.shrob.obfuscation.NameProviderFactory;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.shrob.spec.Flags;
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

    String testName = "shrob/test" + testNumber;

    String testPackageName = "com.android.jack.shrob.test" + testNumber;
    File testFolder = AbstractTestTools.getTestRootDir(testPackageName);

    JackApiToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    Flags flags = new Flags();
    toolchain.setShrobFlags(flags);
    GrammarActions.parse("proguard.flags" + flagNumber, testFolder.getAbsolutePath(), flags);
    File refFolder = new File(testFolder, "refsObfuscationWithoutMapping");

    toolchain.addProperty(NameProviderFactory.NAMEPROVIDER.getName(), "rot13");
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

    //    // ==============================================================

    /*
     * Comment for reviewers: the following code is an attempt to use jack based toolchain.
     * It works, but requires to manipulate (copy, write...) shrob flags.
     * Here I added a shrob option manually, but ideally to be able to use
     * a Flags like object for CLI based toochains, we must be able to dump
     * the flags as a file (TBD). Plus, if flags have "-include" directives with relative
     * paths, files must be in the same location.
     */

//
//    String testName = "shrob/test" + testNumber;
//    File testFolder = TestTools.getJackTestFolder(testName);
//    File refFolder = new File(testFolder, "refsObfuscationWithoutMapping");
//
//    JackBasedToolchain jackToolchain = AbstractTestTools.getJackBasedToolchainAsCandidate();
//
//    jackToolchain.addProperty(NameProviderFactory.NAMEPROVIDER.getName(), "rot13");
//    jackToolchain.addProperty(Options.METHOD_FILTER.getName(), "supported-methods");
//
//    File candidateOutputMapping = TestTools.createTempFile("mapping", ".txt");
//    File refOutputMapping = new File(refFolder, "expected-" + flagNumber + ".txt");
//
//    jackToolchain.getCompilationResult().proguardMappingFile = candidateOutputMapping;
//
//    // TODO(jmhenaff): having to seems like a no go for JackBasedToolchain (i.e. cli)
//    File candidateFlags = new File(testFolder.getAbsolutePath(), "tmp-proguard.flags" + flagNumber);
//    candidateFlags.deleteOnExit();
//    appendStringToFileCopy(new File(testFolder.getAbsolutePath(), "proguard.flags" + flagNumber),
//        candidateFlags, "-printmapping " + candidateOutputMapping.getAbsolutePath());
//
//    SourceToDexComparisonTestEnv env = new SourceToDexComparisonTestEnv(bootclasspath, classpath,
//        TestTools.getJackTestsWithJackFolder(testName));
//
//    env.setProguardFlags(new ProguardFlags[] {new ProguardFlags(candidateFlags)});
//
//    CompilationResult compilationResult = new CompilationResult();
//    compilationResult.proguardMappingFile = refOutputMapping;
//
//    env.setCandidateTestTools(jackToolchain);
//    env.setReferenceTestTools(new DummyTestTools(compilationResult));
//
//    env.addComparator(env.createMappingComparator());
//    env.compare();

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

//  private void appendStringToFileCopy(File source, File dest, String appended) throws IOException {
//    BufferedReader reader = null;
//    BufferedWriter writer = null;
//    try {
//      reader = new BufferedReader(new FileReader(source));
//      writer = new BufferedWriter(new FileWriter(dest));
//      String line;
//      while ((line = reader.readLine()) != null) {
//        writer.write(line);
//        writer.write("\n");
//      }
//      writer.write(appended);
//    } finally {
//      try {
//        reader.close();
//      } catch (IOException e) {
//      }
//      try {
//        writer.close();
//      } catch (IOException e) {
//      }
//    }
//  }

}
