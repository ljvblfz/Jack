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

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

/**
 * JUnit test checking dependencies between Java files.
 */
public class DependenciesTest009 {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Check that usages does not change during incremental compilation and that dependencies are
   * identical.
   */
  @Test
  public void testDependency001() throws Exception {
    IncrementalTestingEnvironment ite =
        new IncrementalTestingEnvironment(TestTools.createTempDir("DependenciesTest_", "_001"));

    ite.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A extends B { }");

    ite.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B extends C { }");

    ite.addJavaFile("jack.incremental", "C.java", "package jack.incremental; \n"
        + "public class C { }");

    ite.incrementalBuildFromFolder();

    CompilerState csm = new CompilerState(ite.getCompilerStateDirectory());
    csm.read();
    Map<String, Set<String>> dependencies1 = csm.computeDependencies();

    ite.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A extends B { public int field;}");

    ite.incrementalBuildFromFolder();

    csm.read();
    Map<String, Set<String>> dependencies2 = csm.computeDependencies();

    assert dependencies1.equals(dependencies2);
    Assert.assertEquals(dependencies1, dependencies2);
  }
}
