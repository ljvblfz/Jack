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

package com.android.jack;

import com.android.jack.category.KnownBugs;
import com.android.jack.util.BytesStreamSucker;

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
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

/**
 * JUnit tests for resource support.
 */
public class ResourceTest {

  @Nonnull
  private static final String COMMON_PATH = "com/android/jack/resource/test001/jack/";
  @Nonnull
  private static final String JACK_FILE_PATH = COMMON_PATH + "IrrelevantForTest.jack";
  @Nonnull
  private static final String RESOURCE1_SHORTPATH = "Resource1";
  @Nonnull
  private static final String RESOURCE2_SHORTPATH = "Resource2";
  @Nonnull
  private static final String RESOURCE3_SHORTPATH = "pack/Resource3";
  @Nonnull
  private static final String RESOURCE4_SHORTPATH = "pack/Resource4";
  @Nonnull
  private static final String RESOURCE1_LONGPATH = COMMON_PATH + RESOURCE1_SHORTPATH;
  @Nonnull
  private static final String RESOURCE2_LONGPATH = COMMON_PATH + RESOURCE2_SHORTPATH;
  @Nonnull
  private static final String RESOURCE3_LONGPATH = COMMON_PATH + RESOURCE3_SHORTPATH;
  @Nonnull
  private static final String RESOURCE4_LONGPATH = COMMON_PATH + RESOURCE4_SHORTPATH;

  @Nonnull
  private static final File FILE =
      TestTools.getJackTestsWithJackFolder("resource/test001");

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testJackArchiveToDexArchive() throws Exception {
    // compile source file to a Jack archive and add resources
    File jackAr = createJackArchiveWithResources();

    // compile Jack archive to dex archive
    File dexAr = TestTools.createTempFile("resourcetestdex", ".zip");
    TestTools.compileJackToDex(new Options(), jackAr, dexAr, true /* zipped */);

    // check that resources are contained in dex archive
    ZipFile zipFile = new ZipFile(dexAr);
    checkResourceContent(zipFile, RESOURCE1_LONGPATH, "Res1");
    checkResourceContent(zipFile, RESOURCE2_LONGPATH, "Res2");
    checkResourceContent(zipFile, RESOURCE3_LONGPATH, "Res3");
    checkResourceContent(zipFile, RESOURCE4_LONGPATH, "Res4");
  }

  @Test
  public void testJackDirToDexArchive() throws Exception {
    // compile source file to a Jack dir
    File jackFolder = TestTools.createTempDir("tempjack", "dir");
    TestTools.compileSourceToJack(new Options(), FILE, TestTools.getDefaultBootclasspathString(),
        jackFolder, false /* non-zipped */);

    // add resources to Jack dir
    copyFileToDir(new File(FILE, RESOURCE1_SHORTPATH), RESOURCE1_LONGPATH, jackFolder);
    copyFileToDir(new File(FILE, RESOURCE2_SHORTPATH), RESOURCE2_LONGPATH, jackFolder);
    copyFileToDir(new File(FILE, RESOURCE3_SHORTPATH), RESOURCE3_LONGPATH, jackFolder);
    copyFileToDir(new File(FILE, RESOURCE4_SHORTPATH), RESOURCE4_LONGPATH, jackFolder);

    // compile Jack dir to dex archive
    File dexAr = TestTools.createTempFile("resourcetestdex", ".zip");
    TestTools.compileJackToDex(new Options(), jackFolder, dexAr, true /* zipped */);

    // check that resources are contained in dex archive
    ZipFile zipFile = new ZipFile(dexAr);
    checkResourceContent(zipFile, RESOURCE1_LONGPATH, "Res1");
    checkResourceContent(zipFile, RESOURCE2_LONGPATH, "Res2");
    checkResourceContent(zipFile, RESOURCE3_LONGPATH, "Res3");
    checkResourceContent(zipFile, RESOURCE4_LONGPATH, "Res4");
  }

  @Test
  public void testJackArchiveToJackArchive() throws Exception {
    // compile source file to a Jack archive and add resources
    File jackAr = createJackArchiveWithResources();

    // run shrobbing from Jack archive to Jack archive
    File shrobbedJackAr = TestTools.createTempFile("shrobbedJackAr", ".zip");
    ProguardFlags flags = new ProguardFlags(new File(FILE, "proguard.flags"));
    TestTools.shrobJackToJack(new Options(),
        jackAr,
        null /* classpath */,
        shrobbedJackAr,
        Collections.singletonList(flags),
        true /* zipped */);

    // check that resources are contained in dex archive
    ZipFile zipFile = new ZipFile(shrobbedJackAr);
    checkResourceContent(zipFile, RESOURCE1_LONGPATH, "Res1");
    checkResourceContent(zipFile, RESOURCE2_LONGPATH, "Res2");
    checkResourceContent(zipFile, RESOURCE3_LONGPATH, "Res3");
    checkResourceContent(zipFile, RESOURCE4_LONGPATH, "Res4");
  }

  @Test
  public void testJackDirToJackArchive() throws Exception {
    // compile source file to a Jack dir
    File jackFolder = TestTools.createTempDir("tempjack", "dir");
    TestTools.compileSourceToJack(new Options(), FILE, TestTools.getDefaultBootclasspathString(),
        jackFolder, false /* non-zipped */);

    // add resources to Jack dir
    copyFileToDir(new File(FILE, RESOURCE1_SHORTPATH), RESOURCE1_LONGPATH, jackFolder);
    copyFileToDir(new File(FILE, RESOURCE2_SHORTPATH), RESOURCE2_LONGPATH, jackFolder);
    copyFileToDir(new File(FILE, RESOURCE3_SHORTPATH), RESOURCE3_LONGPATH, jackFolder);
    copyFileToDir(new File(FILE, RESOURCE4_SHORTPATH), RESOURCE4_LONGPATH, jackFolder);

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
    checkResourceContent(zipFile, RESOURCE1_LONGPATH, "Res1");
    checkResourceContent(zipFile, RESOURCE2_LONGPATH, "Res2");
    checkResourceContent(zipFile, RESOURCE3_LONGPATH, "Res3");
    checkResourceContent(zipFile, RESOURCE4_LONGPATH, "Res4");
  }

