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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A mapping file loader that loads only class and method mappings.
 */
public class MappingFileLoader {

  @Nonnull
  private static final char[] EMPTY_STOP_CHARS = new char[] {};

  @Nonnull
  private static final char[] CLASSINFO_STOP_CHARS = new char[] {':'};

  @Nonnull
  private final Map<String, ClassMapping> oldToNewClassMapping =
      new HashMap<String, ClassMapping>();

  @Nonnull
  private final Map<String, ClassMapping> newToOldClassMapping =
      new HashMap<String, ClassMapping>();

  @CheckForNull
  public ClassMapping getClassMapping(@Nonnull String classBinaryName) {
    // It must be a class descriptor (like 'com/sub/Foo')
    assert classBinaryName.indexOf('.') < 0;
    assert classBinaryName.indexOf('/') > 0;
    return newToOldClassMapping.get(classBinaryName);
  }

  public void load(@Nonnull InputStream input) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    ClassMapping currentClassDesc = null;
    String line;
    while ((line = reader.readLine()) != null) {
      // Remove leading and trailing whitespaces.
      line = line.trim();
      if (line.charAt(line.length() - 1) == ':') {
        // Class mapping line
        currentClassDesc = readClassInfo(line);
        ClassMapping previous =
            newToOldClassMapping.put(currentClassDesc.getClassName(), currentClassDesc);
        assert previous == null;
        previous =
            oldToNewClassMapping.put(currentClassDesc.getOriginalClassName(), currentClassDesc);
        assert previous == null;
      } else if (line.indexOf('(') > 0) {
        // Method mapping line
        readMethodInfo(line, currentClassDesc);
      } else {
        // Ignore other lines (like field info)
      }
    }

