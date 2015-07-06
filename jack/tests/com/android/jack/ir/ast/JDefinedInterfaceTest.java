/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.ir.ast;

import com.android.jack.Options;
import com.android.jack.TestTools;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

public class JDefinedInterfaceTest {

  @Nonnull
  private static final String TEST1_CLASS_BINARY_NAME = "com/android/jack/ir/ast/sam/Test001";
  @Nonnull
  private static final String TEST1_CLASS_SIGNATURE = "L" + TEST1_CLASS_BINARY_NAME + ";";
  @Nonnull
  private static final File TEST1_FILE = TestTools.getJackUnitTestFromBinaryName(TEST1_CLASS_BINARY_NAME);

  @Nonnull
  private static final String TEST2_CLASS_BINARY_NAME = "com/android/jack/ir/ast/sam/Test002";
  @Nonnull
  private static final String TEST2_CLASS_SIGNATURE = "L" + TEST2_CLASS_BINARY_NAME + ";";
  @Nonnull
  private static final File TEST2_FILE = TestTools.getJackUnitTestFromBinaryName(TEST2_CLASS_BINARY_NAME);

  @Nonnull
  private static final String TEST3_CLASS_BINARY_NAME = "com/android/jack/ir/ast/sam/Test003";
  @Nonnull
  private static final String TEST3_CLASS_SIGNATURE = "L" + TEST3_CLASS_BINARY_NAME + ";";
  @Nonnull
  private static final File TEST3_FILE = TestTools.getJackUnitTestFromBinaryName(TEST3_CLASS_BINARY_NAME);

  @Nonnull
  private static final String TEST4_CLASS_BINARY_NAME = "com/android/jack/ir/ast/sam/Test004";
  @Nonnull
  private static final String TEST4_CLASS_SIGNATURE = "L" + TEST4_CLASS_BINARY_NAME + ";";
  @Nonnull
  private static final File TEST4_FILE = TestTools.getJackUnitTestFromBinaryName(TEST4_CLASS_BINARY_NAME);

  @Nonnull
  private static final String TEST5_CLASS_BINARY_NAME = "com/android/jack/ir/ast/sam/Test005";
  @Nonnull
  private static final String TEST5_CLASS_SIGNATURE = "L" + TEST5_CLASS_BINARY_NAME + ";";
  @Nonnull
  private static final File TEST5_FILE = TestTools.getJackUnitTestFromBinaryName(TEST5_CLASS_BINARY_NAME);

  @Nonnull
  private static final String TEST6_CLASS_BINARY_NAME = "com/android/jack/ir/ast/sam/Test006";
  @Nonnull
  private static final String TEST6_CLASS_SIGNATURE = "L" + TEST6_CLASS_BINARY_NAME + ";";
  @Nonnull
  private static final File TEST6_FILE = TestTools.getJackUnitTestFromBinaryName(TEST6_CLASS_BINARY_NAME);

  @Nonnull
  private static final String TEST7_CLASS_BINARY_NAME = "com/android/jack/ir/ast/sam/Test007";
  @Nonnull
  private static final String TEST7_CLASS_SIGNATURE = "L" + TEST7_CLASS_BINARY_NAME + ";";
  @Nonnull
  private static final File TEST7_FILE = TestTools.getJackUnitTestFromBinaryName(TEST7_CLASS_BINARY_NAME);

  @Nonnull
  private static final String TEST8_CLASS_BINARY_NAME = "com/android/jack/ir/ast/sam/Test008";
  @Nonnull
  private static final String TEST8_CLASS_SIGNATURE = "L" + TEST8_CLASS_BINARY_NAME + ";";
  @Nonnull
  private static final File TEST8_FILE = TestTools.getJackUnitTestFromBinaryName(TEST8_CLASS_BINARY_NAME);

  @Nonnull
  private static final String TEST9_CLASS_BINARY_NAME = "com/android/jack/ir/ast/sam/Test009";
  @Nonnull
  private static final String TEST9_CLASS_SIGNATURE = "L" + TEST9_CLASS_BINARY_NAME + ";";
  @Nonnull
  private static final File TEST9_FILE = TestTools.getJackUnitTestFromBinaryName(TEST9_CLASS_BINARY_NAME);

