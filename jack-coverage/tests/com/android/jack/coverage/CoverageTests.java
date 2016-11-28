/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.coverage;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.util.NamingTools;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

public class CoverageTests extends CoverageTest {

  @Test
  public void testSingleClass() throws Exception {
    final String testPackageName = getTestPackageName("test001");
    JackBasedToolchain toolchain = createJackToolchain();
    File coverageMetadataFile = CoverageToolchainBuilder.create(toolchain).build();

    toolchain.srcToExe(AbstractTestTools.createTempDir(), false,
        getTestRootDir(testPackageName));
    JsonArray classes = loadJsonCoverageClasses(coverageMetadataFile);

    // We expect only one class
    Assert.assertNotNull(classes);
    Assert.assertEquals(1, classes.size());
    JsonObject testClass = classes.get(0).getAsJsonObject();
    Assert.assertNotNull(testClass);
    Assert.assertEquals(getClassNameForJson(testPackageName + ".AbstractClass"),
        getClassName(testClass));

    // Check its methods.
    JsonArray methods = testClass.get("methods").getAsJsonArray();
    Assert.assertEquals(3, methods.size()); // abstract and native methods are excluded.

    class NameAndDesc {
      @Nonnull
      public final String name;
      @Nonnull
      public final String desc;

      public NameAndDesc(@Nonnull String name, @Nonnull String desc) {
        this.name = name;
        this.desc = desc;
      }

      @Override
      public boolean equals(Object obj) {
        if (obj instanceof NameAndDesc) {
          NameAndDesc other = (NameAndDesc) obj;
          return name.equals(other.name) && desc.equals(other.desc);
        } else {
          return false;
        }
      }

      @Override
      public int hashCode() {
        String concat = name + desc;
        return concat.hashCode();
      }
    }
    Set<NameAndDesc> expectedMethods = new HashSet<NameAndDesc>();
    expectedMethods.add(new NameAndDesc(NamingTools.INIT_NAME, "()V"));
    expectedMethods.add(new NameAndDesc("staticMethod", "()V"));
    expectedMethods.add(new NameAndDesc("instanceMethod", "()V"));
    for (JsonElement methodElt : methods) {
      String name = methodElt.getAsJsonObject().get("name").getAsString();
      String desc = methodElt.getAsJsonObject().get("desc").getAsString();
      Assert.assertTrue(expectedMethods.contains(new NameAndDesc(name, desc)));
    }
  }

  @Test
  public void testSingleInterface() throws Exception {
    JackBasedToolchain toolchain = createJackToolchain();
    File coverageMetadataFile = CoverageToolchainBuilder.create(toolchain).build();

    toolchain.srcToExe(AbstractTestTools.createTempDir(), false,
        getTestRootDir(getTestPackageName("test002")));

    // Interface must be skipped
    JsonArray classes = loadJsonCoverageClasses(coverageMetadataFile);
    Assert.assertEquals(0, classes.size());
  }

  @Test
  public void testIncludeAll() throws Exception {
    String testPackageName = getTestPackageName("test003");
    JackBasedToolchain toolchain = createJackToolchain();
    File coverageMetadataFile = CoverageToolchainBuilder.create(toolchain)
        .setIncludeFilter("*")
        .build();

    toolchain.srcToExe(AbstractTestTools.createTempDir(), false,
        getTestRootDir(testPackageName));
    JsonArray classes = loadJsonCoverageClasses(coverageMetadataFile);

    // Full coverage: Foo and Bar classes.
    Assert.assertEquals(2, classes.size());
    Collection<? extends String> actualClassNames = collectClassNames(classes);
    Set<String> expectedClassNames =
        Sets.newHashSet(getClassNameForJson(testPackageName + ".foo.Foo"),
            getClassNameForJson(testPackageName + ".foo.bar.Bar"));
    Assert.assertTrue(actualClassNames.containsAll(expectedClassNames));
  }

  @Test
  public void testIncludeSingle() throws Exception {
    String testPackageName = getTestPackageName("test003");
    JackBasedToolchain toolchain = createJackToolchain();
    File coverageMetadataFile = CoverageToolchainBuilder.create(toolchain)
        .setIncludeFilter(testPackageName + ".foo.bar.Bar")
        .build();

    toolchain.srcToExe(AbstractTestTools.createTempDir(), false,
        getTestRootDir(testPackageName));
    JsonArray classes = loadJsonCoverageClasses(coverageMetadataFile);

    // Partial coverage: only Bar class.
    Assert.assertEquals(1, classes.size());
    Collection<? extends String> actualClassNames = collectClassNames(classes);
    Set<String> expectedClassNames =
        Sets.newHashSet(getClassNameForJson(testPackageName + ".foo.bar.Bar"));
    Assert.assertTrue(actualClassNames.containsAll(expectedClassNames));
  }

