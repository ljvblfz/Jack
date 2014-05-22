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

package com.android.jack.errorhandling;

import com.android.jack.IllegalOptionsException;
import com.android.jack.Jack;
import com.android.jack.JackIOException;
import com.android.jack.NothingToDoException;
import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.backend.jayce.ImportConflictException;
import com.android.jack.ir.ast.JPackageLookupException;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.util.NamingTools;
import com.android.sched.util.config.ConfigurationException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class TestingEnvironment {

  @CheckForNull
  ByteArrayOutputStream baos = null;

  @CheckForNull
  PrintStream errRedirectStream = null;

  @Nonnull
  private final File testingFolder;

  @Nonnull
  private final File sourceFolder;

  @Nonnull
  private final File jackFolder;

  public TestingEnvironment() throws IOException {
    this.testingFolder = TestTools.createTempDir("ErrorHandlingTest_", "");
    this.sourceFolder = new File(testingFolder, "src");
    if (!this.sourceFolder.mkdirs()) {
      throw new IOException("Failed to create folder " + this.sourceFolder.getAbsolutePath());
    }
    this.jackFolder = new File(testingFolder, "jack");
    if (!this.jackFolder.mkdirs()) {
      throw new IOException("Failed to create folder " + this.jackFolder.getAbsolutePath());
    }
  }

  @Nonnull
  public File getSourceFolder() {
    return sourceFolder;
  }

  @Nonnull
  public File getJackFolder() {
    return jackFolder;
  }

  @Nonnull
  public File getTestingFolder() {
    return testingFolder;
  }

  public void compile(@Nonnull Options options)
      throws JTypeLookupException,
      JPackageLookupException,
      JackIOException,
      ImportConflictException,
      ConfigurationException,
      IllegalOptionsException,
      NothingToDoException {
    Jack.run(options);
  }

  @Nonnull
  public File addFile(@Nonnull File folder, @Nonnull String packageName, @Nonnull String fileName,
      @Nonnull String fileContent) throws IOException {
    File packageFolder = new File(folder, packageName.replace('.', File.separatorChar));
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
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(javaFile);
      fos.write(fileContent.getBytes());
    } finally {
      if (fos != null) {
        fos.close();
      }
    }

    return javaFile;
  }

  public void deleteJavaFile(@Nonnull File folder, @Nonnull String packageName, @Nonnull String fileName)
      throws IOException {
    File packageFolder = new File(folder, NamingTools.getBinaryName(packageName));
    File javaFile = new File(packageFolder, fileName);
    if (!javaFile.delete()) {
      throw new IOException("Failed to delete file " + javaFile.getAbsolutePath());
    }
  }

  @Nonnull
  public String endErrorRedirection() {
    assert baos != null;
    String err = baos.toString();
    assert errRedirectStream != null;
    errRedirectStream.close();
    return err;
  }

  public void startErrRedirection() {
    baos = new ByteArrayOutputStream();
    errRedirectStream = new PrintStream(baos);
    System.setErr(errRedirectStream);
  }
}
