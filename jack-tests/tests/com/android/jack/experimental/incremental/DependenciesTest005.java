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
import com.android.jack.test.helper.IncrementalTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * JUnit test checking dependencies between Java files.
 */
@Ignore("Tree")
public class DependenciesTest005 {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Check that runtime is correct after incremental compilation due to a constant modification.
   */
  @Test
  public void testDependency001() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A  { public static void main(String[] args) {" +
        "System.out.print(C.str + B.str);} " +
        "} \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B  { public static final String str = \"HELLO\"; } \n");

    ite.addJavaFile("jack.incremental", "C.java",
        "package jack.incremental; \n"+
        "public class C  { public static final String str = \"STRING:\"; } \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B  { public static final String str = \"INCREMENTAL\"; } \n");

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(2, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));

    ite.run("jack.incremental.A", "STRING:INCREMENTAL");
  }


  /**
   * Check that runtime is correct after incremental compilation due to a constant modification.
   */
  @Test
  public void testDependency002() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A  { public static void main(String[] args) {" +
        "System.out.print(B.str);} " +
        "} \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B  { public static final String str = \"HELLO\" + C.str; } \n");

    ite.addJavaFile("jack.incremental", "C.java",
        "package jack.incremental; \n"+
        "public class C  { public static final String str = \" WORLD\"; } \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "C.java",
        "package jack.incremental; \n"+
        "public class C  { public static final String str = \" EVERYBODY\"; } \n");

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(3, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.C"));

    ite.run("jack.incremental.A", "HELLO EVERYBODY");
  }

  /**
   * Check that runtime is correct after incremental compilation due to a constant modification.
   */
  @Test
  public void testDependency003() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A { " +
        "   public static final String A = \"A\";" +
        "   public static final String AB = \"A\" + B.B;"+
        "   public static void main(String[] args) { System.out.print(B.BA); } } \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { \n" +
        "   public static final String B = \"B\"; \n" +
        "   public static final String BA = \"B\" + A.A; }");


    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.run("jack.incremental.A", "BA");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { \n" +
        "   public static final String B = \"B\"; \n" +
        "   public static final String BA = \"B\" + A.AB; }");

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(2, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));

    ite.run("jack.incremental.A", "BAB");
  }

  /**
   * Check that runtime is correct after incremental compilation due to a constant modification.
   */
  @Test
  public void testDependency004() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A  { public static void main(String[] args) {" +
        "System.out.print(B.str);} " +
        "} \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B  { public static final String str = D.str + C.str; } \n");

    ite.addJavaFile("jack.incremental", "C.java",
        "package jack.incremental; \n"+
        "public class C  { public static final String str = \"WORLD\" + E.str; } \n");

    ite.addJavaFile("jack.incremental", "D.java",
        "package jack.incremental; \n"+
        "public class D  { public static final String str = \"HELLO\" + E.str; } \n");

    ite.addJavaFile("jack.incremental", "E.java",
        "package jack.incremental; \n"+
        "public class E  { public static final String str = \"/\"; } \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "C.java",
        "package jack.incremental; \n"+
        "public class C  { public static final String str = \"EVERYBODY\"; } \n");

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(3, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.C"));

    ite.run("jack.incremental.A", "HELLO/EVERYBODY");

    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "E.java",
        "package jack.incremental; \n"+
        "public class E  { public static final String str = \" \"; } \n");

    ite.incrementalBuildFromFolder();

    fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(4, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.D"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.E"));

    ite.run("jack.incremental.A", "HELLO EVERYBODY");
  }
}
