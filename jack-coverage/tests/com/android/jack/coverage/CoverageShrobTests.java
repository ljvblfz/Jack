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

import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackBasedToolchain;

import junit.framework.Assert;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class CoverageShrobTests extends CoverageTest {

  /**
   * Tests that shrunk classes are not in the coverage metadata file.
   */
  @Test
  public void testShrob001() throws Exception {
    String testPackageName = getTestPackageName("shrob.test001");
    File testRootDir = getTestRootDir(testPackageName);
    File srcDir = getTestRootDir(testPackageName);

    JackBasedToolchain toolchain = createJackToolchain();
    toolchain.addProguardFlags(new File(testRootDir, "proguard.flags"));
    File coverageMetadataFile = CoverageToolchainBuilder.create(toolchain).build();

    File outDexFolder = AbstractTestTools.createTempDir();
    toolchain.srcToExe(outDexFolder, false, srcDir);

    JsonArray classes = loadJsonCoverageClasses(coverageMetadataFile);
    Assert.assertEquals(1, classes.size());
    Assert.assertNotNull(getJsonClass(classes, getClassNameForJson(testPackageName + ".Foo")));
  }

  /**
   * Tests that coverage is applied on obfuscated classes.
   *
   * Note: deobfuscation of classes is the job of the code coverage reporter tool.
   */
  @Test
  public void testShrob002() throws Exception {
    String testPackageName = getTestPackageName("shrob.test002");
    File testRootDir = getTestRootDir(testPackageName);
    File srcDir = getTestRootDir(testPackageName);

    JackBasedToolchain toolchain = createJackToolchain();
    toolchain.addProguardFlags(new File(testRootDir, "proguard.flags"));
    // Dump the mapping file to know obfuscated classes' names.
    File mappingFile = AbstractTestTools.createTempFile("coverage-mapping", ".txt");
    toolchain.addProperty("jack.obfuscation.mapping.dump", Boolean.toString(true));
    toolchain.addProperty("jack.obfuscation.mapping.dump.file", mappingFile.getAbsolutePath());
    File coverageMetadataFile = CoverageToolchainBuilder.create(toolchain).build();

    File outDexFolder = AbstractTestTools.createTempDir();
    toolchain.srcToExe(outDexFolder, false, srcDir);

    JsonArray classes = loadJsonCoverageClasses(coverageMetadataFile);
    Assert.assertEquals(2, classes.size());

    // Extract class names from mapping file and check that they are in the coverage metadata file.
    List<String> obfuscatedNames = extractObfuscatedClassNames(mappingFile);
    for (String className : obfuscatedNames) {
      Assert.assertNotNull(getJsonClass(classes, getClassNameForJson(className)));
    }
  }

  @Nonnull
  private List<String> extractObfuscatedClassNames(@Nonnull File mappingFile)
      throws FileNotFoundException, IOException {
    List<String> names = new ArrayList<String>();
    try (BufferedReader reader = new BufferedReader(new FileReader(mappingFile))) {
      String line;
      while (true) {
        line = reader.readLine();
        if (line == null) {
          break;
        }
        // Remove leading and trailing spaces.
        line = line.trim();
        final int lastPos = line.length() - 1;
        if (line.charAt(lastPos) != ':') {
          // Not a class line
          continue;
        }
        final int separatorPos = line.indexOf(MAPPING_FILE_SEPARATOR);
        if (separatorPos < 0) {
          throw new AssertionError();
        }
        // Extract obfuscated class name.
        String className =
            line.substring(separatorPos + MAPPING_FILE_SEPARATOR.length(), lastPos).trim();
        names.add(className);
      }
    }
    return names;
  }

  private static final String MAPPING_FILE_SEPARATOR = "->";
}
