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

import com.android.jack.test.helper.ErrorTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.sched.util.config.UnknownPropertyNameException;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class ConfigErrorTest {

  @Test
  public void testUnknownProperty001() throws Exception {
    ErrorTestHelper ite = new ErrorTestHelper();

    JackApiToolchainBase jackApiToolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    jackApiToolchain.setErrorStream(errOut);

    File sourceFile = AbstractTestTools.createFile(ite.getSourceFolder(), "jack.config", "A.java",
        "package jack.config; \n"+
        "public class A {} \n");

    jackApiToolchain.addProperty("unknown", "true");

    try {
      jackApiToolchain.addToClasspath(jackApiToolchain.getDefaultBootClasspath())
      .srcToExe(
          ite.getOutputDexFolder(), /* zipFile = */ true, ite.getSourceFolder());
      Assert.fail();
    } catch (UnknownPropertyNameException e) {
      // Expected since we use an unknown property.
    } finally {
      Assert.assertEquals("", errOut.toString());
    }
  }

}
