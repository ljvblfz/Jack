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

package com.android.jack.errorhandling;

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.sched.util.config.UnknownPropertyNameException;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigErrorTest {

  @Test
  public void testUnknownProperty001() throws Exception {
    TestingEnvironment ite = new TestingEnvironment();

    File sourceFile = ite.addFile(ite.getSourceFolder(),"jack.config", "A.java",
        "package jack.config; \n"+
        "public class A {} \n");
    File outZip = TestTools.createTempFile("out", ".zip");

    Options options = new Options();
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add(sourceFile.getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setOutputZip(outZip);
    options.addProperty("unknown", "true");
    options.setClasspath(TestTools.getDefaultBootclasspathString());

    try {
      ite.startErrRedirection();
      ite.compile(options);
      Assert.fail();
    } catch (UnknownPropertyNameException e) {
      // Expected since we use an unknown property.
    } finally {
      Assert.assertEquals("", ite.endErrRedirection());
    }
  }

}
