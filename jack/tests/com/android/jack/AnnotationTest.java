/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack;

import com.android.jack.category.RedundantTests;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

/**
 * JUnit test for compilation of annotation.
 */
public class AnnotationTest {

  private static final File[] BOOTCLASSPATH = TestTools.getDefaultBootclasspath();
  private static final File ANNOTATION001_PATH =
      TestTools.getJackTestsWithJackFolder("annotation/test001");

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  @Category(RedundantTests.class)
  public void test001() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        ANNOTATION001_PATH));
  }

  @Test
  public void test001_2() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        new File(ANNOTATION001_PATH, "Annotation2.java")));
  }

  @Test
  public void test001_3() throws Exception {
    TestTools.checkStructure(BOOTCLASSPATH, null,
        new File(ANNOTATION001_PATH, "Annotation2.java"), false /*withDebugInfo*/);
  }

  @Test
  public void test001_4() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(new File[] {
        new File(ANNOTATION001_PATH, "Annotation8.java"),
        new File(ANNOTATION001_PATH, "Annotated2.java")}));
  }

  @Test
  public void test001_5() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(new File[] {
        new File(ANNOTATION001_PATH, "Annotation7.java"),
        new File(ANNOTATION001_PATH, "Annotated3.java")}));
  }

  @Test
  public void test001_13() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        new File(ANNOTATION001_PATH, "Annotation13.java")));
  }

  @Test
  public void test001_OneEnum() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        new File(ANNOTATION001_PATH, "OneEnum.java")));
  }

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  @Test
  @Category(RedundantTests.class)
  public void test002() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("annotation/test002")));
  }

  @Test
  @Category(RedundantTests.class)
  public void test003() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("annotation/test003")));
  }

  @Test
  @Category(RedundantTests.class)
  public void test004() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("annotation/test004")));
  }

  @Test
  @Category(RedundantTests.class)
  public void test005() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("annotation/test005")));
  }

  @Test
  @Category(RedundantTests.class)
  public void test006() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("annotation/test006")));
  }

  @Test
  @Category(RedundantTests.class)
  public void test007() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("annotation/test007")));
  }

  @Test
  @Category(RedundantTests.class)
  public void test008() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("annotation/test008")));
  }

  @Test
  @Category(RedundantTests.class)
  public void test009() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("annotation/test009")));
  }

  /**
   * Compares annotations in dex file to a reference.
   */

  @Test
  public void test001Structure() throws Exception {
    AnnotationTestTools.checkStructure(BOOTCLASSPATH,
        null,
        ANNOTATION001_PATH,
        null);
  }

  @Test
  public void test002Structure() throws Exception {
    AnnotationTestTools.checkStructure(BOOTCLASSPATH,
        null,
        TestTools.getJackTestsWithJackFolder("annotation/test002"),
        null);
  }

  @Test
  public void test003Structure() throws Exception {
    AnnotationTestTools.checkStructure(BOOTCLASSPATH,
        null,
        TestTools.getJackTestsWithJackFolder("annotation/test003"),
        null);
  }

  @Test
  public void test004Structure() throws Exception {
    AnnotationTestTools.checkStructure(BOOTCLASSPATH,
        null,
        TestTools.getJackTestsWithJackFolder("annotation/test004"),
        null);
  }

  @Test
  public void test005Structure() throws Exception {
    AnnotationTestTools.checkStructure(BOOTCLASSPATH,
        null,
        TestTools.getJackTestsWithJackFolder("annotation/test005"),
        null);
  }

  @Test
  public void test006Structure() throws Exception {
    AnnotationTestTools.checkStructure(BOOTCLASSPATH,
        null,
        TestTools.getJackTestsWithJackFolder("annotation/test006"),
        null);
  }

  @Test
  public void test007Structure() throws Exception {
    AnnotationTestTools.checkStructure(BOOTCLASSPATH,
        null,
        TestTools.getJackTestsWithJackFolder("annotation/test007"),
        null);
  }

  @Test
  public void test008Structure() throws Exception {
    AnnotationTestTools.checkStructure(BOOTCLASSPATH,
        null,
        TestTools.getJackTestsWithJackFolder("annotation/test008"),
        null);
  }

  @Test
  public void test009Structure() throws Exception {
    AnnotationTestTools.checkStructure(BOOTCLASSPATH,
        null,
        TestTools.getJackTestsWithJackFolder("annotation/test009"),
        null);
  }
}
