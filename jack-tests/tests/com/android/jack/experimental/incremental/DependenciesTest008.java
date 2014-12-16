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

/**
 * JUnit test checking dependencies between Java files.
 */
public class DependenciesTest008 {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Check that jack files are deleted according to recompiled files.
   */
  @Test
  public void testDependency001() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n" +
        "public class A { \n" +
        " public static class B { public void m() {} } \n" +
        " public static void main(String[] args) {new B().m();} } \n");

      ite.incrementalBuildFromFolder();
      Assert.assertEquals(2, ite.getJackFiles().size());

      ite.addJavaFile("jack.incremental", "A.java",
          "package jack.incremental; \n" +
          "public class A { \n" +
          " public static void main(String[] args) {new B().m();} } \n");

      try {
        ite.incrementalBuildFromFolder();
        Assert.fail();
      } catch (FrontendCompilationException e)  {
        // Error is ok
        Assert.assertEquals(0, ite.getJackFiles().size());
      }
  }

  /**
   * Check that jack files are deleted according to recompiled files.
   */
  @Test
  public void testDependency002() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n" +
        "public class A { } \n" +
        "class B { } \n");

      ite.incrementalBuildFromFolder();
      Assert.assertEquals(2, ite.getJackFiles().size());

      ite.addJavaFile("jack.incremental", "A.java",
          "package jack.incremental; \n" +
          "public class A { }\n");

      ite.incrementalBuildFromFolder();
      Assert.assertEquals(1, ite.getJackFiles().size());
  }

  /**
   * Check that jack files are deleted according to recompiled files.
   */
  @Test
  public void testDependency003() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n" +
        "public class A { } \n" +
        "class B { } \n");

    ite.addJavaFile("jack.incremental", "C.java",
        "package jack.incremental; \n" +
        "public class C { public void test() {new B();} } \n");

      ite.incrementalBuildFromFolder();
      Assert.assertEquals(3, ite.getJackFiles().size());

      ite.addJavaFile("jack.incremental", "A.java",
          "package jack.incremental; \n" +
          "public class A { }\n");

      try {
        ite.incrementalBuildFromFolder();
        Assert.fail();
      } catch (FrontendCompilationException e)  {
        // Error is ok
        Assert.assertEquals(0, ite.getJackFiles().size());
      }
  }
}
