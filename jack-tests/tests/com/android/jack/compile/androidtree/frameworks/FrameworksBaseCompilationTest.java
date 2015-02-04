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

package com.android.jack.compile.androidtree.frameworks;

import com.android.jack.TestTools;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.category.SlowTests;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

@Ignore("Tree")
public class FrameworksBaseCompilationTest {

  private static File[] BOOTCLASSPATH;

  private static File[] CLASSPATH;

  private static File SOURCELIST;

  @BeforeClass
  public static void setUpClass() {
    FrameworksBaseCompilationTest.class.getClassLoader().setDefaultAssertionStatus(true);
    BOOTCLASSPATH = new File[] {
        TestTools.getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/core-libart_intermediates/classes.jack")
      };
    CLASSPATH = new File[] {
        TestTools.getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/conscrypt_intermediates/classes.jack"),
        TestTools.getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/okhttp_intermediates/classes.jack"),
        TestTools.getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/core-junit_intermediates/classes.jack"),
        TestTools.getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/bouncycastle_intermediates/classes.jack"),
        TestTools.getFromAndroidTree(
            "out/target/common/obj/JAVA_LIBRARIES/ext_intermediates/classes.jack")};
    SOURCELIST = TestTools.getTargetLibSourcelist("framework");
  }

  @Test
  @Category(SlowTests.class)
  public void compileFrameworks() throws Exception {
    File outDexFolder = AbstractTestTools.createTempDir();
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.setSourceLevel(SourceLevel.JAVA_7);
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");
    toolchain.addToClasspath(BOOTCLASSPATH).addToClasspath(CLASSPATH)
    .srcToExe(
        outDexFolder,
        /* zipFile = */ false,
        SOURCELIST);
  }
}
