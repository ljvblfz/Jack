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

package com.android.jack.compile.androidtree.ext;

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
public class ExtCompilationTest {

  private static File[] CLASSPATH;
  private static File[] REF_CLASSPATH;

  private static File SOURCELIST;

  @BeforeClass
  public static void setUpClass() {
    ExtCompilationTest.class.getClassLoader().setDefaultAssertionStatus(true);
    CLASSPATH = new File[] {
        TestTools.getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/core-libart_intermediates/classes.jack")
      };
    REF_CLASSPATH = new File[] {
        TestTools.getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/core-libart_intermediates/classes.jar")
      };
    SOURCELIST = TestTools.getTargetLibSourcelist("ext");
  }

  @Test
  @Category(RedundantTests.class)
  public void compileExt() throws Exception {
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.srcToExe(
        AbstractTestTools.getClasspathAsString(CLASSPATH),
        AbstractTestTools.createTempDir(),
        /* zipFile = */ false,
        SOURCELIST);
  }

  @Test
  @Category(SlowTests.class)
  public void compareExtStructure() throws Exception {
    // TODO(jmhenaff): use setBootclasspath
    SourceToDexComparisonTestHelper helper = new CheckDexStructureTestHelper(SOURCELIST);
    helper.setCandidateClasspath(CLASSPATH);
    helper.setReferenceClasspath(REF_CLASSPATH);

    ComparatorDex comparator = helper.createDexFileComparator();
    comparator.setCompareDebugInfoBinary(false);
    comparator.setCompareInstructionNumber(true);
    comparator.setInstructionNumberTolerance(0.4f);

    helper.runTest(comparator);
  }
}
