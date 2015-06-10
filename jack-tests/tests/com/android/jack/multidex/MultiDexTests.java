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
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.dex.MultiDexLegacy;
import com.android.jack.comparator.DifferenceFoundException;
import com.android.jack.dx.io.ClassDef;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.preprocessor.PreProcessor;
import com.android.jack.shrob.ListingComparator;
import com.android.jack.test.category.KnownBugs;
import com.android.jack.test.category.SlowTests;
import com.android.jack.test.comparator.Comparator;
import com.android.jack.test.comparator.ComparatorException;
import com.android.jack.test.comparator.ComparatorFile;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.DummyToolchain;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.LegacyJillToolchain;
import com.android.jack.test.toolchain.JackCliToolchain;
import com.android.sched.util.TextUtils;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

public class MultiDexTests {

  private static File annotations;
  private static File frameworks;
  private static File library;

  private class ComparatorMultiDexListing extends ComparatorFile {

    protected ComparatorMultiDexListing(@Nonnull File reference, @Nonnull File candidate) {
      super(reference, candidate);
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

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

    addCommonOptionsForMultiDex(toolchain, new File(testFolder, "config-001.jpp"));
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");
    toolchain.addProperty(Options.USE_DEFAULT_LIBRARIES.getName(), "false");

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(testFolder);

    env.setCandidateTestTools(toolchain);

    List<File> cp = new ArrayList<File>();
    cp.add(annotations);
    cp.addAll(Arrays.asList(toolchain.getDefaultBootClasspath()));
    cp.add(frameworks);
    env.setCandidateClasspath(cp.toArray(new File[cp.size()]));
    env.setReferenceTestTools(new DummyToolchain());

    env.runTest(new ComparatorMultiDexListing(new File(testFolder, "ref-list-001.txt"),
        env.getCandidateDex()));

    Assert.assertFalse(new File(env.getCandidateDexDir(), "classes2.dex").exists());
  }

  @Test
  public void versionedTest001b() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test001.jack");

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    addCommonOptionsForMultiDex(toolchain, new File(testFolder, "config-001.jpp"));
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");
    toolchain.addProperty(Options.USE_DEFAULT_LIBRARIES.getName(), "false");

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(testFolder);
    env.setCandidateTestTools(toolchain);
    List<File> cp = new ArrayList<File>();
    cp.add(annotations);
    cp.addAll(Arrays.asList(toolchain.getDefaultBootClasspath()));
    cp.add(frameworks);
    env.setCandidateClasspath(cp.toArray(new File[cp.size()]));
    env.setReferenceTestTools(new DummyToolchain());

    Comparator c1 = new ComparatorMultiDexListing(new File(testFolder, "ref-list-002-1.txt"),
        env.getCandidateDex());
    Comparator c2 = new ComparatorMultiDexListing(new File(testFolder, "ref-list-002-2.txt"),
        new File(env.getCandidateDexDir(), "classes2.dex"));
    env.runTest(c1, c2);

    Assert.assertFalse(new File(env.getCandidateDexDir(), "classes3.dex").exists());
  }

  @Test
  public void versionedTest001c() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test001.jack");

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    addCommonOptionsForMultiDex(toolchain, new File(testFolder, "config-003.jpp"));
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");
    toolchain.addProperty(Options.USE_DEFAULT_LIBRARIES.getName(), "false");

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(testFolder);
    env.setCandidateTestTools(toolchain);
    List<File> cp = new ArrayList<File>();
    cp.add(annotations);
    cp.addAll(Arrays.asList(toolchain.getDefaultBootClasspath()));
    cp.add(frameworks);
    env.setCandidateClasspath(cp.toArray(new File[cp.size()]));
    env.setReferenceTestTools(new DummyToolchain());

    Comparator c1 = new ComparatorMultiDexListing(new File(testFolder, "ref-list-003-1.txt"),
        env.getCandidateDex());
    Comparator c2 = new ComparatorMultiDexListing(new File(testFolder, "ref-list-003-2.txt"),
        new File(env.getCandidateDexDir(), "classes2.dex"));
    env.runTest(c1, c2);

