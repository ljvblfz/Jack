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

import com.android.jack.Options;
import com.android.jack.ProguardFlags;
import com.android.jack.TestTools;
import com.android.jack.test.category.KnownBugs;
import com.android.jack.library.FileType;
import com.android.jack.shrob.obfuscation.NameProviderFactory;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.sched.util.stream.ByteStreamSucker;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Collections;
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
    File dexDir = TestTools.createTempDir("resourcetestdex", "dir");
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.libToExe(jackAr, dexDir, /* zipFile = */ false);

    // check that resources are contained in dex dir
    checkResourceContentFromDir(dexDir, RESOURCE1_SHORTPATH, "Res1", /*isLib*/ false);
    checkResourceContentFromDir(dexDir, RESOURCE2_SHORTPATH, "Res2", /*isLib*/ false);
    checkResourceContentFromDir(dexDir, RESOURCE3_SHORTPATH, "Res3", /*isLib*/ false);
    checkResourceContentFromDir(dexDir, RESOURCE4_SHORTPATH, "Res4", /*isLib*/ false);
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
    checkResourceContentFromZip(zipFile, RESOURCE1_SHORTPATH, "Res1", /*isLib*/ false);
    checkResourceContentFromZip(zipFile, RESOURCE2_SHORTPATH, "Res2", /*isLib*/ false);
    checkResourceContentFromZip(zipFile, RESOURCE3_SHORTPATH, "Res3", /*isLib*/ false);
    checkResourceContentFromZip(zipFile, RESOURCE4_SHORTPATH, "Res4", /*isLib*/ false);
  }

  @Test
  public void testJackDirToDexArchive() throws Exception {

    // TODO(jmhenaff) adapt this after rebase. Add API for resources

    // compile source file to a Jack dir
    File jackFolder = TestTools.createTempDir("tempjack", "dir");
    Options options = new Options();
    options.addResource(new File(FILE, "rsc"));
    TestTools.compileSourceToJack(options, FILE, TestTools.getDefaultBootclasspathString(),
        jackFolder, false /* non-zipped */);

    // compile Jack dir to dex archive
    File dexAr = TestTools.createTempFile("resourcetestdex", ".zip");
    TestTools.compileJackToDex(new Options(), jackFolder, dexAr, true /* zipped */);

    // check that resources are contained in dex archive
    ZipFile zipFile = new ZipFile(dexAr);
    checkResourceContentFromZip(zipFile, RESOURCE1_SHORTPATH, "Res1", /*isLib*/ false);
    checkResourceContentFromZip(zipFile, RESOURCE2_SHORTPATH, "Res2", /*isLib*/ false);
    checkResourceContentFromZip(zipFile, RESOURCE3_SHORTPATH, "Res3", /*isLib*/ false);
    checkResourceContentFromZip(zipFile, RESOURCE4_SHORTPATH, "Res4", /*isLib*/ false);

//    // compile source file to a Jack dir
//    File jackFolder = AbstractTestTools.createTempDir();
//    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
//    toolchain.srcToLib(
//        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
//        jackFolder,
//        /* zipFiles = */ false,
//        FILE);
//
//    // add resources to Jack dir
//    copyFileToDir(new File(FILE, RESOURCE1_SHORTPATH), RESOURCE1_LONGPATH, jackFolder);
//    copyFileToDir(new File(FILE, RESOURCE2_SHORTPATH), RESOURCE2_LONGPATH, jackFolder);
//    copyFileToDir(new File(FILE, RESOURCE3_SHORTPATH), RESOURCE3_LONGPATH, jackFolder);
//    copyFileToDir(new File(FILE, RESOURCE4_SHORTPATH), RESOURCE4_LONGPATH, jackFolder);
//
//    // compile Jack dir to dex archive
//    File dexAr = TestTools.createTempFile("resourcetestdex", ".zip");
//    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
//    toolchain.libToDex(jackFolder, dexAr, /* zipFile = */ true);
//
//    // check that resources are contained in dex archive
//    ZipFile zipFile = new ZipFile(dexAr);
//    checkResourceContent(zipFile, RESOURCE1_LONGPATH, "Res1");
//    checkResourceContent(zipFile, RESOURCE2_LONGPATH, "Res2");
//    checkResourceContent(zipFile, RESOURCE3_LONGPATH, "Res3");
//    checkResourceContent(zipFile, RESOURCE4_LONGPATH, "Res4");
  }

  @Test
  public void testJackArchiveToJackArchive() throws Exception {
    // compile source file to a Jack archive and add resources
    File jackAr = createJackArchiveWithResources();

    // run shrobbing from Jack archive to Jack archive
    File shrobbedJackAr = AbstractTestTools.createTempFile("shrobbedJackAr", ".zip");
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addProguardFlags(new File(FILE, "proguard.flags"));
    toolchain.libToLib(jackAr, shrobbedJackAr, /* zipFiles = */ true);

    // check that resources are contained in dex archive
    ZipFile zipFile = new ZipFile(shrobbedJackAr);
    checkResourceContentFromZip(zipFile, RESOURCE1_SHORTPATH, "Res1", /*isLib*/ true);
    checkResourceContentFromZip(zipFile, RESOURCE2_SHORTPATH, "Res2", /*isLib*/ true);
    checkResourceContentFromZip(zipFile, RESOURCE3_SHORTPATH, "Res3", /*isLib*/ true);
    checkResourceContentFromZip(zipFile, RESOURCE4_SHORTPATH, "Res4", /*isLib*/ true);
  }

  @Test
  public void testJackDirToJackArchive() throws Exception {
    // compile source file to a Jack dir
    File jackFolder = TestTools.createTempDir("tempjack", "dir");
    Options options = new Options();
    options.addResource(new File(FILE, "rsc"));
    TestTools.compileSourceToJack(options, FILE, TestTools.getDefaultBootclasspathString(),
        jackFolder, false /* non-zipped */);

    // run shrobbing from Jack dir to Jack archive
    File shrobbedJackAr = TestTools.createTempFile("shrobbedJackAr", ".zip");
    ProguardFlags flags = new ProguardFlags(new File(FILE, "proguard.flags"));
    TestTools.shrobJackToJack(new Options(),
        jackFolder,
        null /* classpath */,
        shrobbedJackAr,
        Collections.singletonList(flags),
        true /* zipped */);

    // check that resources are contained in Jack archive
    ZipFile zipFile = new ZipFile(shrobbedJackAr);
    checkResourceContentFromZip(zipFile, RESOURCE1_SHORTPATH, "Res1", /*isLib*/ true);
    checkResourceContentFromZip(zipFile, RESOURCE2_SHORTPATH, "Res2", /*isLib*/ true);
    checkResourceContentFromZip(zipFile, RESOURCE3_SHORTPATH, "Res3", /*isLib*/ true);
    checkResourceContentFromZip(zipFile, RESOURCE4_SHORTPATH, "Res4", /*isLib*/ true);

//    // compile source file to a Jack dir
//    File jackFolder = TestTools.createTempDir("tempjack", "dir");
//    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
//    toolchain.srcToLib(
//        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
//        jackFolder,
//        /* zipFiles = */ false,
//        FILE);
//
//
//    // add resources to Jack dir
//    copyFileToDir(new File(FILE, RESOURCE1_SHORTPATH), RESOURCE1_LONGPATH, jackFolder);
//    copyFileToDir(new File(FILE, RESOURCE2_SHORTPATH), RESOURCE2_LONGPATH, jackFolder);
//    copyFileToDir(new File(FILE, RESOURCE3_SHORTPATH), RESOURCE3_LONGPATH, jackFolder);
//    copyFileToDir(new File(FILE, RESOURCE4_SHORTPATH), RESOURCE4_LONGPATH, jackFolder);
//
//    // run shrobbing from Jack dir to Jack archive
//    File shrobbedJackAr = AbstractTestTools.createTempFile("shrobbedJackAr", ".zip");
//    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
//    toolchain.addProguardFlags(new File(FILE, "proguard.flags"));
//    toolchain.libToLib(jackFolder, shrobbedJackAr, /* zipFiles = */ true);
//
//    // check that resources are contained in Jack archive
//    ZipFile zipFile = new ZipFile(shrobbedJackAr);
//    checkResourceContent(zipFile, RESOURCE1_LONGPATH, "Res1");
//    checkResourceContent(zipFile, RESOURCE2_LONGPATH, "Res2");
//    checkResourceContent(zipFile, RESOURCE3_LONGPATH, "Res3");
//    checkResourceContent(zipFile, RESOURCE4_LONGPATH, "Res4");
  }

  @Test
  public void testJackArchiveToJackDir() throws Exception {
    // compile source file to a Jack archive and add resources
    File jackAr = createJackArchiveWithResources();

    // run shrobbing from Jack archive to Jack dir
    File shrobbedJackDir = AbstractTestTools.createTempDir();
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addProguardFlags(new File(FILE, "proguard.flags"));
    toolchain.libToLib(jackAr, shrobbedJackDir, /* zipFiles = */ false);

    // check that resources are contained in Jack dir
    checkResourceContentFromDir(shrobbedJackDir, RESOURCE1_SHORTPATH, "Res1", /*isLib*/ true);
    checkResourceContentFromDir(shrobbedJackDir, RESOURCE2_SHORTPATH, "Res2", /*isLib*/ true);
    checkResourceContentFromDir(shrobbedJackDir, RESOURCE3_SHORTPATH, "Res3", /*isLib*/ true);
    checkResourceContentFromDir(shrobbedJackDir, RESOURCE4_SHORTPATH, "Res4", /*isLib*/ true);

  }

  @Test
  public void testJackDirToJackDir() throws Exception {
    /// compile source file to a Jack dir
    File jackFolder = TestTools.createTempDir("tempjack", "dir");
    Options options = new Options();
    options.addResource(new File(FILE, "rsc"));
    TestTools.compileSourceToJack(options, FILE, TestTools.getDefaultBootclasspathString(),
        jackFolder, false /* non-zipped */);

    // run shrobbing from Jack dir to Jack dir
    File shrobbedJackDir = TestTools.createTempDir("shrobbedJack", "dir");
    ProguardFlags flags = new ProguardFlags(new File(FILE, "proguard.flags"));
    TestTools.shrobJackToJack(new Options(),
        jackFolder,
        null /* classpath */,
        shrobbedJackDir,
        Collections.singletonList(flags),
        false /* non-zipped */);

    // check that resources are contained in Jack dir
    checkResourceContentFromDir(shrobbedJackDir, RESOURCE1_SHORTPATH, "Res1", /*isLib*/ true);
    checkResourceContentFromDir(shrobbedJackDir, RESOURCE2_SHORTPATH, "Res2", /*isLib*/ true);
    checkResourceContentFromDir(shrobbedJackDir, RESOURCE3_SHORTPATH, "Res3", /*isLib*/ true);
    checkResourceContentFromDir(shrobbedJackDir, RESOURCE4_SHORTPATH, "Res4", /*isLib*/ true);


//    // compile source file to a Jack dir
//    File jackFolder = AbstractTestTools.createTempDir();
//    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
//    toolchain.srcToLib(
//        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
//        jackFolder,
//        /* zipFiles = */ false,
//        FILE);
//
//    // add resources to Jack dir
//    copyFileToDir(new File(FILE, RESOURCE1_SHORTPATH), RESOURCE1_LONGPATH, jackFolder);
//    copyFileToDir(new File(FILE, RESOURCE2_SHORTPATH), RESOURCE2_LONGPATH, jackFolder);
//    copyFileToDir(new File(FILE, RESOURCE3_SHORTPATH), RESOURCE3_LONGPATH, jackFolder);
//    copyFileToDir(new File(FILE, RESOURCE4_SHORTPATH), RESOURCE4_LONGPATH, jackFolder);
//
//    // run shrobbing from Jack dir to Jack dir
//    File shrobbedJackDir = AbstractTestTools.createTempDir();
//    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
//    toolchain.addProguardFlags(new File(FILE, "proguard.flags"));
//    toolchain.libToLib(jackFolder, shrobbedJackDir, /* zipFiles = */ false);
//
//
//    // check that resources are contained in Jack dir
//    checkResourceContent(shrobbedJackDir, RESOURCE1_LONGPATH, "Res1");
//    checkResourceContent(shrobbedJackDir, RESOURCE2_LONGPATH, "Res2");
//    checkResourceContent(shrobbedJackDir, RESOURCE3_LONGPATH, "Res3");
//    checkResourceContent(shrobbedJackDir, RESOURCE4_LONGPATH, "Res4");
  }

  @Test
  public void testJackDirToDexDir() throws Exception {
    // compile source file to a Jack dir
    File jackFolder = TestTools.createTempDir("tempjack", "dir");
    Options options = new Options();
    options.addResource(new File(FILE, "rsc"));
    TestTools.compileSourceToJack(options, FILE, TestTools.getDefaultBootclasspathString(),
        jackFolder, false /* non-zipped */);

    // compile Jack dir to a dex dir
    File dexDir = TestTools.createTempDir("resourcetestdex", "dir");
    TestTools.compileJackToDex(new Options(), jackFolder, dexDir, false /* zipped */);

    // check that resources are contained in dex dir
    checkResourceContentFromDir(dexDir, RESOURCE1_SHORTPATH, "Res1", /*isLib*/ false);
    checkResourceContentFromDir(dexDir, RESOURCE2_SHORTPATH, "Res2", /*isLib*/ false);
    checkResourceContentFromDir(dexDir, RESOURCE3_SHORTPATH, "Res3", /*isLib*/ false);
    checkResourceContentFromDir(dexDir, RESOURCE4_SHORTPATH, "Res4", /*isLib*/ false);

//    // compile source file to a Jack dir
//    File jackFolder = AbstractTestTools.createTempDir();
//    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
//    toolchain.srcToLib(
//        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
//        jackFolder,
//        /* zipFiles = */ false,
//        FILE);
//
//    // add resources to Jack dir
//    copyFileToDir(new File(FILE, RESOURCE1_SHORTPATH), RESOURCE1_LONGPATH, jackFolder);
//    copyFileToDir(new File(FILE, RESOURCE2_SHORTPATH), RESOURCE2_LONGPATH, jackFolder);
//    copyFileToDir(new File(FILE, RESOURCE3_SHORTPATH), RESOURCE3_LONGPATH, jackFolder);
//    copyFileToDir(new File(FILE, RESOURCE4_SHORTPATH), RESOURCE4_LONGPATH, jackFolder);
//
//    // compile Jack dir to a dex dir
//    File dexDir = AbstractTestTools.createTempDir();
//    AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class)
//      .libToDex(jackFolder, dexDir, /* zipFile = */ false);
//
//    // check that resources are contained in dex dir
//    checkResourceContent(dexDir, RESOURCE1_LONGPATH, "Res1");
//    checkResourceContent(dexDir, RESOURCE2_LONGPATH, "Res2");
//    checkResourceContent(dexDir, RESOURCE3_LONGPATH, "Res3");
//    checkResourceContent(dexDir, RESOURCE4_LONGPATH, "Res4");
  }

  @Test
  public void testJackToDexInSameDir() throws Exception {
    // compile source file to a Jack dir
    File jackFolder = TestTools.createTempDir("tempjack", "dir");
    Options options = new Options();
    options.addResource(new File(FILE, "rsc"));
    TestTools.compileSourceToJack(options, FILE, TestTools.getDefaultBootclasspathString(),
        jackFolder, false /* non-zipped */);

    // compile Jack dir to same dir
    File dexDir = jackFolder;
    TestTools.compileJackToDex(new Options(), jackFolder, dexDir, false /* zipped */);

    // check that resources are contained in dex dir
    checkResourceContentFromDir(dexDir, RESOURCE1_SHORTPATH, "Res1", /*isLib*/ false);
    checkResourceContentFromDir(dexDir, RESOURCE2_SHORTPATH, "Res2", /*isLib*/ false);
    checkResourceContentFromDir(dexDir, RESOURCE3_SHORTPATH, "Res3", /*isLib*/ false);
    checkResourceContentFromDir(dexDir, RESOURCE4_SHORTPATH, "Res4", /*isLib*/ false);

//    // compile source file to a Jack dir
//    File jackFolder = AbstractTestTools.createTempDir();
//    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
//    toolchain.srcToLib(
//        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
//        jackFolder,
//        /* zipFiles = */ false,
//        FILE);
//
//    // add resources to Jack dir
//    copyFileToDir(new File(FILE, RESOURCE1_SHORTPATH), RESOURCE1_LONGPATH, jackFolder);
//    copyFileToDir(new File(FILE, RESOURCE2_SHORTPATH), RESOURCE2_LONGPATH, jackFolder);
//    copyFileToDir(new File(FILE, RESOURCE3_SHORTPATH), RESOURCE3_LONGPATH, jackFolder);
//    copyFileToDir(new File(FILE, RESOURCE4_SHORTPATH), RESOURCE4_LONGPATH, jackFolder);
//
//    // compile Jack dir to same dir
//    File dexDir = jackFolder;
//    AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class)
//      .libToDex(jackFolder, dexDir, /* zipFile = */ false);
//
//    // check that resources are contained in dex dir
//    checkResourceContent(dexDir, RESOURCE1_LONGPATH, "Res1");
//    checkResourceContent(dexDir, RESOURCE2_LONGPATH, "Res2");
//    checkResourceContent(dexDir, RESOURCE3_LONGPATH, "Res3");
//    checkResourceContent(dexDir, RESOURCE4_LONGPATH, "Res4");
  }

  @Test
  @Category(KnownBugs.class)
  public void testResourceContentAdaptation() throws Exception {
    // compile source file to a Jack dir
    File jackOutputFolder = AbstractTestTools.createTempDir();
    //String testName = "resource/test003";
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.resource.test003");
    File jackTestFolder = AbstractTestTools.getTestRootDir("com.android.jack.resource.test003.jack");
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.srcToLib(
          AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
          jackOutputFolder,
          /* zipFiles = */ false,
          testFolder);

    String resource1LongPath = "com/android/jack/resource/test003/jack/A";
    String resource2LongPath = "com/android/jack/resource/test003/jack/A.txt";

    // add resources to Jack dir
    copyFileToDir(new File(jackTestFolder, "A.txt"), resource1LongPath, jackOutputFolder);
    copyFileToDir(new File(jackTestFolder, "A.txt"), resource2LongPath, jackOutputFolder);

    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addProperty(NameProviderFactory.NAMEPROVIDER.getName(), "rot13");
    File shrobbedJackDir = AbstractTestTools.createTempDir();
    toolchain.addProguardFlags(new File(testFolder, "proguard.flags001"));
    toolchain.libToLib(jackOutputFolder, shrobbedJackDir, /* zipFiles = */ false);

    // check that resources are contained in Jack dir and check their contents
    File referenceFileContent = new File(testFolder, "refs/A.txt");
    checkResourceContent(new File(shrobbedJackDir, "pcz/nbqfcvq/wnpx/frgcifpr/hrgh003/wnpx/A"),
        referenceFileContent);
    checkResourceContent(new File(shrobbedJackDir, "pcz/nbqfcvq/wnpx/frgcifpr/hrgh003/wnpx/N.txt"),
        referenceFileContent);
  }

  @Nonnull
  private File createJackArchiveWithResources() throws Exception {
    // compile source file to a Jack file
    //File tempJackFolder = TestTools.createTempDir("jack", "dir");
    File jackAr = TestTools.createTempFile("resourcetestjack", ".zip");
    Options options = new Options();
    options.addResource(new File(FILE, "rsc"));
    TestTools.compileSourceToJack(options, FILE, TestTools.getDefaultBootclasspathString(),
        jackAr, true /* non-zipped */);

    return jackAr;

//    // compile source file to a Jack file
//    File tempJackFolder = AbstractTestTools.createTempDir();
//    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
//    toolchain.srcToLib(
//        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
//        tempJackFolder,
//        /* zipFiles = */ false,
//        FILE);
//
//    // create Jack archive with resources
//    File singleJackFile = new File(tempJackFolder, JACK_FILE_PATH);
//    File jackAr = AbstractTestTools.createTempFile("resourcetestjack", ".zip");
//    ZipOutputStream zos = null;
//    try {
//      zos = new ZipOutputStream(new FileOutputStream(jackAr));
//
//      copyFileToZip(singleJackFile, JACK_FILE_PATH, zos);
//      copyFileToZip(new File(FILE, RESOURCE1_SHORTPATH), RESOURCE1_LONGPATH, zos);
//      copyFileToZip(new File(FILE, RESOURCE2_SHORTPATH), RESOURCE2_LONGPATH, zos);
//      copyFileToZip(new File(FILE, RESOURCE3_SHORTPATH), RESOURCE3_LONGPATH, zos);
//      copyFileToZip(new File(FILE, RESOURCE4_SHORTPATH), RESOURCE4_LONGPATH, zos);
//    } finally {
//      if (zos != null) {
//        zos.close();
//      }
//    }
//    return jackAr;
  }

  private void checkResourceContentFromZip(@Nonnull ZipFile zipFile, @Nonnull String entryName,
      @Nonnull String expectedContent, boolean isLib) throws IOException {
    ZipEntry entry = zipFile.getEntry((isLib ? FileType.RSC.getPrefix() + '/' : "") + entryName);
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

  private void checkResourceContentFromDir(@Nonnull File dir, @Nonnull String path,
      @Nonnull String expectedContent, boolean isLib) throws IOException {
    assert dir.isDirectory();
    File file = new File(isLib ? new File(dir, FileType.RSC.getPrefix()) : dir, path);
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


  private void checkResourceContent(@Nonnull File candidateFileContent,
      @Nonnull File referenceFileContent) throws IOException {
    Assert.assertTrue(candidateFileContent.exists());
    BufferedReader candidateReader = null;
    BufferedReader referenceReader = null;
    try {
      candidateReader = new BufferedReader(new InputStreamReader(new FileInputStream(candidateFileContent)));
      referenceReader = new BufferedReader(new InputStreamReader(new FileInputStream(referenceFileContent)));
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

  private void copyFileToDir(@Nonnull File fileToCopy, @Nonnull String relativePath,
      @Nonnull File dir) throws IOException {
    FileOutputStream fos = null;
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(fileToCopy);
      File copiedFile = new File(dir, relativePath);
      File parentDir = copiedFile.getParentFile();
      if (!parentDir.exists()) {
        boolean res = parentDir.mkdirs();
        if (!res) {
          throw new AssertionError();
        }
      }
      try {
        fos = new FileOutputStream(copiedFile);
        ByteStreamSucker sucker = new ByteStreamSucker(fis, fos);
        sucker.suck();
      } finally {
        if (fos != null) {
          fos.close();
        }
      }
    } finally {
      if (fis != null) {
        fis.close();
      }
    }
  }

}
