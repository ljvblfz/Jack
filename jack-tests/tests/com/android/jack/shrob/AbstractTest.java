/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.shrob;

import com.google.common.io.Files;

import com.android.jack.ProguardFlags;
import com.android.jack.test.category.SlowTests;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Abstract class for running shrob tests
 */
public abstract class AbstractTest {

  @Nonnull
  private static final Charset charSet = Charset.forName("UTF-8");



  protected abstract void runTest(
      @Nonnull String testNumber,
      @Nonnull String flagNumber,
      @Nonnull String mappingNumber)
      throws Exception;

  protected ProguardFlags generateApplyMapping(@Nonnull File mappingFile) throws IOException {
    File applyMapping = AbstractTestTools.createTempFile("mapping.flags", null);
    BufferedWriter writer = new BufferedWriter(new FileWriter(applyMapping));
    writer.append("-applymapping ");
    writer.append(mappingFile.getAbsolutePath());
    writer.close();
    return new ProguardFlags(applyMapping);
  }

  protected ProguardFlags generateInjars(@Nonnull File injar) throws IOException {
    File injarFlags = AbstractTestTools.createTempFile("injars", ".flags");
    BufferedWriter writer = new BufferedWriter(new FileWriter(injarFlags));
    writer.append("-injars ");
    writer.append(injar.getAbsolutePath());
    writer.close();
    return new ProguardFlags(injarFlags);
  }

  @Nonnull
  protected File addOptionsToFlagsFile(@Nonnull File flagsFile, @Nonnull File baseDirectory,
      @Nonnull String options) throws IOException {

    File result = AbstractTestTools.createTempFile("proguard", ".flags");

    Files.write("-basedirectory " + baseDirectory.getAbsolutePath() + " ", result, charSet);
    Files.append(Files.toString(flagsFile, charSet), result, charSet);
    Files.append(options, result, charSet);

    return result;
  }