  @Test
  public void testJackArchiveToJackDir() throws Exception {
    // compile source file to a Jack archive and add resources
    File jackAr = createJackArchiveWithResources();

    // run shrobbing from Jack archive to Jack dir
    File shrobbedJackDir = TestTools.createTempDir("shrobbedJack", "dir");
    ProguardFlags flags = new ProguardFlags(new File(FILE, "proguard.flags"));
    TestTools.shrobJackToJack(new Options(),
        jackAr,
        null /* classpath */,
        shrobbedJackDir,
        Collections.singletonList(flags),
        false /* non-zipped */);

    // check that resources are contained in Jack dir
    checkResourceContent(shrobbedJackDir, RESOURCE1_LONGPATH, "Res1");
    checkResourceContent(shrobbedJackDir, RESOURCE2_LONGPATH, "Res2");
    checkResourceContent(shrobbedJackDir, RESOURCE3_LONGPATH, "Res3");
    checkResourceContent(shrobbedJackDir, RESOURCE4_LONGPATH, "Res4");
  }

  @Test
  public void testJackDirToJackDir() throws Exception {
    /// compile source file to a Jack dir
    File jackFolder = TestTools.createTempDir("tempjack", "dir");
    TestTools.compileSourceToJack(new Options(), FILE, TestTools.getDefaultBootclasspathString(),
        jackFolder, false /* non-zipped */);

    // add resources to Jack dir
    copyFileToDir(new File(FILE, RESOURCE1_SHORTPATH), RESOURCE1_LONGPATH, jackFolder);
    copyFileToDir(new File(FILE, RESOURCE2_SHORTPATH), RESOURCE2_LONGPATH, jackFolder);
    copyFileToDir(new File(FILE, RESOURCE3_SHORTPATH), RESOURCE3_LONGPATH, jackFolder);
    copyFileToDir(new File(FILE, RESOURCE4_SHORTPATH), RESOURCE4_LONGPATH, jackFolder);

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
    checkResourceContent(shrobbedJackDir, RESOURCE1_LONGPATH, "Res1");
    checkResourceContent(shrobbedJackDir, RESOURCE2_LONGPATH, "Res2");
    checkResourceContent(shrobbedJackDir, RESOURCE3_LONGPATH, "Res3");
    checkResourceContent(shrobbedJackDir, RESOURCE4_LONGPATH, "Res4");
  }

  @Nonnull
  private File createJackArchiveWithResources() throws Exception {
    // compile source file to a Jack file
    File tempJackFolder = TestTools.createTempDir("jack", "dir");
    TestTools.compileSourceToJack(new Options(), FILE, TestTools.getDefaultBootclasspathString(),
        tempJackFolder, false /* non-zipped */);

    // create Jack archive with resources
    File singleJackFile = new File(tempJackFolder, JACK_FILE_PATH);
    File jackAr = TestTools.createTempFile("resourcetestjack", ".zip");
    ZipOutputStream zos = null;
    try {
      zos = new ZipOutputStream(new FileOutputStream(jackAr));

      copyFileToZip(singleJackFile, JACK_FILE_PATH, zos);
      copyFileToZip(new File(FILE, RESOURCE1_SHORTPATH), RESOURCE1_LONGPATH, zos);
      copyFileToZip(new File(FILE, RESOURCE2_SHORTPATH), RESOURCE2_LONGPATH, zos);
      copyFileToZip(new File(FILE, RESOURCE3_SHORTPATH), RESOURCE3_LONGPATH, zos);
      copyFileToZip(new File(FILE, RESOURCE4_SHORTPATH), RESOURCE4_LONGPATH, zos);
    } finally {
      if (zos != null) {
        zos.close();
      }
    }
    return jackAr;
  }

  private void checkResourceContent(@Nonnull ZipFile zipFile, @Nonnull String entryName,
      @Nonnull String expectedContent) throws IOException {
    ZipEntry entry = zipFile.getEntry(entryName);
    Assert.assertNotNull(entry);
    BufferedReader reader = null;
    try {
      InputStream in = zipFile.getInputStream(entry);
      reader = new BufferedReader(new InputStreamReader(in));
      String line = reader.readLine();
      Assert.assertEquals(expectedContent, line);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  private void checkResourceContent(@Nonnull File dir, @Nonnull String path,
      @Nonnull String expectedContent) throws IOException {
    assert dir.isDirectory();
    File file = new File(dir, path);
    Assert.assertTrue(file.exists());
    BufferedReader reader = null;
    try {
      InputStream in = new FileInputStream(file);
      reader = new BufferedReader(new InputStreamReader(in));
      String line = reader.readLine();
      Assert.assertEquals(expectedContent, line);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
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
        BytesStreamSucker sucker = new BytesStreamSucker(fis, fos);
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

  private void copyFileToZip(@Nonnull File fileToCopy, @Nonnull String entryName,
      @Nonnull ZipOutputStream zos)
      throws IOException {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(fileToCopy);
      ZipEntry sourceEntry = new ZipEntry(entryName);
      zos.putNextEntry(sourceEntry);
      BytesStreamSucker sucker = new BytesStreamSucker(fis, zos);
      sucker.suck();
    } finally {
      if (fis != null) {
        fis.close();
      }
    }
  }
}
