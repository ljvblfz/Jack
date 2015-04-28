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

package com.android.jack.fileconflict;

import com.android.jack.JackAbortException;
import com.android.jack.backend.jayce.JayceFileImporter;
import com.android.jack.backend.jayce.TypeImportConflictException;
import com.android.jack.library.FileType;
import com.android.jack.library.JackLibrary;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.resource.ResourceImportConflictException;
import com.android.jack.resource.ResourceImporter;
import com.android.jack.shrob.obfuscation.NameProviderFactory;
import com.android.jack.test.category.KnownBugs;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JackCliToolchain;
import com.android.jack.test.toolchain.LegacyJillToolchain;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * JUnit tests for resource support.
 */
public class FileConflictTests {

  @Nonnull
  private static final String COMMON_PATH_001 = "com/android/jack/fileconflict/test001/jack/";
  @Nonnull
  private static final String COMMON_PATH_002 = "com/android/jack/fileconflict/test002/jack/";
  @Nonnull
  private static final String JACK_FILE_PATH_1 =
      FileType.JAYCE.getPrefix() + "/" + COMMON_PATH_001 + "MyClass.jayce";
  @Nonnull
  private static final String JACK_FILE_PATH_2 =
      FileType.JAYCE.getPrefix() + "/" + COMMON_PATH_001 + "MyClass2.jayce";
  @Nonnull
  private static final String JACK_FILE_PATH_3 =
      FileType.JAYCE.getPrefix() + "/" + COMMON_PATH_001 + "MyClass3.jayce";
  @Nonnull
  private static final String DEX_FILE_PATH_1 =
      FileType.DEX.getPrefix() + "/" + COMMON_PATH_001 + "MyClass.dex";
  @Nonnull
  private static final String DEX_FILE_PATH_2 =
      FileType.DEX.getPrefix() + "/" + COMMON_PATH_001 + "MyClass2.dex";
  @Nonnull
  private static final String DEX_FILE_PATH_3 =
      FileType.DEX.getPrefix() + "/" + COMMON_PATH_001 + "MyClass3.dex";
  @Nonnull
  private static final String JACK_FILE_PATH_002_1 =
      FileType.JAYCE.getPrefix() + "/" + COMMON_PATH_002 + "IrrelevantForTest.jayce";
  @Nonnull
  private static final String JACK_FILE_PATH_002_2 =
      FileType.JAYCE.getPrefix() + "/" + COMMON_PATH_002 + "IrrelevantForTest2.jayce";
  @Nonnull
  private static final String RESOURCE1_SHORTPATH = "Resource1";
  @Nonnull
  private static final String RESOURCE2_SHORTPATH = "Resource2";
  @Nonnull
  private static final String RESOURCE3_SHORTPATH = "Resource3";
  @Nonnull
  private static final String RESOURCE1_LONGPATH = FileType.RSC.getPrefix() + "/"
      + RESOURCE1_SHORTPATH;
  @Nonnull
  private static final String RESOURCE2_LONGPATH = FileType.RSC.getPrefix() + "/"
      + RESOURCE2_SHORTPATH;
  @Nonnull
  private static final String RESOURCE3_LONGPATH = FileType.RSC.getPrefix() + "/"
      + RESOURCE3_SHORTPATH;

  @Nonnull
  private static final File TEST001_DIR =
      AbstractTestTools.getTestRootDir("com.android.jack.fileconflict.test001.jack");

  @Nonnull
  private static final File TEST002_DIR =
      AbstractTestTools.getTestRootDir("com.android.jack.fileconflict.test002.jack");

