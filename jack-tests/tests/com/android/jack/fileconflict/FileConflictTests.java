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
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.resource.ResourceImportConflictException;
import com.android.jack.resource.ResourceImporter;
import com.android.jack.shrob.obfuscation.NameProviderFactory;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.IncrementalToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JackCliToolchain;
import com.android.jack.test.toolchain.LegacyJillToolchain;
import com.android.sched.util.location.FileLocation;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * JUnit tests for conflicts between Jayce files and resources.
 */
public class FileConflictTests {

  @Nonnull
  private static final VPath TYPE1_PATH =
      new VPath("com/android/jack/fileconflict/test001/jack/MyClass", '/');
  @Nonnull
  private static final VPath TYPE2_PATH =
      new VPath("com/android/jack/fileconflict/test001/jack/MyClass2", '/');
  @Nonnull
  private static final VPath TYPE3_PATH =
      new VPath("com/android/jack/fileconflict/test001/jack/MyClass3", '/');
  @Nonnull
  private static final VPath RESOURCE1_PATH = new VPath("Resource1", '/');
  @Nonnull
  private static final VPath RESOURCE2_PATH = new VPath("Resource2", '/');
  @Nonnull
  private static final VPath RESOURCE3_PATH = new VPath("Resource3", '/');

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
    InputJackLibrary outputLib = null;
    try {
      outputLib = AbstractTestTools.getInputJackLibrary(jackOutput);
      InputVFile myClass1 = outputLib.getFile(FileType.JAYCE, TYPE1_PATH);
      InputVFile myClass2 = outputLib.getFile(FileType.JAYCE, TYPE2_PATH);
      InputVFile myClass3 = outputLib.getFile(FileType.JAYCE, TYPE3_PATH);
      checkJayceNotEmpty(myClass1);
      checkJayceNotEmpty(myClass2);
      checkJayceNotEmpty(myClass3);
    } catch (FileTypeDoesNotExistException e) {
      Assert.fail();
    } finally {
      if (outputLib != null) {
        outputLib.close();
      }
      Assert.assertTrue(errOut.toString().isEmpty());
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting Jack files, and
   * outputting to a Jack folder, with the collision policy set to "keep-first", and the verbosity
   * set to "DEBUG".
   * @throws Exception
   */
  @Test
  @KnownIssue
  public void test001d() throws Exception {
    File jackOutput = AbstractTestTools.createTempDir();
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    runTest001(jackOutput, "keep-first", errOut /* errorStream */, /* isApiTest = */ false,
        /* verbose = */ true);
    InputJackLibrary outputLib = null;
    try {
      outputLib = AbstractTestTools.getInputJackLibrary(jackOutput);
      InputVFile myClass1 = outputLib.getFile(FileType.JAYCE, TYPE1_PATH);
      InputVFile myClass2 = outputLib.getFile(FileType.JAYCE, TYPE2_PATH);
      InputVFile myClass3 = outputLib.getFile(FileType.JAYCE, TYPE3_PATH);
      checkJayceNotEmpty(myClass1);
      checkJayceNotEmpty(myClass2);
      checkJayceNotEmpty(myClass3);
    } catch (FileTypeDoesNotExistException e) {
      Assert.fail();
    } finally {
      if (outputLib != null) {
        outputLib.close();
      }
      String errString = errOut.toString();
      Assert.assertTrue(
          errString.contains("Type com.android.jack.fileconflict.test001.jack.MyClass"));
      Assert.assertTrue(
          errString.contains("Type com.android.jack.fileconflict.test001.jack.MyClass2"));
      Assert.assertTrue(
          errString.contains("Type com.android.jack.fileconflict.test001.jack.MyClass3"));
      Assert.assertTrue(errString.contains("has already been imported"));
    }
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
    InputJackLibrary lib = null;
    try {
      lib = AbstractTestTools.getInputJackLibrary(jackOutput);
      checkResourceContent(lib, RESOURCE1_PATH, "Res1");
      checkResourceContent(lib, RESOURCE2_PATH, "Res2");
      checkResourceContent(lib, RESOURCE3_PATH, "Res3");
    } finally {
      if (lib != null) {
        lib.close();
      }
    }
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
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void test002f() throws Exception {
    File jackOutput;
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    jackOutput = runTest002(true /* zipped */, "keep-first", errOut, /* isApiTest = */ false,
        /* verbose = */ false);
    InputJackLibrary lib = null;
    try {
      lib = AbstractTestTools.getInputJackLibrary(jackOutput);
      checkResourceContent(lib, RESOURCE1_PATH, "Res1");
      checkResourceContent(lib, RESOURCE2_PATH, "Res2");
      checkResourceContent(lib, RESOURCE3_PATH, "Res3");
    } finally {
      if (lib != null) {
        lib.close();
      }
    }
    Assert.assertTrue(errOut.toString().isEmpty());
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack zip, with the collision policy set to "keep-first", with verbosity set to
   * "DEBUG".
   * @throws Exception
   */
  @Test
  @KnownIssue
  public void test002g() throws Exception {
    File jackOutput;
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    jackOutput = runTest002(true /* zipped */, "keep-first", errOut, /* isApiTest = */ false,
        /* verbose = */ true);
    InputJackLibrary lib = null;
    try {
      lib = AbstractTestTools.getInputJackLibrary(jackOutput);
      checkResourceContent(lib, RESOURCE1_PATH, "Res1");
      checkResourceContent(lib, RESOURCE2_PATH, "Res2");
      checkResourceContent(lib, RESOURCE3_PATH, "Res3");
    } finally {
      if (lib != null) {
        lib.close();
      }
    }
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
  public void test003() throws Exception {

    File jackOutput = AbstractTestTools.createTempDir();
    File testSrcDir = AbstractTestTools.getTestRootDir("com.android.jack.fileconflict.test003");
    File tempJackFolder = AbstractTestTools.createTempDir();

    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(LegacyJillToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.addResourceDir(new File(testSrcDir, "jack/rsc1"))
    .addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        tempJackFolder,
        /* zipFiles = */ false,
        testSrcDir);

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.addResourceDir(new File(testSrcDir, "jack/rsc2"))
    .addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        tempJackFolder,
        /* zipFiles = */ false,
        testSrcDir);

    VPath typePath = new VPath("com/android/jack/fileconflict/test003/jack/MyClass", '/');
    InputJackLibrary lib = null;
    try {
      lib = AbstractTestTools.getInputJackLibrary(tempJackFolder);

      // check Jayce file is not empty
      InputVFile jayceFile = lib.getFile(FileType.JAYCE, typePath);
      checkJayceNotEmpty(jayceFile);

      // check Resource file content
      checkResourceContent(lib, new VPath("Resource", '/'), "Res2");
    } finally {
      if (lib != null) {
        lib.close();
      }
    }
  }

  /**
   * Test the behavior of Jack when renaming a Jayce file along with the resource with a matching
   * name, and when a resource with the same name (after renaming) already exists.
   * @throws Exception
   */
  @Test
  public void test004() throws Exception {
    File jackOutput = AbstractTestTools.createTempDir();
    VPath pathToType = new VPath("com/android/jack/fileconflict/test004/jack/MyClass", '/');
    VPath pathToRenamedType = new VPath("pcz/nbqfcvq/wnpx/svyrpcbsyvph/hrgh004/wnpx/ZmPyngg", '/');
    VPath pathToResource2 = pathToRenamedType.clone().addSuffix(".txt");

    // compile source files to a lib dir while importing ZmPyngg.txt
    File testSrcDir =
        AbstractTestTools.getTestRootDir("com.android.jack.fileconflict.test004.jack");
    File jackImport1 = AbstractTestTools.createTempDir();

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addResourceDir(new File(testSrcDir, "rsc2"))
    .addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        jackImport1,
        /* zipFiles = */ false,
        testSrcDir);

    InputJackLibrary lib = null;
    try {
      lib = AbstractTestTools.getInputJackLibrary(jackImport1);
      checkResourceContent(lib, pathToResource2, "a");
      checkJayceNotEmpty(lib.getFile(FileType.JAYCE, pathToType));
    } finally {
      if (lib != null) {
        lib.close();
      }
    }

    // compile source files to the same lib dir while importing MyClass.txt which should be
    // obfuscated as ZmPyngg.txt
    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addResourceDir(new File(testSrcDir, "rsc1"));
    toolchain.addProguardFlags(new File(testSrcDir, "proguard.flags"));
    toolchain.addProperty(NameProviderFactory.NAMEPROVIDER.getName(), "rot13");
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        jackImport1,
        /* zipFiles = */ false,
        testSrcDir);

    try {
      lib = AbstractTestTools.getInputJackLibrary(jackImport1);
      checkResourceContent(lib, pathToResource2, "MyClass");
      checkJayceNotEmpty(lib.getFile(FileType.JAYCE, pathToRenamedType));
      checkJayceNotEmpty(lib.getFile(FileType.JAYCE, pathToType)); // still here from 1st run
    } finally {
      if (lib != null) {
        lib.close();
      }
    }
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

    // create Jack dirs to import
    File jackLib1 = AbstractTestTools.createTempDir();
    File jackLib2 = AbstractTestTools.createTempDir();

    File srcFile1 = new File(TEST001_DIR, "MyClass.java");
    File srcFile2 = new File(TEST001_DIR, "MyClass2.java");
    File srcFile3 = new File(TEST001_DIR, "MyClass3.java");

    JackBasedToolchain toolchain = getToolchain(isApiTest);
    if (errorStream != null) {
      toolchain.setErrorStream(errorStream);
    }
    toolchain.setVerbose(verbose);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).srcToLib(
        jackLib1,
        /* zipFile = */ false,
        srcFile1, srcFile2);

    toolchain = getToolchain(isApiTest);
    if (errorStream != null) {
      toolchain.setErrorStream(errorStream);
    }
    toolchain.setVerbose(verbose);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).srcToLib(
        jackLib2,
        /* zipFile = */ false,
        srcFile1, srcFile3);

