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

package com.android.jack.resource;

import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.shrob.obfuscation.NameProviderFactory;
import com.android.jack.test.category.KnownBugs;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;

/**
 * JUnit tests for resource support.
 */
public class ResourceTests {

  @Nonnull
  private static final String COMMON_PATH = "com/android/jack/resource/test001/jack/";
  @Nonnull
  private static final String JACK_FILE_PATH = FileType.JAYCE.getPrefix() + "/" + COMMON_PATH
      + "IrrelevantForTest.jayce";
  @Nonnull
  private static final String DEX_FILE_PATH = FileType.DEX.getPrefix() + "/" + COMMON_PATH
      + "IrrelevantForTest.dex";
  @Nonnull
  private static final String RESOURCE1_SHORTPATH = "Resource1";
  @Nonnull
  private static final String RESOURCE2_SHORTPATH = "Resource2";
  @Nonnull
  private static final String RESOURCE3_SHORTPATH = "pack/Resource3";
  @Nonnull
  private static final String RESOURCE4_SHORTPATH = "pack/Resource4";

  @Nonnull
  private static final File FILE =
      AbstractTestTools.getTestRootDir("com.android.jack.resource.test001.jack");

  @BeforeClass
  public static void setUpClass() {
    ResourceTests.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testJackArchiveToDexDir() throws Exception {
    // compile source file to a Jack archive and add resources
    File jackAr = createJackArchiveWithResources();

    // compile Jack archive to a dex dir
    File dexDir = AbstractTestTools.createTempDir();
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.libToExe(jackAr, dexDir, /* zipFile = */ false);

    // check that resources are contained in dex dir
    checkResourceContentFromDir(dexDir, RESOURCE1_SHORTPATH, "Res1");
    checkResourceContentFromDir(dexDir, RESOURCE2_SHORTPATH, "Res2");
    checkResourceContentFromDir(dexDir, RESOURCE3_SHORTPATH, "Res3");
    checkResourceContentFromDir(dexDir, RESOURCE4_SHORTPATH, "Res4");
  }

  @Test
  public void testJackArchiveToDexArchive() throws Exception {
    // compile source file to a Jack archive and add resources
    File jackAr = createJackArchiveWithResources();

    // compile Jack archive to dex archive
    File dexAr = AbstractTestTools.createTempFile("resourcetestdex", ".zip");
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.libToExe(jackAr, dexAr, /* zipFile = */ true);

    // check that resources are contained in dex archive
    ZipFile zipFile = new ZipFile(dexAr);
    checkResourceContentFromZip(zipFile, RESOURCE1_SHORTPATH, "Res1");
    checkResourceContentFromZip(zipFile, RESOURCE2_SHORTPATH, "Res2");
    checkResourceContentFromZip(zipFile, RESOURCE3_SHORTPATH, "Res3");
    checkResourceContentFromZip(zipFile, RESOURCE4_SHORTPATH, "Res4");
  }

  @Test
  public void testJackDirToDexArchive() throws Exception {
    // compile source file to a Jack dir
    File jackFolder = AbstractTestTools.createTempDir();
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addResource(new File(FILE, "rsc"));
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        jackFolder,
        /* zipFiles = */ false,
        FILE);

    // compile Jack dir to dex archive
    File dexAr = AbstractTestTools.createTempFile("resourcetestdex", ".zip");
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.libToExe(jackFolder, dexAr, /* zipFile = */ true);

    // check that resources are contained in dex archive
    ZipFile zipFile = new ZipFile(dexAr);
    checkResourceContentFromZip(zipFile, RESOURCE1_SHORTPATH, "Res1");
    checkResourceContentFromZip(zipFile, RESOURCE2_SHORTPATH, "Res2");
    checkResourceContentFromZip(zipFile, RESOURCE3_SHORTPATH, "Res3");
    checkResourceContentFromZip(zipFile, RESOURCE4_SHORTPATH, "Res4");
  }

