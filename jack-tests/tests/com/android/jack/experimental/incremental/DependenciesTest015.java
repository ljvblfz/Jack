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
import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.test.helper.IncrementalTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackBasedToolchain.MultiDexKind;
import com.android.sched.util.location.FileLocation;

import junit.framework.Assert;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * JUnit test checking incremental support.
 */
public class DependenciesTest015 {



  /**
   * Check that incremental compilation works when library on classpath is modified.
   */
  @Test
  public void testDependency001() throws Exception {
    IncrementalTestHelper iteLib =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    iteLib.setIsApiTest();

    File f = iteLib.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public abstract class A { \n" + "public abstract void m(); }");

    iteLib.incrementalBuildFromFolder();
    iteLib.snapshotJackFilesModificationDate();
    Assert.assertEquals(1, iteLib.getJayceCount());


    IncrementalTestHelper iteProg =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    iteProg.setIsApiTest();

    iteProg.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B extends A { \n" + " @Override public void m(){} }");

    iteProg.incrementalBuildFromFolder(new File[]{iteLib.getCompilerStateFolder()});
    iteProg.snapshotJackFilesModificationDate();
    Assert.assertEquals(1, iteProg.getJayceCount());

    iteLib.deleteJavaFile(f);
    iteLib.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public abstract class A { \n" + "public abstract int m(); }");
    iteLib.incrementalBuildFromFolder();
    iteLib.snapshotJackFilesModificationDate();
    Assert.assertEquals(1, iteLib.getJayceCount());

    ByteArrayOutputStream err = new ByteArrayOutputStream();
    iteProg.setErr(err);
    try {
      iteProg.incrementalBuildFromFolder(new File[] {iteLib.getCompilerStateFolder()});
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertTrue(err.toString().contains(
          "The return type is incompatible with A.m()"));
    }
  }

  /**
   * Check that incremental compilation works when library on import options is modified.
   */
  @Test
  public void testDependency002() throws Exception {
    IncrementalTestHelper iteLib =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    iteLib.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A { \n" + "public void m() {} }");

    iteLib.incrementalBuildFromFolder();
    iteLib.snapshotJackFilesModificationDate();
    Assert.assertEquals(1, iteLib.getJayceCount());


    IncrementalTestHelper iteProg =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    iteProg.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B { \n" + " public void m(){} }");

    iteProg.incrementalBuildFromFolder(null /* classpath */,
        Arrays.asList(iteLib.getCompilerStateFolder()));
    iteProg.snapshotJackFilesModificationDate();
    Assert.assertEquals(1, iteProg.getJayceCount());

    DexBuffer db;
    try (InputStream is = new FileInputStream(iteProg.getDexFile())) {
      db = new DexBuffer(is, new FileLocation(iteProg.getDexFile()));
    }
    Assert.assertTrue(db.typeNames().contains("Ljack/incremental/A;"));
    Assert.assertTrue(db.typeNames().contains("Ljack/incremental/B;"));

    iteLib.addJavaFile("jack.incremental", "C.java", "package jack.incremental; \n"
        + "public class C { \n" + "public void m() {} }");
    iteLib.incrementalBuildFromFolder();
    iteLib.snapshotJackFilesModificationDate();
    Assert.assertEquals(2, iteLib.getJayceCount());


    iteProg.incrementalBuildFromFolder(null, Arrays.asList(iteLib.getCompilerStateFolder()));
    iteProg.snapshotJackFilesModificationDate();
    Assert.assertEquals(1, iteProg.getJayceCount());

    try (InputStream is = new FileInputStream(iteProg.getDexFile())) {
      db = new DexBuffer(is, new FileLocation(iteProg.getDexFile()));
    }
    Assert.assertTrue(db.typeNames().contains("Ljack/incremental/A;"));
    Assert.assertTrue(db.typeNames().contains("Ljack/incremental/B;"));
    Assert.assertTrue(db.typeNames().contains("Ljack/incremental/C;"));
  }

