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

package com.android.jack.compile.androidtree.services;

import com.android.jack.JarJarRules;
import com.android.jack.Options;
import com.android.jack.ProguardFlags;
import com.android.jack.TestTools;
import com.android.jack.category.RedundantTests;
import com.android.jack.category.SlowTests;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

@Ignore("Tree")
public class ServicesCompilationTest {

  private static File[] BOOTCLASSPATH;

  private static File[] CLASSPATH;

  private static File SOURCELIST;

  @BeforeClass
  public static void setUpClass() {
    ServicesCompilationTest.class.getClassLoader().setDefaultAssertionStatus(true);
    BOOTCLASSPATH = new File[] {
        TestTools.getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/core_intermediates/classes.jar")
      };
    CLASSPATH = new File[] {
        TestTools.getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/android.policy_intermediates/classes.jar"),
        TestTools.getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/core-junit_intermediates/classes.jar"),
        TestTools.getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/ext_intermediates/classes.jar"),
        TestTools.getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/framework_intermediates/classes.jar"),
        TestTools.getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/telephony-common_intermediates/classes.jar")};
    SOURCELIST = TestTools.getTargetLibSourcelist("services");
  }

  @Test
  @Category(RedundantTests.class)
  public void compileServices() throws Exception {
    File out = TestTools.createTempFile("services", ".dex");
    String classpath = TestTools.getClasspathsAsString(BOOTCLASSPATH, CLASSPATH);
    TestTools.compileSourceToDex(new Options(), SOURCELIST, classpath, out, false);
  }

  @Test
  @Category(SlowTests.class)
  public void compareServicesStructure() throws Exception {
    TestTools.checkStructure(
        BOOTCLASSPATH,
        CLASSPATH,
        SOURCELIST,
        false /* compareDebugInfoBinary */,
        true /* compareInstructionNumber */,
        0.45f,
        (JarJarRules) null,
        (ProguardFlags[]) null);
  }
}