  @Test
  public void testIncludeWildcard() throws Exception {
    String testPackageName = getTestPackageName("test003");
    JackBasedToolchain toolchain = createJackToolchain();
    File coverageMetadataFile = CoverageToolchainBuilder.create(toolchain)
        .setIncludeFilter(testPackageName + ".foo.bar.*")
        .build();

    toolchain.srcToExe(AbstractTestTools.createTempDir(), false,
        getTestRootDir(testPackageName));
    JsonArray classes = loadJsonCoverageClasses(coverageMetadataFile);

    // Partial coverage: only Bar class.
    Assert.assertEquals(1, classes.size());
    Collection<? extends String> actualClassNames = collectClassNames(classes);
    Set<String> expectedClassNames =
        Sets.newHashSet(getClassNameForJson(testPackageName + ".foo.bar.Bar"));
    Assert.assertTrue(actualClassNames.containsAll(expectedClassNames));
  }

  @Test
  public void testExcludeAll() throws Exception {
    JackBasedToolchain toolchain = createJackToolchain();
    File coverageMetadataFile = CoverageToolchainBuilder.create(toolchain)
        .setExcludeFilter("*")
        .build();

    toolchain.srcToExe(AbstractTestTools.createTempDir(), false,
        getTestRootDir(getTestPackageName("test003")));
    JsonArray classes = loadJsonCoverageClasses(coverageMetadataFile);

    // No coverage at all
    Assert.assertEquals(0, classes.size());
  }

  @Test
  public void testExcludeSingle() throws Exception {
    String testPackageName = getTestPackageName("test003");
    JackBasedToolchain toolchain = createJackToolchain();
    File coverageMetadataFile = CoverageToolchainBuilder.create(toolchain)
        .setExcludeFilter(testPackageName + ".foo.bar.Bar")
        .build();

    toolchain.srcToExe(AbstractTestTools.createTempDir(), false,
        getTestRootDir(testPackageName));
    JsonArray classes = loadJsonCoverageClasses(coverageMetadataFile);

    // Partial coverage: only Foo class.
    Assert.assertEquals(1, classes.size());
    Collection<? extends String> actualClassNames = collectClassNames(classes);
    Set<String> expectedClassNames =
        Sets.newHashSet(getClassNameForJson(testPackageName + ".foo.Foo"));
    Assert.assertTrue(actualClassNames.containsAll(expectedClassNames));
  }

  @Test
  public void testExcludeWildcard() throws Exception {
    String testPackageName = getTestPackageName("test003");
    JackBasedToolchain toolchain = createJackToolchain();
    File coverageMetadataFile = CoverageToolchainBuilder.create(toolchain)
        .setExcludeFilter(testPackageName + ".foo.bar.*")
        .build();

    toolchain.srcToExe(AbstractTestTools.createTempDir(), false,
        getTestRootDir(testPackageName));
    JsonArray classes = loadJsonCoverageClasses(coverageMetadataFile);

    // Partial coverage: only Foo class.
    Assert.assertEquals(1, classes.size());
    Collection<? extends String> actualClassNames = collectClassNames(classes);
    Set<String> expectedClassNames =
        Sets.newHashSet(getClassNameForJson(testPackageName + ".foo.Foo"));
    Assert.assertTrue(actualClassNames.containsAll(expectedClassNames));
  }

  @Test
  public void testIncludeExclude() throws Exception {
    String testPackageName = getTestPackageName("test003");
    JackBasedToolchain toolchain = createJackToolchain();
    File coverageMetadataFile = CoverageToolchainBuilder.create(toolchain)
        .setIncludeFilter(testPackageName + ".foo.bar.*")
        .setExcludeFilter(testPackageName + ".foo.*")
        .build();

    toolchain.srcToExe(AbstractTestTools.createTempDir(), false,
        getTestRootDir(testPackageName));
    JsonArray classes = loadJsonCoverageClasses(coverageMetadataFile);

    // No coverage at all
    Assert.assertEquals(0, classes.size());
  }

  @Test
  public void testImportLibrary_NoPredexing() throws Exception {
    runTestImportLibrary(false);
  }

  @Test
  public void testImportLibrary_WithPredexing() throws Exception {
    runTestImportLibrary(true);
  }

  private void runTestImportLibrary(boolean withPredexing) throws Exception {
    String testPackageName = getTestPackageName("test004");

    // 1 - Create a lib (with or without predexing)
    JackBasedToolchain toolchain = createJackToolchain();
    toolchain.addProperty("jack.library.dex", Boolean.toString(withPredexing));
    File libDir = AbstractTestTools.createTempFile("lib", toolchain.getLibraryExtension());
    File libSrcFiles = new File(getTestRootDir(testPackageName), "lib");
    toolchain.srcToLib(libDir, true, libSrcFiles);

    // 2 - Compile the lib with coverage.
    toolchain = createJackToolchain();
    toolchain.addStaticLibs(libDir);
    File coverageMetadataFile = CoverageToolchainBuilder.create(toolchain).build();
    File srcFiles = new File(getTestRootDir(testPackageName), "src");
    File outDexFolder = AbstractTestTools.createTempDir();
    toolchain.srcToExe(outDexFolder, false, srcFiles);

    // 3 - Check types from the lib are instrumented.
    JsonArray classes = loadJsonCoverageClasses(coverageMetadataFile);
    Assert.assertEquals(2, classes.size());
    checkClassIsInstrumented(testPackageName + ".src.SrcClass", classes);
    checkClassIsInstrumented(testPackageName + ".lib.LibClass", classes);
  }