  @Test
  public void testJackArchiveToJackArchive() throws Exception {
    // compile source file to a Jack archive and add resources
    File jackAr = createJackArchiveWithResources();

    // run shrobbing from Jack archive to Jack archive
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    File shrobbedJackAr =
        AbstractTestTools.createTempFile("shrobbedJackAr", toolchain.getLibraryExtension());
    toolchain.addProguardFlags(new File(FILE, "proguard.flags"));
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.libToLib(jackAr, shrobbedJackAr, /* zipFiles = */ true);

    // check that resources are contained in dex archive
    InputJackLibrary shrobbedLib = null;
    try {
      shrobbedLib = AbstractTestTools.getInputJackLibrary(shrobbedJackAr);
      checkResourceContentFromLib(shrobbedLib, RESOURCE1_SHORTPATH, "Res1");
      checkResourceContentFromLib(shrobbedLib, RESOURCE2_SHORTPATH, "Res2");
      checkResourceContentFromLib(shrobbedLib, RESOURCE3_SHORTPATH, "Res3");
      checkResourceContentFromLib(shrobbedLib, RESOURCE4_SHORTPATH, "Res4");
    } finally {
      if (shrobbedLib != null) {
        shrobbedLib.close();
      }
    }
  }

  @Test
  public void testJackDirToJackArchive() throws Exception {
    // compile source file to a Jack dir
    File jackFolder = AbstractTestTools.createTempDir();
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addResource(new File(FILE, "rsc"));
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        jackFolder,
        /* zipFiles = */ false,
        FILE);

    // run shrobbing from Jack dir to Jack archive
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    File shrobbedJackAr =
        AbstractTestTools.createTempFile("shrobbedJackAr", toolchain.getLibraryExtension());
    toolchain.addProguardFlags(new File(FILE, "proguard.flags"));
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.libToLib(jackFolder, shrobbedJackAr, /* zipFiles = */ true);