    Assert.assertFalse(new File(env.getCandidateDexDir(), "classes3.dex").exists());
  }

  @Test
  public void versionedTest001a_withoutAnnotations() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test001.jack");
    File out = AbstractTestTools.createTempDir();
    JackCliToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackCliToolchain.class);
    addCommonOptionsForMultiDex(toolchain, new File(testFolder, "config-001.jpp"));

    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");
    toolchain.addProperty(Options.USE_DEFAULT_LIBRARIES.getName(), "true");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(frameworks)
    .srcToExe(out, /* zipFile = */false, testFolder);

    String outList = getListingOfDex(new File(out, "classes.dex"));
    ListingComparator.compare(new File(testFolder, "ref-list-001.txt"), outList);
    Assert.assertFalse(new File(out, "classes2.dex").exists());
  }

  @Test
  public void versionedTest001b_minimal_withoutAnnotations() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test001.jack");
    File out = AbstractTestTools.createTempDir();
    JackCliToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackCliToolchain.class);
    addCommonOptionsForMultiDex(toolchain, new File(testFolder, "config-001.jpp"));

    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");
    toolchain.addProperty(Options.USE_DEFAULT_LIBRARIES.getName(), "true");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(frameworks)
    .srcToExe(out, /* zipFile = */false, testFolder);

    String outList = getListingOfDex(new File(out, "classes.dex"));
    ListingComparator.compare(new File(testFolder, "ref-list-002-1.txt"), outList);
    String outList2 = getListingOfDex(new File(out, "classes2.dex"));
    ListingComparator.compare(new File(testFolder, "ref-list-002-2.txt"), outList2);
    Assert.assertFalse(new File(out, "classes3.dex").exists());
  }

  @Test
  public void versionedTest001c_withoutAnnotations() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test001.jack");
    File out = AbstractTestTools.createTempDir();
    JackCliToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackCliToolchain.class);
    addCommonOptionsForMultiDex(toolchain, new File(testFolder, "config-003.jpp"));

    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");
    toolchain.addProperty(Options.USE_DEFAULT_LIBRARIES.getName(), "true");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(frameworks)
    .srcToExe(out, /* zipFile = */false, testFolder);

    String outList = getListingOfDex(new File(out, "classes.dex"));
    ListingComparator.compare(new File(testFolder, "ref-list-003-1.txt"), outList);
    String outList2 = getListingOfDex(new File(out, "classes2.dex"));
    ListingComparator.compare(new File(testFolder, "ref-list-003-2.txt"), outList2);
    Assert.assertFalse(new File(out, "classes3.dex").exists());
  }

  private void addCommonOptionsForMultiDex(@Nonnull JackBasedToolchain toolchain,
      @Nonnull File configFile) {
    toolchain.addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
    toolchain.addProperty(PreProcessor.ENABLE.getName(), "true");
    toolchain.addProperty(PreProcessor.FILE.getName(), configFile.getAbsolutePath());
  }

  private String getListingOfDex(@Nonnull File dex) throws IOException {
    assert dex.isFile();
    StringBuilder sb = new StringBuilder();
    for (ClassDef def : new DexBuffer(dex).classDefs()) {
      sb.append(def.getTypeName());
      sb.append(":");
      sb.append(TextUtils.LINE_SEPARATOR);
    }
    return sb.toString();
  }

  @Nonnull
  private static File prepareLib(@Nonnull File sources, @Nonnull File... classpath)
      throws Exception {
    File outDir = AbstractTestTools.createTempDir();
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(LegacyJillToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(classpath)
    .srcToLib(
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
  private static File prepareLibrary(@Nonnull File classpath) throws IOException, Exception {
    return prepareLib(AbstractTestTools.getTestRootDir("com.android.jack.multidex.fakelibrary"),
        classpath);
  }

  @Nonnull
  private static File prepareAutoLibrary() throws Exception {
    File jppDir = AbstractTestTools.getTestRootDir("com.android.jack.multidex.jpp");
    File autoLibrary = AbstractTestTools.createTempDir();
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(LegacyJillToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.addMetaDir(jppDir)
    .addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(frameworks)
    .srcToLib(
        autoLibrary,
        /* zipFiles = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.multidex.fakelibrary"));
    return autoLibrary;
  }

  @Test
  @Category(SlowTests.class)
  public void legacyAppTest002a() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test002.jack");
    File out = AbstractTestTools.createTempDir();

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    addCommonOptionsForMultiDex(toolchain, new File(testFolder, "config-001.jpp"));
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");
    toolchain.addProperty(Options.USE_DEFAULT_LIBRARIES.getName(), "false");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(annotations)
    .addToClasspath(frameworks)
    .addToClasspath(library)
    .srcToExe(
        out,
        /* zipFile = */ false,
        testFolder);

    String outList = getListingOfDex(new File(out, "classes.dex"));
    ListingComparator.compare(
        new File(testFolder,"ref-list-002a-1.txt"), outList);
    String outList2 = getListingOfDex(new File(out, "classes2.dex"));
    ListingComparator.compare(
        new File(testFolder,"ref-list-002a-2.txt"), outList2);
    File classes3Dex = new File(out, "classes3.dex");
    Assert.assertFalse(classes3Dex.exists());
  }

  @Test
  @Category(SlowTests.class)
  public void legacyAppTest002b() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test002.jack");
    File out = AbstractTestTools.createTempDir();

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    addCommonOptionsForMultiDex(toolchain, new File(testFolder, "config-001.jpp"));
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");
    toolchain.addStaticLibs(library);
    toolchain.addProperty(Options.USE_DEFAULT_LIBRARIES.getName(), "false");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(annotations)
    .addToClasspath(frameworks)
    .srcToExe(
        out,
        /* zipFile = */ false,
        testFolder);

    String outList = getListingOfDex(new File(out, "classes.dex"));
    // The old toolchain is doing a little better than us here it seems to identify when
    // InterfaceWithEnum.class instance is used or not.
    ListingComparator.compare(
        new File(testFolder,"ref-list-002b-1.txt"), outList);
    String outList2 = getListingOfDex(new File(out, "classes2.dex"));
    ListingComparator.compare(
        new File(testFolder,"ref-list-002b-2.txt"), outList2);
    Assert.assertFalse(new File(out, "classes3.dex").exists());
  }

  @Test
  @Category(SlowTests.class)
  public void legacyAppTest002b_auto() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test002.jack");
    File autoLibrary = prepareAutoLibrary();

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");
    toolchain.addStaticLibs(autoLibrary);
    toolchain.addProperty(Options.USE_DEFAULT_LIBRARIES.getName(), "false");

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(testFolder);
    env.setCandidateTestTools(toolchain);
    List<File> cp = new ArrayList<File>();
    cp.add(annotations);
    cp.addAll(Arrays.asList(toolchain.getDefaultBootClasspath()));
    cp.add(frameworks);
    env.setCandidateClasspath(cp.toArray(new File[cp.size()]));
    env.setReferenceTestTools(new DummyToolchain());

    Comparator c1 = new ComparatorMultiDexListing(new File(testFolder, "ref-list-002b-1.txt"),
        env.getCandidateDex());
    Comparator c2 = new ComparatorMultiDexListing(new File(testFolder, "ref-list-002b-2.txt"),
        new File(env.getCandidateDexDir(), "classes2.dex"));
    env.runTest(c1, c2);

    Assert.assertFalse(new File(env.getCandidateDexDir(), "classes3.dex").exists());
  }

  @Test
  @Category(SlowTests.class)
  public void legacyAppTest002a_withoutAnnotations() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test002.jack");
    File out = AbstractTestTools.createTempDir();

    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackCliToolchain.class);
    addCommonOptionsForMultiDex(toolchain, new File(testFolder,"config-001.jpp"));

    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");
    toolchain.addProperty(Options.USE_DEFAULT_LIBRARIES.getName(), "true");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(frameworks)
    .addToClasspath(library)
    .srcToExe(out, /* zipFile = */ false, testFolder);

    String outList = getListingOfDex(new File(out, "classes.dex"));
    ListingComparator.compare(
        new File(testFolder,"ref-list-002a-1.txt"), outList);
    String outList2 = getListingOfDex(new File(out, "classes2.dex"));
    ListingComparator.compare(
        new File(testFolder,"ref-list-002a-2.txt"), outList2);
    File classes3Dex = new File(out, "classes3.dex");
    Assert.assertFalse(classes3Dex.exists());
  }

  @Test
  @Category(SlowTests.class)
  public void legacyAppTest002b_withoutAnnotations() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test002.jack");
    File out = AbstractTestTools.createTempDir();

    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackCliToolchain.class);
    addCommonOptionsForMultiDex(toolchain, new File(testFolder,"config-001.jpp"));

    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");
    toolchain.addStaticLibs(library);
    toolchain.addProperty(Options.USE_DEFAULT_LIBRARIES.getName(), "true");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(frameworks)
    .srcToExe(out, /* zipFile = */ false, testFolder);

    String outList = getListingOfDex(new File(out, "classes.dex"));
    // The old toolchain is doing a little better than us here it seems to identify when
    // InterfaceWithEnum.class instance is used or not.
    ListingComparator.compare(
        new File(testFolder,"ref-list-002b-1.txt"), outList);
    String outList2 = getListingOfDex(new File(out, "classes2.dex"));
    ListingComparator.compare(
        new File(testFolder,"ref-list-002b-2.txt"), outList2);
    Assert.assertFalse(new File(out, "classes3.dex").exists());
  }

  @Test
  public void legacyAppTest002b_auto_withoutAnnotations() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test002.jack");
    File autoLibrary = prepareAutoLibrary();

    File out = AbstractTestTools.createTempDir();
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(LegacyJillToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackCliToolchain.class, exclude);
    toolchain.addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");
    toolchain.addStaticLibs(autoLibrary);
    toolchain.addProperty(Options.USE_DEFAULT_LIBRARIES.getName(), "true");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(frameworks)
    .srcToExe(out, /* zipFile = */ false, testFolder);

    String outList = getListingOfDex(new File(out, "classes.dex"));
    // The old toolchain is doing a little better than us here it seems to identify when
    // InterfaceWithEnum.class instance is used or not.
    ListingComparator.compare(
        new File(testFolder,"ref-list-002b-1.txt"), outList);
    String outList2 = getListingOfDex(new File(out, "classes2.dex"));
    ListingComparator.compare(
        new File(testFolder,"ref-list-002b-2.txt"), outList2);
    Assert.assertFalse(new File(out, "classes3.dex").exists());
  }

  /**
   * Verifies that classes annotated with runtime visible annotations are put in main dex.
   */
  @Test
  public void annotatedTest003() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.multidex.test003.jack");
    File out = AbstractTestTools.createTempDir();
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");
    toolchain.addProperty(Options.USE_DEFAULT_LIBRARIES.getName(), "false");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(annotations)
    .addToClasspath(frameworks)
    .srcToExe(out, /* zipFile = */ false, testFolder);

    String outList = getListingOfDex(new File(out, "classes.dex"));
    ListingComparator.compare(
        new File(testFolder,"ref-list-003-1.txt"), outList);
    String outList2 = getListingOfDex(new File(out, "classes2.dex"));
    ListingComparator.compare(
        new File(testFolder,"ref-list-003-2.txt"), outList2);
    Assert.assertFalse(new File(out, "classes3.dex").exists());
  }
}
