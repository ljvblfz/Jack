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

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * JUnit test checking dependencies between Java files.
 */
public class DependenciesTest013 {



  /**
   * Check that incremental compilation support array creation.
   */
  @Test
  public void testDependency001() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.setIsApiTest();

    ite.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A { \n"
        + "public int getLength() { "
        + "   return new B[1].length; "
        + "} "
        + "}");

    File f = ite.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B { \n"
        + "}");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();
    Assert.assertEquals(2, ite.getJayceCount());

    ite.deleteJavaFile(f);

    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ite.setErr(err);

    try {
      ite.incrementalBuildFromFolder();
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // Ok
    } finally {
      Assert.assertTrue(err.toString().contains("B cannot be resolved to a type"));
    }
  }

  /**
   * Check that incremental compilation support array usages.
   */
  @Test
  public void testDependency002() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.setIsApiTest();

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

    File f = ite.addJavaFile("jack.incremental", "C.java", "package jack.incremental; \n"
        + "public class C { \n"
        + "}");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();
    Assert.assertEquals(3, ite.getJayceCount());

    ite.deleteJavaFile(f);

    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ite.setErr(err);

    try {
      ite.incrementalBuildFromFolder();
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // Ok
    } finally {
      Assert.assertTrue(err.toString().contains("C cannot be resolved to a type"));
    }
  }

  /**
   * Check that incremental compilation support array usages.
   */
  @Test
  public void testDependency003() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.setIsApiTest();

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

    File f = ite.addJavaFile("jack.incremental", "C.java", "package jack.incremental; \n"
        + "public class C { \n"
        + "}");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();
    Assert.assertEquals(3, ite.getJayceCount());

    ite.deleteJavaFile(f);

    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ite.setErr(err);

    try {
      ite.incrementalBuildFromFolder();
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // Ok
    } finally {
      Assert.assertTrue(err.toString().contains("C cannot be resolved to a type"));
    }
  }
}

