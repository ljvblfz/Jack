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

package com.android.jack.experimental.incremental;

import com.android.jack.Options;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.resource.ResourceTests;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.IncrementalToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * JUnit test checking behavior of incremental mode with other options.
 */
public class IncrementalTests {

  /**
   * Test compilation with incremental + generation of dex and jack lib
   * at the same time + import of an unpredexed library.
   * @throws Exception
   */
  @Test
  public void testIncremental001() throws Exception {

    List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(1);
    excludeList.add(IncrementalToolchain.class);

    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
    File unpredexedLibrary =
        AbstractTestTools.createTempFile("unpredexedLibrary", toolchain.getLibraryExtension());
    toolchain.addProperty(Options.GENERATE_DEX_IN_LIBRARY.getName(), "false");
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.srcToLib(
        unpredexedLibrary,
        /* zipFiles = */ true,
        AbstractTestTools.getTestRootDir("com.android.jack.experimental.incremental.test001.lib"));

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
    File library =
        AbstractTestTools.createTempFile("library", toolchain.getLibraryExtension());
    File dexFile = AbstractTestTools.createTempDir();
    toolchain.addStaticLibs(unpredexedLibrary);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.setOutputJack(library, /* zipFiles = */ true);
    toolchain.setIncrementalFolder(AbstractTestTools.createTempDir());
    toolchain.srcToExe(
        dexFile,
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.experimental.incremental.test001.jack"));
  }

  /**
   * Incremental test with import of a resource dir, compiling sources to a dex directly.
   */
  @Test
  public void testIncremental002ResourceImportToExe() throws Exception {

    File srcDir =
        AbstractTestTools.getTestRootDir("com.android.jack.experimental.incremental.test002.jack");
    File incrementalDir = AbstractTestTools.createTempDir();

    //copy resources to a tmp dir
    File rscDestDir = AbstractTestTools.createTempDir();
    File rscOriginalDir =
        AbstractTestTools.getTestRootDir("com.android.jack.experimental.incremental.test002.rsc");
    File rscOriginalFile1 = new File(rscOriginalDir, "resource1.txt");
    AbstractTestTools.copyFileToDir(rscOriginalFile1, "resource1.txt", rscDestDir);
    File rscDestFile1 = new File(rscDestDir, "resource1.txt");
    File rscOriginalFile2 = new File(rscOriginalDir, "pack/resource2.txt");
    AbstractTestTools.copyFileToDir(rscOriginalFile2, "pack/resource2.txt", rscDestDir);
    File rscDestFile2 = new File(rscDestDir, "pack/resource2.txt");

    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    File dexDir = AbstractTestTools.createTempDir();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.setIncrementalFolder(incrementalDir);
    toolchain.addResourceDir(rscDestDir);
    toolchain.srcToExe(
        dexDir,
        /* zipFile = */ false,
        srcDir);

    ResourceTests.checkResourceContentFromDir(dexDir, "resource1.txt", "resource1");
    ResourceTests.checkResourceContentFromDir(dexDir, "pack/resource2.txt", "resource2");

    // modify resources and rebuild
    FileWriter writer1 = new FileWriter(rscDestFile1);
    try {
      writer1.write("resource1.1");
    } finally {
      writer1.close();
    }

    FileWriter writer2 = new FileWriter(rscDestFile2);
    try {
      writer2.write("resource2.1");
    } finally {
      writer2.close();
    }

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    File dexDir2 = AbstractTestTools.createTempDir();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.setIncrementalFolder(incrementalDir);
    toolchain.addResourceDir(rscDestDir);
    toolchain.srcToExe(
        dexDir2,
        /* zipFile = */ false,
        srcDir);

    ResourceTests.checkResourceContentFromDir(dexDir2, "resource1.txt", "resource1.1");
    ResourceTests.checkResourceContentFromDir(dexDir2, "pack/resource2.txt", "resource2.1");
  }

  @Test
  public void testIncremental002ResourceImportToLib() throws Exception {

    List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(1);
    excludeList.add(JillBasedToolchain.class); // We check Jack libraries, we don't want Jars

    File srcDir =
        AbstractTestTools.getTestRootDir("com.android.jack.experimental.incremental.test002.jack");
    File incrementalDir = AbstractTestTools.createTempDir();

    //copy resources to a tmp dir
    File rscDestDir = AbstractTestTools.createTempDir();
    File rscOriginalDir =
        AbstractTestTools.getTestRootDir("com.android.jack.experimental.incremental.test002.rsc");
    File rscOriginalFile1 = new File(rscOriginalDir, "resource1.txt");
    AbstractTestTools.copyFileToDir(rscOriginalFile1, "resource1.txt", rscDestDir);
    File rscDestFile1 = new File(rscDestDir, "resource1.txt");
    File rscOriginalFile2 = new File(rscOriginalDir, "pack/resource2.txt");
    AbstractTestTools.copyFileToDir(rscOriginalFile2, "pack/resource2.txt", rscDestDir);
    File rscDestFile2 = new File(rscDestDir, "pack/resource2.txt");

    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
    File lib = AbstractTestTools.createTempFile("lib", toolchain.getLibraryExtension());
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.setIncrementalFolder(incrementalDir);
    toolchain.addResourceDir(rscDestDir);
    toolchain.srcToLib(lib, /* zipFiles = */ true, srcDir);

    // check lib
    InputJackLibrary libObject = AbstractTestTools.getInputJackLibrary(lib);
    try {
      ResourceTests.checkResourceContentFromLib(libObject, "resource1.txt", "resource1");
      ResourceTests.checkResourceContentFromLib(libObject, "pack/resource2.txt", "resource2");
    } finally {
      libObject.close();
    }

    // modify resources and rebuild
    FileWriter writer1 = new FileWriter(rscDestFile1);
    try {
      writer1.write("resource1.1");
    } finally {
      writer1.close();
    }

    FileWriter writer2 = new FileWriter(rscDestFile2);
    try {
      writer2.write("resource2.1");
    } finally {
      writer2.close();
    }

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.setIncrementalFolder(incrementalDir);
    toolchain.addResourceDir(rscDestDir);
    toolchain.srcToLib(lib, /* zipFiles = */ true, srcDir);

    // check lib
    libObject = AbstractTestTools.getInputJackLibrary(lib);
    try {
      ResourceTests.checkResourceContentFromLib(libObject, "resource1.txt", "resource1.1");
      ResourceTests.checkResourceContentFromLib(libObject, "pack/resource2.txt", "resource2.1");
    } finally {
      libObject.close();
    }
  }

  @Test
  public void testIncremental002ResourceImportToExePlusLib() throws Exception {

    List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(1);
    excludeList.add(JillBasedToolchain.class); // We check Jack libraries, we don't want Jars

    File srcDir =
        AbstractTestTools.getTestRootDir("com.android.jack.experimental.incremental.test002.jack");
    File incrementalDir = AbstractTestTools.createTempDir();

    //copy resources to a tmp dir
    File rscDestDir = AbstractTestTools.createTempDir();
    File rscOriginalDir =
        AbstractTestTools.getTestRootDir("com.android.jack.experimental.incremental.test002.rsc");
    File rscOriginalFile1 = new File(rscOriginalDir, "resource1.txt");
    AbstractTestTools.copyFileToDir(rscOriginalFile1, "resource1.txt", rscDestDir);
    File rscDestFile1 = new File(rscDestDir, "resource1.txt");
    File rscOriginalFile2 = new File(rscOriginalDir, "pack/resource2.txt");
    AbstractTestTools.copyFileToDir(rscOriginalFile2, "pack/resource2.txt", rscDestDir);
    File rscDestFile2 = new File(rscDestDir, "pack/resource2.txt");

    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
    File dexDir = AbstractTestTools.createTempDir();
    File lib = AbstractTestTools.createTempFile("lib", toolchain.getLibraryExtension());
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.setIncrementalFolder(incrementalDir);
    toolchain.addResourceDir(rscDestDir);
    toolchain.setOutputJack(lib, /* zipFiles = */ true);
    toolchain.srcToExe(
        dexDir,
        /* zipFile = */ false,
        srcDir);

    // check dex dir
    ResourceTests.checkResourceContentFromDir(dexDir, "resource1.txt", "resource1");
    ResourceTests.checkResourceContentFromDir(dexDir, "pack/resource2.txt", "resource2");

    // check lib
    InputJackLibrary libObject = AbstractTestTools.getInputJackLibrary(lib);
    try {
      ResourceTests.checkResourceContentFromLib(libObject, "resource1.txt", "resource1");
      ResourceTests.checkResourceContentFromLib(libObject, "pack/resource2.txt", "resource2");
    } finally {
      libObject.close();
    }

    // modify resources and rebuild
    FileWriter writer1 = new FileWriter(rscDestFile1);
    try {
      writer1.write("resource1.1");
    } finally {
      writer1.close();
    }

    FileWriter writer2 = new FileWriter(rscDestFile2);
    try {
      writer2.write("resource2.1");
    } finally {
      writer2.close();
    }

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
    File dexDir2 = AbstractTestTools.createTempDir();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.setIncrementalFolder(incrementalDir);
    toolchain.addResourceDir(rscDestDir);
    toolchain.setOutputJack(lib, /* zipFiles = */ true);
    toolchain.srcToExe(
        dexDir2,
        /* zipFile = */ false,
        srcDir);

    // check dex dir
    ResourceTests.checkResourceContentFromDir(dexDir2, "resource1.txt", "resource1.1");
    ResourceTests.checkResourceContentFromDir(dexDir2, "pack/resource2.txt", "resource2.1");

    // check lib
    libObject = AbstractTestTools.getInputJackLibrary(lib);
    try {
      ResourceTests.checkResourceContentFromLib(libObject, "resource1.txt", "resource1.1");
      ResourceTests.checkResourceContentFromLib(libObject, "pack/resource2.txt", "resource2.1");
    } finally {
        libObject.close();
    }
  }
}
