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

import com.google.common.base.Splitter;

import com.android.jack.JackAbortException;
import com.android.jack.Options;
import com.android.jack.dx.io.ClassData;
import com.android.jack.dx.io.ClassData.Method;
import com.android.jack.dx.io.ClassDef;
import com.android.jack.dx.io.Code;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.dx.io.MethodId;
import com.android.jack.test.helper.FileChecker;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;
import com.android.jack.util.AndroidApiLevel;
import com.android.jack.util.AndroidApiLevel.ProvisionalLevel;

import junit.framework.Assert;

import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * JUnit test for usage of invoke polymorphic.
 */
public class InvokePolymorphicTests {

  @Nonnegative
  private static final long N_API_LEVEL = 24;

  @Nonnegative
  private static final long O_API_LEVEL = 26;

  private RuntimeTestInfo INVOKE_POLYMORPHIC_001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.invokepolymorphic.test001"),
      "com.android.jack.java7.invokepolymorphic.test001.Tests").setSrcDirName("");

  private RuntimeTestInfo INVOKE_POLYMORPHIC_002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.invokepolymorphic.test002"),
      "com.android.jack.java7.invokepolymorphic.test002.Tests").setSrcDirName("");

  private RuntimeTestInfo INVOKE_POLYMORPHIC_003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.invokepolymorphic.test003"),
      "com.android.jack.java7.invokepolymorphic.test003.Tests").setSrcDirName("");

  private RuntimeTestInfo INVOKE_POLYMORPHIC_004 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.invokepolymorphic.test004"),
      "com.android.jack.java7.invokepolymorphic.test004.Tests").setSrcDirName("");

  private RuntimeTestInfo INVOKE_POLYMORPHIC_005 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.invokepolymorphic.test005"),
      "com.android.jack.java7.invokepolymorphic.test005.Tests").setSrcDirName("");

  private RuntimeTestInfo INVOKE_POLYMORPHIC_006 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.invokepolymorphic.test006"),
      "com.android.jack.java7.invokepolymorphic.test006.Tests").setSrcDirName("");

  private RuntimeTestInfo INVOKE_POLYMORPHIC_007 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.invokepolymorphic.test007"),
      "com.android.jack.java7.invokepolymorphic.test007.Tests").setSrcDirName("")
          .addFileChecker(new FileChecker() {
            @Override
            public void check(@Nonnull File file) throws Exception {
              DexBuffer dexBuffer = new DexBuffer(file);
              for (ClassDef classDef : dexBuffer.classDefs()) {
                if (classDef.getTypeName()
                    .equals("Lcom/android/jack/java7/invokepolymorphic/test007/Tests;")) {
                  Assert.assertTrue(classDef.getClassDataOffset() != 0);
                  ClassData classData = dexBuffer.readClassData(classDef);
                  for (Method method : classData.getVirtualMethods()) {
                    MethodId methodId = dexBuffer.methodIds().get(method.getMethodIndex());
                    if (dexBuffer.strings().get(methodId.getNameIndex()).equals("testDummy")) {
                      Code code = dexBuffer.readCode(method);
                      Assert.assertEquals(5, code.getOutsSize());
                    }
                  }
                }
              }
            }
          });

  @Test
  @Runtime
  @KnownIssue
  public void testInvokePolymorphic001() throws Exception {
    run(INVOKE_POLYMORPHIC_001);
  }

  @Test
  @Runtime
  @KnownIssue
  public void testInvokePolymorphic002() throws Exception {
    run(INVOKE_POLYMORPHIC_002);
  }

  @Test
  @Runtime
  @KnownIssue
  public void testInvokePolymorphic003() throws Exception {
    run(INVOKE_POLYMORPHIC_003);
  }

  @Test
  @Runtime
  @KnownIssue
  public void testInvokePolymorphic004() throws Exception {
    run(INVOKE_POLYMORPHIC_004);
  }

  @Test
  @Runtime
  @KnownIssue
  public void testInvokePolymorphic005() throws Exception {
    run(INVOKE_POLYMORPHIC_005);
  }

  @Test
  @Runtime
  @KnownIssue
  public void testInvokePolymorphic006() throws Exception {
    run(INVOKE_POLYMORPHIC_006);
  }

  /**
   * Test that invoke-polymorphic is not allowed without o-b1 or o-b2 as android api level.
   */
  @Test
  @KnownIssue
  public void testInvokePolymorphic006_1() throws Exception {
    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();

    toolchain.setOutputStream(out);
    toolchain.setErrorStream(errOut);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());

    try {
      toolchain.srcToExe(AbstractTestTools.createTempDir(), /* zipFile = */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.java7.invokepolymorphic.test006"));
      Assert.fail();
    } catch (JackAbortException e) {
      // 7 errors + the remaining of error output
      Assert.assertEquals(7 + 1, Splitter.on("ERROR:").splitToList(errOut.toString()).size());
    }
  }

  /**
   * Test that invoke-polymorphic is allowed for Android o-b1.
   */
  @Test
  @KnownIssue
  public void testInvokePolymorphic006_2() throws Exception {
    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();

    toolchain.setOutputStream(out);
    toolchain.setErrorStream(errOut);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.addProperty(Options.ANDROID_MIN_API_LEVEL.getName(), Options.ANDROID_MIN_API_LEVEL
        .getCodec().formatValue(new AndroidApiLevel(ProvisionalLevel.O_BETA1)));

    toolchain.srcToExe(AbstractTestTools.createTempDir(), /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.java7.invokepolymorphic.test006"));
    Assert.assertTrue(errOut.toString().isEmpty());
  }

  @Test
  @Runtime
  @KnownIssue
  public void testInvokePolymorphic007() throws Exception {
    run(INVOKE_POLYMORPHIC_007);
  }

  private void run(@Nonnull RuntimeTestInfo rti) throws Exception {
    new RuntimeTestHelper(rti).setSourceLevel(SourceLevel.JAVA_7)
        .addProperty(Options.ANDROID_MIN_API_LEVEL.getName(), Options.ANDROID_MIN_API_LEVEL
            .getCodec().formatValue(new AndroidApiLevel(ProvisionalLevel.O_BETA1)))
        .compileAndRunTest();
  }
}