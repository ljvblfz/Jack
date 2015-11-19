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

package com.android.jack.array;

import com.android.jack.Options;
import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackBasedToolchain;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.annotation.Nonnull;

public class ArrayTests extends RuntimeTest {

  @Nonnull
  private static final String PROTECTED_INNER_ACCESS_MESSAGE_CHECK =
      " an IllegalAccessError. As a workaround you may change the inner class visibility";

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.array.test001"),
    "com.android.jack.array.test001.dx.Tests");

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test001() throws Exception {
    new RuntimeTestHelper(TEST001).compileAndRunTest();
  }

  /**
   * Verifies that the warning about protected inner is thrown when necessary.
   */
  @Test
  public void protectedInnerArrayAccessCheck001() throws Exception {
    File testFolder =
        AbstractTestTools.getTestRootDir("com.android.jack.array.protectedinnercheck001.jack");
    File out = AbstractTestTools.createTempDir();

    JackBasedToolchain jackToolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    jackToolchain.addProperty(Options.ANDROID_MIN_API_LEVEL.getName(), "19");

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    jackToolchain.setErrorStream(errOut);

    try {
      jackToolchain.addToClasspath(jackToolchain.getDefaultBootClasspath())
      .srcToExe(out, /* zipFile = */ false, testFolder);
    } finally {
      Assert.assertTrue(errOut.toString().contains(PROTECTED_INNER_ACCESS_MESSAGE_CHECK));
    }
  }

  /**
   * Verifies that the warning about protected inner is not thrown when min api is 20.
   */
  @Test
  public void protectedInnerArrayAccessCheck002() throws Exception {
    File testFolder =
        AbstractTestTools.getTestRootDir("com.android.jack.array.protectedinnercheck001.jack");
    File out = AbstractTestTools.createTempDir();

    JackBasedToolchain jackToolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    jackToolchain.addProperty(Options.ANDROID_MIN_API_LEVEL.getName(), "20");

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    jackToolchain.setErrorStream(errOut);

    try {
      jackToolchain.addToClasspath(jackToolchain.getDefaultBootClasspath())
      .srcToExe(out, /* zipFile = */ false, testFolder);
    } finally {
      Assert.assertFalse(errOut.toString().contains(PROTECTED_INNER_ACCESS_MESSAGE_CHECK));
    }
  }

  /**
   * Verifies that the warning about protected inner is not thrown when min api is 21.
   */
  @Test
  public void protectedInnerArrayAccessCheck003() throws Exception {
    File testFolder =
        AbstractTestTools.getTestRootDir("com.android.jack.array.protectedinnercheck001.jack");
    File out = AbstractTestTools.createTempDir();

    JackBasedToolchain jackToolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    jackToolchain.addProperty(Options.ANDROID_MIN_API_LEVEL.getName(), "21");

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    jackToolchain.setErrorStream(errOut);

    try {
      jackToolchain.addToClasspath(jackToolchain.getDefaultBootClasspath())
      .srcToExe(out, /* zipFile = */ false, testFolder);
    } finally {
      Assert.assertFalse(errOut.toString().contains(PROTECTED_INNER_ACCESS_MESSAGE_CHECK));
    }
  }

  /**
   * Verifies that the warning about protected inner is not thrown when doing some access not
   * triggering the bug.
   */
  @Test
  public void protectedInnerArrayAccessCheck004() throws Exception {
    File testFolder =
        AbstractTestTools.getTestRootDir("com.android.jack.array.protectedinnercheck004.jack");
    File out = AbstractTestTools.createTempDir();

    JackBasedToolchain jackToolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    jackToolchain.addProperty(Options.ANDROID_MIN_API_LEVEL.getName(), "19");

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    jackToolchain.setErrorStream(errOut);

    try {
      jackToolchain.addToClasspath(jackToolchain.getDefaultBootClasspath())
      .srcToExe(out, /* zipFile = */ false, testFolder);
    } finally {
      Assert.assertFalse(errOut.toString().contains(PROTECTED_INNER_ACCESS_MESSAGE_CHECK));
    }
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(TEST001);
  }
}
