/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.coverage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;

public class CoverageJava8Tests extends CoverageTest {
  /**
   * Tests that lambda anonymous classes are not instrumented.
   */
  @Test
  public void test001() throws Exception {
    String testPackageName = getTestPackageName("java8.test001");
    File srcDir = CoverageTest.getTestRootDir(testPackageName);

    JackBasedToolchain toolchain = createJackToolchain();
    toolchain.setSourceLevel(SourceLevel.JAVA_8);
    File coverageMetadataFile = CoverageToolchainBuilder.create(toolchain).build();

    File outDexFolder = AbstractTestTools.createTempDir();
    toolchain.srcToExe(outDexFolder, false, srcDir);

    JsonArray classes = loadJsonCoverageClasses(coverageMetadataFile);
    Assert.assertEquals(1, classes.size());  // Foo class only

    JsonObject jsonClass = getJsonClass(classes, getClassNameForJson(testPackageName + ".Foo"));
    Assert.assertNotNull(jsonClass);

    JsonArray jsonClassMethods = jsonClass.get("methods").getAsJsonArray();
    Assert.assertEquals(4, jsonClassMethods.size());  // constructor + 2 methods + lambda
  }

  /**
   * Tests that static and default methods from interfaces are instrumented.
   */
  @Test
  public void test002() throws Exception {
    String testPackageName = getTestPackageName("java8.test002");
    File srcDir = CoverageTest.getTestRootDir(testPackageName);

    JackBasedToolchain toolchain = createJackToolchain();
    toolchain.setSourceLevel(SourceLevel.JAVA_8);
    // Static and default methods in interfaces are only allowed since API level 24.
    toolchain.addProperty("jack.android.min-api-level", "24");
    File coverageMetadataFile = CoverageToolchainBuilder.create(toolchain).build();

    File outDexFolder = AbstractTestTools.createTempDir();
    toolchain.srcToExe(outDexFolder, false, srcDir);

    JsonArray classes = loadJsonCoverageClasses(coverageMetadataFile);
    Assert.assertEquals(2, classes.size());  // Foo + SAM classes
    Assert.assertNotNull(getJsonClass(classes, getClassNameForJson(testPackageName + ".Foo")));
    Assert.assertNotNull(getJsonClass(classes, getClassNameForJson(testPackageName + ".SAM")));
  }
}
