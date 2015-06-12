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
import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.test.helper.IncrementalTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * JUnit test checking dependencies between Java files.
 */
public class DependenciesTest006 {



  /**
   * Check that runtime is correct after class renaming.
   */
  @Test
  public void testDependency001() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.setIsApiTest();

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A  { public static void main(String[] args) {" +
        "System.out.print(new B().getString());} " +
        "} \n");

    File f = ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B  { public String getString() { return (\"B\"); } } \n");

    ite.addJavaFile("jack.incremental", "C.java",
        "package jack.incremental; \n"+
        "public class C  { } \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.deleteJavaFile(f);
    ite.addJavaFile("jack.incremental", "_B.java",
        "package jack.incremental; \n"+
        "public class _B  { public String getString() { return (\"_B\"); } } \n");

    try {
      ite.incrementalBuildFromFolder();
      Assert.fail();
    } catch (FrontendCompilationException e)  {
      // Error is ok
    }

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A  { public static void main(String[] args) {" +
        "System.out.print(new _B().getString());} " +
        "} \n");

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(2, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental._B"));

    ite.run("jack.incremental.A", "_B");
  }
}
