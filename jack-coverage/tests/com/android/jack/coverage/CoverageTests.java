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
import com.google.gson.JsonParser;

import com.android.jack.test.TestsProperties;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiV03Toolchain;
import com.android.jack.test.toolchain.JackApiV04Toolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JackCliToolchain;
import com.android.jack.util.NamingTools;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.WrongPermissionException;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class CoverageTests {
  @Nonnull
  private static final String JACOCO_RUNTIME_PACKAGE = "org.jacoco.agent.rt.internal_04864a1";

  @Nonnull private static final String COVERAGE_TEST_PACKAGE = "com.android.jack.coverage";

  @Test
  public void testSingleClass() throws Exception {
    final String testPackageName = getTestPackageName("test001");
    JsonArray classes = compileAndReadJson(testPackageName);

    // We expect only one class
    Assert.assertNotNull(classes);
    Assert.assertEquals(1, classes.size());
    JsonObject testClass = classes.get(0).getAsJsonObject();
    Assert.assertNotNull(testClass);
    Assert.assertEquals(
        getClassNameForJson(testPackageName + ".AbstractClass"),
        getClassName(testClass));

    // Check its methods.
    JsonArray methods = testClass.get("methods").getAsJsonArray();
    Assert.assertEquals(3, methods.size()); // abstract and native methods are excluded.

    class NameAndDesc {
      @Nonnull public final String name;
      @Nonnull public final String desc;

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

  @Nonnull
  private static String getClassName(JsonArray classArray, int index) {
    return getClassName(classArray.get(index).getAsJsonObject());
  }

  @Nonnull
  private static String getClassName(JsonObject classObject) {
    return classObject.get("name").getAsString();
  }

  @Test
  public void testSingleInterface() throws Exception {
    JsonArray classes = compileAndReadJson(getTestPackageName("test002"));

    // Interface must be skipped
    Assert.assertEquals(0, classes.size());
  }

  @Test
  public void testIncludeAll() throws Exception {
    String testPackageName = getTestPackageName("test003");
    JsonArray classes = compileAndReadJson(testPackageName, "*", null);

    // Full coverage: Foo and Bar classes.
    Assert.assertEquals(2, classes.size());
    Collection<? extends String> actualClassNames = collectClassNames(classes);
    Set<String> expectedClassNames = Sets.newHashSet(
        getClassNameForJson(testPackageName + ".foo.Foo"),
        getClassNameForJson(testPackageName + ".foo.bar.Bar"));
    Assert.assertTrue(actualClassNames.containsAll(expectedClassNames));
  }

  @Test
  public void testIncludeSingle() throws Exception {
    String testPackageName = getTestPackageName("test003");
    String includeFilter = testPackageName + ".foo.bar.Bar";
    JsonArray classes = compileAndReadJson(testPackageName, includeFilter, null);

    // Partial coverage: only Bar class.
    Assert.assertEquals(1, classes.size());
    Collection<? extends String> actualClassNames = collectClassNames(classes);
    Set<String> expectedClassNames = Sets.newHashSet(
        getClassNameForJson(testPackageName + ".foo.bar.Bar"));
    Assert.assertTrue(actualClassNames.containsAll(expectedClassNames));
  }

  @Test
  public void testIncludeWildcard() throws Exception {
    String testPackageName = getTestPackageName("test003");
    String includeFilter = testPackageName + ".foo.bar.*";
    JsonArray classes = compileAndReadJson(testPackageName, includeFilter, null);

    // Partial coverage: only Bar class.
    Assert.assertEquals(1, classes.size());
    Collection<? extends String> actualClassNames = collectClassNames(classes);
    Set<String> expectedClassNames = Sets.newHashSet(
        getClassNameForJson(testPackageName + ".foo.bar.Bar"));
    Assert.assertTrue(actualClassNames.containsAll(expectedClassNames));
  }

  @Test
  public void testExcludeAll() throws Exception {
    JsonArray classes = compileAndReadJson(getTestPackageName("test003"), null, "*");

    // No coverage at all
    Assert.assertEquals(0, classes.size());
  }

  @Test
  public void testExcludeSingle() throws Exception {
    String testPackageName = getTestPackageName("test003");
    String excludeFilter = testPackageName + ".foo.bar.Bar";
    JsonArray classes = compileAndReadJson(getTestPackageName("test003"), null, excludeFilter);

    // Partial coverage: only Foo class.
    Assert.assertEquals(1, classes.size());
    Collection<? extends String> actualClassNames = collectClassNames(classes);
    Set<String> expectedClassNames = Sets.newHashSet(
        getClassNameForJson(testPackageName + ".foo.Foo"));
    Assert.assertTrue(actualClassNames.containsAll(expectedClassNames));
  }

  @Test
  public void testExcludeWildcard() throws Exception {
    String testPackageName = getTestPackageName("test003");
    String excludeFilter = testPackageName + ".foo.bar.*";
    JsonArray classes = compileAndReadJson(testPackageName, null, excludeFilter);

    // Partial coverage: only Foo class.
    Assert.assertEquals(1, classes.size());
    Collection<? extends String> actualClassNames = collectClassNames(classes);
    Set<String> expectedClassNames = Sets.newHashSet(
        getClassNameForJson(testPackageName + ".foo.Foo"));
    Assert.assertTrue(actualClassNames.containsAll(expectedClassNames));
  }

  @Test
  public void testIncludeExclude() throws Exception {
    String testPackageName = getTestPackageName("test003");
    String includeFilter = testPackageName + ".foo.bar.*";
    String excludeFilter = testPackageName + ".foo.*";
    JsonArray classes = compileAndReadJson(testPackageName, includeFilter, excludeFilter);

    // No coverage at all
    Assert.assertEquals(0, classes.size());
  }

  @Test
  public void testPreDex() throws Exception {
    String testPackageName = getTestPackageName("test004");

    // 1 - Create a lib.
    JackBasedToolchain toolchain = createJackToolchain();
    File libDir = AbstractTestTools.createTempDir();
    File libSrcFiles = new File(getTestRootDir(testPackageName), "lib");
    toolchain.srcToLib(libDir, false, libSrcFiles);

    // 2 - Compile the lib with coverage.
    toolchain = createJackToolchain();
    toolchain.addStaticLibs(libDir);
    File coverageMetadataFile = enableCodeCoverage(toolchain, null, null);
    File srcFiles = new File(getTestRootDir(testPackageName), "src");
    File outDexFolder = AbstractTestTools.createTempDir();
    toolchain.srcToExe(outDexFolder, false, srcFiles);

    // 3 - Check types from the lib are instrumented.
    JsonArray classes = loadJsonCoverageClasses(coverageMetadataFile);
    JsonObject testClass =
        getJsonClass(classes, getClassNameForJson(testPackageName + ".lib.LibClass"));
    JsonArray probesArray = testClass.get("probes").getAsJsonArray();
    Assert.assertNotNull(probesArray);
    Assert.assertTrue(probesArray.size() > 0);
  }

  @Test
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
      File coverageFileOne = enableCodeCoverage(toolchain, null, null);
      File outDexFolderOne = AbstractTestTools.createTempDir();
      toolchain.srcToExe(outDexFolderOne, false, testRootDir);
      classIdOne = getClassIdOf(coverageFileOne, className);
    }

    // Compile with coverage only again into a different coverage file.
    {
      toolchain = createJackToolchain();
      File coverageFileTwo = enableCodeCoverage(toolchain, null, null);
      File outDexFolderTwo = AbstractTestTools.createTempDir();
      toolchain.srcToExe(outDexFolderTwo, false, testRootDir);
      classIdTwo = getClassIdOf(coverageFileTwo, className);
    }

    // Compile with coverage *and* proguard to shrink LibClass so it loses its unusedMethod.
    {
      toolchain = createJackToolchain();
      File coverageFileThree = enableCodeCoverage(toolchain, null, null);
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
      File coverageFileOne = enableCodeCoverage(toolchain, null, null);
      File outDexFolderOne = AbstractTestTools.createTempDir();
      toolchain.srcToExe(outDexFolderOne, false, testRootDirV1);
      classIdV1 = getClassIdOf(coverageFileOne, className);
    }

    // Compile with coverage the version v2
    {
      toolchain = createJackToolchain();
      File coverageFileTwo = enableCodeCoverage(toolchain, null, null);
      File outDexFolderTwo = AbstractTestTools.createTempDir();
      toolchain.srcToExe(outDexFolderTwo, false, testRootDirV2);
      classIdV2 = getClassIdOf(coverageFileTwo, className);
    }

    // Compile with coverage the version v2 again
    {
      toolchain = createJackToolchain();
      File coverageFileThree = enableCodeCoverage(toolchain, null, null);
      File outDexFolderThree = AbstractTestTools.createTempDir();
      toolchain.srcToExe(outDexFolderThree, false, testRootDirV2);
      classIdV2_2 = getClassIdOf(coverageFileThree, className);
    }

    // We should generate different class IDs when they are different (after shrinking here).
    assertNotEquals("Expected different class IDs", classIdV1, classIdV2);

    assertEquals("Expected different class IDs", classIdV2, classIdV2_2);
  }

  private static void assertNotEquals(@Nonnull String msg, long expected, long actual) {
    if (expected == actual) {
      StringBuilder stringBuilder = new StringBuilder(msg);
      stringBuilder.append(": expected=");
      stringBuilder.append(Long.toHexString(expected));
      stringBuilder.append(", actual=");
      stringBuilder.append(Long.toHexString(actual));
      Assert.fail(stringBuilder.toString());
    }
  }

  private static long getClassIdOf(@Nonnull File coverageFile, @Nonnull String className)
      throws IOException {
    JsonArray classesArray = loadJsonCoverageClasses(coverageFile);
    for (int i = 0, e = classesArray.size(); i < e; ++i) {
      JsonObject classObject = classesArray.get(i).getAsJsonObject();
      String jsonClassName = classObject.get("name").getAsString();
      if (className.equals(jsonClassName)) {
        long id = classObject.get("id").getAsLong();
        return id;
      }
    }
    throw new AssertionError("No class " + className + " in coverage file");
  }

  @Nonnull
  private static JsonObject getJsonClass(
      @Nonnull JsonArray jsonClasses, @Nonnull String className) {
    for (JsonElement jsonElement : jsonClasses) {
      JsonObject jsonClass = jsonElement.getAsJsonObject();
      if (jsonClass.get("name").getAsString().equals(className)) {
        return jsonClass;
      }
    }
    throw new AssertionError("No class " + className);
  }

  @Nonnull
  private static String getClassNameForJson(@Nonnull String className) {
    return NamingTools.getBinaryName(className);
  }

  @Nonnull
  private Collection<? extends String> collectClassNames(@Nonnull JsonArray classes) {
    Set<String> classNames = new HashSet<String>();
    for (JsonElement arrayElt: classes) {
      classNames.add(getClassName(arrayElt.getAsJsonObject()));
    }
    return classNames;
  }

  @Nonnull
  private static JackBasedToolchain createJackToolchain() {
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    return toolchain;
  }

  /**
   * Enable code coverage for the given toolchain.
   *
   * @param toolchain the toolchain to configure with code coverage
   * @param includeFilter the 'include' class filter
   * @param excludeFilter the 'exclude' class filter
   * @return the coverage metadata file generated by the compilation
   * @throws Exception if the compilation fails
   */
  private static File enableCodeCoverage(
      @Nonnull JackBasedToolchain toolchain,
      @Nonnull String includeFilter,
      @Nonnull String excludeFilter) throws Exception {
    File coverageMetadataFile = createTempCoverageMetadataFile();
    toolchain.addProperty("jack.coverage", "true");
    toolchain.addProperty(
        "jack.coverage.metadata.file", coverageMetadataFile.getAbsolutePath());
    toolchain.addProperty(
        "jack.coverage.jacoco.package", JACOCO_RUNTIME_PACKAGE);
    if (includeFilter != null) {
      toolchain.addProperty("jack.coverage.jacoco.include", includeFilter);
    }
    if (excludeFilter != null) {
      toolchain.addProperty("jack.coverage.jacoco.exclude", excludeFilter);
    }
    toolchain.addToClasspath(getJacocoAgentLib());
    File pluginFile = getCodeCoveragePluginFile();
    List<File> pluginPath = Collections.singletonList(pluginFile);
    List<String> pluginNames = Collections.singletonList("com.android.jack.coverage.CodeCoverage");
    if (toolchain instanceof JackCliToolchain) {
      JackCliToolchain cliToolchain = (JackCliToolchain) toolchain;
      cliToolchain.setPluginPath(pluginPath);
      cliToolchain.setPluginNames(pluginNames);
    } else {
      // TODO: need to rework API toolchain hierarchy in test framework to avoid these if/else.
      if (toolchain instanceof JackApiV03Toolchain) {
        JackApiV03Toolchain jackApiV03 = (JackApiV03Toolchain) toolchain;
        jackApiV03.setPluginPath(pluginPath);
        jackApiV03.setPluginNames(pluginNames);
      } else if (toolchain instanceof JackApiV04Toolchain) {
        JackApiV04Toolchain jackApiV04 = (JackApiV04Toolchain) toolchain;
        jackApiV04.setPluginPath(pluginPath);
        jackApiV04.setPluginNames(pluginNames);
      } else {
        throw new AssertionError("Unsupported toolchain: " + toolchain.getClass().getName());
      }
    }
    return coverageMetadataFile;
  }

  @Nonnull
  private JsonArray compileAndReadJson(@Nonnull String testPackageName)
      throws CannotCreateFileException, CannotChangePermissionException, WrongPermissionException,
          IOException, Exception {
    return compileAndReadJson(testPackageName, null, null);
  }

  @Nonnull
  private static File createTempCoverageMetadataFile()
      throws CannotCreateFileException, CannotChangePermissionException {
    return AbstractTestTools.createTempFile("coverage", ".metadata");
  }

  private File compileDexWithCoverage(@Nonnull File[] sourceFiles,
      @CheckForNull String includeFilter,
      @CheckForNull String excludeFilter,
      @Nonnull File[] staticLibs) throws Exception {
    File outDexFolder = AbstractTestTools.createTempDir();
    JackBasedToolchain toolchain = createJackToolchain();
    File coverageMetadataFile = enableCodeCoverage(toolchain, includeFilter, excludeFilter);

    // Setup classpath.
    toolchain.addStaticLibs(staticLibs);

    toolchain.srcToExe(outDexFolder, false, sourceFiles);

    return coverageMetadataFile;
  }

  @Nonnull
  private JsonArray compileAndReadJson(
      @Nonnull String testPackageName,
      @CheckForNull String includeFilter,
      @CheckForNull String excludeFilter)
      throws CannotCreateFileException, CannotChangePermissionException, WrongPermissionException,
          IOException, Exception {
    File sourceDir = getTestRootDir(testPackageName);
    File coverageMetadataFile = compileDexWithCoverage(new File[]{sourceDir},
        includeFilter, excludeFilter, new File[0]);

    return loadJsonCoverageClasses(coverageMetadataFile);
  }

  @Nonnull
  private static JsonArray loadJsonCoverageClasses(@Nonnull File coverageMetadataFile)
      throws IOException {
    Assert.assertTrue(coverageMetadataFile.length() > 0);

    JsonObject root = loadJson(coverageMetadataFile).getAsJsonObject();
    Assert.assertNotNull(root);

    String version = root.get("version").getAsString();
    Assert.assertNotNull(version);
    Assert.assertEquals("1.0", version);

    JsonArray classes =
        root.get("data").getAsJsonArray();
    Assert.assertNotNull(classes);
    return classes;
  }

  @Nonnull
  private static JsonElement loadJson(@Nonnull File jsonFile) throws IOException {
    JsonParser parser = new JsonParser();
    FileReader reader = new FileReader(jsonFile);
    try {
      return parser.parse(reader);
    } finally {
      reader.close();
    }
  }

  @Nonnull
  private static File getJacocoAgentLib() {
    return new File(
        TestsProperties.getJackRootDir(), "jacoco/org.jacoco.agent.rt-0.7.5.201505241946-all.jar");
  }

  @Nonnull
  private static File getCodeCoveragePluginFile() {
    return new File(
        TestsProperties.getJackRootDir(), "jack-coverage/dist/jack-coverage-plugin.jar");
  }

  @Nonnull
  private String getTestPackageName(@Nonnull String testName) {
    return COVERAGE_TEST_PACKAGE + "." + testName;
  }

  /**
   * Workaround AbstractTestTools.getTestRootDir hardcoded for Jack tests only.
   */
  @Nonnull
  private static final File getTestRootDir(@Nonnull String testPackageName) {
    File jackRootDir = TestsProperties.getJackRootDir();
    File jackCoverageDir = new File(jackRootDir, "jack-coverage");
    File jackCoverageTestsDir = new File(jackCoverageDir, "tests");
    return new File(jackCoverageTestsDir, testPackageName.replace('.', File.separatorChar));
  }
}