    // run Jack on Jack dirs
    toolchain = getToolchain(isApiTest);
    toolchain.addProguardFlags(new File(TEST001_DIR, "proguard.flags"));
    toolchain.addStaticLibs(jackLib1, jackLib2);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    if (collisionPolicy != null) {
      toolchain.addProperty(JayceFileImporter.COLLISION_POLICY.getName(), collisionPolicy);
    }
    if (errorStream != null) {
      toolchain.setErrorStream(errorStream);
    }
    toolchain.libToLib(new File [] {jackLib1, jackLib2}, jackOutput, /* zipFiles = */ false);
  }

  @Nonnull
  private File runTest002(boolean zip, @CheckForNull String collisionPolicy,
      @CheckForNull OutputStream errorStream, boolean isApiTest, boolean verbose) throws Exception {
    // compile source files to a Jack dir
    File jackImport1 = AbstractTestTools.createTempDir();
    File lib1 = new File(TEST002_DIR, "lib1");

    JackBasedToolchain toolchain = getToolchain(isApiTest);
    toolchain.addResourceDir(new File(lib1, "rsc"));
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
    toolchain.addResourceDir(new File(lib2, "rsc"));
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

  private void checkResourceContent(@Nonnull InputJackLibrary lib, @Nonnull VPath path,
      @Nonnull String expectedContent) throws IOException, FileTypeDoesNotExistException {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(
          new InputStreamReader(lib.getFile(FileType.RSC, path).getInputStream()));
      String line = reader.readLine();
      Assert.assertEquals(expectedContent, line);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  private void checkJayceNotEmpty(@Nonnull InputVFile jayceFile) {
    File file = new File(((FileLocation) jayceFile.getLocation()).getPath());
    Assert.assertTrue(file.length() > 0);
  }
}
