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

import com.android.jack.library.FileType;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.LibraryIOException;
import com.android.jack.test.helper.IncrementalTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.IncrementalToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.LegacyJillToolchain;
import com.android.jack.test.toolchain.TwoStepsToolchain;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import junit.framework.Assert;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
  @KnownIssue
  public void testTouch() throws Exception {

    File lib1 = createLib("jack.lib1");
    File lib2 = createLib("jack.lib2");

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

    JackBasedToolchain toolchain = getIncrementalToolchain();
    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    toolchain.addStaticLibs(lib1, lib2);
    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.addResourceDir(rscDir);
    toolchain.addToClasspath(defaultClasspath).srcToExe(helper.getDexOutDir(),
        /* zipFiles = */ false, helper.getSourceFolder());
    Thread.sleep(1000); // "lastModified()" lacks precision...
    helper.snapshotJackFilesModificationDate();

    // check the content of the incremental dir
    Assert.assertEquals(2, getCount(helper.getCompilerStateFolder(), FileType.JAYCE));
    Assert.assertEquals(4, getCount(helper.getCompilerStateFolder(), FileType.RSC));

    // check the content of the output jack lib
    Assert.assertEquals(4, getCount(outputJack, FileType.JAYCE));
    Assert.assertEquals(4, getCount(outputJack, FileType.RSC));

    // touch Source1, rsc1 and rsc4
    source1.setLastModified(System.currentTimeMillis());
    writeToRsc(rsc1, "rsc1.1");
    writeToRsc(rsc4, "rsc4.1");

    helper.snapshotJackFilesModificationDate();

    toolchain = getIncrementalToolchain();
    toolchain.addStaticLibs(lib1, lib2);
    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.addResourceDir(rscDir);
    toolchain.addToClasspath(defaultClasspath).srcToExe(helper.getDexOutDir(),
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
    checkResourceContent("jack/source/rsc4", "rsc4.1", helper.getCompilerStateFolder());

    // check the content of the output jack lib
    Assert.assertEquals(4, getCount(outputJack, FileType.JAYCE));
    Assert.assertEquals(4, getCount(outputJack, FileType.RSC));
    checkResourceContent("rsc1", "rsc1.1", outputJack);
    checkResourceContent("jack/source/rsc4", "rsc4.1", outputJack);
  }

  @Test
  @KnownIssue
  public void testDelete() throws Exception {
    File lib1 = createLib("jack.lib1");
    File lib2 = createLib("jack.lib2");

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

    JackBasedToolchain toolchain = getIncrementalToolchain();
    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    toolchain.addStaticLibs(lib1, lib2);
    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.addResourceDir(rscDir);
    toolchain.addToClasspath(defaultClasspath).srcToExe(helper.getDexOutDir(),
        /* zipFiles = */ false, helper.getSourceFolder());
    Thread.sleep(1000); // "lastModified()" lacks precision...
    helper.snapshotJackFilesModificationDate();

    // check the content of the incremental dir
    Assert.assertEquals(2, getCount(helper.getCompilerStateFolder(), FileType.JAYCE));
    Assert.assertEquals(4, getCount(helper.getCompilerStateFolder(), FileType.RSC));

    // check the content of the output jack lib
    Assert.assertEquals(4, getCount(outputJack, FileType.JAYCE));
    Assert.assertEquals(4, getCount(outputJack, FileType.RSC));

    // delete Source1, rsc1 and rsc4
    helper.deleteJavaFile(source1);
    AbstractTestTools.deleteFile(rsc1);
    AbstractTestTools.deleteFile(rsc4);
    helper.snapshotJackFilesModificationDate();

    toolchain = getIncrementalToolchain();
    toolchain.addStaticLibs(lib1, lib2);
    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.addResourceDir(rscDir);
    toolchain.addToClasspath(defaultClasspath).srcToExe(helper.getDexOutDir(),
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
  private File createLib(@Nonnull String packageName) throws Exception {
    IncrementalTestHelper iteLib =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());
    File f = iteLib.addJavaFile(packageName, "A.java", "package " + packageName + "; \n"
        + "public interface A { \n" + "public void m(); }");

    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    File libOut = AbstractTestTools.createTempFile(packageName, toolchain.getLibraryExtension());
    toolchain.addToClasspath(defaultClasspath).srcToLib(libOut, /* zipFiles = */ true,
        iteLib.getSourceFolder());
    return libOut;
  }

  @Nonnull
  private static JackBasedToolchain getIncrementalToolchain() {
    List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(3);
    excludeList.add(LegacyJillToolchain.class);
    excludeList.add(IncrementalToolchain.class);
    excludeList.add(TwoStepsToolchain.class);
    JackBasedToolchain jackToolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
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
}

