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

package com.android.jack.experimental.incremental;

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.jayce.JayceFileImporter;
import com.android.jack.util.ExecuteFile;
import com.android.jack.util.NamingTools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class IncrementalTestingEnvironment extends TestTools {

  @CheckForNull
  ByteArrayOutputStream baosOut = null;

  @CheckForNull
  ByteArrayOutputStream baosErr = null;

  @CheckForNull
  PrintStream errRedirectStream = null;

  @CheckForNull
  PrintStream outRedirectStream = null;

  @Nonnull
  private final File compilerStateDir;

  @Nonnull
  private final File testingFolder;

  @Nonnull
  private final File sourceFolder;

  @Nonnull
  private final File dexFile;

  @Nonnull
  private final File jackFolder;

  private final Map<String, Long> fileModificationDate = new HashMap<String, Long>();

  @Nonnull
  private final Set<File> javaFiles = new HashSet<File>();

  public IncrementalTestingEnvironment(@Nonnull File testingFolder) throws IOException {
    this.testingFolder = testingFolder;
    this.sourceFolder = new File(testingFolder, "src");
    if (!this.sourceFolder.mkdirs()) {
      throw new IOException("Failed to create folder " + this.sourceFolder.getAbsolutePath());
    }

    dexFile = new File(testingFolder, DexFileWriter.DEX_FILENAME);
    compilerStateDir = new File(testingFolder, "compileState");
    if (!compilerStateDir.exists() && !compilerStateDir.mkdir()) {
      throw new IOException("Failed to create folder " + compilerStateDir.getAbsolutePath());
    }
    jackFolder = new File(compilerStateDir, "jackFiles");
  }

  public void addJavaFile(@Nonnull String packageName, @Nonnull String fileName,
      @Nonnull String fileContent) throws IOException {
    File packageFolder = new File(sourceFolder, packageName.replace('.', File.separatorChar));
    if (!packageFolder.exists() && !packageFolder.mkdirs()) {
      throw new IOException("Failed to create folder " + packageFolder.getAbsolutePath());
    }
    File javaFile = new File(packageFolder, fileName);
    if (javaFile.exists() && !javaFile.delete()) {
      throw new IOException("Failed to delete file " + javaFile.getAbsolutePath());
    }
    if (!javaFile.createNewFile()) {
      throw new IOException("Failed to create file " + javaFile.getAbsolutePath());
    }
    javaFiles.add(javaFile);
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(javaFile);
      fos.write(fileContent.getBytes());
    } finally {
      if (fos != null) {
        fos.close();
      }
    }
  }

  public void deleteJavaFile(@Nonnull String packageName, @Nonnull String fileName)
      throws IOException {
    File packageFolder = new File(sourceFolder, NamingTools.getBinaryName(packageName));
    File javaFile = new File(packageFolder, fileName);
    if (!javaFile.delete()) {
      throw new IOException("Failed to delete file " + javaFile.getAbsolutePath());
    }
    javaFiles.remove(javaFile);
  }

  @Nonnull
  public File getCompilerStateFolder() {
    return compilerStateDir;
  }

  public void incrementalBuildFromFolder() throws Exception {
    incrementalBuildFromFolder(null, Collections.<File>emptyList());
  }

  public void incrementalBuildFromFolder(@Nonnull File[] classpath) throws Exception {
    incrementalBuildFromFolder(classpath, Collections.<File>emptyList());
  }

  public void incrementalBuildFromFolder(@CheckForNull File[] classpath,
      @Nonnull List<File> imports) throws Exception {
    startOutRedirection();
    startErrRedirection();

    Options options = TestTools.buildCommandLineArgs(testingFolder);
    options.setIncrementalFolder(getCompilerStateFolder());
    options.setJayceImports(imports);

    compileSourceToDex(options, sourceFolder,
        classpath == null ? TestTools.getClasspathAsString(TestTools.getDefaultBootclasspath())
            : TestTools.getClasspathsAsString(TestTools.getDefaultBootclasspath(), classpath),
        testingFolder);

    Thread.sleep(1000);

    System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
    System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
  }

  @Nonnull
  public String run(@Nonnull String mainClass) throws IOException {
    ExecuteFile ef = new ExecuteFile("rm -rf /tmp/android-data/dalvik-cache/*");
    ef.run();

    ef = new ExecuteFile("dalvik -classpath " + dexFile.getAbsolutePath() + " " + mainClass);
    ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
    ef.setOut(baosOut);
    // Be careful, Output on Err is forgotten
    ef.setErr(new ByteArrayOutputStream());
    if (!ef.run()) {
      throw new AssertionError("Execution failed");
    }

    baosOut.flush();
    baosOut.close();

    return (baosOut.toString());
  }

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
        fqnOfRebuiltTypes.add(binaryTypeName.replace(File.separatorChar,'.'));
      }
    }

    return (fqnOfRebuiltTypes);
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

  @Nonnull
  public String getStringRepresentingOut() {
    assert baosOut != null;
    String out = baosOut.toString();
    assert outRedirectStream != null;
    outRedirectStream.close();
    return out;
  }

  @Nonnull
  public String getStringRepresentingErr() {
    assert baosErr != null;
    String err = baosErr.toString();
    assert errRedirectStream != null;
    errRedirectStream.close();
    return err;
  }

  @Nonnull
  public File getJackFolder() {
    return jackFolder;
  }

  @Nonnull
  public List<File> getJackFiles() {
    List<File> jackFiles = new ArrayList<File>();
    fillJackFiles(jackFolder, jackFiles);
    return (jackFiles);
  }

  @Nonnull
  public File getDexFile() {
    return dexFile;
  }

  private void startErrRedirection() {
    baosErr = new ByteArrayOutputStream();
    errRedirectStream = new PrintStream(baosErr);
    System.setErr(errRedirectStream);
  }

  private void startOutRedirection() {
    baosOut = new ByteArrayOutputStream();
    outRedirectStream = new PrintStream(baosOut);
    System.setOut(outRedirectStream);
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

  private static void compileSourceToDex(@Nonnull Options options,
      @Nonnull File sourceFolderOrSourceList,
      @CheckForNull String classpath,
      @Nonnull File out) throws Exception {
    options.setEcjArguments(TestTools.buildEcjArgs());
    addFile(sourceFolderOrSourceList, options.getEcjArguments());
    options.setClasspath(classpath);
    options.setOutputDir(out);
    JackIncremental.run(options);
  }
}
