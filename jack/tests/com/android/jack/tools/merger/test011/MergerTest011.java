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

package com.android.jack.tools.merger.test011;

import com.android.jack.JackUserException;
import com.android.jack.Main;
import com.android.jack.TestTools;
import com.android.jack.category.SlowTests;
import com.android.jack.tools.merger.FieldIdOverflowException;
import com.android.jack.tools.merger.MergerTestTools;
import com.android.jack.tools.merger.MethodIdOverflowException;
import com.android.jack.tools.merger.TypeIdOverflowException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * JUnit test checking that merging can throw overflow exceptions.
 */
public class MergerTest011 extends MergerTestTools {

  private static int fileCount = 655;

  private static final String expectedExceptionMessage =
      "Index overflow while merging dex files. Try using multidex";

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testMergerWithHighNumberOfMethods() throws Exception {
    File srcFolder = TestTools.createTempDir("oneDexPerType", "SrcFolder");

    // One CstMethodRef is also created for call to object.init()
    for (int fileIdx = 0; fileIdx < fileCount; fileIdx++) {
      generateJavaFileWithMethods(srcFolder, fileIdx, 100);
    }
    generateJavaFileWithMethods(srcFolder, fileCount, 36);

    try {
      buildOneDexPerType(TestTools.getDefaultBootclasspathString(), srcFolder, false /* withDebug */);
      Assert.fail();
    } catch (JackUserException e) {
      Assert.assertEquals(expectedExceptionMessage, e.getMessage());
      Throwable cause = e.getCause();
      Assert.assertTrue(cause instanceof MethodIdOverflowException);
    }
  }

  @Test
  public void testMergerWithHighNumberOfFields() throws Exception {
    File srcFolder = TestTools.createTempDir("oneDexPerType", "SrcFolder");

    for (int fileIdx = 0; fileIdx < fileCount; fileIdx++) {
      generateJavaFileWithFields(srcFolder, fileIdx, 100);
    }
    generateJavaFileWithFields(srcFolder, fileCount, 37);

    try {
      buildOneDexPerType(TestTools.getDefaultBootclasspathString(), srcFolder, false /* withDebug */);
      Assert.fail();
    } catch (JackUserException e) {
      Assert.assertEquals(expectedExceptionMessage, e.getMessage());
      Throwable cause = e.getCause();
      Assert.assertTrue(cause instanceof FieldIdOverflowException);
    }
  }

  @Test
  @Category(SlowTests.class)
  public void testMergerWithHighNumberOfTypes() throws Exception {
    File srcFolder = TestTools.createTempDir("oneDexPerType", "SrcFolder");

    for (int fileIdx = 0; fileIdx < fileCount; fileIdx++) {
      generateJavaFileWithTypes(srcFolder, fileIdx, 100);
    }
    generateJavaFileWithTypes(srcFolder, fileCount, 36);

    try {
      buildOneDexPerType(TestTools.getDefaultBootclasspathString(), srcFolder, false /* withDebug */);
      Assert.fail();
    } catch (JackUserException e) {
      Assert.assertEquals(expectedExceptionMessage, e.getMessage());
      Throwable cause = e.getCause();
      Assert.assertTrue(cause instanceof TypeIdOverflowException);
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
      StringBuilder content =
          new StringBuilder("package jack.merger; \n" + "public class A" + fileIdx+ " {");
      // -1 due to implicit init method
      for (int mthIdx = 0; mthIdx < methodCount - 1; mthIdx++) {
        content.append("public void m" + mthIdx + "() {}");
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
      StringBuilder content =
          new StringBuilder("package jack.merger; \n" + "public class A" + fileIdx+ " {");
      for (int fieldIdx = 0; fieldIdx < fieldCount; fieldIdx++) {
        content.append("public int f" + fieldIdx + ";");
      }
      content.append("} \n");
      fos.write(content.toString().getBytes());
    } finally {
      if (fos != null) {
        fos.close();
      }
    }
  }

  private void generateJavaFileWithTypes(@Nonnull File srcFolder, @Nonnegative int fileIdx,
      @Nonnegative int typeCount) throws IOException, FileNotFoundException {
    File javaFile = new File(srcFolder, "A" + fileIdx + ".java");
    if (!javaFile.createNewFile()) {
      throw new IOException("Failed to create file " + javaFile.getAbsolutePath());
    }
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(javaFile);
      StringBuilder content =
          new StringBuilder("package jack.merger; \n" + "public class A" + fileIdx+ " {");
      for (int typeIdx = 0; typeIdx < typeCount; typeIdx++) {
        content.append("public class c" + typeIdx + " {}");
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
