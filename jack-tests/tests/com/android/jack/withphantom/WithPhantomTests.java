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

package com.android.jack.withphantom;

import com.android.jack.ProguardFlags;
import com.android.jack.library.FileType;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.LegacyJillToolchain;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
/**
 * Test compilation involving phantoms.
 */
public class WithPhantomTests {

  @Nonnull
  private static final String TEST001 = "com.android.jack.withphantom.test001";
  @Nonnull
  private static final String TEST001_JACK = TEST001 + ".jack";
  @Nonnull
  private static final String TEST002 = "com.android.jack.withphantom.test002";
  @Nonnull
  private static final String TEST002_JACK = TEST002 + ".jack";

  @Nonnull
  private static String fixPath(@Nonnull String unixPath) {
    return unixPath.replace('/', File.separatorChar);
  }

  @Test
  @Ignore
  public void testPhantomOuter() throws Exception {
    File tempJackFolder = AbstractTestTools.createTempDir();
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(LegacyJillToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        tempJackFolder,
        /* zipFiles = */ false,
        AbstractTestTools.getTestRootDir(TEST001_JACK));

    boolean deleted =
        new File(tempJackFolder, FileType.JAYCE.getPrefix() + File.separatorChar
            + fixPath("com/android/jack/withphantom/test001/jack/A.jayce")).delete();
    Assert.assertTrue(deleted);

    File testFolder = AbstractTestTools.getTestRootDir(TEST001);

    File tempOut1 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.addProguardFlags(new ProguardFlags(testFolder, "shrink1.flags"))
    .libToLib(tempJackFolder, tempOut1, /* zipFiles = */ false);

    File tempOut2 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.addProguardFlags(new ProguardFlags(testFolder, "shrink2.flags"))
    .libToLib(tempJackFolder, tempOut2, /* zipFiles = */ false);

    File tempOut3 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.addProguardFlags(new ProguardFlags(testFolder, "obf1.flags"))
    .libToLib(tempJackFolder, tempOut3, /* zipFiles = */ false);

    File tempOut4 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.addProguardFlags(new ProguardFlags(testFolder, "obf2.flags"))
    .libToLib(tempJackFolder, tempOut4, /* zipFiles = */ false);

    File tempOut5 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.libToExe(tempJackFolder, tempOut5, /* zipFile = */ false);
  }

  @Test
  @Ignore
  public void testPhantomInner() throws Exception {
    File tempJackFolder = AbstractTestTools.createTempDir();
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(LegacyJillToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        tempJackFolder,
        /* zipFiles = */ false,
        AbstractTestTools.getTestRootDir(TEST001_JACK));

    boolean deleted =
        new File(tempJackFolder, fixPath(FileType.JAYCE.getPrefix() + File.separatorChar
            + "com/android/jack/withphantom/test001/jack/A$Inner1.jayce")).delete();
    Assert.assertTrue(deleted);
    deleted =
        new File(tempJackFolder, fixPath(FileType.DEX.getPrefix() + File.separatorChar
            + "com/android/jack/withphantom/test001/jack/A$Inner1.dex")).delete();
    Assert.assertTrue(deleted);

    File testFolder = AbstractTestTools.getTestRootDir(TEST001);

    File tempOut1 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addProguardFlags(new ProguardFlags(testFolder, "shrink1.flags"))
    .libToLib(tempJackFolder, tempOut1, /* zipFiles = */ false);

    File tempOut2 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addProguardFlags(new ProguardFlags(testFolder, "shrink2.flags"))
    .libToLib(tempJackFolder, tempOut2, /* zipFiles = */ false);

    File tempOut3 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addProguardFlags(new ProguardFlags(testFolder, "obf1.flags"))
    .libToLib(tempJackFolder, tempOut3, /* zipFiles = */ false);

    File tempOut4 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addProguardFlags(new ProguardFlags(testFolder, "obf2.flags"))
    .libToLib(tempJackFolder, tempOut4, /* zipFiles = */ false);

    File tempOutFolder = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.libToExe(tempJackFolder, tempOutFolder, /* zipFile = */ false);
  }

  @KnownIssue
  @Test
  public void testPhantomLocal() throws Exception {
    File tempJackFolder = AbstractTestTools.createTempDir();
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        tempJackFolder,
        /* zipFiles = */ false,
        AbstractTestTools.getTestRootDir(TEST002_JACK));

    File[] inners =
        new File(tempJackFolder, fixPath(FileType.JAYCE.getPrefix() + File.separatorChar
            + "com/android/jack/withphantom/test002/jack/")).listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.startsWith("A$");
          }
        });
    for (File file : inners) {
      Assert.assertTrue(file.delete());
    }

    File testFolder = AbstractTestTools.getTestRootDir(TEST002);

    File tempOut1 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addProguardFlags(new ProguardFlags(testFolder, "obf1.flags"))
    .libToLib(tempJackFolder, tempOut1, /* zipFiles = */ false);

    File tempOutFolder = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.libToExe(tempJackFolder, tempOutFolder, /* zipFile = */ false);
  }

  @Test
  @Ignore
  public void testPhantomLocalOuter() throws Exception {
    File tempJackFolder = AbstractTestTools.createTempDir();
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(LegacyJillToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        tempJackFolder,
        /* zipFiles = */ false,
        AbstractTestTools.getTestRootDir(TEST002_JACK));

    boolean deleted =
        new File(tempJackFolder, fixPath(FileType.JAYCE.getPrefix() + File.separatorChar
            + "com/android/jack/withphantom/test002/jack/A.jayce")).delete();
    Assert.assertTrue(deleted);

    File testFolder = AbstractTestTools.getTestRootDir(TEST002);

    File tempOut1 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.addProguardFlags(new ProguardFlags(testFolder, "obf1.flags"))
    .libToLib(tempJackFolder, tempOut1, /* zipFiles = */ false);

    File tempOutFolder = AbstractTestTools.createTempDir();
    AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class)
    .libToExe(tempJackFolder, tempOutFolder, /* zipFile = */ false);
  }

}