    // check that resources are contained in Jack archive
    InputJackLibrary shrobbedLib = null;
    try {
      shrobbedLib = AbstractTestTools.getInputJackLibrary(shrobbedJackAr);
      checkResourceContentFromLib(shrobbedLib, RESOURCE1_SHORTPATH, "Res1");
      checkResourceContentFromLib(shrobbedLib, RESOURCE2_SHORTPATH, "Res2");
      checkResourceContentFromLib(shrobbedLib, RESOURCE3_SHORTPATH, "Res3");
      checkResourceContentFromLib(shrobbedLib, RESOURCE4_SHORTPATH, "Res4");
    } finally {
      if (shrobbedLib != null) {
        shrobbedLib.close();
      }
    }
  }

  @Test
  public void testJackArchiveToJackDir() throws Exception {
    // compile source file to a Jack archive and add resources
    File jackAr = createJackArchiveWithResources();

    // run shrobbing from Jack archive to Jack dir
    File shrobbedJackDir = AbstractTestTools.createTempDir();
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addProguardFlags(new File(FILE, "proguard.flags"));
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.libToLib(jackAr, shrobbedJackDir, /* zipFiles = */ false);

    // check that resources are contained in Jack dir
    InputJackLibrary shrobbedLib = null;
    try {
      shrobbedLib = AbstractTestTools.getInputJackLibrary(shrobbedJackDir);
      checkResourceContentFromLib(shrobbedLib, RESOURCE1_SHORTPATH, "Res1");
      checkResourceContentFromLib(shrobbedLib, RESOURCE2_SHORTPATH, "Res2");
      checkResourceContentFromLib(shrobbedLib, RESOURCE3_SHORTPATH, "Res3");
      checkResourceContentFromLib(shrobbedLib, RESOURCE4_SHORTPATH, "Res4");
    } finally {
      if (shrobbedLib != null) {
        shrobbedLib.close();
      }
    }
  }

  @Test
  public void testJackDirToJackDir() throws Exception {
    // compile source file to a Jack dir
    File jackFolder = AbstractTestTools.createTempDir();
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addResource(new File(FILE, "rsc"));
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        jackFolder,
        /* zipFiles = */ false,
        FILE);

    // run shrobbing from Jack dir to Jack dir
    File shrobbedJackDir = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addProguardFlags(new File(FILE, "proguard.flags"));
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.libToLib(jackFolder, shrobbedJackDir, /* zipFiles = */ false);

    // check that resources are contained in Jack dir
    InputJackLibrary shrobbedLib = null;
    try {
      shrobbedLib = AbstractTestTools.getInputJackLibrary(shrobbedJackDir);
      checkResourceContentFromLib(shrobbedLib, RESOURCE1_SHORTPATH, "Res1");
      checkResourceContentFromLib(shrobbedLib, RESOURCE2_SHORTPATH, "Res2");
      checkResourceContentFromLib(shrobbedLib, RESOURCE3_SHORTPATH, "Res3");
      checkResourceContentFromLib(shrobbedLib, RESOURCE4_SHORTPATH, "Res4");
    } finally {
      if (shrobbedLib != null) {
        shrobbedLib.close();
      }
    }
  }

  @Test
  public void testJackDirToDexDir() throws Exception {
    // compile source file to a Jack dir
    File jackFolder = AbstractTestTools.createTempDir();
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addResource(new File(FILE, "rsc"));
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        jackFolder,
        /* zipFiles = */ false,
        FILE);

    // compile Jack dir to a dex dir
    File dexDir = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.libToExe(jackFolder, dexDir, /* zipFile = */ false);

    // check that resources are contained in dex dir
    checkResourceContentFromDir(dexDir, RESOURCE1_SHORTPATH, "Res1");
    checkResourceContentFromDir(dexDir, RESOURCE2_SHORTPATH, "Res2");
    checkResourceContentFromDir(dexDir, RESOURCE3_SHORTPATH, "Res3");
    checkResourceContentFromDir(dexDir, RESOURCE4_SHORTPATH, "Res4");
  }

  @Test
  public void testJackToDexInSameDir() throws Exception {
    // compile source file to a Jack dir
    File jackFolder = AbstractTestTools.createTempDir();
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addResource(new File(FILE, "rsc"));
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        jackFolder,
        /* zipFiles = */ false,
        FILE);

    // compile Jack dir to same dir
    File dexDir = jackFolder;
    AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class)
      .libToExe(jackFolder, dexDir, /* zipFile = */ false);

    // check that resources are contained in dex dir
    checkResourceContentFromDir(dexDir, RESOURCE1_SHORTPATH, "Res1");
    checkResourceContentFromDir(dexDir, RESOURCE2_SHORTPATH, "Res2");
    checkResourceContentFromDir(dexDir, RESOURCE3_SHORTPATH, "Res3");
    checkResourceContentFromDir(dexDir, RESOURCE4_SHORTPATH, "Res4");
  }

  @Test
  @Category(KnownBugs.class)
  public void testResourceContentAdaptation() throws Exception {
    // compile source file to a Jack dir
    File jackOutputFolder = AbstractTestTools.createTempDir();
    //String testName = "resource/test003";
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.resource.test003");
    File jackTestFolder =
        AbstractTestTools.getTestRootDir("com.android.jack.resource.test003.jack");
    File rscFolder = new File(jackTestFolder, "rsc");
    String resource1LongPath = "com/android/jack/resource/test003/jack/A";
    String resource2LongPath = "com/android/jack/resource/test003/jack/A.txt";
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addResource(rscFolder).addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
          jackOutputFolder,
          /* zipFiles = */ false,
          testFolder);

    // check that resources are contained in Jack dir and check their contents
    InputJackLibrary lib = null;
    try {
      lib = AbstractTestTools.getInputJackLibrary(jackOutputFolder);
      InputVFile candidateFile1 = lib.getFile(FileType.RSC, new VPath(resource1LongPath, '/'));
      InputVFile candidateFile2 = lib.getFile(FileType.RSC, new VPath(resource2LongPath, '/'));
      checkResourceContent(candidateFile1, new File(rscFolder, resource1LongPath));
      checkResourceContent(candidateFile2, new File(rscFolder, resource2LongPath));
    } finally {
      if (lib != null) {
        lib.close();
      }
    }

    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addProperty(NameProviderFactory.NAMEPROVIDER.getName(), "rot13");
    File shrobbedJackDir = AbstractTestTools.createTempDir();
    toolchain.addProguardFlags(new File(testFolder, "proguard.flags001"));
    toolchain.libToLib(jackOutputFolder, shrobbedJackDir, /* zipFiles = */ false);

    // check that resources are contained in Jack dir and check their contents
    InputJackLibrary shrobbedLib = null;
    try {
      shrobbedLib = AbstractTestTools.getInputJackLibrary(shrobbedJackDir);
      File referenceFile = new File(testFolder, "refs/A.txt");
      InputVFile candidateFile1 = shrobbedLib.getFile(FileType.RSC,
          new VPath("pcz/nbqfcvq/wnpx/frgcifpr/hrgh003/wnpx/A", '/'));
      InputVFile candidateFile2 = shrobbedLib.getFile(FileType.RSC,
          new VPath("pcz/nbqfcvq/wnpx/frgcifpr/hrgh003/wnpx/N.txt", '/'));
      checkResourceContent(candidateFile1, referenceFile);
      checkResourceContent(candidateFile2, referenceFile);
    } finally {
      if (shrobbedLib != null) {
        shrobbedLib.close();
      }
    }
  }

  @Nonnull
  private File createJackArchiveWithResources() throws Exception {
    // compile source file to a Jack file
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    File jackAr = AbstractTestTools.createTempFile("resourcetestjack", toolchain.getLibraryExtension());
    toolchain.addResource(new File(FILE, "rsc"));
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        jackAr,
        /* zipFiles = */ true,
        FILE);

    return jackAr;
  }

  private void checkResourceContentFromZip(@Nonnull ZipFile zipFile, @Nonnull String entryName,
      @Nonnull String expectedContent) throws IOException {
    ZipEntry entry = zipFile.getEntry(entryName);
    Assert.assertNotNull(entry);
    BufferedReader candidateReader = null;
    BufferedReader referenceReader = null;
    try {
      InputStream in = zipFile.getInputStream(entry);
      candidateReader = new BufferedReader(new InputStreamReader(in));
      referenceReader = new BufferedReader(new StringReader(expectedContent));
      compareReadLines(referenceReader, candidateReader);
    } finally {
      if (candidateReader != null) {
        candidateReader.close();
      }
      if (referenceReader != null) {
        referenceReader.close();
      }
    }
  }

  private void checkResourceContentFromLib(@Nonnull InputJackLibrary lib, @Nonnull String path,
      @Nonnull String expectedContent) throws IOException, FileTypeDoesNotExistException {
    InputVFile rescFile = lib.getFile(FileType.RSC, new VPath(path, '/'));
    BufferedReader candidateReader = null;
    BufferedReader referenceReader = null;
    try {
      candidateReader = new BufferedReader(new InputStreamReader(rescFile.getInputStream()));
      referenceReader = new BufferedReader(new StringReader(expectedContent));
      compareReadLines(referenceReader, candidateReader);
    } finally {
      if (candidateReader != null) {
        candidateReader.close();
      }
      if (referenceReader != null) {
        referenceReader.close();
      }
    }
  }

  private void checkResourceContentFromDir(@Nonnull File dir, @Nonnull String path,
      @Nonnull String expectedContent) throws IOException {
    assert dir.isDirectory();
    File file = new File(dir, path);
    Assert.assertTrue(file.exists());
    BufferedReader candidateReader = null;
    BufferedReader referenceReader = null;
    try {
      InputStream in = new FileInputStream(file);
      candidateReader = new BufferedReader(new InputStreamReader(in));
      referenceReader = new BufferedReader(new StringReader(expectedContent));
      compareReadLines(referenceReader, candidateReader);
    } finally {
      if (candidateReader != null) {
        candidateReader.close();
      }
      if (referenceReader != null) {
        referenceReader.close();
      }
    }
  }


  private void checkResourceContent(@Nonnull InputVFile candidateFile,
      @Nonnull File referenceFile) throws IOException {
    BufferedReader candidateReader = null;
    BufferedReader referenceReader = null;
    try {
      candidateReader = new BufferedReader(new InputStreamReader(candidateFile.getInputStream()));
      referenceReader =
          new BufferedReader(new InputStreamReader(new FileInputStream(referenceFile)));
      compareReadLines(referenceReader, candidateReader);
    } finally {
      if (candidateReader != null) {
        candidateReader.close();
      }
      if (referenceReader != null) {
        referenceReader.close();
      }
    }
  }

  private void compareReadLines(@Nonnull BufferedReader referenceReader,
      @Nonnull BufferedReader candidateReader) throws IOException {
      String referenceLine = referenceReader.readLine();
      while (referenceLine != null) {
        String candidateLine = candidateReader.readLine();
        Assert.assertEquals(referenceLine, candidateLine);
        referenceLine = referenceReader.readLine();
      }
      Assert.assertNull(candidateReader.readLine());
  }
}
