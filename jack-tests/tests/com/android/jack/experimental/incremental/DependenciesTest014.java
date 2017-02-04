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

import com.android.jack.dx.io.DexBuffer;
import com.android.jack.test.helper.IncrementalTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * JUnit test checking incremental support when files are deleted.
 */
public class DependenciesTest014 {



  /**
   * Check that incremental compilation works when file without dependency is deleted.
   */
  @Test
  public void testDependency001() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A { \n" + "}");

    File f = ite.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B { \n" + "}");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();
    Assert.assertEquals(2, ite.getJayceCount());

    ite.deleteJavaFile(f);
    ite.incrementalBuildFromFolder();
    try (InputStream is = new FileInputStream(ite.getDexFile())) {
      DexBuffer db = new DexBuffer(is);
      for (String typeName : db.typeNames()) {
        if (typeName.equals("Ljack/incremental/B;")) {
          Assert.fail();
        }
      }
    }
  }

  /**
   * Check that incremental compilation works when dependency file is deleted.
   */
  @Test
  public void testDependency002() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A { \n" + "}");

    File fB = ite.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B extends C { \n" + "}");

    File fC = ite.addJavaFile("jack.incremental", "C.java", "package jack.incremental; \n"
        + "public class C { \n" + "}");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();
    Assert.assertEquals(3, ite.getJayceCount());

    ite.deleteJavaFile(fB);
    ite.deleteJavaFile(fC);
    ite.incrementalBuildFromFolder();
    DexBuffer db;
    try (InputStream is = new FileInputStream(ite.getDexFile())) {
      db = new DexBuffer(is);
    }
    for (String typeName : db.typeNames()) {
      if (typeName.equals("Ljack/incremental/B;") ||
          typeName.equals("Ljack/incremental/C;")) {
        Assert.fail();
      }
    }
  }
}

