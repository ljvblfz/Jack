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

import java.io.File;
import java.util.List;

/**
 * JUnit test checking incremental support.
 */
public class DependenciesTest015 {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Check that incremental compilation works when library on classpath is modified.
   */
  @Test
  public void testDependency001() throws Exception {
    IncrementalTestingEnvironment iteLib =
        new IncrementalTestingEnvironment(TestTools.createTempDir("DependenciesTest_", "_001_lib"));

    iteLib.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public abstract class A { \n" + "public abstract void m(); }");

    iteLib.incrementalBuildFromFolder();
    iteLib.snapshotJackFilesModificationDate();
    List<File> jackFilesLib = iteLib.getJackFiles();
    Assert.assertEquals(1, jackFilesLib.size());


    IncrementalTestingEnvironment iteProg =
        new IncrementalTestingEnvironment(TestTools.createTempDir("DependenciesTest_", "_001_prog"));

    iteProg.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B extends A { \n" + " @Override public void m(){} }");

    iteProg.incrementalBuildFromFolder(new File[]{iteLib.getJackFolder()});
    iteProg.snapshotJackFilesModificationDate();
    Assert.assertEquals(1, iteProg.getJackFiles().size());

    iteLib.deleteJavaFile("jack.incremental", "A.java");
    iteLib.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public abstract class A { \n" + "public abstract int m(); }");
    iteLib.incrementalBuildFromFolder();
    iteLib.snapshotJackFilesModificationDate();
    jackFilesLib = iteLib.getJackFiles();
    Assert.assertEquals(1, jackFilesLib.size());

    try {
      iteProg.incrementalBuildFromFolder(new File[] {iteLib.getJackFolder()});
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertTrue(iteProg.getStringRepresentingErr().contains(
          "The return type is incompatible with A.m()"));
    }
  }
}