  @Test
  public void testSam1() throws Exception {
    JDefinedInterface type = getType(TEST1_FILE, TEST1_CLASS_SIGNATURE);
    Assert.assertTrue(type.isSingleAbstractMethodType());
    Assert.assertEquals("sam", type.getSingleAbstractMethod().getName());
    Assert.assertEquals("int", type.getSingleAbstractMethod().getType().getName());
  }

  @Test
  public void testSam2() throws Exception {
    JDefinedInterface type = getType(TEST2_FILE, TEST2_CLASS_SIGNATURE);
    Assert.assertTrue(type.isSingleAbstractMethodType());
    Assert.assertEquals("sam", type.getSingleAbstractMethod().getName());
    Assert.assertEquals("int", type.getSingleAbstractMethod().getType().getName());
  }

  @Test
  public void testSam3() throws Exception {
    checkIsNoSam(TEST3_FILE, TEST3_CLASS_SIGNATURE);
  }

  @Test
  public void testSam4() throws Exception {
    checkIsNoSam(TEST4_FILE, TEST4_CLASS_SIGNATURE);
  }

  @Test
  public void testSam5() throws Exception {
    JDefinedInterface type = getType(TEST5_FILE, TEST5_CLASS_SIGNATURE);
    Assert.assertTrue(type.isSingleAbstractMethodType());
    Assert.assertEquals("sam", type.getSingleAbstractMethod().getName());
    Assert.assertEquals("int", type.getSingleAbstractMethod().getType().getName());
    Assert.assertEquals("Test005", type.getSingleAbstractMethod().getEnclosingType().getName());
  }

  @Test
  public void testSam6() throws Exception {
    JDefinedInterface type = getType(TEST6_FILE, TEST6_CLASS_SIGNATURE);
    Assert.assertTrue(type.isSingleAbstractMethodType());
    Assert.assertEquals("sam", type.getSingleAbstractMethod().getName());
    Assert.assertEquals("int", type.getSingleAbstractMethod().getType().getName());
    Assert.assertEquals("Test006", type.getSingleAbstractMethod().getEnclosingType().getName());
  }

  @Test
  public void testSam7() throws Exception {
    JDefinedInterface type = getType(TEST7_FILE, TEST7_CLASS_SIGNATURE);
    Assert.assertTrue(type.isSingleAbstractMethodType());
    Assert.assertEquals("sam", type.getSingleAbstractMethod().getName());
    Assert.assertEquals("int", type.getSingleAbstractMethod().getType().getName());
  }

  @Test
  public void testSam8() throws Exception {
    JDefinedInterface type = getType(TEST8_FILE, TEST8_CLASS_SIGNATURE);
    Assert.assertTrue(type.isSingleAbstractMethodType());
    Assert.assertEquals("sam", type.getSingleAbstractMethod().getName());
    Assert.assertEquals("I11", type.getSingleAbstractMethod().getType().getName());
  }

  @Test
  public void testSam9() throws Exception {
    checkIsNoSam(TEST9_FILE, TEST9_CLASS_SIGNATURE);
  }

  @Nonnull
  private JDefinedInterface getType(@Nonnull File testFile, @Nonnull String typeName)
      throws IOException, Exception {
    Options options = TestTools.buildCommandLineArgs(testFile);
    JSession session = TestTools.buildJAst(options);
    Assert.assertNotNull(session);
    JDefinedInterface type = (JDefinedInterface) session.getLookup().getType(typeName);
    Assert.assertNotNull(type);
    return type;
  }

  private void checkIsNoSam(@Nonnull File testFile, @Nonnull String typeName)
      throws IOException, Exception {
    Options options = TestTools.buildCommandLineArgs(testFile);
    JSession session = TestTools.buildJAst(options);
    Assert.assertNotNull(session);
    JDefinedInterface type = (JDefinedInterface) session.getLookup().getType(typeName);
    Assert.assertNotNull(type);

    Assert.assertFalse(type.isSingleAbstractMethodType());
    Assert.assertNull(type.getSingleAbstractMethod());
  }
}
