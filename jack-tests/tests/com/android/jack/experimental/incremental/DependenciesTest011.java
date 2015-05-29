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
public class DependenciesTest011 {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Check that incremental compilation support throws declaration.
   */
  @Test
  public void testDependency001() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.setIsApiTest();

    File f = ite.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A extends Exception { "
        + "}");

    ite.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B { \n"
        + "    public void m() throws A { }"
        + "}");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();
    Assert.assertEquals(2, ite.getJayceCount());

    ite.deleteJavaFile(f);

    try {
      ite.incrementalBuildFromFolder();
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // Ok
    }

    ite.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B { \n"
        + "    public void m() { }"
        + "}");

    ite.incrementalBuildFromFolder();
    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(1, fqnOfRebuiltTypes.size());
  }
}

