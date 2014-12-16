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
import java.util.List;

/**
 * JUnit test checking dependencies between Java files.
 */
public class DependenciesTest001 {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Checks that compilation failed due to deletion of B.
   * Compilation must recompile A and failed since B does not longer exists.
   */
  @Test
  public void testDependency001() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A extends B {} \n");

    File f = ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B {} \n");

    ite.incrementalBuildFromFolder();

    ite.deleteJavaFile(f);

    try {
      ite.incrementalBuildFromFolder();
      Assert.fail();
    } catch (FrontendCompilationException e)  {
      // Error is ok
    }
  }

  /**
   * Checks that compilation failed due to deletion of I.
   * Compilation must recompile A and failed since I does not longer exists.
   */
  @Test
  public void testDependency002() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A implements I {} \n");

    File f = ite.addJavaFile("jack.incremental", "I.java",
        "package jack.incremental; \n"+
        "public interface I {} \n");

    ite.incrementalBuildFromFolder();

    ite.deleteJavaFile(f);

    try {
      ite.incrementalBuildFromFolder();
      Assert.fail();
    } catch (FrontendCompilationException e)  {
      // Error is ok
    }
  }

  /**
   * Check that only type A is recompiled since it was modified (extends of B removed).
   */
  @Test
  public void testDependency003() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A extends B {} \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B {} \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(1, fqnOfRebuiltTypes.size());
    Assert.assertEquals("jack.incremental.A", fqnOfRebuiltTypes.get(0));
  }

  /**
   * Check that no types are recompiled between two incremental build without modification.
   */
  @Test
  public void testDependency004() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A extends B {} \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B {} \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(0, fqnOfRebuiltTypes.size());
  }

  /**
   * Check that B and C are recompiled since C is modified.
   * B must also be recompiled since it used C.
   */
  @Test
  public void testDependency005() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public void m(C c) {} } \n");

    ite.addJavaFile("jack.incremental", "C.java",
        "package jack.incremental; \n"+
        "public class C {} \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "C.java",
        "package jack.incremental; \n"+
        "public class C { public void m() {} } \n");

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(2, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.C"));
  }

  /**
   * Check that B and C are recompiled since C is modified.
   * B must also be recompiled since it used C.
   */
  @Test
  public void testDependency006() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public void m() {C c = new C();} } \n");

    ite.addJavaFile("jack.incremental", "C.java",
        "package jack.incremental; \n"+
        "public class C {} \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "C.java",
        "package jack.incremental; \n"+
        "public class C { public void m() {} } \n");

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(2, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.C"));
  }

  /**
   * Check that A, B and C are recompiled since C is modified.
   * A, B must be recompiled since they are sub-type of C.
   */
  @Test
  public void testDependency007() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A extends B {} \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends C {} \n");

    ite.addJavaFile("jack.incremental", "C.java",
        "package jack.incremental; \n"+
        "public class C {} \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "C.java",
        "package jack.incremental; \n"+
        "public class C { public void m() {} } \n");

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(3, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.C"));
  }

  /**
   * Check that compilation of A failed due to a modification into B.
   */
  @Test
  public void testDependency008() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A { public void test() {new B().m();}} \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public void m(){} } \n");

    ite.incrementalBuildFromFolder();

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public void m(int i){} } \n");

    try {
      ite.incrementalBuildFromFolder();
      Assert.fail();
    } catch (FrontendCompilationException e)  {
      // Error is ok
    }
  }

  /**
   * Check that A and B are recompiled.
   * A must be recompiled since it used a field of B.
   */
  @Test
  public void testDependency009() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A { public int getCst() {return B.i;} } \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public static int i; } \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public static int i; public static int j; } \n");

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(2, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));
  }

  /**
   * Check that A and B are recompiled.
   * A must be recompiled since it used a method of B.
   */
  @Test
  public void testDependency010() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A { public void callTest() {B.test();} } \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public static void test(){} } \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public static void test() {}  public static void test1() {} } \n");

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(2, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));
  }

  /**
   * Check that A is recompiled and that compilation failed since B does not longer exists.
   */
  @Test
  public void testDependency011() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A { public boolean callTest(Object o) { return o instanceof B;} } \n");

    File f = ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B {} \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.deleteJavaFile(f);

    try {
      ite.incrementalBuildFromFolder();
      Assert.fail();
    } catch (FrontendCompilationException e)  {
      // Error is ok
    }
  }

  /**
   * Check that A and B are recompiled.
   * A must be recompiled since it used a constant from B.
   */
  @Test
  public void testDependency012() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A { public int getCst() {return B.i;} } \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public static final int i = 10; } \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public static final int i = 12; } \n");

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(2, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));
  }

  /**
   * Check that A and B are recompiled.
   * A must be recompiled since it used a constant from B.
   */
  @Test
  public void testDependency013() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A { public int getCst() {return B.i1 + B.i2;} } \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public static final int i1 = 10; public static final int i2 = 20; } \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public static final int i1 = 20; public static final int i2 = 20; } \n");

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(2, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));
  }

  /**
   * Check that A and B are recompiled.
   * A must be recompiled since it used a constant from B.
   */
  @Test
  public void testDependency014() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A { public int getCst() {return B.b + C.c;} } \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public static final int b = 10; } \n");

    ite.addJavaFile("jack.incremental", "C.java",
        "package jack.incremental; \n"+
        "public class C { public static final int c = 10; } \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "C.java",
        "package jack.incremental; \n"+
        "public class C { public static final int c = 20; } \n");

    ite.incrementalBuildFromFolder();
    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(2, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.C"));

    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public static final int b = 20; } \n");

    ite.incrementalBuildFromFolder();
    fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(2, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));
  }

  /**
   * Check that second and third compilation failed since I was modified but not B for the second
   * compilation and not A for the third. Check that fourth compilation rebuilt A, B, I since I and
   * B was modified.
   */
  @Test
  public void testDependency015() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A extends B { @Override public void m() {} } \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B implements I { @Override public void m() {}  } \n");

    ite.addJavaFile("jack.incremental", "I.java",
        "package jack.incremental; \n"+
        "public interface I { public void m(); } \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "I.java",
        "package jack.incremental; \n"+
        "public interface I { public void m(int i); } \n");

    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ite.setErr(err);

    try {
      ite.incrementalBuildFromFolder();
      Assert.fail();
    } catch (FrontendCompilationException e)  {
      // Error is ok
      Assert.assertTrue(
          err.toString().contains("The type B must implement the inherited abstract method I.m(int)"));
      Assert.assertTrue(
          err.toString().contains("The method m() of type B must override or implement a supertype method"));
    }

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B implements I { @Override public void m(int i) {}  } \n");

    err = new ByteArrayOutputStream();
    ite.setErr(err);

    try {
      ite.incrementalBuildFromFolder();
      Assert.fail();
    } catch (FrontendCompilationException e)  {
      // Error is ok
      Assert.assertTrue(err.toString()
          .contains("The method m() of type A must override or implement a supertype method"));
    }

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A extends B { @Override public void m(int i) {}  } \n");

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(3, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.I"));
  }
}
