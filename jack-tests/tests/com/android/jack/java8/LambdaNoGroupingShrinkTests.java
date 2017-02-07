/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack.java8;

import com.android.jack.Options;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public class LambdaNoGroupingShrinkTests {

  @Nonnull
  private File PROGUARD_SHRINK_NOTHING = new File(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test044"),
      "shrink-nothing.flags");

  @Nonnull
  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test044"),
      "com.android.jack.java8.lambda.test044.jack.Tests");

  @Test
  @Runtime
  public void testLamba040_whole() throws Exception {

    File lib = makeLibrary(new File[]{}, TEST001.directory);

    test(TEST001.jUnit, lib);
  }

  @Test
  @Runtime
  public void testLamba040_ByLib() throws Exception {

    File lib2 = makeLibrary(new File[]{}, new File(TEST001.directory, "lib2"));
    File lib1 = makeLibrary(new File[]{lib2}, new File(TEST001.directory, "lib1"));
    File libJack = makeLibrary(new File[]{lib2, lib1}, new File(TEST001.directory, "jack"));

    test(TEST001.jUnit, lib2, lib1, libJack);

  }

  @Test
  @Runtime
  public void ltestLamba040_ByTestClass() throws Exception {

    File libs = makeLibrary(new File[]{},
            new File(TEST001.directory, "lib1"), new File(TEST001.directory, "lib2"));
    File b2 = makeLibrary(new File[]{libs}, new File(TEST001.directory, "jack/B2.java"));
    File b3 = makeLibrary(new File[]{libs}, new File(TEST001.directory, "jack/B3.java"));
    File test = makeLibrary(new File[]{libs, b2, b3},
        new File(TEST001.directory, "jack/Tests.java"));

    test(TEST001.jUnit, libs, b2, b3, test);

  }

  @Nonnull
  protected File makeLibrary(@Nonnull File[] classpath, @Nonnull File... src) throws Exception {
    JackBasedToolchain toolchain = getToolchain();

    File lib = AbstractTestTools.createTempFile("lib", ".jack");
    toolchain
        .addToClasspath(toolchain.getDefaultBootClasspath())
        .addToClasspath(classpath)
        .setSourceLevel(SourceLevel.JAVA_8)
        .addProguardFlags(PROGUARD_SHRINK_NOTHING)
        .srcToLib(lib, /* zipFiles = */ true, src);

    return lib;
  }

  @Nonnull
  protected void test(@Nonnull String test, @Nonnull File... libs) throws Exception {
    JackBasedToolchain toolchain = getToolchain();

    File dex = AbstractTestTools.createTempFile("dex", ".jar");
    toolchain
        .setSourceLevel(SourceLevel.JAVA_8)
        .addProguardFlags(PROGUARD_SHRINK_NOTHING)
        .libToExe(libs, dex, /* zipFiles = */ true);

    RuntimeTestHelper.runOnRuntimeEnvironments(
        Collections.singletonList(test),
        RuntimeTestHelper.getJunitDex(), dex);

  }

  @Nonnull
  protected JackBasedToolchain getToolchain() {
    List<Class<? extends IToolchain>> excludedToolchains =
        new ArrayList<Class<? extends IToolchain>>();
    excludedToolchains.add(JillBasedToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    return toolchain.addProperty(Options.LAMBDA_GROUPING_SCOPE.getName(), "none");
  }

}
