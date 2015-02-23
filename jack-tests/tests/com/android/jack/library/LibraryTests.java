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

package com.android.jack.library;

import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.AndroidToolchain;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JackCliToolchain;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.InputZipFile;
import com.android.sched.vfs.ReadZipFS;
import com.android.sched.vfs.VFS;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class LibraryTests {
  @BeforeClass
  public static void setUpClass() {
    LibraryTests.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testEmptyLibraryInClassPath() throws Exception {
    File emptyLib = createEmptyLibrary();

    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(emptyLib)
    .srcToExe(AbstractTestTools.createTempFile("library001", ".jack"), /* zipFile = */
        true, AbstractTestTools.getTestRootDir("com.android.jack.library.test001.jack"));
  }

  @Test
  public void testRscLibraryInClassPath() throws Exception {
    File emptyLib = createRscLibrary();

    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(emptyLib)
    .srcToExe(AbstractTestTools.createTempFile("library001", ".jack"), /* zipFile = */
        true, AbstractTestTools.getTestRootDir("com.android.jack.library.test001.jack"));
  }

  @Test
  public void testImportEmptyLibrary() throws Exception {
    File lib = createEmptyLibrary();
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.addStaticLibs(lib);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(AbstractTestTools.createTempFile("library001", ".jack"), /* zipFile = */
        true, AbstractTestTools.getTestRootDir("com.android.jack.library.test001.jack"));
  }

  @Test
  public void testImportRscLibrary() throws Exception {
    File lib = createRscLibrary();
    File out = AbstractTestTools.createTempFile("library001", ".jack");

    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(JackCliToolchain.class);
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class, exclude);
    toolchain.addStaticLibs(lib);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(out, /* zipFile = */ true,
        AbstractTestTools.getTestRootDir("com.android.jack.library.test001.jack"));

    RunnableHooks hooks = new RunnableHooks();
    VFS vfs = new ReadZipFS(
        new InputZipFile(out.getPath(), hooks, Existence.MUST_EXIST, ChangePermission.NOCHANGE));
    try {
      InputJackLibrary inputJackLibrary = JackLibraryFactory.getInputLibrary(vfs);
      Assert.assertTrue(inputJackLibrary.containsFileType(FileType.RSC));
    } finally {
      vfs.close();
    }
  }

  @Nonnull
  private File createEmptyLibrary() throws IOException, Exception {
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    File emptyLib = AbstractTestTools.createTempFile("empty", toolchain.getLibraryExtension());
    toolchain.srcToLib(emptyLib, /* zipFiles = */ true);

    return emptyLib;
  }

  @Nonnull
  private File createRscLibrary() throws IOException, Exception {
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    File emptyLib = AbstractTestTools.createTempFile("rsc", toolchain.getLibraryExtension());

    toolchain.addResource(AbstractTestTools.getTestRootDir("com.android.jack.library.test001.lib"));
    toolchain.srcToLib(emptyLib, /* zipFiles = */ true);

    return emptyLib;
  }
}
