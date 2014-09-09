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

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.dex.MultiDexLegacy;
import com.android.jack.preprocessor.PreProcessor;
import com.android.jack.shrob.ListingComparator;
import com.android.jack.util.ExecuteFile;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.Nonnull;

public class MultiDexTests {

  private static File annotations;
  private static File frameworks;
  private static File library;

  @BeforeClass
  public static void init() throws IOException, Exception {
    annotations = prepareAnnotations();

    frameworks = prepareFrameworks();

    library = prepareLibrary(frameworks);
  }

  @Test
  public void versionedTest001a() throws Exception {

    File testFolder = TestTools.getJackTestsWithJackFolder("multidex/test001");
    File out = TestTools.createTempDir("out", "");
    Options app1Options = createCommonOptionsForMultiDex(new File(testFolder, "config-001.jpp"));

    app1Options.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");

    TestTools.compileSourceToDex(app1Options, testFolder, TestTools.getDefaultBootclasspathString()
        + File.pathSeparator + annotations.getPath() + File.pathSeparator + frameworks.getPath(),
        out, false);

    File outList = getListingOfDex(new File(out, "classes.dex"));
    ListingComparator.compare(new File(testFolder, "ref-list-001.txt"), outList);
    Assert.assertFalse(new File(out, "classes2.dex").exists());
    return;
  }

  @Test
  public void versionedTest001b() throws Exception {

    File testFolder = TestTools.getJackTestsWithJackFolder("multidex/test001");
    File out = TestTools.createTempDir("out", "");
    Options app1Options = createCommonOptionsForMultiDex(new File(testFolder, "config-001.jpp"));

    app1Options.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");

    TestTools.compileSourceToDex(app1Options, testFolder, TestTools.getDefaultBootclasspathString()
        + File.pathSeparator + annotations.getPath() + File.pathSeparator + frameworks.getPath(),
        out, false);

    File outList = getListingOfDex(new File(out, "classes.dex"));
    ListingComparator.compare(new File(testFolder, "ref-list-002-1.txt"), outList);
    File outList2 = getListingOfDex(new File(out, "classes2.dex"));
    ListingComparator.compare(new File(testFolder, "ref-list-002-2.txt"), outList2);
    Assert.assertFalse(new File(out, "classes3.dex").exists());
    return;
  }

  @Test
  public void versionedTest001c() throws Exception {

    File testFolder = TestTools.getJackTestsWithJackFolder("multidex/test001");
    File out = TestTools.createTempDir("out", "");
    Options app1Options = createCommonOptionsForMultiDex(new File(testFolder, "config-003.jpp"));

    app1Options.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");

    TestTools.compileSourceToDex(app1Options, testFolder, TestTools.getDefaultBootclasspathString()
        + File.pathSeparator + annotations.getPath() + File.pathSeparator + frameworks.getPath(),
        out, false);

    File outList = getListingOfDex(new File(out, "classes.dex"));
    ListingComparator.compare(new File(testFolder, "ref-list-003-1.txt"), outList);
    File outList2 = getListingOfDex(new File(out, "classes2.dex"));
    ListingComparator.compare(new File(testFolder, "ref-list-003-2.txt"), outList2);
    Assert.assertFalse(new File(out, "classes3.dex").exists());
    return;
  }

  private Options createCommonOptionsForMultiDex(@Nonnull File configFile) throws IOException {
    File tmpOut = TestTools.createTempDir("tmp", "");
    Options app1Options = new Options();
    app1Options.addProperty(Options.TYPEDEX_DIR.getName(), tmpOut.getPath());
    app1Options.addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
    app1Options.addProperty(PreProcessor.ENABLE.getName(), "true");
    app1Options.addProperty(PreProcessor.FILE.getName(), configFile.getAbsolutePath());
    return app1Options;
  }

  private File getListingOfDex(File out) throws IOException, FileNotFoundException {
    ExecuteFile exec =
        new ExecuteFile(new String[]{
            "bash", "-c", "dexdump "
        + out.getAbsolutePath() +
        " | grep \"  Class descriptor  : \" | cut -d\\' -f2 | sed -e 's/$/:/'"});

    File outList = TestTools.createTempFile("types", ".txt");

    exec.setOut(outList);
    Assert.assertTrue(exec.run());
    return outList;
  }

  @Nonnull
  private static File prepareFrameworks() throws IOException, Exception {
    File frameworks = TestTools.createTempDir("frameworks", "");
    TestTools.compileSourceToJack(new Options(),
        TestTools.getJackTestFolder("multidex/fakeframeworks"),
        TestTools.getDefaultBootclasspathString(), frameworks, false);
    return frameworks;
  }

  @Nonnull
  private static File prepareAnnotations() throws IOException, Exception {
    File annotations = TestTools.createTempDir("multidexAnnotations", "");
    TestTools.compileSourceToJack(new Options(),
        TestTools.getFromAndroidTree(
            "toolchain/jack/jack-tests/tests/com/android/jack/annotations/"),
            TestTools.getDefaultBootclasspathString(), annotations, false);
    return annotations;
  }

  @Nonnull
  private static File prepareLibrary(@Nonnull File frameworks) throws IOException, Exception {
    File library = TestTools.createTempDir("multidexLibrary", "");
    TestTools.compileSourceToJack(new Options(),
        TestTools.getJackTestFolder("multidex/fakelibrary"),
        TestTools.getDefaultBootclasspathString() + File.pathSeparator + frameworks.getPath(),
        library, false);
    return library;
  }

  @Test
  public void legacyAppTest002a() throws Exception {

    File testFolder = TestTools.getJackTestsWithJackFolder("multidex/test002");
    File out = TestTools.createTempDir("out", "");
    Options app1Options = createCommonOptionsForMultiDex(
        new File(testFolder,"config-001.jpp"));
    app1Options.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");

    TestTools.compileSourceToDex(app1Options, testFolder, TestTools.getDefaultBootclasspathString()
        + File.pathSeparator + annotations.getPath() + File.pathSeparator + frameworks.getPath()
        + File.pathSeparator + library.getPath(), out, false);

    Assert.assertTrue(new File(out, "classes.dex").exists());
    Assert.assertTrue(new File(out, "classes2.dex").exists());
    Assert.assertFalse(new File(out, "classes3.dex").exists());
    return;
  }

  @Test
  public void legacyAppTest002b() throws Exception {

    File testFolder = TestTools.getJackTestsWithJackFolder("multidex/test002");
    File out = TestTools.createTempDir("out", "");
    Options app1Options = createCommonOptionsForMultiDex(
        new File(testFolder,"config-001.jpp"));
    app1Options.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");
    app1Options.addJayceImport(library);

    TestTools.compileSourceToDex(app1Options, testFolder, TestTools.getDefaultBootclasspathString()
        + File.pathSeparator + annotations.getPath() + File.pathSeparator + frameworks.getPath(),
        out, false);

    File outList = getListingOfDex(new File(out, "classes.dex"));
    // The old toolchain is doing a little better than us here it seems to identify when
    // InterfaceWithEnum.class instance is used or not.
    ListingComparator.compare(
        new File(testFolder,"ref-list-002-1.txt"), outList);
    File outList2 = getListingOfDex(new File(out, "classes2.dex"));
    ListingComparator.compare(
        new File(testFolder,"ref-list-002-2.txt"), outList2);
    Assert.assertFalse(new File(out, "classes3.dex").exists());
    return;
  }

}
