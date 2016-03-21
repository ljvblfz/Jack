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

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.android.jack.test.TestsProperties;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackBasedToolchain;
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
import java.util.HashSet;
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
  private JsonArray compileAndReadJson(@Nonnull String testPackageName)
      throws CannotCreateFileException, CannotChangePermissionException, WrongPermissionException,
          IOException, Exception {
    return compileAndReadJson(testPackageName, null, null);
  }

  @Nonnull
  private JsonArray compileAndReadJson(
      @Nonnull String testPackageName,
      @CheckForNull String includeFilter,
      @CheckForNull String excludeFilter)
      throws CannotCreateFileException, CannotChangePermissionException, WrongPermissionException,
          IOException, Exception {
    File outDexFolder = AbstractTestTools.createTempDir();
    File coverageMetadataFile = AbstractTestTools.createTempFile("coverage", ".metadata");
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);

    // Setup toolchain for code coverage.
    toolchain.addProperty(CodeCoverage.CODE_COVERVAGE.getName(), "true");
    toolchain.addProperty(
        CodeCoverage.COVERAGE_METADATA_FILE.getName(), coverageMetadataFile.getAbsolutePath());
    toolchain.addProperty(
        CodeCoverage.COVERAGE_JACOCO_PACKAGE_NAME.getName(), JACOCO_RUNTIME_PACKAGE);
    if (includeFilter != null) {
      toolchain.addProperty(CodeCoverage.COVERAGE_JACOCO_INCLUDES.getName(), includeFilter);
    }
    if (excludeFilter != null) {
      toolchain.addProperty(CodeCoverage.COVERAGE_JACOCO_EXCLUDES.getName(), excludeFilter);
    }

    // Setup classpath.
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.addToClasspath(getJacocoAgentLib());

    toolchain.srcToExe(outDexFolder, false, AbstractTestTools.getTestRootDir(testPackageName));

    Assert.assertTrue(coverageMetadataFile.length() > 0);

    JsonObject root = loadJson(coverageMetadataFile).getAsJsonObject();
    Assert.assertNotNull(root);

    String version = root.get(CodeCoverageMetadataFileWriter.JSON_VERSION_ATTRIBUTE).getAsString();
    Assert.assertNotNull(version);
    Assert.assertEquals(CodeCoverageMetadataFileWriter.VERSION, version);

    JsonArray classes =
        root.get(CodeCoverageMetadataFileWriter.JSON_DATA_ATTRIBUTE).getAsJsonArray();
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
  private String getTestPackageName(@Nonnull String testName) {
    return COVERAGE_TEST_PACKAGE + "." + testName;
  }
}
