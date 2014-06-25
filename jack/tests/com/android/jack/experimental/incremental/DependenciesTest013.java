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
import com.android.jack.frontend.FrontendCompilationException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test checking dependencies between Java files.
 */
public class DependenciesTest013 {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Check that incremental compilation support array creation.
   */
  @Test
  public void testDependency001() throws Exception {
    IncrementalTestingEnvironment ite =
        new IncrementalTestingEnvironment(TestTools.createTempDir("DependenciesTest_", "_001"));

    ite.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A { \n"
        + "public int getLength() { "
        + "   return new B[1].length; "
        + "} "
        + "}");

    ite.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B { \n"
        + "}");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();
    Assert.assertEquals(2, ite.getJackFiles().size());

    ite.deleteJavaFile("jack.incremental", "B.java");

    try {
      ite.incrementalBuildFromFolder();
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // Ok
    } finally {
      Assert.assertTrue(ite.getStringRepresentingErr().contains("B cannot be resolved to a type"));
    }
  }

  /**
   * Check that incremental compilation support array usages.
   */
  @Test
  public void testDependency002() throws Exception {
    IncrementalTestingEnvironment ite =
        new IncrementalTestingEnvironment(TestTools.createTempDir("DependenciesTest_", "_001"));

    ite.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A { \n"
        + "public int getLength() { "
        + "   return B.array.length; "
        + "} "
        + "}");

    ite.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B { \n"
        + "public static C []array; \n"
        + "}");

    ite.addJavaFile("jack.incremental", "C.java", "package jack.incremental; \n"
        + "public class C { \n"
        + "}");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();
    Assert.assertEquals(3, ite.getJackFiles().size());

    ite.deleteJavaFile("jack.incremental", "C.java");

    try {
      ite.incrementalBuildFromFolder();
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // Ok
    } finally {
      Assert.assertTrue(ite.getStringRepresentingErr().contains("C cannot be resolved to a type"));
    }
  }

  /**
   * Check that incremental compilation support array usages.
   */
  @Test
  public void testDependency003() throws Exception {
    IncrementalTestingEnvironment ite =
        new IncrementalTestingEnvironment(TestTools.createTempDir("DependenciesTest_", "_001"));

    ite.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A { \n"
        + "public int getLength() { "
        + "   return B.array.length; "
        + "} "
        + "}");

    ite.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B { \n"
        + "public static C [][]array; \n"
        + "}");

    ite.addJavaFile("jack.incremental", "C.java", "package jack.incremental; \n"
        + "public class C { \n"
        + "}");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();
    Assert.assertEquals(3, ite.getJackFiles().size());

    ite.deleteJavaFile("jack.incremental", "C.java");

    try {
      ite.incrementalBuildFromFolder();
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // Ok
    } finally {
      Assert.assertTrue(ite.getStringRepresentingErr().contains("C cannot be resolved to a type"));
    }
  }
}

