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

package com.android.jack.dexcomparator.test;

import com.android.jack.DexComparator;
import com.android.jack.DifferenceFoundException;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;


public class BinaryCodeComparisonTest {

  @Nonnull
  private static final File testSource1 = new File("testsource1");
  @Nonnull
  private static final File testSource2 = new File("testsource2");

  @Test
  public void testDifferentBinaryCodeComparison() throws IOException {
    String sourcePath = "com/android/jack/dexcomparator/test/A.java";
    File a1 = new File(testSource1, sourcePath);
    File a2 = new File(testSource2, sourcePath);
    File dex1 = File.createTempFile("dex1", ".dex");
    dex1.deleteOnExit();
    TestTools.compileToDexWithJack(a1, dex1);
    File dex2 = File.createTempFile("dex2", ".dex");
    dex2.deleteOnExit();
    TestTools.compileToDexWithJack(a2, dex2);
    try {
      new DexComparator(false /* compareDebugInfo */, true /* strict */,
          false /* compareDebugInfoBinarily */, true /* compareCodeBinarily */).compare(dex1, dex2);
      Assert.fail();
    } catch (DifferenceFoundException e) {
    }
    try {
      new DexComparator(false /* compareDebugInfo */, true /* strict */,
          false /* compareDebugInfoBinarily */, true /* compareCodeBinarily */).compare(dex2, dex1);
      Assert.fail();
    } catch (DifferenceFoundException e) {
    }
  }

  @Test
  public void testIdenticalBinaryCodeComparison() throws IOException {
    String sourcePath = "com/android/jack/dexcomparator/test/A.java";
    File a1 = new File(testSource1, sourcePath);
    File dex1 = File.createTempFile("dex1", ".dex");
    dex1.deleteOnExit();
    TestTools.compileToDexWithJack(a1, dex1);
    try {
      new DexComparator(false /* compareDebugInfo */, true /* strict */,
          false /* compareDebugInfoBinarily */, true /* compareCodeBinarily */).compare(dex1, dex1);
    } catch (DifferenceFoundException e) {
      Assert.fail(e.getMessage());
    }
  }
}