  private void checkClassIsInstrumented(@Nonnull String classFqName, @Nonnull JsonArray classes) {
    JsonObject testClass = getJsonClass(classes, getClassNameForJson(classFqName));
    Assert.assertNotNull(testClass);
    JsonArray probesArray = testClass.get("probes").getAsJsonArray();
    Assert.assertNotNull(probesArray);
    Assert.assertTrue(probesArray.size() > 0);

  }

  @Test
  @KnownIssue  // flaky due to non-determinism
  public void testClassId_005() throws Exception {
    String testPackageName = getTestPackageName("test005");
    File testRootDir = getTestRootDir(testPackageName);
    final String className = getClassNameForJson(testPackageName + ".jack.LibClass");

    long classIdOne;
    long classIdTwo;
    long classIdThree;
    JackBasedToolchain toolchain;

    // Compile with coverage only
    {
      toolchain = createJackToolchain();
      File coverageFileOne = CoverageToolchainBuilder.create(toolchain).build();
      File outDexFolderOne = AbstractTestTools.createTempDir();
      toolchain.srcToExe(outDexFolderOne, false, testRootDir);
      classIdOne = getClassIdOf(coverageFileOne, className);
    }

    // Compile with coverage only again into a different coverage file.
    {
      toolchain = createJackToolchain();
      File coverageFileTwo = CoverageToolchainBuilder.create(toolchain).build();
      File outDexFolderTwo = AbstractTestTools.createTempDir();
      toolchain.srcToExe(outDexFolderTwo, false, testRootDir);
      classIdTwo = getClassIdOf(coverageFileTwo, className);
    }

    // Compile with coverage *and* proguard to shrink LibClass so it loses its unusedMethod.
    {
      toolchain = createJackToolchain();
      File coverageFileThree = CoverageToolchainBuilder.create(toolchain).build();
      File proguardFile = new File(testRootDir, "proguard.flags");
      toolchain.addProguardFlags(proguardFile);
      File outDexFolderThree = AbstractTestTools.createTempDir();
      toolchain.srcToExe(outDexFolderThree, false, testRootDir);
      classIdThree = getClassIdOf(coverageFileThree, className);
    }

    // We should generate the same class ID for the same class.
    Assert.assertEquals("Expected same class IDs", classIdOne, classIdTwo);

    // We should generate different class IDs when they are different (after shrinking here).
    assertNotEquals("Expected different class IDs", classIdOne, classIdThree);
  }

  @Test
  public void testClassId_006() throws Exception {
    String testPackageNameV1 = getTestPackageName("test006_v1");
    File testRootDirV1 = getTestRootDir(testPackageNameV1);

    String testPackageNameV2 = getTestPackageName("test006_v2");
    File testRootDirV2 = getTestRootDir(testPackageNameV2);

    final String className = getClassNameForJson("jack.SrcClass");
    long classIdV1;
    long classIdV2;
    long classIdV2_2;
    JackBasedToolchain toolchain;

    // Compile with coverage the version v1
    {
      toolchain = createJackToolchain();
      File coverageFileOne = CoverageToolchainBuilder.create(toolchain).build();
      File outDexFolderOne = AbstractTestTools.createTempDir();
      toolchain.srcToExe(outDexFolderOne, false, testRootDirV1);
      classIdV1 = getClassIdOf(coverageFileOne, className);
    }

    // Compile with coverage the version v2
    {
      toolchain = createJackToolchain();
      File coverageFileTwo = CoverageToolchainBuilder.create(toolchain).build();
      File outDexFolderTwo = AbstractTestTools.createTempDir();
      toolchain.srcToExe(outDexFolderTwo, false, testRootDirV2);
      classIdV2 = getClassIdOf(coverageFileTwo, className);
    }

    // Compile with coverage the version v2 again
    {
      toolchain = createJackToolchain();
      File coverageFileThree = CoverageToolchainBuilder.create(toolchain).build();
      File outDexFolderThree = AbstractTestTools.createTempDir();
      toolchain.srcToExe(outDexFolderThree, false, testRootDirV2);
      classIdV2_2 = getClassIdOf(coverageFileThree, className);
    }

    // We should generate different class IDs when they are different (after shrinking here).
    assertNotEquals("Expected different class IDs", classIdV1, classIdV2);

    assertEquals("Expected different class IDs", classIdV2, classIdV2_2);
  }
}
