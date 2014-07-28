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
import com.android.jack.dx.io.DexBuffer;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;

/**
 * JUnit test checking incremental support when files are deleted.
 */
public class DependenciesTest014 {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Check that incremental compilation works when file without dependency is deleted.
   */
  @Test
  public void testDependency001() throws Exception {
    IncrementalTestingEnvironment ite =
        new IncrementalTestingEnvironment(TestTools.createTempDir("DependenciesTest_", "_001"));

    ite.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A { \n" + "}");

    ite.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B { \n" + "}");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();
    Assert.assertEquals(2, ite.getJackFiles().size());

    ite.deleteJavaFile("jack.incremental", "B.java");
    ite.incrementalBuildFromFolder();
    DexBuffer db = new DexBuffer(new FileInputStream(ite.getDexFile()));
    for (String typeName : db.typeNames()) {
      if (typeName.equals("Ljack/incremental/B;")) {
        Assert.fail();
      }
    }
  }

  /**
   * Check that incremental compilation works when dependency file is deleted.
   */
  @Test
  public void testDependency002() throws Exception {
    IncrementalTestingEnvironment ite =
        new IncrementalTestingEnvironment(TestTools.createTempDir("DependenciesTest_", "_001"));

    ite.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A { \n" + "}");

    ite.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B extends C { \n" + "}");

    ite.addJavaFile("jack.incremental", "C.java", "package jack.incremental; \n"
        + "public class C { \n" + "}");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();
    Assert.assertEquals(3, ite.getJackFiles().size());

    ite.deleteJavaFile("jack.incremental", "B.java");
    ite.deleteJavaFile("jack.incremental", "C.java");
    ite.incrementalBuildFromFolder();
    DexBuffer db = new DexBuffer(new FileInputStream(ite.getDexFile()));
    for (String typeName : db.typeNames()) {
      if (typeName.equals("Ljack/incremental/B;") ||
          typeName.equals("Ljack/incremental/C;")) {
        Assert.fail();
      }
    }
  }
}

