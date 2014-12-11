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

import com.android.jack.JackAbortException;
import com.android.jack.Main;
import com.android.jack.backend.dex.DexWritingException;
import com.android.jack.backend.dex.SingleDexOverflowException;
import com.android.jack.test.category.SlowTests;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.tools.merger.FieldIdOverflowException;
import com.android.jack.tools.merger.MergerTestTools;
import com.android.jack.tools.merger.MethodIdOverflowException;
import com.android.jack.tools.merger.TypeIdOverflowException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
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

  @Nonnull
  private static final String EXPECTED_MESSAGE =
      "Error during the dex writing phase: classes.dex has too many IDs. Try using multi-dex";


  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testMergerWithHighNumberOfMethods() throws Exception {
    File srcFolder = AbstractTestTools.createTempDir();

    // One CstMethodRef is also created for call to object.init()
    for (int fileIdx = 0; fileIdx < fileCount; fileIdx++) {
      generateJavaFileWithMethods(srcFolder, fileIdx, 100);
    }
    generateJavaFileWithMethods(srcFolder, fileCount, 36);

    ByteArrayOutputStream err = new ByteArrayOutputStream();

    try {
      buildOneDexPerType(srcFolder, /* withDebug = */false, /* out = */ null, err);
      Assert.fail();
    }  catch (JackAbortException e) {
      Throwable cause = e.getCause();
      Assert.assertTrue(cause instanceof DexWritingException);
      Assert.assertTrue(cause.getCause() instanceof SingleDexOverflowException);
      Assert.assertTrue(cause.getCause().getCause() instanceof MethodIdOverflowException);
      Assert.assertTrue(err.toString().contains(EXPECTED_MESSAGE));
    }
  }

  @Test
  public void testMergerWithHighNumberOfFields() throws Exception {
    File srcFolder = AbstractTestTools.createTempDir();

    for (int fileIdx = 0; fileIdx < fileCount; fileIdx++) {
      generateJavaFileWithFields(srcFolder, fileIdx, 100);
    }
    generateJavaFileWithFields(srcFolder, fileCount, 37);

    ByteArrayOutputStream err = new ByteArrayOutputStream();

    try {
      buildOneDexPerType(srcFolder, /* withDebug = */false, /* out = */ null, err);
      Assert.fail();
    } catch (JackAbortException e) {
      Throwable cause = e.getCause();
      Assert.assertTrue(cause instanceof DexWritingException);
      Assert.assertTrue(cause.getCause() instanceof SingleDexOverflowException);
      Assert.assertTrue(cause.getCause().getCause() instanceof FieldIdOverflowException);
      Assert.assertTrue(err.toString().contains(EXPECTED_MESSAGE));
    }
  }

  @Test
  @Category(SlowTests.class)
  public void testMergerWithHighNumberOfTypes() throws Exception {
    File srcFolder = AbstractTestTools.createTempDir();

    for (int fileIdx = 0; fileIdx < fileCount; fileIdx++) {
      generateJavaFileWithTypes(srcFolder, fileIdx, 100);
    }
    generateJavaFileWithTypes(srcFolder, fileCount, 36);

    ByteArrayOutputStream err = new ByteArrayOutputStream();

    try {
      buildOneDexPerType(srcFolder, /* withDebug = */false, /* out = */ null, err);
      Assert.fail();
    } catch (JackAbortException e) {
      Throwable cause = e.getCause();
      Assert.assertTrue(cause instanceof DexWritingException);
      Assert.assertTrue(cause.getCause() instanceof SingleDexOverflowException);
      Assert.assertTrue(cause.getCause().getCause() instanceof TypeIdOverflowException);
      Assert.assertTrue(err.toString().contains(EXPECTED_MESSAGE));
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
