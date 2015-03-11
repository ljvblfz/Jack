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

import com.android.jack.comparator.DexAnnotationsComparator;
import com.android.jack.comparator.DifferenceFoundException;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class AnnotationComparisonTest {

  @Nonnull
  private static final File testSource1 = new File("testsource1");
  @Nonnull
  private static final File testSource2 = new File("testsource2");

  @Test
  public void testOutOfOrderMemberClassesAnnotations() throws IOException,
      DifferenceFoundException {
    String sourcePath = "com/android/jack/dexcomparator/test/Outer.java";
    File a1 = new File(testSource1, sourcePath);
    File a2 = new File(testSource2, sourcePath);
    File dex1 = File.createTempFile("dex1", ".dex");
    dex1.deleteOnExit();
    TestTools.compileToDexWithJack(a1, dex1);
    File dex2 = File.createTempFile("dex2", ".dex");
    dex2.deleteOnExit();
    TestTools.compileToDexWithJack(a2, dex2);
    new DexAnnotationsComparator().compare(dex1, dex2);
  }

  @Test
  public void testOutOfOrderThrowsAnnotations() throws IOException,
      DifferenceFoundException {
    String sourcePath = "com/android/jack/dexcomparator/test/Throws.java";
    File a1 = new File(testSource1, sourcePath);
    File a2 = new File(testSource2, sourcePath);
    File dex1 = File.createTempFile("dex1", ".dex");
    dex1.deleteOnExit();
    TestTools.compileToDexWithJack(a1, dex1);
    File dex2 = File.createTempFile("dex2", ".dex");
    dex2.deleteOnExit();
    TestTools.compileToDexWithJack(a2, dex2);
    new DexAnnotationsComparator().compare(dex1, dex2);
  }

  @Test
  public void testOutOfOrderCustomAnnotations() throws IOException {
    String sourcePath1 = "com/android/jack/dexcomparator/test/MyAnnotation.java";
    String sourcePath2 = "com/android/jack/dexcomparator/test/Annotated.java";
    File dex1 = File.createTempFile("dex1", ".dex");
    dex1.deleteOnExit();
    List<File> sourceList1 = new ArrayList<File>(2);
    sourceList1.add(new File(testSource1, sourcePath1));
    sourceList1.add(new File(testSource1, sourcePath2));
    TestTools.compileToDexWithJack(sourceList1, dex1);
    File dex2 = File.createTempFile("dex2", ".dex");
    dex2.deleteOnExit();
    List<File> sourceList2 = new ArrayList<File>(2);
    sourceList2.add(new File(testSource2, sourcePath1));
    sourceList2.add(new File(testSource2, sourcePath2));
    TestTools.compileToDexWithJack(sourceList2, dex2);
    try {
      new DexAnnotationsComparator().compare(dex1, dex2);
      Assert.fail();
    } catch (DifferenceFoundException e) {
    }
  }

  @Test
  public void testSameOrderCustomAnnotations() throws IOException,
      DifferenceFoundException {
    String sourcePath1 = "com/android/jack/dexcomparator/test/MyAnnotation.java";
    String sourcePath2 = "com/android/jack/dexcomparator/test/Annotated.java";
    File dex1 = File.createTempFile("dex1", ".dex");
    dex1.deleteOnExit();
    List<File> sourceList1 = new ArrayList<File>(2);
    sourceList1.add(new File(testSource1, sourcePath1));
    sourceList1.add(new File(testSource1, sourcePath2));
    TestTools.compileToDexWithJack(sourceList1, dex1);
    new DexAnnotationsComparator().compare(dex1, dex1);
  }

}