    // Now that we know how types may have been renamed by obfuscation, we need to update method
    // signatures accordingly.
    updateMethodMappings();
  }

  private void updateMethodMappings() {
    assert oldToNewClassMapping.size() == newToOldClassMapping.size();
    for (ClassMapping cd : newToOldClassMapping.values()) {
      cd.updateMethodMapping(this);
    }
  }

  @Nonnull
  private ClassMapping readClassInfo(@Nonnull String line) {
    // qualifiedOldClassName -> newClassName:
    try {
      int startIndex = readWhiteSpaces(line, 0);
      int endIndex = readNameUntilSeparatorOrWhitespace(line, startIndex);
      String qualifiedOldClassName = line.substring(startIndex, endIndex);
      startIndex = readWhiteSpaces(line, endIndex);
      startIndex = readSeparator(line, startIndex);
      startIndex = readWhiteSpaces(line, startIndex);
      endIndex = readName(line, startIndex, CLASSINFO_STOP_CHARS);
      String newClassName = line.substring(startIndex, endIndex);
      return new ClassMapping(NamingUtils.fqNameToBinaryName(newClassName),
          NamingUtils.fqNameToBinaryName(qualifiedOldClassName));
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new AssertionError("Invalid line '" + line + "'");
    }
  }

  private void readMethodInfo(@Nonnull String line, @Nonnull ClassMapping currentClassDesc) {
    // (startLineInfo:endLineInfo:)? type oldMethodName\((type(, type)*)?\) -> newMethodName
    int startIndex = readWhiteSpaces(line, 0);
    startIndex = readLineInfo(line, startIndex);
    startIndex = readWhiteSpaces(line, startIndex);
    int endIndex = line.indexOf(')', startIndex);
    assert endIndex > 0;
    String oldMethodDeclaration = line.substring(startIndex, endIndex + 1);
    startIndex = endIndex + 1;
    startIndex = readWhiteSpaces(line, startIndex);
    startIndex = readSeparator(line, startIndex);
    startIndex = readWhiteSpaces(line, startIndex);
    endIndex = readName(line, startIndex, EMPTY_STOP_CHARS);
    String newMethodName = line.substring(startIndex, endIndex);

    // Add method info to current class.
    currentClassDesc.addMethod(oldMethodDeclaration, newMethodName);
  }

  @Nonnegative
  private int readNameUntilSeparatorOrWhitespace(@Nonnull String line, @Nonnegative int index) {
    int length = line.length();
    char c = line.charAt(index);
    while (!Character.isWhitespace(c)) {
      if (c == '-' && line.charAt(index + 1) == '>') {
        // We check the second char to avoid bad parsing of names containing '-'
        break;
      }
      if (++index < length) {
        c = line.charAt(index);
      } else {
        break;
      }
    }
    return index;
  }

  private static int readSeparator(@Nonnull String line, int index) {
    if (line.charAt(index) != '-' || line.charAt(index + 1) != '>') {
      throw new AssertionError();
    }
    return index + 2;
  }

  private static int readName(@Nonnull String line, @Nonnegative int index,
      @Nonnull char[] stopChars) {
    int length = line.length();
    char c = line.charAt(index);
    while (!Character.isWhitespace(c) && !charInArray(c, stopChars)) {
      if (++index < length) {
        c = line.charAt(index);
      } else {
        break;
      }
    }
    return index;
  }

  private static boolean charInArray(char c, @Nonnull char[] array) {
    for (char c2 : array) {
      if (c == c2) {
        return true;
      }
    }
    return false;
  }

  private static int readWhiteSpaces(@Nonnull String line, @Nonnegative int index) {
    while (Character.isWhitespace(line.charAt(index))) {
      ++index;
    }
    return index;
  }

  private static int readLineInfo(@Nonnull String line, @Nonnegative int index) {
    char c = line.charAt(index);
    while (Character.isDigit(c) || c == ':') {
      index++;
      c = line.charAt(index);
    }
    return index;
  }


  /**
   * A description of the class in the mapping file
   */
  public static class ClassMapping {
    @Nonnull
    private final String className;
    @Nonnull
    private final String originalClassName;

    /**
     * Temporary map associating original method declarations to their new method name (without
     * parameter signatures). This map is set to null once the final mapping is computed.
     */
    @CheckForNull
    private Map<String, String> oldToNewMethodMap = new HashMap<String, String>();

    /**
     * Maps method signatures to their original one.
     */
    @CheckForNull
    private Map<String, String> newToOldMethodMap = null;

    public ClassMapping(@Nonnull String className, @Nonnull String originalName) {
      this.className = className;
      this.originalClassName = originalName;
    }

    @Nonnull
    public String getClassName() {
      return className;
    }

    @Nonnull
    public String getOriginalClassName() {
      return originalClassName;
    }

    public void updateMethodMapping(@Nonnull MappingFileLoader loader) {
      assert newToOldMethodMap == null;
      assert oldToNewMethodMap != null;

      newToOldMethodMap = new HashMap<String, String>(oldToNewMethodMap.size());
      for (Entry<String, String> e : oldToNewMethodMap.entrySet()) {
        String oldMethodDeclaration = e.getKey();
        String newMethodName = e.getValue();

        // The old method declaration is '<returnType> <oldName>(<paramType>[,<paramType>]*)'

        // Extract return type.
        int returnTypeEndPos = oldMethodDeclaration.indexOf(' ');
        String returnType = oldMethodDeclaration.substring(0, returnTypeEndPos);

        // Extract methodName
        int oldMethodNameStartPos = readWhiteSpaces(oldMethodDeclaration, returnTypeEndPos);
        int oldMethodNameEndPos = oldMethodDeclaration.indexOf('(', oldMethodNameStartPos);
        String oldMethodName =
            oldMethodDeclaration.substring(oldMethodNameStartPos, oldMethodNameEndPos);

        // Extract parameters
        int parameterListStartPos = oldMethodNameEndPos + 1;
        int parameterListEndPos = oldMethodDeclaration.indexOf(')', parameterListStartPos);
        String parameterList =
            oldMethodDeclaration.substring(parameterListStartPos, parameterListEndPos);
        String[] paramTypes = parameterList.isEmpty() ? new String[0] : parameterList.split(",");

        // Create old and new signatures.
        StringBuilder oldMethodSig = new StringBuilder(oldMethodName);
        StringBuilder newMethodSig = new StringBuilder(newMethodName);
        oldMethodSig.append('(');
        newMethodSig.append('(');

        // Convert parameters.
        for (String parameterType : paramTypes) {
          // Remove leading/trailing whitespaces.
          parameterType = parameterType.trim();
          String oldParameterTypeSig = NamingUtils.fqNameToSignature(parameterType);
          String newParameterTypeSig = oldToNewClassName(loader, oldParameterTypeSig);
          oldMethodSig.append(oldParameterTypeSig);
          newMethodSig.append(newParameterTypeSig);
        }

        oldMethodSig.append(')');
        newMethodSig.append(')');

        // Append return type
        String oldReturnTypeSig = NamingUtils.fqNameToSignature(returnType);
        String newReturnTypeSig = oldToNewClassName(loader, oldReturnTypeSig);
        oldMethodSig.append(oldReturnTypeSig);
        newMethodSig.append(newReturnTypeSig);

        newToOldMethodMap.put(newMethodSig.toString(), oldMethodSig.toString());
      }

      // Reset the map with finalized entries.
      oldToNewMethodMap = null;
    }

    @Nonnull
    private static String oldToNewClassName(@Nonnull MappingFileLoader loader,
        @Nonnull String oldClassName) {
      int lastCharPos = oldClassName.length() - 1;
      if (oldClassName.charAt(lastCharPos) != ';') {
        // THis is not a reference type: nothing to convert
        return oldClassName;
      }
      if (oldClassName.charAt(0) == '[') {
        return '[' + oldToNewClassName(loader, oldClassName.substring(1));
      }
      assert oldClassName.charAt(0) == 'L';
      String binaryName = NamingUtils.signatureToBinaryName(oldClassName);
      ClassMapping cm = loader.oldToNewClassMapping.get(binaryName);
      if (cm != null) {
        // Convert old to new.
        return NamingUtils.binaryNameToSignature(cm.getClassName());
      } else {
        // No mapping so nothing to convert.
        return oldClassName;
      }
    }

    /**
     * Return the original method signature for the given signature or null if it is not part of
     * the mapping.
     */
    @CheckForNull
    public String getOriginalMethodSignature(@Nonnull String methodSignature) {
      assert newToOldMethodMap != null;
      assert oldToNewMethodMap == null;
      return newToOldMethodMap.get(methodSignature);
    }

    public void addMethod(@Nonnull String oldMethodDeclaration, @Nonnull String newMethodName) {
      assert oldToNewMethodMap != null;
      assert newToOldMethodMap == null;
      oldToNewMethodMap.put(oldMethodDeclaration, newMethodName);
    }

  }
}
