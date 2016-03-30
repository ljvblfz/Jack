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

package com.android.jack.sourcepath;

import com.android.jack.Options;
import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.sched.vfs.VPath;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SourcepathTests {

  /**
   * Source path test with directory.
   */
  @Test
  public void test001() throws Exception {
    File testRootDir = AbstractTestTools.getTestRootDir("com.android.jack.sourcepath.test001");

    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    // Because source path is not supported by the toolchain
    exclude.add(JillBasedToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude );

    File testOut = AbstractTestTools.createTempDir();

    toolchain.addProperty(Options.SOURCE_PATH.getName(),
        AbstractTestTools.getTestRootDir("").getPath())
    .addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(testOut, false, new File(testRootDir, "jack"));

    InputJackLibrary libOut = AbstractTestTools.getInputJackLibrary(testOut);
    libOut.getFile(FileType.JAYCE,
        new VPath("com/android/jack/sourcepath/test001/lib/Sourcepath001Lib", '/'));
    try {
      libOut.getFile(FileType.JAYCE,
          new VPath("com/android/jack/sourcepath/test001/lib/Sourcepath001UnusedLib", '/'));
      Assert.fail();
    } catch (FileTypeDoesNotExistException e) {
      // expected
    }

  }

  /**
   * Source path test with zip.
   */
  @Test
  public void test002() throws Exception {
    File testRootDir = AbstractTestTools.getTestRootDir("com.android.jack.sourcepath.test002");

    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    // Because source path is not supported by the toolchain
    exclude.add(JillBasedToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude );

    File testOut = AbstractTestTools.createTempDir();

    toolchain.addProperty(Options.SOURCE_PATH.getName(),
        new File(new File(testRootDir, "lib"), "sourcepath002lib.zip").getPath())
    .addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(testOut, false, new File(testRootDir, "jack"));

    InputJackLibrary libOut = AbstractTestTools.getInputJackLibrary(testOut);
    libOut.getFile(FileType.JAYCE,
        new VPath("com/android/jack/sourcepath/test002/lib/Sourcepath002Lib", '/'));
    try {
      libOut.getFile(FileType.JAYCE,
          new VPath("com/android/jack/sourcepath/test002/lib/Sourcepath002UnusedLib", '/'));
      Assert.fail();
    } catch (FileTypeDoesNotExistException e) {
      // expected
    }
  }

}
