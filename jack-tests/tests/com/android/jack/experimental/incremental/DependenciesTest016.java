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

import com.android.jack.JackAbortException;
import com.android.jack.library.FileType;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.LibraryIOException;
import com.android.jack.resource.ResourceImporter;
import com.android.jack.test.helper.IncrementalTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.IncrementalToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.TwoStepsToolchain;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import junit.framework.Assert;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * JUnit test checking incremental support.
 */
public class DependenciesTest016 {

  /**
   * Test incremental support with a scenario close to what the SDK does:
   * - several imported libraries
   * - output Jack library at the same time as output dex
   */
  @Test
  public void testTouch() throws Exception {

    File lib1 = createLib("jack.lib1", /* resourceDir = */ null);
    File lib2 = createLib("jack.lib2", /* resourceDir = */ null);

    IncrementalTestHelper helper =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    File source1 = helper.addJavaFile("jack.source", "Source1.java", "package jack.source; \n"
        + "public class Source1 implements jack.lib1.A, jack.lib2.A { \n"
        + "@Override public void m(){} }");

    helper.addJavaFile("jack.source", "Source2.java", "package jack.source; \n"
        + "public class Source2 implements jack.lib1.A, jack.lib2.A { \n"
        + "@Override public void m(){} }");

    File outputJack = AbstractTestTools.createTempFile("output", ".jack");

    File rscDir = AbstractTestTools.createTempDir();
    File rsc1 = new File(rscDir, "rsc1");
    writeToRsc(rsc1, "rsc1");
    File rsc2 = new File(rscDir, "rsc2");
    writeToRsc(rsc2, "rsc2");

    File sourceDir = new File(rscDir, "jack/source");
    sourceDir.mkdirs();
    File rsc3 = new File(sourceDir, "rsc3");
    writeToRsc(rsc3, "rsc3");
    File rsc4 = new File(sourceDir, "rsc4");
    writeToRsc(rsc4, "rsc4");

    File outputDex1 = AbstractTestTools.createTempDir();

    JackBasedToolchain toolchain = getIncrementalToolchain(JackBasedToolchain.class);
    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    toolchain.addStaticLibs(lib1, lib2);
    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.addResourceDir(rscDir);
    toolchain.addToClasspath(defaultClasspath).srcToExe(outputDex1,
        /* zipFiles = */ false, helper.getSourceFolder());
    Thread.sleep(1000); // "lastModified()" lacks precision...
    helper.snapshotJackFilesModificationDate();

    // check the content of the incremental dir
    Assert.assertEquals(2, getCount(helper.getCompilerStateFolder(), FileType.JAYCE));
    Assert.assertEquals(4, getCount(helper.getCompilerStateFolder(), FileType.RSC));
    checkResourceContent("rsc1", "rsc1", helper.getCompilerStateFolder());
    checkResourceContent("rsc2", "rsc2", helper.getCompilerStateFolder());
    checkResourceContent("jack/source/rsc3", "rsc3", helper.getCompilerStateFolder());
    checkResourceContent("jack/source/rsc4", "rsc4", helper.getCompilerStateFolder());

    // check the content of the output jack lib
    Assert.assertEquals(4, getCount(outputJack, FileType.JAYCE));
    Assert.assertEquals(4, getCount(outputJack, FileType.RSC));
    checkResourceContent("rsc1", "rsc1", outputJack);
    checkResourceContent("rsc2", "rsc2", outputJack);
    checkResourceContent("jack/source/rsc3", "rsc3", outputJack);
    checkResourceContent("jack/source/rsc4", "rsc4", outputJack);

    // check the content of the resources in the output dex dir
    checkResourceContentInDexDir("rsc1", "rsc1", outputDex1);
    checkResourceContentInDexDir("rsc2", "rsc2", outputDex1);
    checkResourceContentInDexDir("jack/source/rsc3", "rsc3", outputDex1);
    checkResourceContentInDexDir("jack/source/rsc4", "rsc4", outputDex1);

    // touch Source1, rsc1 and rsc4
    source1.setLastModified(System.currentTimeMillis());
    writeToRsc(rsc1, "rsc1.1");
    writeToRsc(rsc4, "rsc4.1");

    helper.snapshotJackFilesModificationDate();

    File outputDex2 = AbstractTestTools.createTempDir();

    toolchain = getIncrementalToolchain(JackBasedToolchain.class);
    toolchain.addStaticLibs(lib1, lib2);
    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.addResourceDir(rscDir);
    toolchain.addToClasspath(defaultClasspath).srcToExe(outputDex2,
        /* zipFiles = */ false, helper.getSourceFolder());
    Thread.sleep(1000);

    // check which files have been recompiled
    List<String> recompiledTypes = helper.getFQNOfRebuiltTypes();
    Assert.assertEquals(1, recompiledTypes.size());
    Assert.assertEquals("jack.source.Source1", recompiledTypes.get(0));

    // check the content of the incremental dir
    Assert.assertEquals(2, getCount(helper.getCompilerStateFolder(), FileType.JAYCE));
    Assert.assertEquals(4, getCount(helper.getCompilerStateFolder(), FileType.RSC));
    checkResourceContent("rsc1", "rsc1.1", helper.getCompilerStateFolder());
    checkResourceContent("rsc2", "rsc2", helper.getCompilerStateFolder());
    checkResourceContent("jack/source/rsc3", "rsc3", helper.getCompilerStateFolder());
    checkResourceContent("jack/source/rsc4", "rsc4.1", helper.getCompilerStateFolder());

    // check the content of the output jack lib
    Assert.assertEquals(4, getCount(outputJack, FileType.JAYCE));
    Assert.assertEquals(4, getCount(outputJack, FileType.RSC));
    checkResourceContent("rsc1", "rsc1.1", outputJack);
    checkResourceContent("rsc2", "rsc2", outputJack);
    checkResourceContent("jack/source/rsc3", "rsc3", outputJack);
    checkResourceContent("jack/source/rsc4", "rsc4.1", outputJack);

    // check the content of the resources in the output dex dir
    checkResourceContentInDexDir("rsc1", "rsc1.1", outputDex2);
    checkResourceContentInDexDir("rsc2", "rsc2", outputDex2);
    checkResourceContentInDexDir("jack/source/rsc3", "rsc3", outputDex2);
    checkResourceContentInDexDir("jack/source/rsc4", "rsc4.1", outputDex2);
  }

