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

package com.android.jack.compile.androidtree.bouncycastle;

import com.android.jack.JarJarRules;
import com.android.jack.TestTools;
import com.android.jack.category.RedundantTests;
import com.android.jack.category.SlowTests;
import com.android.jack.test.comparator.ComparatorDex;
import com.android.jack.test.helper.CheckDexStructureTestHelper;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.AndroidToolchain;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

@Ignore("Tree")
public class BouncycastleCompilationTest {

  private static File[] CLASSPATH;
  private static File[] REF_CLASSPATH;

  private static File SOURCELIST;

  private static JarJarRules JARJAR_RULES;

  @BeforeClass
  public static void setUpClass() {
    BouncycastleCompilationTest.class.getClassLoader().setDefaultAssertionStatus(true);
    CLASSPATH = new File[] {
        TestTools.getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/core-libart_intermediates/classes.jack")
      };
    REF_CLASSPATH = new File[] {
        TestTools.getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/core-libart_intermediates/classes.jar")
     };
    SOURCELIST = TestTools.getTargetLibSourcelist("bouncycastle");
    JARJAR_RULES = new JarJarRules(
        TestTools.getFromAndroidTree("external/bouncycastle/jarjar-rules.txt"));
  }

  @Test
  @Category(RedundantTests.class)
  public void compileBouncycastle() throws Exception {
    File outDexFolder = AbstractTestTools.createTempDir();
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.disableDxOptimizations();
    toolchain.srcToExe(
        AbstractTestTools.getClasspathAsString(CLASSPATH),
        outDexFolder,
        /* zipFile = */ false,
        SOURCELIST);
  }

  @Test
  @Category(SlowTests.class)
  public void compareBouncycastleStructure() throws Exception {
    SourceToDexComparisonTestHelper helper = new CheckDexStructureTestHelper(SOURCELIST);
    // TODO(jmhenaff): use setBootclasspath
    helper.setCandidateClasspath(CLASSPATH);
    helper.setReferenceClasspath(REF_CLASSPATH);
    helper.setJarjarRulesFile(JARJAR_RULES);

    ComparatorDex comparator = helper.createDexFileComparator();
    comparator.setCompareDebugInfoBinary(false);
    comparator.setCompareInstructionNumber(true);
    comparator.setInstructionNumberTolerance(0.4f);

    helper.runTest(comparator);
  }
}
