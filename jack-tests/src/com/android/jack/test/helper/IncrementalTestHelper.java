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

package com.android.jack.test.helper;

import com.android.jack.backend.jayce.JayceFileImporter;
import com.android.jack.library.FileType;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.LibraryIOException;
import com.android.jack.test.runner.AbstractRuntimeRunner;
import com.android.jack.test.runner.RuntimeRunner;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.IncrementalToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain.MultiDexKind;
import com.android.jack.test.toolchain.JackCliToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.TwoStepsToolchain;
import com.android.sched.util.file.CannotGetModificationTimeException;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import junit.framework.Assert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class is used to write tests on incremental compilation.
 */
public class IncrementalTestHelper {

  @Nonnull
  private final File testingFolder;
  @Nonnull
  private final File sourceFolder;
  @Nonnull
  private final File dexOutDir;
  @Nonnull
  private final File dexFile;
  @Nonnull
  private final File compilerStateFolder;
  @Nonnull
  private final Set<File> javaFiles = new HashSet<File>();
  @Nonnull
  private final Map<VPath, FileTime> fileModificationDate = new HashMap<VPath, FileTime>();
  @Nonnull
  private OutputStream out = System.out;
  @Nonnull
  private OutputStream err = System.err;
  @Nonnull
  private final Map<String, String> properties = new HashMap<String, String>();

  private boolean isApiTest = false;

  public IncrementalTestHelper(@Nonnull File testingFolder) throws IOException {
    this.testingFolder = testingFolder;
    this.sourceFolder = new File(testingFolder, "src");
    if (!this.sourceFolder.mkdirs()) {
      throw new IOException("Failed to create folder " + this.sourceFolder.getAbsolutePath());
    }
    compilerStateFolder = new File(testingFolder, "compileState");
    if (!compilerStateFolder.exists() && !compilerStateFolder.mkdir()) {
      throw new IOException("Failed to create folder " + compilerStateFolder.getAbsolutePath());
    }

    dexOutDir = new File(testingFolder, "outputBinary");
    if (!dexOutDir.exists() && !dexOutDir.mkdir()) {
      throw new IOException("Failed to create folder " + dexOutDir.getAbsolutePath());
    }
    dexFile = new File(dexOutDir, "classes.dex");
  }

  public void setOut(OutputStream out) {
    this.out = out;
  }

  public void setErr(OutputStream err) {
    this.err = err;
  }

  public void setIsApiTest() {
    isApiTest = true;
  }

  @Nonnull
  public File addJavaFile(@Nonnull String packageName, @Nonnull String fileName,
      @Nonnull String fileContent) throws IOException {
    File file = AbstractTestTools.createFile(sourceFolder, packageName, fileName, fileContent);
    javaFiles.add(file);
    return file;
  }

  public void deleteJavaFile(@Nonnull File javaFile)
      throws IOException {
    AbstractTestTools.deleteFile(javaFile);
    javaFiles.remove(javaFile);
  }

  @Nonnull
  public File getCompilerStateFolder() {
    return compilerStateFolder;
  }

  public void cleanSnapshot() {
    fileModificationDate.clear();
  }

  public void snapshotJackFilesModificationDate() throws LibraryIOException {
    InputJackLibrary compilerStateLib = null;
    try {
      compilerStateLib = AbstractTestTools.getInputJackLibrary(compilerStateFolder);
      Iterator<InputVFile> jayceIter = compilerStateLib.iterator(FileType.JAYCE);
      while (jayceIter.hasNext()) {
        InputVFile jayceFile = jayceIter.next();
        try {
          fileModificationDate.put(jayceFile.getPathFromRoot(),
              jayceFile.getLastModified());
        } catch (CannotGetModificationTimeException e) {
          throw new AssertionError(e);
        }
      }
    } finally {
      if (compilerStateLib != null) {
        compilerStateLib.close();
      }
    }
  }

