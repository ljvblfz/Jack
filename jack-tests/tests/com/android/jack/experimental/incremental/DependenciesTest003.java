/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.experimental.incremental;

import com.android.jack.Main;
import com.android.jack.TestTools;
import com.android.jack.test.helper.IncrementalTestHelper;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * JUnit test checking dependencies between Java files.
 */
public class DependenciesTest003 {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Check that file modification implying to transform an interface call to a virtual call is well
   * detected.
   */
  @Test
  public void testDependency001() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(TestTools.createTempDir("DependenciesTest_", "_001"));

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A  { public void test(B b) {b.call1().call2();} } \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public I call1() {return new C();} } \n");

    ite.addJavaFile("jack.incremental", "C.java",
        "package jack.incremental; \n"+
        "public class C  implements I { @Override public void call2() { } } \n");

    ite.addJavaFile("jack.incremental", "I.java",
        "package jack.incremental; \n"+
        "public interface I {  public void call2(); } \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public C call1() {return new C();} } \n");

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(2, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));
  }
}
