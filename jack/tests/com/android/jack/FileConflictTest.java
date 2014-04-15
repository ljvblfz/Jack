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

import com.android.jack.backend.jayce.ImportConflictException;
import com.android.jack.backend.jayce.JayceFileImporter;
import com.android.jack.backend.jayce.ResourceImportConflictException;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * JUnit tests for resource support.
 */
public class FileConflictTest {

  @Nonnull
  private static final String COMMON_PATH_001 = "com/android/jack/fileconflict/test001/jack/";
  @Nonnull
  private static final String COMMON_PATH_002 = "com/android/jack/fileconflict/test002/jack/";
  @Nonnull
  private static final String JACK_FILE_PATH_1 = COMMON_PATH_001 + "MyClass.jack";
  @Nonnull
  private static final String JACK_FILE_PATH_2 = COMMON_PATH_001 + "MyClass2.jack";
  @Nonnull
  private static final String JACK_FILE_PATH_3 = COMMON_PATH_001 + "MyClass3.jack";
  @Nonnull
  private static final String JACK_FILE_PATH_002_1 = COMMON_PATH_002 + "IrrelevantForTest.jack";
  @Nonnull
  private static final String JACK_FILE_PATH_002_2 = COMMON_PATH_002 + "IrrelevantForTest2.jack";
  @Nonnull
  private static final String RESOURCE1_SHORTPATH = "Resource1";
  @Nonnull
  private static final String RESOURCE2_SHORTPATH = "Resource2";
  @Nonnull
  private static final String RESOURCE3_SHORTPATH = "Resource3";
  @Nonnull
  private static final String RESOURCE1_LONGPATH = COMMON_PATH_002 + RESOURCE1_SHORTPATH;
  @Nonnull
  private static final String RESOURCE2_LONGPATH = COMMON_PATH_002 + RESOURCE2_SHORTPATH;
  @Nonnull
  private static final String RESOURCE3_LONGPATH = COMMON_PATH_002 + RESOURCE3_SHORTPATH;

  @Nonnull
  private static final File TEST001_DIR =
      TestTools.getJackTestsWithJackFolder("fileconflict/test001");

