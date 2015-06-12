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
import org.junit.Test;

import java.util.List;

/**
 * JUnit test checking dependencies between Java files.
 */
public class DependenciesTest004 {



  /**
   * Check that changing a class into an interface is well detected.
   */
  @Test
  public void testDependency001() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.addJavaFile("jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A  { public void test(B b) {b.call1();} } \n");

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B { public void call1() {} } \n");

    ite.incrementalBuildFromFolder();
    ite.snapshotJackFilesModificationDate();

    ite.addJavaFile("jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public interface B { public void call1(); } \n");

    ite.incrementalBuildFromFolder();

    List<String> fqnOfRebuiltTypes = ite.getFQNOfRebuiltTypes();
    Assert.assertEquals(2, fqnOfRebuiltTypes.size());
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.A"));
    Assert.assertTrue(fqnOfRebuiltTypes.contains("jack.incremental.B"));
  }
}
