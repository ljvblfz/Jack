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
import com.android.jack.test.helper.IncrementalTestHelper;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * JUnit test checking dependencies between Java files.
 */
@Ignore("Tree")
public class DependenciesTest010 {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Check that incremental compilation support switch on constant value.
   */
  @Test
  public void testDependency001() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(TestTools.createTempDir("DependenciesTest_", "_001"));

    ite.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A { "
        + "public static void main(String[] args) {"
        + "     System.out.print(new A().test(2));"
        + "   }"
        + "   public int test(int value) {"
        + "       switch(value) {"
        + "         case B.val1: return 1;"
        + "         case B.val2: return 2;"
        + "         case B.val3: return 3;"
        + "      }"
        + "    return 0;"
        + " } }");

    ite.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B { \n"
        + "    public static final int val1 = 1;"
        + "    public static final int val2 = 2;"
        + "    public static final int val3 = val2 + C.val4;"
        + "}");

    ite.addJavaFile("jack.incremental", "C.java", "package jack.incremental; \n"
        + "public class C { \n"
        + "    public static final int val4 = 1;"
        + "}");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();
    Assert.assertEquals("2", ite.run("jack.incremental.A"));

    ite.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B { \n"
        + "    public static final int val1 =11;"
        + "    public static final int val2 =12;"
        + "    public static final int val3 =13;"
        + "}");

    ite.incrementalBuildFromFolder();
    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(2, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));
    Assert.assertEquals("0", ite.run("jack.incremental.A"));

    ite.snapshotJackFilesModificationDate();
    ite.addJavaFile("jack.incremental", "C.java", "package jack.incremental; \n"
        + "public class C { \n"
        + "    public static final int val4 = 0;"
        + "}");

    ite.incrementalBuildFromFolder();
    fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(1, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.C"));
    Assert.assertEquals("0", ite.run("jack.incremental.A"));

    ite.snapshotJackFilesModificationDate();
    ite.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A { "
        + "public static void main(String[] args) {"
        + "     System.out.print(new A().test(12));"
        + "   }"
        + "   public int test(int value) {"
        + "       switch(value) {"
        + "         case B.val1: return 1;"
        + "         case B.val2: return 2;"
        + "         case B.val3: return 3;"
        + "      }"
        + "    return 0;"
        + " } }");

    ite.incrementalBuildFromFolder();
    fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(1, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertEquals("2", ite.run("jack.incremental.A"));
  }


  /**
   * Check that incremental compilation support switch on enum.
   */
  @Test
  public void testDependency002() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(TestTools.createTempDir("DependenciesTest_", "_002"));

    ite.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "import jack.incremental.B;"
        + "public class A { "
        + "public static void main(String[] args) {"
        + "     System.out.print(new A().test(B.VAL1));"
        + "   }"
        + "   public int test(B b) {"
        + "       switch(b) {"
        + "         case VAL1: return 1;"
        + "         case VAL2: return C.val4;"
        + "         case VAL3: return 3;"
        + "      }"
        + "    return 0;"
        + " } }");

    ite.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public enum B { \n"
        + "    VAL1,"
        + "    VAL2,"
        + "    VAL3"
        + "}");

    ite.addJavaFile("jack.incremental", "C.java", "package jack.incremental; \n"
        + "public class C { \n"
        + "    public static final int val4 =2;"
        + "}");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();
    Assert.assertEquals("1", ite.run("jack.incremental.A"));

    ite.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public enum B { \n"
        + "    VAL1,"
        + "    VAL2,"
        + "    VAL3,"
        + "    VAL4"
        + "}");

    ite.incrementalBuildFromFolder();
    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(2, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));
    Assert.assertEquals("1", ite.run("jack.incremental.A"));
  }

}

