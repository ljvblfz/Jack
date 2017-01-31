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

package com.android.jack.tools.jacoco;

import com.android.jack.tools.jacoco.MappingFileLoader.ClassMapping;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import junit.framework.Assert;
import org.junit.Test;

public class MappingFileLoaderTest {

  @Test
  public void testClassLineParsing() throws IOException {
    String oldClassName = "com.Foo";
    String newClassName = "com.A";

    List<String> testLines = generateStringWithSpaces(oldClassName, "->", newClassName, ":");
    for (String testLine : testLines) {
      MappingFileLoader loader = loadMappingFile(testLine);

      ClassMapping cm = loader.getClassMapping(NamingUtils.fqNameToBinaryName(newClassName));
      Assert.assertNotNull("No match for line '" + testLine + "'", cm);
      Assert.assertEquals("Invalid mapping for line '" + testLine + "'",
          NamingUtils.fqNameToBinaryName(oldClassName), cm.getOriginalClassName());
    }
  }

  @Test
  public void testMethodLineParsing() throws IOException {
    String oldClassName = "com.Foo";
    String newClassName = "com.A";

    List<String> testLines = generateStringWithSpaces("void m()", "->", "newM");
    for (String testLine : testLines) {
      MappingFileLoader loader = loadMappingFile(
          "com.Foo -> com.A:",
          testLine);

      ClassMapping cm = loader.getClassMapping(NamingUtils.fqNameToBinaryName(newClassName));
      Assert.assertNotNull("No match for line '" + testLine + "'", cm);
      Assert.assertEquals("Invalid mapping for line '" + testLine + "'",
          NamingUtils.fqNameToBinaryName(oldClassName), cm.getOriginalClassName());
      Assert.assertEquals("Failure with line '" + testLine + "'",
          "m()V", cm.getOriginalMethodSignature("newM()V"));
    }
  }

  @Test
  public void testMethodLineParsingWithLineInfo() throws IOException {
    String oldClassName = "com.Foo";
    String newClassName = "com.A";

    List<String> testLines = generateStringWithSpaces("1:5", "void m()", "->", "newM");
    for (String testLine : testLines) {
      MappingFileLoader loader = loadMappingFile(
          "com.Foo -> com.A:",
          testLine);

      ClassMapping cm = loader.getClassMapping(NamingUtils.fqNameToBinaryName(newClassName));
      Assert.assertNotNull("No match for line '" + testLine + "'", cm);
      Assert.assertEquals("Invalid mapping for line '" + testLine + "'",
          NamingUtils.fqNameToBinaryName(oldClassName), cm.getOriginalClassName());
      Assert.assertEquals("Failure with line '" + testLine + "'",
          "m()V", cm.getOriginalMethodSignature("newM()V"));
    }
  }

  @Test
  public void testParsingWithignoredFieldInfo() throws IOException {
    String oldClassName = "com.Foo";
    String newClassName = "com.A";

    List<String> testLines = generateStringWithSpaces("void m()", "->", "newM");
    for (String testLine : testLines) {
      MappingFileLoader loader = loadMappingFile(
          "com.Foo -> com.A:",
          "void m() -> newM",
          "oldField -> newField");

      ClassMapping cm = loader.getClassMapping(NamingUtils.fqNameToBinaryName(newClassName));
      Assert.assertNotNull("No match for line '" + testLine + "'", cm);
      Assert.assertEquals("Invalid mapping for line '" + testLine + "'",
          NamingUtils.fqNameToBinaryName(oldClassName), cm.getOriginalClassName());
      Assert.assertEquals("Failure with line '" + testLine + "'",
          "m()V", cm.getOriginalMethodSignature("newM()V"));
    }
  }

  @Test
  public void testInvalidClassLine() throws IOException {
    // Missing trailing ':' character.
    byte[] buffer = buildBuffer("com.Foo -> com.A");

    MappingFileLoader loader = new MappingFileLoader();
    InputStream input = new ByteArrayInputStream(buffer);
    try {
      loader.load(input);
      Assert.fail();
    } catch (AssertionError expected) {
      // OK, we got our exception.
    } finally {
      input.close();
    }
  }

  @Test
  public void testSignatureConversion() throws IOException {
    MappingFileLoader loader = loadMappingFile(
        "com.Foo -> com.A:",
        "void m(int) -> newM1",
        "void m(int, int) -> newM2",
        "void m(com.Foo) -> newM3",
        "void m(com.Foo[]) -> newM4"
    );
    ClassMapping cm = loader.getClassMapping("com/A");
    Assert.assertNotNull(cm);

    // Check unknown signature
    Assert.assertNull(cm.getOriginalMethodSignature("notRenamed()V"));

    // Check conversions
    Assert.assertEquals("m(I)V", cm.getOriginalMethodSignature("newM1(I)V"));
    Assert.assertEquals("m(II)V", cm.getOriginalMethodSignature("newM2(II)V"));

    // Check method name and parameters are restored
    Assert.assertEquals("m(Lcom/Foo;)V", cm.getOriginalMethodSignature("newM3(Lcom/A;)V"));
    Assert.assertEquals("m([Lcom/Foo;)V", cm.getOriginalMethodSignature("newM4([Lcom/A;)V"));
  }

  /**
   * Generate a list of all possible strings when the given {@code strings} are separated or not by
   * a space character (including leading and trailing space).
   */
  private static List<String> generateStringWithSpaces(String... strings) {
    int spacesPosCount = strings.length + 1;
    int spacesCombinationsCount = 1 << spacesPosCount;
    List<String> list = new ArrayList<String>(spacesCombinationsCount);

    for (int i = 0; i < spacesCombinationsCount; ++i) {
      StringBuilder sb = new StringBuilder();
      int j = 0;
      for (; j < strings.length; ++j) {
        String s = strings[j];
        if (isBitSet(i, j)) {
          sb.append(' ');
        }
        sb.append(s);
      }
      assert j == strings.length;
      // End space
      if (isBitSet(i, j)) {
        sb.append(' ');
      }
      list.add(sb.toString());
    }
    return list;
  }

  private static boolean isBitSet(int number, int index) {
    return (number & (1 << index)) != 0;
  }

  @Test
  public void testGenerateSpaces() {
    List<String> strings = generateStringWithSpaces("str");
    Assert.assertNotNull(strings);
    Assert.assertEquals(4, strings.size());
    Assert.assertTrue(strings.contains("str"));
    Assert.assertTrue(strings.contains(" str"));
    Assert.assertTrue(strings.contains("str "));
    Assert.assertTrue(strings.contains(" str "));
  }

  static MappingFileLoader loadMappingFile(@Nonnull String... lines) throws IOException {
    byte[] buffer = buildBuffer(lines);
    MappingFileLoader loader = new MappingFileLoader();
    InputStream input = new ByteArrayInputStream(buffer);
    try {
      loader.load(input);
    } finally {
      input.close();
    }
    return loader;
  }

  private static byte[] buildBuffer(@Nonnull String... lines) throws IOException {
    byte[] result;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      PrintWriter pw = new PrintWriter(baos);
      for (String line : lines) {
        pw.println(line);
      }
      pw.flush();
      result = baos.toByteArray();
    } finally {
      baos.close();
    }
    return result;
  }
}