  @Test
  public void test1_001() throws Exception {
    runTest("001", "001", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_002() throws Exception {
    runTest("001", "002", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_003() throws Exception {
    runTest("001", "003", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_004() throws Exception {
    runTest("001", "004", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_005() throws Exception {
    runTest("001", "005", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_005_002() throws Exception {
    runTest("001", "005", "002");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_005_003() throws Exception {
    runTest("001", "005", "003");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_006() throws Exception {
    runTest("001", "006", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_007() throws Exception {
    runTest("001", "007", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_008() throws Exception {
    runTest("001", "008", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_009() throws Exception {
    runTest("001", "009", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_010() throws Exception {
    runTest("001", "010", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_011() throws Exception {
    runTest("001", "011", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_012() throws Exception {
    runTest("001", "012", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_013() throws Exception {
    runTest("001", "013", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_014() throws Exception {
    runTest("001", "014", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_015() throws Exception {
    runTest("001", "015", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_016() throws Exception {
    runTest("001", "016", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_017() throws Exception {
    runTest("001", "017", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_018() throws Exception {
    runTest("001", "018", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_019() throws Exception {
    runTest("001", "019", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_020() throws Exception {
    runTest("001", "020", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_021() throws Exception {
    runTest("001", "021", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test1_022() throws Exception {
    runTest("001", "022", "");
  }

  @Test
  public void test2_001() throws Exception {
    runTest("002", "001", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test2_002() throws Exception {
    runTest("002", "002", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test2_003() throws Exception {
    runTest("002", "003", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test2_004() throws Exception {
    runTest("002", "004", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test2_005() throws Exception {
    runTest("002", "005", "");
  }

  @Test
  public void test4_001() throws Exception {
    runTest("004", "001", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test4_002() throws Exception {
    runTest("004", "002", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test4_003() throws Exception {
    runTest("004", "003", "");
  }

  @Test
  public void test5_001() throws Exception {
    runTest("005", "001", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test5_002() throws Exception {
    runTest("005", "002", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test5_003() throws Exception {
    runTest("005", "003", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test5_004() throws Exception {
    runTest("005", "004", "");
  }

  @Test
  public void test5_005() throws Exception {
    runTest("005", "005", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test5_006() throws Exception {
    runTest("005", "006", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test5_007() throws Exception {
    runTest("005", "007", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test5_008() throws Exception {
    runTest("005", "008", "");
  }

  @Test
  public void test6_001() throws Exception {
    runTest("006", "001", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test6_002() throws Exception {
    runTest("006", "002", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test6_003() throws Exception {
    runTest("006", "003", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test6_004() throws Exception {
    runTest("006", "004", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test6_005() throws Exception {
    runTest("006", "005", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test6_006() throws Exception {
    runTest("006", "006", "");
  }

  @Test
  public void test7_001() throws Exception {
    runTest("007", "001", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test7_002() throws Exception {
    runTest("007", "002", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test7_003() throws Exception {
    runTest("007", "003", "");
  }

  @Test
  public void test8_001() throws Exception {
    runTest("008", "001", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test8_002() throws Exception {
    runTest("008", "002", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test8_003() throws Exception {
    runTest("008", "003", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test8_004() throws Exception {
    runTest("008", "004", "");
  }

  @Test
  public void test9_001() throws Exception {
    runTest("009", "001", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test9_002() throws Exception {
    runTest("009", "002", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test9_003() throws Exception {
    runTest("009", "003", "");
  }

  @Test
  public void test10_001() throws Exception {
    runTest("010", "001", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test10_002() throws Exception {
    runTest("010", "002", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test10_003() throws Exception {
    runTest("010", "003", "");
  }

  @Test
  public void test11_001() throws Exception {
    runTest("011", "001", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test11_002() throws Exception {
    runTest("011", "002", "");
  }

  @Test
  public void test12_001() throws Exception {
    runTest("012", "001", "");
  }

  @Test
  public void test13_001() throws Exception {
    runTest("013", "001", "");
  }

  @Test
  public void test14_001() throws Exception {
    runTest("014", "001", "");
  }

  @Test
  public void test15_001() throws Exception {
    runTest("015", "001", "");
  }

  @Test
  public void test16_001() throws Exception {
    runTest("016", "001", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test16_002() throws Exception {
    runTest("016", "002", "");
  }

  @Test
  public void test17_001() throws Exception {
    runTest("017", "001", "");
  }

  @Test
  public void test18_001() throws Exception {
    runTest("018", "001", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test18_002() throws Exception {
    runTest("018", "002", "");
  }

  @Test
  public void test19_001() throws Exception {
    runTest("019", "001", "");
  }

  @Test
  public void test21_001() throws Exception {
    runTest("021", "001", "");
  }

  @Test
  public void test22_001() throws Exception {
    runTest("022", "001", "");
  }

  @Test
  public void test23_001() throws Exception {
    runTest("023", "001", "");
  }

  @Test
  public void test25_001() throws Exception {
    runTest("025", "001", "");
  }

  @Test
  public void test26_001() throws Exception {
    runTest("026", "001", "");
  }

  @Test
  public void test29_001() throws Exception {
    runTest("029", "001", "");
  }

  @Test
  public void test30_001() throws Exception {
    runTest("030", "001", "");
  }

  @Test
  public void test31_001() throws Exception {
    runTest("031", "001", "");
  }

  @Test
  public void test31_002() throws Exception {
    runTest("031", "002", "");
  }

  @Test
  public void test32_001() throws Exception {
    runTest("032", "001", "");
  }

  @Test
  public void test33_001() throws Exception {
    runTest("033", "001", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test33_002() throws Exception {
    runTest("033", "002", "");
  }

  @Test
  public void test34_001() throws Exception {
    runTest("034", "001", "");
  }

  @Test
  public void test35_001() throws Exception {
    runTest("035", "001", "");
  }

  @Test
  public void test36_001() throws Exception {
    runTest("036", "001", "");
  }

  @Test
  public void test37_001() throws Exception {
    runTest("037", "001", "");
  }

  @Test
  public void test38_001() throws Exception {
    runTest("038", "001", "");
  }

  @Test
  public void test39_001() throws Exception {
    runTest("039", "001", "");
  }

  @Test
  @Category(SlowTests.class)
  public void test40_001() throws Exception {
    runTest("040", "001", "");
  }

  @Test
  @KnownIssue
  public void test41_001() throws Exception {
    runTest("041", "001", "");
  }

  @Test
  public void test44_001() throws Exception {
    runTest("044", "001", "");
  }

  @Test
  public void test45_001() throws Exception {
    runTest("045", "001", "");
  }

  @Test
  public void test47_001() throws Exception {
    runTest("047", "001", "");
  }

  @Test
  public void test52_001() throws Exception {
    runTest("052", "001", "");
  }

  protected void checkToolchainIsNotJillBased() {
    List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(1);
    excludeList.add(JillBasedToolchain.class);
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain(IToolchain.class, excludeList);
  }
}
