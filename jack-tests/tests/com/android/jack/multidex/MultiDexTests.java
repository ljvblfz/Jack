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

import com.google.common.io.Files;

import com.android.jack.DifferenceFoundException;
import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.dex.MultiDexLegacy;
import com.android.jack.library.FileType;
import com.android.jack.preprocessor.PreProcessor;
import com.android.jack.shrob.ListingComparator;
import com.android.jack.test.comparator.Comparator;
import com.android.jack.test.comparator.ComparatorException;
import com.android.jack.test.comparator.ComparatorFile;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.DummyToolchain;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiToolchain;
import com.android.jack.util.ExecuteFile;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nonnull;

public class MultiDexTests {

  private static File annotations;
  private static File frameworks;
  private static File library;

  private class ComparatorMultiDexListing extends ComparatorFile {

    protected ComparatorMultiDexListing(File candidate, File reference) {
      super(candidate, reference);
    }

    @Override
    public void compare() throws DifferenceFoundException, ComparatorException {
      try {
        ListingComparator.compare(reference, getListingOfDex(candidate));
      } catch (IOException e) {
        throw new ComparatorException(e);
      }
    }
  }

  @BeforeClass
  public static void init() throws IOException, Exception {
    MultiDexTests.class.getClassLoader().setDefaultAssertionStatus(true);

    annotations = prepareAnnotations();

    frameworks = prepareFrameworks();

    library = prepareLibrary(frameworks);
  }

  @Test
  public void versionedTest001a() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test001.jack");

    JackApiToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);

    addCommonOptionsForMultiDex(toolchain, new File(testFolder, "config-001.jpp"));
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(testFolder);

    env.setCandidateTestTools(toolchain);

    List<File> cp = new ArrayList<File>();
    cp.addAll(Arrays.asList(toolchain.getDefaultBootClasspath()));
    cp.add(annotations);
    cp.add(frameworks);
    env.setCandidateClasspath(cp.toArray(new File[cp.size()]));
    env.setReferenceTestTools(new DummyToolchain());

    env.runTest(new ComparatorMultiDexListing(env.getCandidateDex(), new File(testFolder, "ref-list-001.txt")));

    Assert.assertFalse(new File(env.getCandidateDexDir(), "classes2.dex").exists());
  }

  @Test
  public void versionedTest001b() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test001.jack");

    JackApiToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    addCommonOptionsForMultiDex(toolchain, new File(testFolder, "config-001.jpp"));
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(testFolder);
    env.setCandidateTestTools(toolchain);
    List<File> cp = new ArrayList<File>();
    cp.addAll(Arrays.asList(toolchain.getDefaultBootClasspath()));
    cp.add(annotations);
    cp.add(frameworks);
    env.setCandidateClasspath(cp.toArray(new File[cp.size()]));
    env.setReferenceTestTools(new DummyToolchain());

    Comparator c1 = new ComparatorMultiDexListing(env.getCandidateDex(), new File(testFolder, "ref-list-002-1.txt"));
    Comparator c2 = new ComparatorMultiDexListing(new File(env.getCandidateDexDir(), "classes2.dex"), new File(testFolder, "ref-list-002-2.txt"));
    env.runTest(c1, c2);

    Assert.assertFalse(new File(env.getCandidateDexDir(), "classes3.dex").exists());
  }

  @Test
  public void versionedTest001c() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test001.jack");

    JackApiToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    addCommonOptionsForMultiDex(toolchain, new File(testFolder, "config-003.jpp"));
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(testFolder);
    env.setCandidateTestTools(toolchain);
    List<File> cp = new ArrayList<File>();
    cp.addAll(Arrays.asList(toolchain.getDefaultBootClasspath()));
    cp.add(annotations);
    cp.add(frameworks);
    env.setCandidateClasspath(cp.toArray(new File[cp.size()]));
    env.setReferenceTestTools(new DummyToolchain());

    Comparator c1 = new ComparatorMultiDexListing(env.getCandidateDex(), new File(testFolder, "ref-list-003-1.txt"));
    Comparator c2 = new ComparatorMultiDexListing(new File(env.getCandidateDexDir(), "classes2.dex"), new File(testFolder, "ref-list-003-2.txt"));
    env.runTest(c1, c2);

    Assert.assertFalse(new File(env.getCandidateDexDir(), "classes3.dex").exists());
  }

  @Test
  public void versionedTest001a_withoutAnnotations() throws Exception {

    File testFolder = TestTools.getJackTestsWithJackFolder("multidex/test001");
    File out = TestTools.createTempDir("out", "");
    Options app1Options = addCommonOptionsForMultiDex(new File(testFolder, "config-001.jpp"));

    app1Options.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");

    TestTools.compileSourceToDex(app1Options, testFolder, TestTools.getDefaultClasspathString()
        + File.pathSeparator + frameworks.getPath(),
        out, false);

    File outList = getListingOfDex(new File(out, "classes.dex"));
    ListingComparator.compare(new File(testFolder, "ref-list-001.txt"), outList);
    Assert.assertFalse(new File(out, "classes2.dex").exists());
    return;
  }

  @Test
  public void versionedTest001b__withoutAnnotations() throws Exception {

    File testFolder = TestTools.getJackTestsWithJackFolder("multidex/test001");
    File out = TestTools.createTempDir("out", "");
    Options app1Options = addCommonOptionsForMultiDex(new File(testFolder, "config-001.jpp"));

    app1Options.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");

    TestTools.compileSourceToDex(app1Options, testFolder, TestTools.getDefaultClasspathString()
        + File.pathSeparator + frameworks.getPath(),
        out, false);

    File outList = getListingOfDex(new File(out, "classes.dex"));
    ListingComparator.compare(new File(testFolder, "ref-list-002-1.txt"), outList);
    File outList2 = getListingOfDex(new File(out, "classes2.dex"));
    ListingComparator.compare(new File(testFolder, "ref-list-002-2.txt"), outList2);
    Assert.assertFalse(new File(out, "classes3.dex").exists());
    return;
  }

  @Test
  public void versionedTest001c_withoutAnnotations() throws Exception {

    File testFolder = TestTools.getJackTestsWithJackFolder("multidex/test001");
    File out = TestTools.createTempDir("out", "");
    Options app1Options = addCommonOptionsForMultiDex(new File(testFolder, "config-003.jpp"));

    app1Options.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");

    TestTools.compileSourceToDex(app1Options, testFolder, TestTools.getDefaultClasspathString()
        + File.pathSeparator + frameworks.getPath(),
        out, false);

    File outList = getListingOfDex(new File(out, "classes.dex"));
    ListingComparator.compare(new File(testFolder, "ref-list-003-1.txt"), outList);
    File outList2 = getListingOfDex(new File(out, "classes2.dex"));
    ListingComparator.compare(new File(testFolder, "ref-list-003-2.txt"), outList2);
    Assert.assertFalse(new File(out, "classes3.dex").exists());
    return;
  }

  private Options addCommonOptionsForMultiDex(@Nonnull File configFile) {
    Options app1Options = new Options();
    app1Options.addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
    app1Options.addProperty(PreProcessor.ENABLE.getName(), "true");
    app1Options.addProperty(PreProcessor.FILE.getName(), configFile.getAbsolutePath());
    return app1Options;
  }

  private void addCommonOptionsForMultiDex(@Nonnull JackApiToolchain toolchain,
      @Nonnull File configFile) {
    toolchain.addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
    toolchain.addProperty(PreProcessor.ENABLE.getName(), "true");
    toolchain.addProperty(PreProcessor.FILE.getName(), configFile.getAbsolutePath());
  }

  private File getListingOfDex(@Nonnull File dex) throws IOException, FileNotFoundException {
    assert dex.isFile();
    ExecuteFile exec =
        new ExecuteFile(new String[]{
            "bash", "-c", AbstractTestTools.getPrebuilt("dexdump").getAbsolutePath() + " "
        + dex.getAbsolutePath() +
        " | grep \"  Class descriptor  : \" | cut -d\\' -f2 | sed -e 's/$/:/'"});

    File outList = TestTools.createTempFile("types", ".txt");

    exec.setOut(outList);
    Assert.assertTrue(exec.run());
    return outList;
  }

  private int getTypeCountInDex(@Nonnull File dex) throws IOException, FileNotFoundException {
    assert dex.isFile();
    ExecuteFile exec =
        new ExecuteFile(new String[]{
            "bash", "-c", AbstractTestTools.getPrebuilt("dexdump").getAbsolutePath() + " "
        + dex.getAbsolutePath() +
        " | grep \"  Class descriptor  : \" | wc -l"});

    File out = TestTools.createTempFile("typeNumber", ".txt");

    exec.setOut(out);
    Assert.assertTrue(exec.run());
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(out)));
    try {
      String readLine = reader.readLine();
      assert readLine != null;
      return Integer.parseInt(readLine.trim());
    } finally {
      reader.close();
    }
  }

  @Nonnull
  private static File prepareLib(@Nonnull File sources, @Nonnull File... classpath) throws Exception {
    File outDir = AbstractTestTools.createTempDir();
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.srcToLib(
        AbstractTestTools.getClasspathsAsString(toolchain.getDefaultBootClasspath(), classpath),
        outDir,
        /* zipFiles = */ false,
        sources);
    return outDir;
  }

  @Nonnull
  private static File prepareFrameworks() throws IOException, Exception {
    return prepareLib(AbstractTestTools.getTestRootDir("com.android.jack.multidex.fakeframeworks"));
  }

  @Nonnull
  protected static File prepareAnnotations() throws IOException, Exception {
    return prepareLib(AbstractTestTools.getTestRootDir("com.android.jack.annotations"));
  }

  @Nonnull
  private static File prepareLibrary(@Nonnull File frameworks) throws IOException, Exception {
    return prepareLib(AbstractTestTools.getTestRootDir("com.android.jack.multidex.fakelibrary"),
        frameworks);
  }

  private static void setMetaIntoJackProperties(@Nonnull File library) throws IOException {
    File jackProperties = new File(library, "jack.properties");
    Properties libraryProperties = new Properties();
    FileInputStream fis = null;
    FileOutputStream fos = null;
    try {
      fis = new FileInputStream(jackProperties);
      libraryProperties.load(fis);
    } catch (IOException e) {
      Assert.fail();
    } finally {
      if (fis != null) {
        fis.close();
      }
    }
    try {
      fos = new FileOutputStream(jackProperties);
      libraryProperties.put(FileType.JPP.buildPropertyName(null /*suffix*/), "true");
        libraryProperties.store(fos, "Library properties");
    } catch (IOException e) {
      Assert.fail();
    } finally {
      if (fos != null) {
        fos.close();
      }
    }
  }

  @Test
  public void legacyAppTest002a() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test002.jack");
    File out = AbstractTestTools.createTempDir();

    JackApiToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    addCommonOptionsForMultiDex(toolchain, new File(testFolder, "config-001.jpp"));
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");

    toolchain.srcToExe(
        AbstractTestTools.getClasspathsAsString(toolchain.getDefaultBootClasspath(), new File [] {annotations, frameworks, library}),
        out,
        /* zipFile = */ false,
        testFolder);

    File classesDex = new File(out, "classes.dex");
    Assert.assertTrue(classesDex.exists());
    File classes2Dex = new File(out, "classes2.dex");
    Assert.assertTrue(classes2Dex.exists());
    File classes3Dex = new File(out, "classes3.dex");
    Assert.assertFalse(classes3Dex.exists());
    int totalTypeNumber = getTypeCountInDex(classesDex) + getTypeCountInDex(classes2Dex);
    Assert.assertEquals(100, totalTypeNumber);
    return;
  }

  @Test
  public void legacyAppTest002b() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test002.jack");
    File out = AbstractTestTools.createTempDir();

    JackApiToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    addCommonOptionsForMultiDex(toolchain, new File(testFolder, "config-001.jpp"));
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");
    toolchain.addStaticLibs(library);

    toolchain.srcToExe(
        AbstractTestTools.getClasspathsAsString(toolchain.getDefaultBootClasspath(), new File [] {annotations, frameworks}),
        out,
        /* zipFile = */ false,
        testFolder);

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

  @Test
  public void legacyAppTest002b_auto() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test002.jack");
    File autoLibrary = prepareLibrary(frameworks);
    setMetaIntoJackProperties(autoLibrary);
    File jackInf = new File(autoLibrary, FileType.JPP.getPrefix());
    Assert.assertTrue(jackInf.mkdir());
    Files.copy(new File(testFolder,"config-001.jpp"), new File(jackInf, "config-001.jpp"));

    JackApiToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    toolchain.addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");
    toolchain.addStaticLibs(autoLibrary);

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(testFolder);
    env.setCandidateTestTools(toolchain);
    List<File> cp = new ArrayList<File>();
    cp.addAll(Arrays.asList(toolchain.getDefaultBootClasspath()));
    cp.add(annotations);
    cp.add(frameworks);
    env.setCandidateClasspath(cp.toArray(new File[cp.size()]));
    env.setReferenceTestTools(new DummyToolchain());

    Comparator c1 = new ComparatorMultiDexListing(env.getCandidateDex(), new File(testFolder, "ref-list-002-1.txt"));
    Comparator c2 = new ComparatorMultiDexListing(new File(env.getCandidateDexDir(), "classes2.dex"), new File(testFolder, "ref-list-002-2.txt"));
    env.runTest(c1, c2);

    Assert.assertFalse(new File(env.getCandidateDexDir(), "classes3.dex").exists());
  }

  @Test
  public void legacyAppTest002a_withoutAnnotations() throws Exception {

    File testFolder = TestTools.getJackTestsWithJackFolder("multidex/test002");
    File out = TestTools.createTempDir("out", "");
    Options app1Options = addCommonOptionsForMultiDex(
        new File(testFolder,"config-001.jpp"));
    app1Options.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");

    TestTools.compileSourceToDex(app1Options, testFolder, TestTools.getDefaultClasspathString()
        + File.pathSeparator + frameworks.getPath()
        + File.pathSeparator + library.getPath(), out, false);

    File classesDex = new File(out, "classes.dex");
    Assert.assertTrue(classesDex.exists());
    File classes2Dex = new File(out, "classes2.dex");
    Assert.assertTrue(classes2Dex.exists());
    File classes3Dex = new File(out, "classes3.dex");
    Assert.assertFalse(classes3Dex.exists());
    int totalTypeNumber = getTypeCountInDex(classesDex) + getTypeCountInDex(classes2Dex);
    Assert.assertEquals(100, totalTypeNumber);
    return;
  }

  @Test
  public void legacyAppTest002b_withoutAnnotations() throws Exception {

    File testFolder = TestTools.getJackTestsWithJackFolder("multidex/test002");
    File out = TestTools.createTempDir("out", "");
    Options app1Options = addCommonOptionsForMultiDex(
        new File(testFolder,"config-001.jpp"));
    app1Options.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");
    app1Options.addJayceImport(library);

    TestTools.compileSourceToDex(app1Options, testFolder, TestTools.getDefaultClasspathString()
        + File.pathSeparator + frameworks.getPath(),
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

  @Test
  public void legacyAppTest002b_auto_withoutAnnotations() throws Exception {

    File testFolder = TestTools.getJackTestsWithJackFolder("multidex/test002");
    File autoLibrary = prepareLibrary(frameworks);
    setMetaIntoJackProperties(autoLibrary);
    File jackInf = new File(autoLibrary, FileType.JPP.getPrefix());
    Assert.assertTrue(jackInf.mkdir());
    Files.copy(new File(testFolder,"config-001.jpp"), new File(jackInf, "config-001.jpp"));

    File out = TestTools.createTempDir("out", "");
    Options app1Options = new Options();
    app1Options.addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
    app1Options.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");
    app1Options.addJayceImport(autoLibrary);

    TestTools.compileSourceToDex(app1Options, testFolder, TestTools.getDefaultClasspathString()
        + File.pathSeparator + frameworks.getPath(),
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

  /**
   * Verifies that classes annotated with runtime visible annotations are put in main dex.
   */
  @Test
  public void legacyAppTest003() throws Exception {

    File testFolder = TestTools.getJackTestsWithJackFolder("multidex/test003");

    File out = TestTools.createTempDir("out", "");
    Options appOptions = new Options();
    appOptions.addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
    appOptions.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");

    TestTools.compileSourceToDex(appOptions, testFolder, TestTools.getDefaultClasspathString()
        + File.pathSeparator + annotations.getPath() + File.pathSeparator + frameworks.getPath(),
        out, false);

    File outList = getListingOfDex(new File(out, "classes.dex"));
    ListingComparator.compare(
        new File(testFolder,"ref-list-003-1.txt"), outList);
    File outList2 = getListingOfDex(new File(out, "classes2.dex"));
    ListingComparator.compare(
        new File(testFolder,"ref-list-003-2.txt"), outList2);
    Assert.assertFalse(new File(out, "classes3.dex").exists());
    return;
  }
}