  @Test
  public void testDelete() throws Exception {
    File lib1 = createLib("jack.lib1", /* resourceDir = */ null);
    File lib2 = createLib("jack.lib2", /* resourceDir = */ null);

    IncrementalTestHelper helper =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    File source1 = helper.addJavaFile("jack.source", "Source1.java", "package jack.source; \n"
        + "public class Source1 implements jack.lib1.A, jack.lib2.A { \n"
        + "@Override public void m(){} }");

    helper.addJavaFile("jack.source", "Source2.java", "package jack.source; \n"
        + "public class Source2 implements jack.lib1.A, jack.lib2.A { \n"
        + "@Override public void m(){} }");

    File outputJack = AbstractTestTools.createTempFile("output", ".jack");
    File outputDex1 = AbstractTestTools.createTempDir();

    File rscDir = AbstractTestTools.createTempDir();
    File rsc1 = new File(rscDir, "rsc1");
    writeToRsc(rsc1, "rsc1");
    File rsc2 = new File(rscDir, "rsc2");
    writeToRsc(rsc2, "rsc2");

    File sourceDir = new File(rscDir, "jack/source");
    sourceDir.mkdirs();
    File rsc3 = new File(sourceDir, "rsc3");
    writeToRsc(rsc3, "rsc3");
    File rsc4 = new File(sourceDir, "rsc4");
    writeToRsc(rsc4, "rsc4");

    JackBasedToolchain toolchain = getIncrementalToolchain(JackBasedToolchain.class);
    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    toolchain.addStaticLibs(lib1, lib2);
    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.addResourceDir(rscDir);
    toolchain.addToClasspath(defaultClasspath).srcToExe(outputDex1,
        /* zipFiles = */ false, helper.getSourceFolder());
    Thread.sleep(1000); // "lastModified()" lacks precision...
    helper.snapshotJackFilesModificationDate();

    // check the content of the incremental dir
    Assert.assertEquals(2, getCount(helper.getCompilerStateFolder(), FileType.JAYCE));
    Assert.assertEquals(4, getCount(helper.getCompilerStateFolder(), FileType.RSC));

    // check the content of the output jack lib
    Assert.assertEquals(4, getCount(outputJack, FileType.JAYCE));
    Assert.assertEquals(4, getCount(outputJack, FileType.RSC));

    // check the content of the resources in the output dex dir
    checkResourceContentInDexDir("rsc1", "rsc1", outputDex1);
    checkResourceContentInDexDir("rsc2", "rsc2", outputDex1);
    checkResourceContentInDexDir("jack/source/rsc3", "rsc3", outputDex1);
    checkResourceContentInDexDir("jack/source/rsc4", "rsc4", outputDex1);

    // delete Source1, rsc1 and rsc4
    helper.deleteJavaFile(source1);
    AbstractTestTools.deleteFile(rsc1);
    AbstractTestTools.deleteFile(rsc4);
    helper.snapshotJackFilesModificationDate();

    File outputDex2 = AbstractTestTools.createTempDir();

    toolchain = getIncrementalToolchain(JackBasedToolchain.class);
    toolchain.addStaticLibs(lib1, lib2);
    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.addResourceDir(rscDir);
    toolchain.addToClasspath(defaultClasspath).srcToExe(outputDex2,
        /* zipFiles = */ false, helper.getSourceFolder());
    Thread.sleep(1000);

    // check which files have been recompiled
    List<String> recompiledTypes = helper.getFQNOfRebuiltTypes();
    Assert.assertEquals(0, recompiledTypes.size());

    // check the content of the incremental dir
    Assert.assertEquals(1, getCount(helper.getCompilerStateFolder(), FileType.JAYCE));
    Assert.assertEquals(2, getCount(helper.getCompilerStateFolder(), FileType.RSC));

    // check the content of the output jack lib
    Assert.assertEquals(3, getCount(outputJack, FileType.JAYCE));
    Assert.assertEquals(2, getCount(outputJack, FileType.RSC));

    // check the content of the resources in the output dex dir
    checkResourceContentInDexDir("rsc2", "rsc2", outputDex2);
    checkResourceContentInDexDir("jack/source/rsc3", "rsc3", outputDex2);
    checkResourceDoesntExistInDexDir("rsc1", outputDex2);
    checkResourceDoesntExistInDexDir("jack/source/rsc4", outputDex2);
  }

