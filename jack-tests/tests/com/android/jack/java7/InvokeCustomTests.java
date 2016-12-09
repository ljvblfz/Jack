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

package com.android.jack.java7;

import com.android.jack.Options;
import com.android.jack.test.TestsProperties;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;
import com.android.jack.util.AndroidApiLevel;
import com.android.jack.util.AndroidApiLevel.ProvisionalLevel;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * JUnit test for usage of invoke custom.
 */
public class InvokeCustomTests {

  private RuntimeTestInfo INVOKE_CUSTOM_001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.invokecustom.test001"),
      "com.android.jack.java7.invokecustom.test001.Tests").setSrcDirName("");

  private RuntimeTestInfo INVOKE_CUSTOM_002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.invokecustom.test002"),
      "com.android.jack.java7.invokecustom.test002.Tests").setSrcDirName("");

  private RuntimeTestInfo INVOKE_CUSTOM_003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.invokecustom.test003"),
      "com.android.jack.java7.invokecustom.test003.Tests").setSrcDirName("");

  private RuntimeTestInfo INVOKE_CUSTOM_004 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.invokecustom.test004"),
      "com.android.jack.java7.invokecustom.test004.Tests").setSrcDirName("");

  @Test
  @Runtime
  @KnownIssue
  public void testInvokeCustom001() throws Exception {
    run(INVOKE_CUSTOM_001);
  }

  @Test
  @Runtime
  @KnownIssue
  public void testInvokeCustom002() throws Exception {
    run(INVOKE_CUSTOM_002);
  }

  @Test
  @Runtime
  @KnownIssue
  public void testInvokeCustom003() throws Exception {
    run(INVOKE_CUSTOM_003);
  }

  @Test
  @Runtime
  @KnownIssue
  public void testInvokeCustom004() throws Exception {
    run(INVOKE_CUSTOM_004);
  }

  private void run(@Nonnull RuntimeTestInfo rti) throws Exception {
    List<Class<? extends IToolchain>> excludeClazz = new ArrayList<Class<? extends IToolchain>>(1);
    excludeClazz.add(JillBasedToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeClazz);

    File outClassesDex = AbstractTestTools.createTempDir();
    toolchain.addProperty(Options.ANDROID_MIN_API_LEVEL.getName(), Options.ANDROID_MIN_API_LEVEL
        .getCodec().formatValue(new AndroidApiLevel(ProvisionalLevel.O_BETA2)))
        .setSourceLevel(SourceLevel.JAVA_7).addToClasspath(toolchain.getDefaultBootClasspath())
        .addToClasspath(new File(TestsProperties.getJackRootDir(),
            "jack-tests/prebuilts/jack-test-annotations-lib.jack"))
        .srcToExe(outClassesDex, /* zipFile = */ false,
            new File(rti.directory, rti.srcDirName));

    RuntimeTestHelper.runOnRuntimeEnvironments(Collections.singletonList(rti.jUnit),
        RuntimeTestHelper.getJunitDex(), new File(outClassesDex, "classes.dex"));
  }
}