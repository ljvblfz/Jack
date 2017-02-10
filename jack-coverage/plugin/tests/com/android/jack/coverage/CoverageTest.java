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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.android.jack.test.TestsProperties;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.util.NamingTools;

import junit.framework.Assert;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public abstract class CoverageTest {

  @Nonnull
  private static final String COVERAGE_TEST_PACKAGE = "com.android.jack.coverage";

  @Nonnull
  protected static String getTestPackageName(@Nonnull String testName) {
    return COVERAGE_TEST_PACKAGE + "." + testName;
  }

  /**
   * Workaround AbstractTestTools.getTestRootDir hardcoded for Jack tests only.
   */
  @Nonnull
  protected static final File getTestRootDir(@Nonnull String testPackageName) {
    File rootDir = TestsProperties.getJackRootDir();
    rootDir = new File(rootDir, "jack-coverage");
    rootDir = new File(rootDir, "plugin");
    rootDir = new File(rootDir, "tests");
    return new File(rootDir, testPackageName.replace('.', File.separatorChar));
  }

  @Nonnull
  protected static JackBasedToolchain createJackToolchain() {
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    return toolchain;
  }


  protected static void assertNotEquals(@Nonnull String msg, long expected, long actual) {
    if (expected == actual) {
      StringBuilder stringBuilder = new StringBuilder(msg);
      stringBuilder.append(": expected=");
      stringBuilder.append(Long.toHexString(expected));
      stringBuilder.append(", actual=");
      stringBuilder.append(Long.toHexString(actual));
      Assert.fail(stringBuilder.toString());
    }
  }

  protected static long getClassIdOf(@Nonnull File coverageFile, @Nonnull String className)
      throws IOException {
    JsonArray classesArray = loadJsonCoverageClasses(coverageFile);
    JsonObject classObject = getJsonClass(classesArray, className);
    return classObject.get("id").getAsLong();
  }

  @CheckForNull
  protected static JsonObject getJsonClass(@Nonnull JsonArray jsonClasses,
      @Nonnull String className) {
    for (JsonElement jsonElement : jsonClasses) {
      JsonObject jsonClass = jsonElement.getAsJsonObject();
      if (jsonClass.get("name").getAsString().equals(className)) {
        return jsonClass;
      }
    }
    return null;
  }

  @Nonnull
  protected static String getClassNameForJson(@Nonnull String className) {
    return NamingTools.getBinaryName(className);
  }

  @Nonnull
  protected static String getClassName(@Nonnull JsonArray classArray, @Nonnegative int index) {
    return getClassName(classArray.get(index).getAsJsonObject());
  }

  @Nonnull
  protected static String getClassName(@Nonnull JsonObject classObject) {
    return classObject.get("name").getAsString();
  }

  @Nonnull
  protected Collection<? extends String> collectClassNames(@Nonnull JsonArray classes) {
    Set<String> classNames = new HashSet<String>();
    for (JsonElement arrayElt : classes) {
      classNames.add(getClassName(arrayElt.getAsJsonObject()));
    }
    return classNames;
  }

  @Nonnull
  protected static JsonArray loadJsonCoverageClasses(@Nonnull File coverageMetadataFile)
      throws IOException {
    Assert.assertTrue(coverageMetadataFile.length() > 0);

    JsonObject root = loadJson(coverageMetadataFile).getAsJsonObject();
    Assert.assertNotNull(root);

    String version = root.get("version").getAsString();
    Assert.assertNotNull(version);
    Assert.assertEquals("1.0", version);

    JsonArray classes = root.get("data").getAsJsonArray();
    Assert.assertNotNull(classes);
    return classes;
  }

  @Nonnull
  protected static JsonElement loadJson(@Nonnull File jsonFile) throws IOException {
    JsonParser parser = new JsonParser();
    try (FileReader reader = new FileReader(jsonFile)) {
      return parser.parse(reader);
    }
  }
}