  /**
   * Check that incremental compilation works when library on import options is modified, and also
   * adding a new source file (which modifies the number of Jayce file in the incremental folder).
   */
  @Test
  public void testDependency002b() throws Exception {
    IncrementalTestHelper iteLib =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    iteLib.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A { \n" + "public void m() {} }");

    iteLib.incrementalBuildFromFolder();
    iteLib.snapshotJackFilesModificationDate();
    Assert.assertEquals(1, iteLib.getJayceCount());


    IncrementalTestHelper iteProg =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    iteProg.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B { \n" + " public void m(){} }");

    iteProg.incrementalBuildFromFolder(null /* classpath */,
        Arrays.asList(iteLib.getCompilerStateFolder()));
    iteProg.snapshotJackFilesModificationDate();
    Assert.assertEquals(1, iteProg.getJayceCount());

    DexBuffer db;
    try (InputStream is = new FileInputStream(iteProg.getDexFile())) {
      db = new DexBuffer(is, new FileLocation(iteProg.getDexFile()));
    }
    Assert.assertTrue(db.typeNames().contains("Ljack/incremental/A;"));
    Assert.assertTrue(db.typeNames().contains("Ljack/incremental/B;"));

    iteLib.addJavaFile("jack.incremental", "C.java", "package jack.incremental; \n"
        + "public class C { \n" + "public void m() {} }");
    iteLib.incrementalBuildFromFolder();
    iteLib.snapshotJackFilesModificationDate();
    Assert.assertEquals(2, iteLib.getJayceCount());

    iteProg.addJavaFile("jack.incremental", "D.java", "package jack.incremental; \n"
        + "public class D { \n" + " public void m(){} }");

    iteProg.incrementalBuildFromFolder(null, Arrays.asList(iteLib.getCompilerStateFolder()));
    iteProg.snapshotJackFilesModificationDate();
    Assert.assertEquals(2, iteProg.getJayceCount());

    try (InputStream is = new FileInputStream(iteProg.getDexFile())) {
      db = new DexBuffer(is, new FileLocation(iteProg.getDexFile()));
    }
    Assert.assertTrue(db.typeNames().contains("Ljack/incremental/A;"));
    Assert.assertTrue(db.typeNames().contains("Ljack/incremental/B;"));
    Assert.assertTrue(db.typeNames().contains("Ljack/incremental/C;"));
    Assert.assertTrue(db.typeNames().contains("Ljack/incremental/D;"));
  }

  /**
   * Check that incremental compilation works when library on import options is modified and that
   * multi-dex native option is enabled.
   */
  @Test
  public void testDependency003() throws Exception {
    IncrementalTestHelper iteLib =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    iteLib.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A { \n" + "public void m() {} }");

    iteLib.incrementalBuildFromFolder();
    iteLib.snapshotJackFilesModificationDate();
    Assert.assertEquals(1, iteLib.getJayceCount());


    IncrementalTestHelper iteProg =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    iteProg.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B { \n" + " public void m(){} }");

    iteProg.incrementalBuildFromFolder(null /* classpath */,
        Arrays.asList(iteLib.getCompilerStateFolder()), MultiDexKind.NATIVE);
    iteProg.snapshotJackFilesModificationDate();
    Assert.assertEquals(1, iteProg.getJayceCount());

    DexBuffer db;
    try (InputStream is = new FileInputStream(iteProg.getDexFile())) {
      db = new DexBuffer(is, new FileLocation(iteProg.getDexFile()));
    }
    Assert.assertTrue(db.typeNames().contains("Ljack/incremental/A;"));
    Assert.assertTrue(db.typeNames().contains("Ljack/incremental/B;"));

    iteLib.addJavaFile("jack.incremental", "C.java", "package jack.incremental; \n"
        + "public class C { \n" + "public void m() {} }");
    iteLib.incrementalBuildFromFolder();
    iteLib.snapshotJackFilesModificationDate();
    Assert.assertEquals(2, iteLib.getJayceCount());


    iteProg.incrementalBuildFromFolder(null, Arrays.asList(iteLib.getCompilerStateFolder()));
    iteProg.snapshotJackFilesModificationDate();
    Assert.assertEquals(1, iteProg.getJayceCount());

    try (InputStream is = new FileInputStream(iteProg.getDexFile())) {
      db = new DexBuffer(is, new FileLocation(iteProg.getDexFile()));
    }
    Assert.assertTrue(db.typeNames().contains("Ljack/incremental/A;"));
    Assert.assertTrue(db.typeNames().contains("Ljack/incremental/B;"));
    Assert.assertTrue(db.typeNames().contains("Ljack/incremental/C;"));
  }
}

