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

package com.android.jack.error;

import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiToolchain;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * JUnit checking that compilation failed properly.
 */
public class ErrorTest {

  @BeforeClass
  public static void setUpClass() {
    ErrorTest.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testError001() throws Exception {
    checkInternalCompilerException("error.test001");
  }

  @Test
  public void testError002() throws Exception {
    checkInternalCompilerException("error.test002");
  }

  private void checkInternalCompilerException(@Nonnull String testName)
      throws Exception, IOException {
    JackApiToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);

    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setErrorStream(err);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToExe(
          AbstractTestTools.createTempDir(),
          /* zipFile = */ false,
          AbstractTestTools.getTestRootDir("com.android.jack." + testName + ".jack"));
    } catch (FrontendCompilationException e) {
      Assert.assertTrue(!err.toString().contains("InternalCompilerException"));
    } catch (RuntimeException e) {
      Assert.fail();
    }
  }
}