  @Nonnull
  private static final File TEST002_DIR =
      TestTools.getJackTestsWithJackFolder("fileconflict/test002");

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting Jack files, and
   * outputting to a Jack folder, with no collision policy specified.
   * @throws Exception
   */
  @Test
  public void test001a() throws Exception {
    File jackOutput = TestTools.createTempDir("jackoutput", "dir");
    try {
      runTest001(jackOutput, null);
      Assert.fail();
    } catch (ImportConflictException e) {
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting Jack files, and
   * outputting to a Jack folder, with the collision policy set to "fail".
   * @throws Exception
   */
  @Test
  public void test001b() throws Exception {
    File jackOutput = TestTools.createTempDir("jackoutput", "dir");
    try {
      runTest001(jackOutput, "fail");
      Assert.fail();
    } catch (ImportConflictException e) {
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting Jack files, and
   * outputting to a Jack folder, with the collision policy set to "keep-first".
   * @throws Exception
   */
  @Test
  public void test001c() throws Exception {
    File jackOutput = TestTools.createTempDir("jackoutput", "dir");
    runTest001(jackOutput, "keep-first");
    File myClass1 = new File(jackOutput, JACK_FILE_PATH_1);
    File myClass2 = new File(jackOutput, JACK_FILE_PATH_2);
    File myClass3 = new File(jackOutput, JACK_FILE_PATH_3);
    Assert.assertTrue(myClass1.exists());
    Assert.assertTrue(myClass2.exists());
    Assert.assertTrue(myClass3.exists());
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack folder, with no collision policy specified.
   * @throws Exception
   */
  @Test
  public void test002a() throws Exception {
    File jackOutput = TestTools.createTempDir("jackoutput", "dir");
    try {
      runTest002(jackOutput, false /* non-zipped */, null);
      Assert.fail();
    } catch (ResourceImportConflictException e) {
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack folder, with the collision policy set to "fail".
   * @throws Exception
   */
  @Test
  public void test002b() throws Exception {
    File jackOutput = TestTools.createTempDir("jackoutput", "dir");
    try {
      runTest002(jackOutput, false /* non-zipped */, "fail");
      Assert.fail();
    } catch (ResourceImportConflictException e) {
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack folder, with the collision policy set to "keep-first".
   * @throws Exception
   */
  @Test
  public void test002c() throws Exception {
    File jackOutput = TestTools.createTempDir("jackoutput", "dir");
    runTest002(jackOutput, false /* non-zipped */, "keep-first");
    checkResourceContent(jackOutput, RESOURCE1_LONGPATH, "Res1");
    checkResourceContent(jackOutput, RESOURCE2_LONGPATH, "Res2");
    checkResourceContent(jackOutput, RESOURCE3_LONGPATH, "Res3");
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack zip, with no collision policy specified.
   * @throws Exception
   */
  @Test
  public void test002d() throws Exception {
    File jackOutput = TestTools.createTempFile("jackoutput", ".zip");
    try {
      runTest002(jackOutput, true /* zipped */, null);
      Assert.fail();
    } catch (ResourceImportConflictException e) {
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack zip, with the collision policy set to "fail".
   * @throws Exception
   */
  @Test
  public void test002e() throws Exception {
    File jackOutput = TestTools.createTempFile("jackoutput", ".zip");
    try {
      runTest002(jackOutput, true /* zipped */, "fail");
      Assert.fail();
    } catch (ResourceImportConflictException e) {
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack zip, with the collision policy set to "keep-first".
   * @throws Exception
   */
  @Test
  public void test002f() throws Exception {
    File jackOutput = TestTools.createTempFile("jackoutput", ".zip");
    runTest002(jackOutput, true /* zipped */, "keep-first");
    ZipFile zipFile = new ZipFile(jackOutput);
    checkResourceContent(zipFile, RESOURCE1_LONGPATH, "Res1");
    checkResourceContent(zipFile, RESOURCE2_LONGPATH, "Res2");
    checkResourceContent(zipFile, RESOURCE3_LONGPATH, "Res3");
  }

  /**
   * Test the behavior of Jack when outputting a Jack file to a Jack folder where a Jack file of the
   * same name already exists. We expect the previous file to be overridden.
   * @throws Exception
   */
  @Test
  public void test003a() throws Exception {
    File jackOutput = TestTools.createTempDir("jackoutput", "dir");

    // compile source files to a Jack dir
    File testSrcDir = TestTools.getJackTestsWithJackFolder("fileconflict/test003");
    File tempJackFolder = TestTools.createTempDir("jack", "dir");
    TestTools.compileSourceToJack(new Options(), testSrcDir,
        TestTools.getDefaultBootclasspathString(), tempJackFolder, false /* non-zipped */);

    // get paths for Jack files
    String jackFilePath = "com/android/jack/fileconflict/test003/jack/MyClass.jack";
    File myClass1 = new File(tempJackFolder, jackFilePath);

    // create Jack dirs to import
    File jackImport1 = TestTools.createTempDir("jackimport1", "dir");
    copyFileToDir(myClass1, jackFilePath, jackImport1);

    // copy Jack file to output dir
    copyFileToDir(myClass1, jackFilePath, jackOutput);

    // run Jack on Jack dir
    ProguardFlags flags = new ProguardFlags(new File(testSrcDir, "proguard.flags"));
    TestTools.shrobJackToJack(new Options(),
        jackImport1,
        null,
        jackOutput,
        Collections.singletonList(flags),
        false /* non-zipped */);
  }

  /**
   * Test the behavior of Jack when outputting a resource to a Jack folder where a file of the
   * same name already exists. We expect the previous file to be overridden.
   * @throws Exception
   */
  @Test
  public void test003b() throws Exception {
    File jackOutput = TestTools.createTempDir("jackoutput", "dir");

    // compile source files to a Jack dir
    File testSrcDir = TestTools.getJackTestsWithJackFolder("fileconflict/test003");
    File tempJackFolder = TestTools.createTempDir("jack", "dir");
    TestTools.compileSourceToJack(new Options(), testSrcDir,
        TestTools.getDefaultBootclasspathString(), tempJackFolder, false /* non-zipped */);

    // get paths for Jack files
    String jackFilePath = "com/android/jack/fileconflict/test003/jack/MyClass.jack";
    File myClass1 = new File(tempJackFolder, jackFilePath);

    // create Jack dirs to import
    File jackImport1 = TestTools.createTempDir("jackimport1", "dir");
    String resourcePath = "com/android/jack/fileconflict/test003/jack/Resource";
    File resource = new File(testSrcDir, "Resource");
    copyFileToDir(myClass1, jackFilePath, jackImport1);
    copyFileToDir(resource, resourcePath, jackImport1);

    // copy a different resource to output dir with the same name
    File resource2 = new File(testSrcDir, "Resource2");
    copyFileToDir(resource2, resourcePath, jackOutput);

    // run Jack on Jack dir
    ProguardFlags flags = new ProguardFlags(new File(testSrcDir, "proguard.flags"));
    TestTools.shrobJackToJack(new Options(),
        jackImport1,
        null,
        jackOutput,
        Collections.singletonList(flags),
        false /* non-zipped */);

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
    File jackOutput = TestTools.createTempDir("jackoutput", "dir");

    // compile source files to a Jack dir
    File testSrcDir = TestTools.getJackTestsWithJackFolder("fileconflict/test004");
    File tempJackFolder = TestTools.createTempDir("jack", "dir");
    TestTools.compileSourceToJack(new Options(), testSrcDir,
        TestTools.getDefaultBootclasspathString(), tempJackFolder, false /* non-zipped */);

    // get paths for Jack files
    String jackFilePath = "com/android/jack/fileconflict/test004/jack/MyClass.jack";
    File myClass1 = new File(tempJackFolder, jackFilePath);

    // create Jack dirs to import
    File jackImport1 = TestTools.createTempDir("jackimport1", "dir");
    File resource = new File(testSrcDir, "MyClass.txt");
    copyFileToDir(myClass1, jackFilePath, jackImport1);
    copyFileToDir(resource, "com/android/jack/fileconflict/test004/jack/MyClass.txt", jackImport1);
    System.out.println(jackImport1.getAbsolutePath());

    // copy a different resource to output dir with the same name
    File resource2 = new File(testSrcDir, "a.txt");
    copyFileToDir(resource2, "a/a.txt", jackOutput);
    System.out.println(jackOutput.getAbsolutePath());

    // run Jack on Jack dir
    ProguardFlags flags = new ProguardFlags(new File(testSrcDir, "proguard.flags"));
    TestTools.shrobJackToJack(new Options(),
        jackImport1,
        null,
        jackOutput,
        Collections.singletonList(flags),
        false /* non-zipped */);
  }

  private void runTest001(@Nonnull File jackOutput, @CheckForNull String collisionPolicy)
      throws Exception {
    // compile source files to a Jack dir
    File tempJackFolder = TestTools.createTempDir("jack", "dir");
    TestTools.compileSourceToJack(new Options(), TEST001_DIR,
        TestTools.getDefaultBootclasspathString(), tempJackFolder, false /* non-zipped */);

    // get paths for Jack files
    File myClass1 = new File(tempJackFolder, JACK_FILE_PATH_1);
    File myClass2 = new File(tempJackFolder, JACK_FILE_PATH_2);
    File myClass3 = new File(tempJackFolder, JACK_FILE_PATH_3);

    // create Jack dirs to import
    File jackImport1 = TestTools.createTempDir("jackimport1", "dir");
    File jackImport2 = TestTools.createTempDir("jackimport2", "dir");
    copyFileToDir(myClass1, JACK_FILE_PATH_1, jackImport1);
    copyFileToDir(myClass2, JACK_FILE_PATH_2, jackImport1);
    copyFileToDir(myClass1, JACK_FILE_PATH_1, jackImport2);
    copyFileToDir(myClass3, JACK_FILE_PATH_3, jackImport2);

    // run Jack on Jack dirs
    ProguardFlags flags = new ProguardFlags(new File(TEST001_DIR, "proguard.flags"));
    Options options = new Options();
    List<File> jayceImports = new ArrayList<File>(2);
    jayceImports.add(jackImport1);
    jayceImports.add(jackImport2);
    options.setJayceImports(jayceImports);
    options.setProguardFlagsFile(Collections.<File>singletonList(flags));
    options.setJayceOutputDir(jackOutput);
    if (collisionPolicy != null) {
      options.addProperty(JayceFileImporter.COLLISION_POLICY.getName(), collisionPolicy);
    }
    Jack.run(options);
  }

  private void runTest002(@Nonnull File jackOutput, boolean zip,
      @CheckForNull String collisionPolicy) throws Exception {
    // compile source files to a Jack dir
    File tempJackFolder = TestTools.createTempDir("jack", "dir");
    TestTools.compileSourceToJack(new Options(), TEST002_DIR,
        TestTools.getDefaultBootclasspathString(), tempJackFolder, false /* non-zipped */);

    // get paths for Jack files
    File myClass1 = new File(tempJackFolder, JACK_FILE_PATH_002_1);
    File myClass2 = new File(tempJackFolder, JACK_FILE_PATH_002_2);

    // get paths for resources
    File resource1 = new File(TEST002_DIR, RESOURCE1_SHORTPATH);
    File resource2 = new File(TEST002_DIR, RESOURCE2_SHORTPATH);
    File resource3 = new File(TEST002_DIR, RESOURCE3_SHORTPATH);

    // create Jack dirs to import
    File jackImport1 = TestTools.createTempDir("jackimport1", "dir");
    File jackImport2 = TestTools.createTempDir("jackimport2", "dir");
    copyFileToDir(myClass1, JACK_FILE_PATH_002_1, jackImport1);
    copyFileToDir(resource1, RESOURCE1_LONGPATH, jackImport1);
    copyFileToDir(resource2, RESOURCE2_LONGPATH, jackImport1);
    copyFileToDir(myClass2, JACK_FILE_PATH_002_2, jackImport2);
    copyFileToDir(resource2, RESOURCE1_LONGPATH, jackImport2);
    copyFileToDir(resource3, RESOURCE3_LONGPATH, jackImport2);

    // run Jack on Jack dirs
    ProguardFlags flags = new ProguardFlags(new File(TEST002_DIR, "proguard.flags"));
    Options options = new Options();
    List<File> jayceImports = new ArrayList<File>(2);
    jayceImports.add(jackImport1);
    jayceImports.add(jackImport2);
    options.setJayceImports(jayceImports);
    options.setProguardFlagsFile(Collections.<File>singletonList(flags));
    if (zip) {
      options.setJayceOutputZip(jackOutput);
    } else {
      options.setJayceOutputDir(jackOutput);
    }
    if (collisionPolicy != null) {
      options.addProperty(JayceFileImporter.RESOURCE_COLLISION_POLICY.getName(), collisionPolicy);
    }
    Jack.run(options);
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