  @Test
  public void testConflictsKeepFirst() throws Exception {
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    runTestConflict("keep-first", errOut, JackBasedToolchain.class);
    Assert.assertEquals("", errOut.toString());
  }

  @Test
  public void testConflictsFail() throws Exception {
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    try {
      // API only because we check errors
      runTestConflict("fail", errOut, JackApiToolchainBase.class);
      Assert.fail();
    } catch (JackAbortException e) {
      // expected
      String errString = errOut.toString();
      Assert.assertTrue(errString.contains("Library reading phase: Resource 'rsc1' from"));
      Assert.assertTrue(errString.contains("'/rsc/rsc1' has already been imported from"));
    }
  }

  public void runTestConflict(@Nonnull String collisionPolicy, @Nonnull OutputStream errorStream,
      @Nonnull Class<? extends JackBasedToolchain> baseToolchain) throws Exception {

    File rscDirLib1 = AbstractTestTools.createTempDir();
    {
      File rsc1 = new File(rscDirLib1, "rsc1");
      writeToRsc(rsc1, "rsc1lib");

      File subRscDir = new File(rscDirLib1, "jack/source");
      subRscDir.mkdirs();
      File rsc3 = new File(subRscDir, "rsc3");
      writeToRsc(rsc3, "rsc3lib");
    }

    File rscDirLib2 = AbstractTestTools.createTempDir();
    {
      File rsc2 = new File(rscDirLib2, "rsc2");
      writeToRsc(rsc2, "rsc2lib");

      File subRscDir = new File(rscDirLib2, "jack/source");
      subRscDir.mkdirs();
      File rsc4 = new File(subRscDir, "rsc4");
      writeToRsc(rsc4, "rsc4lib");
    }

    File lib1 = createLib("jack.lib1", rscDirLib1);
    File lib2 = createLib("jack.lib2", rscDirLib2);

    File standaloneRscDir = AbstractTestTools.createTempDir();

    IncrementalTestHelper helper =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    File source1 = helper.addJavaFile("jack.source", "Source1.java", "package jack.source; \n"
        + "public class Source1 implements jack.lib1.A, jack.lib2.A { \n"
        + "@Override public void m(){} }");

    helper.addJavaFile("jack.source", "Source2.java", "package jack.source; \n"
        + "public class Source2 implements jack.lib1.A, jack.lib2.A { \n"
        + "@Override public void m(){} }");

    File outputJack = AbstractTestTools.createTempFile("output", ".jack");

    /* 1st step: import an empty resource dir */

    File outputDex1 = AbstractTestTools.createTempDir();

    JackBasedToolchain toolchain = getIncrementalToolchain(baseToolchain);
    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    toolchain.addStaticLibs(lib1, lib2);
    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.addResourceDir(standaloneRscDir);
    toolchain.setErrorStream(errorStream);
    toolchain.addToClasspath(defaultClasspath).srcToExe(outputDex1,
        /* zipFiles = */ false, helper.getSourceFolder());
    Thread.sleep(1000); // "lastModified()" lacks precision...
    helper.snapshotJackFilesModificationDate();

    // check the content of the incremental dir
    Assert.assertEquals(2, getCount(helper.getCompilerStateFolder(), FileType.JAYCE));
    Assert.assertEquals(4, getCount(helper.getCompilerStateFolder(), FileType.RSC));

    // check the content of the output jack lib
    Assert.assertEquals(4, getCount(outputJack, FileType.JAYCE));
    Assert.assertEquals(4, getCount(outputJack, FileType.RSC));
    checkResourceContent("rsc1", "rsc1lib", outputJack);
    checkResourceContent("rsc2", "rsc2lib", outputJack);
    checkResourceContent("jack/source/rsc3", "rsc3lib", outputJack);
    checkResourceContent("jack/source/rsc4", "rsc4lib", outputJack);

    // check the content of the resources in the output dex dir
    checkResourceContentInDexDir("rsc1", "rsc1lib", outputDex1);
    checkResourceContentInDexDir("rsc2", "rsc2lib", outputDex1);
    checkResourceContentInDexDir("jack/source/rsc3", "rsc3lib", outputDex1);
    checkResourceContentInDexDir("jack/source/rsc4", "rsc4lib", outputDex1);

    /* 2nd step: add a resource in the imported resource dir that conflicts with a resource from
     * lib1 */

    File outputDex2 = AbstractTestTools.createTempDir();

    File rsc1 = new File(standaloneRscDir, "rsc1");
    writeToRsc(rsc1, "rsc1sa");

    toolchain = getIncrementalToolchain(baseToolchain);
    toolchain.addStaticLibs(lib1, lib2);
    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.addResourceDir(standaloneRscDir);
    toolchain.setErrorStream(errorStream);
    toolchain.addProperty(ResourceImporter.RESOURCE_COLLISION_POLICY.getName(), collisionPolicy);
    toolchain.addToClasspath(defaultClasspath).srcToExe(outputDex2,
        /* zipFiles = */ false, helper.getSourceFolder());
    Thread.sleep(1000); // "lastModified()" lacks precision...
    helper.snapshotJackFilesModificationDate();

    // check the content of the incremental dir
    Assert.assertEquals(2, getCount(helper.getCompilerStateFolder(), FileType.JAYCE));
    Assert.assertEquals(4, getCount(helper.getCompilerStateFolder(), FileType.RSC));

    // check the content of the output jack lib
    Assert.assertEquals(4, getCount(outputJack, FileType.JAYCE));
    Assert.assertEquals(4, getCount(outputJack, FileType.RSC));
    checkResourceContent("rsc1", "rsc1sa", outputJack);
    checkResourceContent("rsc2", "rsc2lib", outputJack);
    checkResourceContent("jack/source/rsc3", "rsc3lib", outputJack);
    checkResourceContent("jack/source/rsc4", "rsc4lib", outputJack);

    // check the content of the resources in the output dex dir
    checkResourceContentInDexDir("rsc1", "rsc1sa", outputDex2);
    checkResourceContentInDexDir("rsc2", "rsc2lib", outputDex2);
    checkResourceContentInDexDir("jack/source/rsc3", "rsc3lib", outputDex2);
    checkResourceContentInDexDir("jack/source/rsc4", "rsc4lib", outputDex2);

    // check that no files have been recompiled
    List<String> recompiledTypes = helper.getFQNOfRebuiltTypes();
    Assert.assertEquals(0, recompiledTypes.size());

    /* 3rd step: add a resource in the imported resource dir that conflicts with a resource from
     * lib2 */

    File outputDex3 = AbstractTestTools.createTempDir();

    File subRscDir = new File(standaloneRscDir, "jack/source");
    subRscDir.mkdirs();
    File rsc4 = new File(subRscDir, "rsc4");
    writeToRsc(rsc4, "rsc4sa");

    toolchain = getIncrementalToolchain(baseToolchain);
    toolchain.addStaticLibs(lib1, lib2);
    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.addResourceDir(standaloneRscDir);
    toolchain.setErrorStream(errorStream);
    toolchain.addProperty(ResourceImporter.RESOURCE_COLLISION_POLICY.getName(), collisionPolicy);
    toolchain.addToClasspath(defaultClasspath).srcToExe(outputDex3,
        /* zipFiles = */ false, helper.getSourceFolder());
    Thread.sleep(1000); // "lastModified()" lacks precision...
    helper.snapshotJackFilesModificationDate();

    // check the content of the incremental dir
    Assert.assertEquals(2, getCount(helper.getCompilerStateFolder(), FileType.JAYCE));
    Assert.assertEquals(4, getCount(helper.getCompilerStateFolder(), FileType.RSC));

    // check the content of the output jack lib
    Assert.assertEquals(4, getCount(outputJack, FileType.JAYCE));
    Assert.assertEquals(4, getCount(outputJack, FileType.RSC));
    checkResourceContent("rsc1", "rsc1sa", outputJack);
    checkResourceContent("rsc2", "rsc2lib", outputJack);
    checkResourceContent("jack/source/rsc3", "rsc3lib", outputJack);
    checkResourceContent("jack/source/rsc4", "rsc4sa", outputJack);

    // check the content of the resources in the output dex dir
    checkResourceContentInDexDir("rsc1", "rsc1sa", outputDex3);
    checkResourceContentInDexDir("rsc2", "rsc2lib", outputDex3);
    checkResourceContentInDexDir("jack/source/rsc3", "rsc3lib", outputDex3);
    checkResourceContentInDexDir("jack/source/rsc4", "rsc4sa", outputDex3);

    // check that no files have been recompiled
    recompiledTypes = helper.getFQNOfRebuiltTypes();
    Assert.assertEquals(0, recompiledTypes.size());

    /* 4th step: delete resources from the imported resource dir */

    File outputDex4 = AbstractTestTools.createTempDir();

    // delete rsc1 and rsc4
    AbstractTestTools.deleteFile(rsc1);
    AbstractTestTools.deleteFile(rsc4);
    helper.snapshotJackFilesModificationDate();

    toolchain = getIncrementalToolchain(baseToolchain);
    toolchain.addStaticLibs(lib1, lib2);
    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.addResourceDir(standaloneRscDir);
    toolchain.setErrorStream(errorStream);
    toolchain.addProperty(ResourceImporter.RESOURCE_COLLISION_POLICY.getName(), collisionPolicy);
    toolchain.addToClasspath(defaultClasspath).srcToExe(outputDex4,
        /* zipFiles = */ false, helper.getSourceFolder());
    Thread.sleep(1000); // "lastModified()" lacks precision...
    helper.snapshotJackFilesModificationDate();

    // check the content of the incremental dir
    Assert.assertEquals(2, getCount(helper.getCompilerStateFolder(), FileType.JAYCE));
    Assert.assertEquals(4, getCount(helper.getCompilerStateFolder(), FileType.RSC));

    // check the content of the output jack lib
    Assert.assertEquals(4, getCount(outputJack, FileType.JAYCE));
    Assert.assertEquals(4, getCount(outputJack, FileType.RSC));
    checkResourceContent("rsc1", "rsc1lib", outputJack);
    checkResourceContent("rsc2", "rsc2lib", outputJack);
    checkResourceContent("jack/source/rsc3", "rsc3lib", outputJack);
    checkResourceContent("jack/source/rsc4", "rsc4lib", outputJack);

    // check the content of the resources in the output dex dir
    checkResourceContentInDexDir("rsc1", "rsc1lib", outputDex4);
    checkResourceContentInDexDir("rsc2", "rsc2lib", outputDex4);
    checkResourceContentInDexDir("jack/source/rsc3", "rsc3lib", outputDex4);
    checkResourceContentInDexDir("jack/source/rsc4", "rsc4lib", outputDex4);

    // check that no files have been recompiled
    recompiledTypes = helper.getFQNOfRebuiltTypes();
    Assert.assertEquals(0, recompiledTypes.size());
  }