  @Nonnull
  public List<String> getFQNOfRebuiltTypes() throws LibraryIOException {
    assert !fileModificationDate.isEmpty();

    List<String> fqnOfRebuiltTypes = new ArrayList<String>();
    InputJackLibrary compilerStateLib = null;
    try {
      compilerStateLib = AbstractTestTools.getInputJackLibrary(compilerStateFolder);
      Iterator<InputVFile> jayceIter = compilerStateLib.iterator(FileType.JAYCE);
      while (jayceIter.hasNext()) {
        InputVFile jayceFile = jayceIter.next();
        VPath path = jayceFile.getPathFromRoot();
        FileTime previousDate = fileModificationDate.get(path);
        try {
          if (previousDate == null || jayceFile.getLastModified().compareTo(previousDate) > 0) {
            String fqnWithExtension = path.getPathAsString('.');
            String fqn = fqnWithExtension.substring(0,
                fqnWithExtension.lastIndexOf(JayceFileImporter.JAYCE_FILE_EXTENSION));
            fqnOfRebuiltTypes.add(fqn);
          }
        } catch (CannotGetModificationTimeException e) {
          throw new AssertionError(e);
        }
      }
    } finally {
      if (compilerStateLib != null) {
        compilerStateLib.close();
      }
    }

    return fqnOfRebuiltTypes;
  }

  @Nonnull
  public File getSourceFolder() {
    return sourceFolder;
  }

  public void incrementalBuildFromFolder() throws Exception {
    incrementalBuildFromFolder(null, Collections.<File>emptyList());
  }

  public void incrementalBuildFromFolder(@Nonnull File[] classpath) throws Exception {
    incrementalBuildFromFolder(classpath, Collections.<File>emptyList());
  }

  public void incrementalBuildFromFolder(@CheckForNull File[] classpath,
      @Nonnull List<File> imports) throws Exception {
    incrementalBuildFromFolder(classpath, imports, MultiDexKind.NONE);
  }

  public void incrementalBuildFromFolder(@CheckForNull File[] classpath,
      @Nonnull List<File> imports, @Nonnull MultiDexKind multiDexKind) throws Exception {

    List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(1);
    excludeList.add(IncrementalToolchain.class);
    excludeList.add(TwoStepsToolchain.class);
    excludeList.add(JillBasedToolchain.class);
    if (isApiTest) {
      excludeList.add(JackCliToolchain.class);
    }

    JackBasedToolchain jackToolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
    jackToolchain.setIncrementalFolder(getCompilerStateFolder());
    jackToolchain.addStaticLibs(imports.toArray(new File[imports.size()]));
    jackToolchain.setMultiDexKind(multiDexKind);

    jackToolchain.setOutputStream(out);
    jackToolchain.setErrorStream(err);

    File[] bootclasspath = jackToolchain.getDefaultBootClasspath();

    jackToolchain.addToClasspath(bootclasspath);
    if (classpath != null) {
      jackToolchain.addToClasspath(classpath);
    }

    for (Map.Entry<String, String> property : properties.entrySet()) {
      jackToolchain.addProperty(property.getKey(), property.getValue());
    }

    jackToolchain.srcToExe(dexOutDir, /* zipFile = */ false, sourceFolder);

    Thread.sleep(1000);
  }

  @Nonnull
  public void run(@Nonnull String mainClass, @Nonnull String expected) throws Exception {
    List<RuntimeRunner> runnerList = AbstractTestTools.listRuntimeTestRunners();
    for (RuntimeRunner runner : runnerList) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ((AbstractRuntimeRunner) runner).setOutputStream(out);
      Assert.assertEquals(0, runner.run(new String[0], mainClass, dexFile));
      Assert.assertEquals(expected, out.toString());
    }
  }

  @Nonnull
  public File getDexFile() {
    return dexFile;
  }

  @Nonnull
  public IncrementalTestHelper addProperty(@Nonnull String propertyName,
      @Nonnull String propertyValue) {
    properties.put(propertyName, propertyValue);
    return this;
  }

  @Nonnull
  public File getDexOutDir() {
    return dexOutDir;
  }

  @Nonnull
  public int getJayceCount() throws LibraryIOException {
    int size = 0;
    InputJackLibrary compilerStateLib = null;
    try {
      compilerStateLib = AbstractTestTools.getInputJackLibrary(compilerStateFolder);
      Iterator<InputVFile> jayceIter = compilerStateLib.iterator(FileType.JAYCE);
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
}
