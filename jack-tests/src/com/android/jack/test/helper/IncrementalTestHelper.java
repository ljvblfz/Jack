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
import com.android.jack.test.runner.AbstractRuntimeRunner;
import com.android.jack.test.runner.RuntimeRunner;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;

import junit.framework.Assert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
  private final File jackFolder;
  @Nonnull
  private final Set<File> javaFiles = new HashSet<File>();
  @Nonnull
  private final Map<String, Long> fileModificationDate = new HashMap<String, Long>();
  @Nonnull
  private OutputStream out = System.out;
  @Nonnull
  private OutputStream err = System.err;

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
    jackFolder = new File(compilerStateFolder, FileType.JAYCE.getPrefix());
  }

  public void setOut(OutputStream out) {
    this.out = out;
  }

  public void setErr(OutputStream err) {
    this.err = err;
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

  public void snapshotJackFilesModificationDate() {
    List<File> jackFiles = new ArrayList<File>();
    fillJackFiles(jackFolder, jackFiles);
    for (File jackFile : jackFiles) {
      fileModificationDate.put(jackFile.getAbsolutePath(), Long.valueOf(jackFile.lastModified()));
    }
  }

  private void fillJackFiles(@Nonnull File file, @Nonnull List<File> jackFiles) {
    if (file.isDirectory()) {
      for (File subFile : file.listFiles()) {
        fillJackFiles(subFile, jackFiles);
      }
    } else if (file.getName().endsWith(JayceFileImporter.JAYCE_FILE_EXTENSION)) {
      jackFiles.add(file);
    }
  }

  @Nonnull
  public List<String> getFQNOfRebuiltTypes() {
    assert !fileModificationDate.isEmpty();

    List<String> fqnOfRebuiltTypes = new ArrayList<String>();
    List<File> jackFiles = new ArrayList<File>();
    fillJackFiles(jackFolder, jackFiles);

    for (File jackFile : jackFiles) {
      Long previousDate = fileModificationDate.get(jackFile.getAbsolutePath());
      if (previousDate == null || jackFile.lastModified() > previousDate.longValue()) {
        String jackFileName = jackFile.getAbsolutePath();
        String binaryTypeName = jackFileName.substring(0, jackFileName.indexOf(".jayce"));
        binaryTypeName = binaryTypeName.substring(jackFolder.getAbsolutePath().length() + 1);
        fqnOfRebuiltTypes.add(binaryTypeName.replace(File.separatorChar, '.'));
      }
    }

    return (fqnOfRebuiltTypes);
  }

  public void incrementalBuildFromFolder() throws Exception {
    incrementalBuildFromFolder(null, Collections.<File>emptyList());
  }

  public void incrementalBuildFromFolder(@Nonnull File[] classpath) throws Exception {
    incrementalBuildFromFolder(classpath, Collections.<File>emptyList());
  }

  public void incrementalBuildFromFolder(@CheckForNull File[] classpath,
      @Nonnull List<File> imports) throws Exception {

    List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(1);
    excludeList.add(JillBasedToolchain.class);

    JackApiToolchain jackToolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchain.class, excludeList);
    jackToolchain.setIncrementalFolder(getCompilerStateFolder());
    jackToolchain.addStaticLibs(imports.toArray(new File[imports.size()]));

    jackToolchain.setOutputStream(out);
    jackToolchain.setErrorStream(err);

    File[] bootclasspath = jackToolchain.getDefaultBootClasspath();

    jackToolchain.srcToExe(classpath == null ? AbstractTestTools.getClasspathAsString(bootclasspath)
        : AbstractTestTools.getClasspathsAsString(bootclasspath, classpath), dexOutDir,
        /* zipFile = */ false, sourceFolder);

    Thread.sleep(1000);
  }

  @Nonnull
  public void run(@Nonnull String mainClass, @Nonnull String expected) throws Exception {
    List<RuntimeRunner> runnerList = AbstractTestTools.listRuntimeTestRunners(null);
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
  public File getJackFolder() {
    return jackFolder;
  }

  @Nonnull
  public List<File> getJackFiles() {
    return AbstractTestTools.getFiles(jackFolder, ".jayce");
  }

}