  @BeforeClass
  public static void setUpClass() {
    FileConflictTests.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting Jack files, and
   * outputting to a Jack folder, with no collision policy specified.
   * @throws Exception
   */
  @Test
  public void test001a() throws Exception {
    File jackOutput = AbstractTestTools.createTempDir();
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    try {
      runTest001(jackOutput, null, errOut, /* isApiTest = */ true, /* verbose = */ false);
      Assert.fail();
    } catch (JackAbortException e) {
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof TypeImportConflictException);
      String errString = errOut.toString();
      Assert.assertTrue(
          errString.contains("Type com.android.jack.fileconflict.test001.jack.MyClass"));
      Assert.assertTrue(errString.contains("has already been imported"));
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting Jack files, and
   * outputting to a Jack folder, with the collision policy set to "fail".
   * @throws Exception
   */
  @Test
  public void test001b() throws Exception {
    File jackOutput = AbstractTestTools.createTempDir();
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    try {
      runTest001(jackOutput, "fail", errOut, /* isApiTest = */ true, /* verbose = */ false);
      Assert.fail();
    } catch (JackAbortException e) {
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof TypeImportConflictException);
      String errString = errOut.toString();
      Assert.assertTrue(
          errString.contains("Type com.android.jack.fileconflict.test001.jack.MyClass"));
      Assert.assertTrue(errString.contains("has already been imported"));
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting Jack files, and
   * outputting to a Jack folder, with the collision policy set to "keep-first", and the default
   * verbosity.
   * @throws Exception
   */
  @Test
  public void test001c() throws Exception {
    File jackOutput = AbstractTestTools.createTempDir();
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    runTest001(jackOutput, "keep-first", errOut /* errorStream */, /* isApiTest = */ false,
        /* verbose = */ false);
    File myClass1 = new File(jackOutput, JACK_FILE_PATH_1);
    File myClass2 = new File(jackOutput, JACK_FILE_PATH_2);
    File myClass3 = new File(jackOutput, JACK_FILE_PATH_3);
    Assert.assertTrue(myClass1.exists());
    Assert.assertTrue(myClass2.exists());
    Assert.assertTrue(myClass3.exists());
    Assert.assertTrue(errOut.toString().isEmpty());
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting Jack files, and
   * outputting to a Jack folder, with the collision policy set to "keep-first", and the verbosity
   * set to "DEBUG".
   * @throws Exception
   */
  @Test
  @Category(KnownBugs.class)
  public void test001d() throws Exception {
    File jackOutput = AbstractTestTools.createTempDir();
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    runTest001(jackOutput, "keep-first", errOut /* errorStream */, /* isApiTest = */ false,
        /* verbose = */ true);
    File myClass1 = new File(jackOutput, JACK_FILE_PATH_1);
    File myClass2 = new File(jackOutput, JACK_FILE_PATH_2);
    File myClass3 = new File(jackOutput, JACK_FILE_PATH_3);
    Assert.assertTrue(myClass1.exists());
    Assert.assertTrue(myClass2.exists());
    Assert.assertTrue(myClass3.exists());
    String errString = errOut.toString();
    Assert.assertTrue(
        errString.contains("Type com.android.jack.fileconflict.test001.jack.MyClass"));
    Assert.assertTrue(
        errString.contains("Type com.android.jack.fileconflict.test001.jack.MyClass2"));
    Assert.assertTrue(
        errString.contains("Type com.android.jack.fileconflict.test001.jack.MyClass3"));
    Assert.assertTrue(errString.contains("has already been imported"));
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack folder, with no collision policy specified.
   * @throws Exception
   */
  @Test
  public void test002a() throws Exception {
    File jackOutput;
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    try {
      jackOutput = runTest002(false /* non-zipped */, null /* collisionPolicy */, errOut,
          /* isApiTest = */ true, /* verbose = */ false);
      Assert.fail();
    } catch (JackAbortException e) {
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof ResourceImportConflictException);
      String errString = errOut.toString();
      Assert.assertTrue(errString.contains("Resource in"));
      Assert.assertTrue(errString.contains("rsc/Resource1"));
      Assert.assertTrue(errString.contains("has already been imported"));
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack folder, with the collision policy set to "fail".
   * @throws Exception
   */
  @Test
  public void test002b() throws Exception {
    File jackOutput;
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    try {
      jackOutput = runTest002(false /* non-zipped */, "fail", errOut, /* isApiTest = */ true,
          /* verbose = */ false);
      Assert.fail();
    } catch (JackAbortException e) {
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof ResourceImportConflictException);
      String errString = errOut.toString();
      Assert.assertTrue(errString.contains("Resource in"));
      Assert.assertTrue(errString.contains("rsc/Resource1"));
      Assert.assertTrue(errString.contains("has already been imported"));
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack folder, with the collision policy set to "keep-first", with default
   * verbosity.
   * @throws Exception
   */
  @Test
  public void test002c() throws Exception {
    File jackOutput;
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    jackOutput = runTest002(false /* non-zipped */, "keep-first", errOut, /* isApiTest = */ false,
        /* verbose = */ false);
    checkResourceContent(jackOutput, RESOURCE1_LONGPATH, "Res1");
    checkResourceContent(jackOutput, RESOURCE2_LONGPATH, "Res2");
    checkResourceContent(jackOutput, RESOURCE3_LONGPATH, "Res3");
    Assert.assertTrue(errOut.toString().isEmpty());
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack zip, with no collision policy specified.
   * @throws Exception
   */
  @Test
  public void test002d() throws Exception {
    File jackOutput;
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    try {
      jackOutput = runTest002(true /* zipped */, null, errOut, /* isApiTest = */ true,
          /* verbose = */ false);
      Assert.fail();
    } catch (JackAbortException e) {
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof ResourceImportConflictException);
      String errString = errOut.toString();
      Assert.assertTrue(errString.contains("Resource in"));
      Assert.assertTrue(errString.contains("rsc/Resource1"));
      Assert.assertTrue(errString.contains("has already been imported"));
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack zip, with the collision policy set to "fail".
   * @throws Exception
   */
  @Test
  public void test002e() throws Exception {
    File jackOutput;
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    try {
      jackOutput = runTest002(true /* zipped */, "fail", errOut, /* isApiTest = */ true,
          /* verbose = */ false);
      Assert.fail();
    } catch (JackAbortException e) {
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof ResourceImportConflictException);
      String errString = errOut.toString();
      Assert.assertTrue(errString.contains("Resource in"));
      Assert.assertTrue(errString.contains("rsc/Resource1"));
      Assert.assertTrue(errString.contains("has already been imported"));
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack zip, with the collision policy set to "keep-first", with default
   * verbosity.
   * @throws Exception
   */
  @Test
  public void test002f() throws Exception {
    File jackOutput;
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    jackOutput = runTest002(true /* zipped */, "keep-first", errOut, /* isApiTest = */ false,
        /* verbose = */ false);
    ZipFile zipFile = new ZipFile(jackOutput);
    checkResourceContent(zipFile, RESOURCE1_LONGPATH, "Res1");
    checkResourceContent(zipFile, RESOURCE2_LONGPATH, "Res2");
    checkResourceContent(zipFile, RESOURCE3_LONGPATH, "Res3");
    Assert.assertTrue(errOut.toString().isEmpty());
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack zip, with the collision policy set to "keep-first", with verbosity set to
   * "DEBUG".
   * @throws Exception
   */
  @Test
  @Category(KnownBugs.class)
  public void test002g() throws Exception {
    File jackOutput;
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    jackOutput = runTest002(true /* zipped */, "keep-first", errOut, /* isApiTest = */ false,
        /* verbose = */ true);
    ZipFile zipFile = new ZipFile(jackOutput);
    checkResourceContent(zipFile, RESOURCE1_LONGPATH, "Res1");
    checkResourceContent(zipFile, RESOURCE2_LONGPATH, "Res2");
    checkResourceContent(zipFile, RESOURCE3_LONGPATH, "Res3");
    String errString = errOut.toString();
    Assert.assertTrue(errString.contains("Resource in"));
    Assert.assertTrue(errString.contains("rsc/Resource1"));
    Assert.assertTrue(errString.contains("has already been imported"));
  }

  /**
   * Test the behavior of Jack when outputting a Jack file to a Jack folder where a Jack file of the
   * same name already exists. We expect the previous file to be overridden.
   * @throws Exception
   */
  @Test
  public void test003a() throws Exception {
    // compile source files to a Jack dir
    File jackOutput = AbstractTestTools.createTempDir();
    File testSrcDir = AbstractTestTools.getTestRootDir("com.android.jack.fileconflict.test003");
    File tempJackFolder = AbstractTestTools.createTempDir();

    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(LegacyJillToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        tempJackFolder,
        /* zipFiles = */ false,
        testSrcDir);

    // get paths for Jack files
    String jackFilePath =
        FileType.JAYCE.getPrefix() + "/com/android/jack/fileconflict/test003/jack/MyClass.jayce";
    File myClass1 = new File(tempJackFolder, jackFilePath);

    // get paths for Dex files
    String dexFilePath =
        FileType.DEX.getPrefix() + "/com/android/jack/fileconflict/test003/jack/MyClass.dex";
    File myClass1Dex = new File(tempJackFolder, dexFilePath);

    String libPropName = JackLibrary.LIBRARY_PROPERTIES_VPATH.getPathAsString('/');
    File libProperties = new File(tempJackFolder, libPropName);
    File digestFile = new File(tempJackFolder, FileType.DEX.getPrefix() + "/digest");


    // create Jack dirs to import
    File jackImport1 = AbstractTestTools.createTempDir();
    AbstractTestTools.copyFileToDir(libProperties, libPropName, jackImport1);
    AbstractTestTools.copyFileToDir(myClass1, jackFilePath, jackImport1);
    AbstractTestTools.copyFileToDir(myClass1Dex, dexFilePath, jackImport1);
    AbstractTestTools.copyFileToDir(digestFile, FileType.DEX.getPrefix() + "/digest", jackImport1);

    // copy Jack file to output dir
    AbstractTestTools.copyFileToDir(myClass1, jackFilePath, jackOutput);
    AbstractTestTools.copyFileToDir(myClass1Dex, dexFilePath, jackOutput);

    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.addProguardFlags(new File(testSrcDir, "proguard.flags"));
    toolchain.libToLib(jackImport1, jackOutput, false);
  }

  /**
   * Test the behavior of Jack when outputting a resource to a Jack folder where a file of the
   * same name already exists. We expect the previous file to be overridden.
   * @throws Exception
   */
  @Test
  @Ignore("Now jack generate library, a previous file can not exists")
  public void test003b() throws Exception {
    File jackOutput = AbstractTestTools.createTempDir();

    // compile source files to a Jack dir
    File testSrcDir = AbstractTestTools.getTestRootDir("com.android.jack.fileconflict.test003.jack");
    File tempJackFolder = AbstractTestTools.createTempDir();
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        tempJackFolder,
        /* zipFiles = */ false,
        testSrcDir);

    // get paths for Jack files
    String jackFilePath =
        FileType.JAYCE.getPrefix() + "/com/android/jack/fileconflict/test003/jack/MyClass.jayce";
    File myClass1 = new File(tempJackFolder, jackFilePath);

    String libPropName = JackLibrary.LIBRARY_PROPERTIES_VPATH.getPathAsString('/');
    File libProperties = new File(tempJackFolder, libPropName);

    // create Jack dirs to import
    File jackImport1 = AbstractTestTools.createTempDir();
    String resourcePath = "com/android/jack/fileconflict/test003/jack/Resource";
    File resource = new File(testSrcDir, "Resource");
    AbstractTestTools.copyFileToDir(libProperties, libPropName, jackImport1);
    AbstractTestTools.copyFileToDir(myClass1, jackFilePath, jackImport1);
    AbstractTestTools.copyFileToDir(resource, resourcePath, jackImport1);

    // copy a different resource to output dir with the same name
    File resource2 = new File(testSrcDir, "Resource2");
    AbstractTestTools.copyFileToDir(resource2, resourcePath, jackOutput);

    // run Jack on Jack dir
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.libToLib(jackImport1, jackOutput, /* zipFiles = */ false);

    checkResourceContent(jackOutput, resourcePath, "Res1");

  }

  /**
   * Test the behavior of Jack when renaming a Jack file along with the resource with a matching
   * name, and when a resource with the same name (after renaming) already exists.
   * @throws Exception
   */
  @Test
  @Category(KnownBugs.class)
  public void test004() throws Exception {
    File jackOutput = AbstractTestTools.createTempDir();

    // compile source files to a Jack dir
    File testSrcDir = AbstractTestTools.getTestRootDir("com.android.jack.fileconflict.test004.jack");
    File tempJackFolder = AbstractTestTools.createTempDir();

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        tempJackFolder,
        /* zipFiles = */ false,
        testSrcDir);

    // get paths for Jack files
    String jackFilePath =
        FileType.JAYCE.getPrefix() + "/com/android/jack/fileconflict/test004/jack/MyClass.jayce";
    File myClass1 = new File(tempJackFolder, jackFilePath);

    String libPropName = JackLibrary.LIBRARY_PROPERTIES_VPATH.getPathAsString('/');
    File libProperties = new File(tempJackFolder, libPropName);

    // create Jack dirs to import
    File jackImport1 = AbstractTestTools.createTempDir();
    File resource = new File(testSrcDir, "MyClass.txt");
    AbstractTestTools.copyFileToDir(libProperties, libPropName, jackImport1);
    AbstractTestTools.copyFileToDir(myClass1, jackFilePath, jackImport1);
    AbstractTestTools.copyFileToDir(resource, "com/android/jack/fileconflict/test004/jack/MyClass.txt", jackImport1);

    // copy a different resource to output dir with the same name
    File resource2 = new File(testSrcDir, "a.txt");
    AbstractTestTools.copyFileToDir(resource2, "pcz/nbqfcvq/wnpx/svyrpcbsyvph/hrgh004/wnpx/ZmPyngg.txt", jackOutput);

    // run Jack on Jack dir
    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addProguardFlags(new File(testSrcDir, "proguard.flags"));
    toolchain.addProperty(NameProviderFactory.NAMEPROVIDER.getName(), "rot13");
    toolchain.libToLib(jackImport1, jackOutput, /* zipFiles = */ false);

    checkResourceContent(jackOutput, "pcz/nbqfcvq/wnpx/svyrpcbsyvph/hrgh004/wnpx/ZmPyngg.txt",
        "MyClass");
  }

  @Nonnull
  private JackBasedToolchain getToolchain(boolean isApiTest) {
    List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(1);
    excludeList.add(LegacyJillToolchain.class);
    if (isApiTest) {
      excludeList.add(JackCliToolchain.class);
    }
    return AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class, excludeList);
  }

  private void runTest001(@Nonnull File jackOutput, @CheckForNull String collisionPolicy,
      @CheckForNull OutputStream errorStream, boolean isApiTest, boolean verbose) throws Exception {
    // compile source files to a Jack dir
    File tempJackFolder = AbstractTestTools.createTempDir();

    JackBasedToolchain toolchain = getToolchain(isApiTest);

    if (errorStream != null) {
      toolchain.setErrorStream(errorStream);
    }
    toolchain.setVerbose(verbose);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).srcToLib(
        tempJackFolder,
        /* zipFile = */ false,
        TEST001_DIR);

    // get paths for Jack files
    File myClass1 = new File(tempJackFolder, JACK_FILE_PATH_1);
    File myClass2 = new File(tempJackFolder, JACK_FILE_PATH_2);
    File myClass3 = new File(tempJackFolder, JACK_FILE_PATH_3);

    // get paths for dex files
    File myClass1Dex = new File(tempJackFolder, DEX_FILE_PATH_1);
    File myClass2Dex = new File(tempJackFolder, DEX_FILE_PATH_2);
    File myClass3Dex = new File(tempJackFolder, DEX_FILE_PATH_3);

    String libPropName = JackLibrary.LIBRARY_PROPERTIES_VPATH.getPathAsString('/');
    File libProperties = new File(tempJackFolder, libPropName);
    File digestFile = new File(tempJackFolder, FileType.DEX.getPrefix() + "/digest");

    // create Jack dirs to import
    File jackImport1 = AbstractTestTools.createTempDir();
    File jackImport2 = AbstractTestTools.createTempDir();
    AbstractTestTools.copyFileToDir(digestFile, FileType.DEX.getPrefix() + "/digest", jackImport1);
    AbstractTestTools.copyFileToDir(digestFile, FileType.DEX.getPrefix() + "/digest", jackImport2);
    AbstractTestTools.copyFileToDir(libProperties, libPropName, jackImport1);
    AbstractTestTools.copyFileToDir(myClass1, JACK_FILE_PATH_1, jackImport1);
    AbstractTestTools.copyFileToDir(myClass1Dex, DEX_FILE_PATH_1, jackImport1);
    AbstractTestTools.copyFileToDir(myClass2, JACK_FILE_PATH_2, jackImport1);
    AbstractTestTools.copyFileToDir(myClass2Dex, DEX_FILE_PATH_2, jackImport1);
    AbstractTestTools.copyFileToDir(libProperties, libPropName, jackImport2);
    AbstractTestTools.copyFileToDir(myClass1, JACK_FILE_PATH_1, jackImport2);
    AbstractTestTools.copyFileToDir(myClass1Dex, DEX_FILE_PATH_1, jackImport2);
    AbstractTestTools.copyFileToDir(myClass3, JACK_FILE_PATH_3, jackImport2);
    AbstractTestTools.copyFileToDir(myClass3Dex, DEX_FILE_PATH_3, jackImport2);

    // run Jack on Jack dirs
    toolchain = getToolchain(isApiTest);
    toolchain.addProguardFlags(new File(TEST001_DIR, "proguard.flags"));
    toolchain.addStaticLibs(jackImport1, jackImport2);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());

    if (collisionPolicy != null) {
      toolchain.addProperty(JayceFileImporter.COLLISION_POLICY.getName(), collisionPolicy);
    }
    if (errorStream != null) {
      toolchain.setErrorStream(errorStream);
    }
    toolchain.libToLib(new File [] {jackImport1, jackImport2}, jackOutput, /* zipFiles = */ false);
  }

  @Nonnull
  private File runTest002(boolean zip, @CheckForNull String collisionPolicy,
      @CheckForNull OutputStream errorStream, boolean isApiTest, boolean verbose) throws Exception {
    // compile source files to a Jack dir
    File jackImport1 = AbstractTestTools.createTempDir();
    File lib1 = new File(TEST002_DIR, "lib1");

    JackBasedToolchain toolchain = getToolchain(isApiTest);
    toolchain.addResource(new File(lib1, "rsc"));
    if (errorStream != null) {
      toolchain.setErrorStream(errorStream);
    }
    toolchain.setVerbose(verbose);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).srcToLib(
        jackImport1,
        /* zipFiles = */ false,
        lib1);

    toolchain = getToolchain(isApiTest);
    File jackImport2 = AbstractTestTools.createTempDir();
    File lib2 = new File(TEST002_DIR, "lib2");
    toolchain = getToolchain(isApiTest);
    toolchain.addResource(new File(lib2, "rsc"));
    if (errorStream != null) {
      toolchain.setErrorStream(errorStream);
    }
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).srcToLib(
        jackImport2,
        /* zipFiles = */ false,
        lib2);

    // run Jack on Jack dirs
    toolchain = getToolchain(isApiTest);
    toolchain.addProguardFlags(new File(TEST002_DIR, "proguard.flags"));
    if (collisionPolicy != null) {
      toolchain.addProperty(ResourceImporter.RESOURCE_COLLISION_POLICY.getName(), collisionPolicy);
    }
    File jackOutput;
    if (zip) {
      jackOutput = AbstractTestTools.createTempFile("jackOutput", toolchain.getLibraryExtension());
    } else {
      jackOutput = AbstractTestTools.createTempDir();
    }
    if (errorStream != null) {
      toolchain.setErrorStream(errorStream);
    }
    toolchain.libToLib(new File[] {jackImport1,jackImport2}, jackOutput, /* zipFiles = */ zip);

    return jackOutput;
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
}