  private void writeToRsc(@Nonnull File rsc, @Nonnull String rscContent) throws IOException {
    FileWriter writer = new FileWriter(rsc);
    try {
      writer.write(rscContent);
    } finally {
      writer.close();
    }
  }

  @Nonnull
  private File createLib(@Nonnull String packageName, @CheckForNull File resourceDir)
      throws Exception {
    IncrementalTestHelper iteLib =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());
    File f = iteLib.addJavaFile(packageName, "A.java", "package " + packageName + "; \n"
        + "public interface A { \n" + "public void m(); }");

    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    File libOut = AbstractTestTools.createTempFile(packageName, toolchain.getLibraryExtension());
    if (resourceDir != null) {
      toolchain.addResourceDir(resourceDir);
    }
    toolchain.addToClasspath(defaultClasspath).srcToLib(libOut, /* zipFiles = */ true,
        iteLib.getSourceFolder());
    return libOut;
  }

  @Nonnull
  private static JackBasedToolchain getIncrementalToolchain(
      @Nonnull Class<? extends JackBasedToolchain> baseToolchain) {
    List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(3);
    excludeList.add(JillBasedToolchain.class);
    excludeList.add(IncrementalToolchain.class);
    excludeList.add(TwoStepsToolchain.class);
    JackBasedToolchain jackToolchain =
        AbstractTestTools.getCandidateToolchain(baseToolchain, excludeList);
    return jackToolchain;
  }

  @Nonnegative
  private int getCount(@Nonnull File lib, @Nonnull FileType fileType) throws LibraryIOException {
    int size = 0;
    InputJackLibrary compilerStateLib = null;
    try {
      compilerStateLib = AbstractTestTools.getInputJackLibrary(lib);
      Iterator<InputVFile> jayceIter = compilerStateLib.iterator(fileType);
      while (jayceIter.hasNext()) {
        size++;
        jayceIter.next();
      }
    } finally {
      if (compilerStateLib != null) {
        compilerStateLib.close();
      }
    }
    return size;
  }

  private void checkResourceContent(@Nonnull String rscPath,
      @Nonnull String rscContent, @Nonnull File libFile) throws Exception {

    InputJackLibrary lib = null;
    BufferedReader reader = null;
    try {
      lib = AbstractTestTools.getInputJackLibrary(libFile);
      InputVFile vFile = lib.getFile(FileType.RSC, new VPath(rscPath, '/'));
      reader = new BufferedReader(new InputStreamReader(vFile.getInputStream()));
      Assert.assertEquals(rscContent, reader.readLine());
      Assert.assertNull(reader.readLine());
    } finally {
      if (reader != null) {
        reader.close();
      }
      if (lib != null) {
        lib.close();
      }
    }
  }

  private void checkResourceContentInDexDir(@Nonnull String rscPath,
      @Nonnull String rscContent, @Nonnull File dexDir) throws Exception {

    File rscFile = new File(dexDir, rscPath);

    try (FileInputStream fis = new FileInputStream(rscFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));) {
      Assert.assertEquals(rscContent, reader.readLine());
      Assert.assertNull(reader.readLine());
    }
  }

  private void checkResourceDoesntExistInDexDir(@Nonnull String rscPath, @Nonnull File dexDir) {
    File rscFile = new File(dexDir, rscPath);
    Assert.assertFalse(rscFile.exists());
  }
}

