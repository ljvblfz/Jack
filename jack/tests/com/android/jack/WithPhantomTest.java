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

package com.android.jack;

import com.android.jack.category.KnownBugs;
import com.android.sched.util.collect.Lists;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.FilenameFilter;

import javax.annotation.Nonnull;

/**
 * Test compilation involving phantoms.
 */
public class WithPhantomTest {

  @Nonnull
  private static final String BOOTCLASSPATH = TestTools.getDefaultBootclasspathString();
  @Nonnull
  private static final String TEST001 = "withphantom/test001";
  @Nonnull
  private static final String TEST002 = "withphantom/test002";

  @Nonnull
  private static String fixPath(@Nonnull String unixPath) {
    return unixPath.replace('/', File.separatorChar);
  }

  @Category(value = KnownBugs.class)
  @Test
  public void testPhantomOuter() throws Exception {
    File tempJackFolder = TestTools.createTempDir("jack", "dir");
    TestTools.compileSourceToJack(new Options(),
        TestTools.getJackTestsWithJackFolder(TEST001),
        BOOTCLASSPATH, tempJackFolder, false /* non-zipped */);
    boolean deleted = new File(tempJackFolder,
        fixPath("com/android/jack/withphantom/test001/jack/A.jack")).delete();
    Assert.assertTrue(deleted);

    File testFolder = TestTools.getJackTestFolder(TEST001);

    File tempOut1 = TestTools.createTempDir("jack", "dir");
    TestTools.shrobJackToJack(new Options(),
        tempJackFolder,
        BOOTCLASSPATH,
        tempOut1,
        Lists.create(
            new ProguardFlags(testFolder, "shrink1.flags")),
        false /* non-zipped */);

    File tempOut2 = TestTools.createTempDir("jack", "dir");
    TestTools.shrobJackToJack(new Options(),
        tempJackFolder,
        BOOTCLASSPATH,
        tempOut2,
        Lists.create(
            new ProguardFlags(testFolder, "shrink2.flags")),
        false /* non-zipped */);

    File tempOut3 = TestTools.createTempDir("jack", "dir");
    TestTools.shrobJackToJack(new Options(),
        tempJackFolder,
        BOOTCLASSPATH,
        tempOut3,
        Lists.create(
            new ProguardFlags(testFolder, "obf1.flags")),
        false /* non-zipped */);

    File tempOut4 = TestTools.createTempDir("jack", "dir");
    TestTools.shrobJackToJack(new Options(),
        tempJackFolder,
        BOOTCLASSPATH,
        tempOut4,
        Lists.create(
            new ProguardFlags(testFolder, "obf2.flags")),
        false /* non-zipped */);

    File tempOut5 = TestTools.createTempFile("jack", ".dex");
    TestTools.compileJackToDex(new Options(), tempJackFolder, tempOut5, false /* non-zipped */);

  }

  @Test
  public void testPhantomInner() throws Exception {
    File tempJackFolder = TestTools.createTempDir("jack", "dir");
    TestTools.compileSourceToJack(new Options(),
        TestTools.getJackTestsWithJackFolder(TEST001),
        BOOTCLASSPATH, tempJackFolder, false /* non-zipped */);
    boolean deleted = new File(tempJackFolder,
        fixPath("com/android/jack/withphantom/test001/jack/A$Inner1.jack")).delete();
    Assert.assertTrue(deleted);

    File testFolder = TestTools.getJackTestFolder(TEST001);

    File tempOut1 = TestTools.createTempDir("jack", "dir");
    TestTools.shrobJackToJack(new Options(),
        tempJackFolder,
        BOOTCLASSPATH,
        tempOut1,
        Lists.create(
            new ProguardFlags(testFolder, "shrink1.flags")),
        false /* non-zipped */);

    File tempOut2 = TestTools.createTempDir("jack", "dir");
    TestTools.shrobJackToJack(new Options(),
        tempJackFolder,
        BOOTCLASSPATH,
        tempOut2,
        Lists.create(
            new ProguardFlags(testFolder, "shrink2.flags")),
        false /* non-zipped */);

    File tempOut3 = TestTools.createTempDir("jack", "dir");
    TestTools.shrobJackToJack(new Options(),
        tempJackFolder,
        BOOTCLASSPATH,
        tempOut3,
        Lists.create(
            new ProguardFlags(testFolder, "obf1.flags")),
        false /* non-zipped */);

    File tempOut4 = TestTools.createTempDir("jack", "dir");
    TestTools.shrobJackToJack(new Options(),
        tempJackFolder,
        BOOTCLASSPATH,
        tempOut4,
        Lists.create(
            new ProguardFlags(testFolder, "obf2.flags")),
        false /* non-zipped */);

    File tempOut5 = TestTools.createTempFile("jack", ".dex");
    TestTools.compileJackToDex(new Options(), tempJackFolder, tempOut5, false /* non-zipped */);
  }

  @Category(value = KnownBugs.class)
  @Test
  public void testPhantomLocal() throws Exception {
    File tempJackFolder = TestTools.createTempDir("jack", "dir");
    TestTools.compileSourceToJack(new Options(),
        TestTools.getJackTestsWithJackFolder(TEST002),
        BOOTCLASSPATH, tempJackFolder, false /* non-zipped */);
    File[] inners = new File(tempJackFolder,
        fixPath("com/android/jack/withphantom/test002/jack/")).listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.startsWith("A$");
          }
        });
    for (File file : inners) {
      Assert.assertTrue(file.delete());
    }

    File testFolder = TestTools.getJackTestFolder(TEST002);

    File tempOut1 = TestTools.createTempDir("jack", "dir");
    TestTools.shrobJackToJack(new Options(),
        tempJackFolder,
        BOOTCLASSPATH,
        tempOut1,
        Lists.create(
            new ProguardFlags(testFolder, "obf1.flags")),
        false /* non-zipped */);

    File tempOut2 = TestTools.createTempFile("jack", ".dex");
    TestTools.compileJackToDex(new Options(), tempJackFolder, tempOut2, false /* non-zipped */);
  }

  @Category(value = KnownBugs.class)
  @Test
  public void testPhantomLocalOuter() throws Exception {
    File tempJackFolder = TestTools.createTempDir("jack", "dir");
    TestTools.compileSourceToJack(new Options(),
        TestTools.getJackTestsWithJackFolder(TEST002),
        BOOTCLASSPATH, tempJackFolder, false /* non-zipped */);
    boolean deleted = new File(tempJackFolder,
        fixPath("com/android/jack/withphantom/test002/jack/A.jack")).delete();
    Assert.assertTrue(deleted);

    File testFolder = TestTools.getJackTestFolder(TEST002);

    File tempOut1 = TestTools.createTempDir("jack", "dir");
    TestTools.shrobJackToJack(new Options(),
        tempJackFolder,
        BOOTCLASSPATH,
        tempOut1,
        Lists.create(
            new ProguardFlags(testFolder, "obf1.flags")),
        false /* non-zipped */);

    File tempOut2 = TestTools.createTempFile("jack", ".dex");
    TestTools.compileJackToDex(new Options(), tempJackFolder, tempOut2, false /* non-zipped */);
  }

}
