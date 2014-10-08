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

package com.android.jack.multidex;

import com.android.jack.JackUserException;
import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.dex.MultiDexLegacy;
import com.android.jack.tools.merger.FieldIdOverflowException;
import com.android.jack.tools.merger.MethodIdOverflowException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class MultiDexOverflowTests {

  private static File annotations;

  @BeforeClass
  public static void init() throws IOException, Exception {
    annotations = MultiDexTests.prepareAnnotations();
  }

  @Test
  public void testMinimalMainDexOverflowWithMethods() throws Exception {
    File srcFolder = TestTools.createTempDir("src", "dir");
    File outFolder = TestTools.createTempDir("out", "dir");

    int fileCount = 655;
    for (int fileIdx = 0; fileIdx < fileCount; fileIdx++) {
      generateJavaFileWithMethods(srcFolder, fileIdx, 100);
    }
    generateJavaFileWithMethods(srcFolder, fileCount, 36);

    Options options = new Options();
    options.addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
    options.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");
    try {
      TestTools.compileSourceToDex(options, srcFolder, TestTools.getClasspathsAsString(
          TestTools.getDefaultBootclasspath(), new File[] {annotations}), outFolder, false /* zip */
          );
      Assert.fail();
    } catch (JackUserException e) {
      Assert.assertEquals("Too many classes in main dex. Index overflow while merging dex files",
          e.getMessage());
      Throwable cause = e.getCause();
      Assert.assertTrue(cause instanceof MethodIdOverflowException);
    }
  }

  @Test
  public void testStandardMainDexOverflowWithFields() throws Exception {
    File srcFolder = TestTools.createTempDir("src", "dir");
    File outFolder = TestTools.createTempDir("out", "dir");

    int fileCount = 655;
    for (int fileIdx = 0; fileIdx < fileCount; fileIdx++) {
      generateJavaFileWithFields(srcFolder, fileIdx, 100);
    }
    generateJavaFileWithFields(srcFolder, fileCount, 37);

    Options options = new Options();
    options.addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
    options.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");
    try {
      TestTools.compileSourceToDex(options, srcFolder, TestTools.getClasspathsAsString(
          TestTools.getDefaultBootclasspath(), new File[] {annotations}), outFolder, false /* zip */
          );
      Assert.fail();
    } catch (JackUserException e) {
      Assert.assertEquals("Too many classes in main dex. Index overflow while merging dex files",
          e.getMessage());
      Throwable cause = e.getCause();
      Assert.assertTrue(cause instanceof FieldIdOverflowException);
    }
  }

  private void generateJavaFileWithMethods(@Nonnull File srcFolder, @Nonnegative int fileIdx,
      @Nonnegative int methodCount) throws IOException, FileNotFoundException {
    File javaFile = new File(srcFolder, "A" + fileIdx + ".java");
    if (!javaFile.createNewFile()) {
      throw new IOException("Failed to create file " + javaFile.getAbsolutePath());
    }
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(javaFile);
      StringBuilder content = new StringBuilder("package jack.merger; \n"
          + "@com.android.jack.annotations.ForceInMainDex public class A" + fileIdx + " {\n");
      // -1 due to implicit init method
      for (int mthIdx = 0; mthIdx < methodCount - 1; mthIdx++) {
        content.append("public void m" + mthIdx + "() {} \n");
      }
      content.append("} \n");
      fos.write(content.toString().getBytes());
    } finally {
      if (fos != null) {
        fos.close();
      }
    }
  }

  private void generateJavaFileWithFields(@Nonnull File srcFolder, @Nonnegative int fileIdx,
      @Nonnegative int fieldCount) throws IOException, FileNotFoundException {
    File javaFile = new File(srcFolder, "A" + fileIdx + ".java");
    if (!javaFile.createNewFile()) {
      throw new IOException("Failed to create file " + javaFile.getAbsolutePath());
    }
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(javaFile);
      StringBuilder content = new StringBuilder("package jack.merger; \n"
          + "@com.android.jack.annotations.ForceInMainDex public class A" + fileIdx + " {\n");
      for (int fieldIdx = 0; fieldIdx < fieldCount; fieldIdx++) {
        content.append("public int f" + fieldIdx + ";\n");
      }
      content.append("} \n");
      fos.write(content.toString().getBytes());
    } finally {
      if (fos != null) {
        fos.close();
      }
    }
  }

}
