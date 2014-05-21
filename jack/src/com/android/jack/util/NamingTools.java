/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.util;


import com.android.jack.lookup.JLookup;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Class providing tools related to naming.
 */
public class NamingTools {

  private static final char NON_SOURCE_CONFLICTING_CHAR = '-';

  private static final char GENERATED_FILE_SUPPORTED_CHAR = '_';

  public static final char PACKAGE_SOURCE_SEPARATOR = '.';

  public static final String STATIC_INIT_NAME = "<clinit>";

  public static final String INIT_NAME = "<init>";

  /**
   * Return a string representing a valid name for generated files and which does not conflict with
   * name coming from Java source files.
   */
  @Nonnull
  public static String getNonSourceConflictingName(@Nonnull String name) {
    return NON_SOURCE_CONFLICTING_CHAR + getValidName(name);
  }

  public static boolean isNameSynthetic(@Nonnull String name) {
    return name.indexOf(NON_SOURCE_CONFLICTING_CHAR) != -1;
  }

  /**
   * Return a string representing a valid name for generated files.
   */
  @Nonnull
  public static String getValidName(@Nonnull String name) {
    // TODO(mikaelpeltier): Update method to reflect generated files specification.
    return (name.replace(JLookup.PACKAGE_SEPARATOR, GENERATED_FILE_SUPPORTED_CHAR));
  }

  @Nonnull
  public static String getTypeSignatureName(@Nonnull String typeName) {
    return ('L' + getBinaryName(typeName) + ';');
  }

  @Nonnull
  public static String getBinaryName(@Nonnull String srcTypeName) {
    return srcTypeName.replace('.', JLookup.PACKAGE_SEPARATOR);
  }

  @Nonnull
  public static String getClassBinaryNameFromDescriptor(@Nonnull String classDescriptor) {
    assert isClassDescriptor(classDescriptor) : "Invalid class descriptor "
        + classDescriptor;
    return classDescriptor.substring(1, classDescriptor.length() - 1);
  }

  @Nonnull
  public static String getSimpleClassNameFromBinaryName(@Nonnull String classBinaryName) {
    int simpleNameIndex = classBinaryName.lastIndexOf(JLookup.PACKAGE_SEPARATOR);
    return (simpleNameIndex < 0) ? classBinaryName : classBinaryName.substring(simpleNameIndex + 1);
  }

  @Nonnull
  public static String getSimpleClassNameFromDescriptor(@Nonnull String classDescriptor) {
    return getSimpleClassNameFromBinaryName(
        getClassBinaryNameFromDescriptor(classDescriptor));
  }

  @Nonnull
  public static String getPackageNameFromBinaryName(@Nonnull String binaryName) {
    int nameIndex = binaryName.lastIndexOf(JLookup.PACKAGE_SEPARATOR);
    return (nameIndex < 0) ? "" : binaryName.substring(0, nameIndex);
  }

  public static boolean isIdentifier(@Nonnull String name) {
    int nameLength = name.length();
    if (nameLength == 0) {
      return false;
    }
    if (!Character.isJavaIdentifierStart(name.charAt(0))) {
      return false;
    }
    for (int i = 1; i < nameLength; i++) {
      char curChar = name.charAt(i);
      if (!(Character.isJavaIdentifierPart(curChar) || curChar == '-')) {
        return false;
      }
    }
    return true;
  }

  public static boolean isClassDescriptor(@Nonnull String descriptor) {
    return SignatureParser.isClassSignature(descriptor);
  }

  public static boolean isTypeDescriptor(@Nonnull String descriptor) {
    return SignatureParser.isTypeSignature(descriptor);
  }

  public static boolean isPackageBinaryName(@Nonnull String name) {
    return isPackageName(name.toCharArray(), 0, name.length(), JLookup.PACKAGE_SEPARATOR,
        PACKAGE_SOURCE_SEPARATOR);
  }

  public static boolean isPackageSourceName(@Nonnull String name) {
    return isPackageName(name.toCharArray(), 0, name.length(), PACKAGE_SOURCE_SEPARATOR,
        JLookup.PACKAGE_SEPARATOR);
  }

  private static boolean isPackageName(
      @Nonnull char[] buffer, @Nonnegative int pos, @Nonnegative int length, char usedSeparator,
      char forbiddenSeparator) {
    while (pos < length) {
      // First letter of an Ident (an Ident can't be empty)
      char ch = buffer[pos++];
      if (ch == '[' || ch == forbiddenSeparator || ch == usedSeparator || ch == ';') {
        return false;
      }

      // Next letters of an Ident
      while (ch != usedSeparator && pos < length) {
        ch = buffer[pos++];
        if (ch == '[' || ch == forbiddenSeparator) {
          return false;
        }
      }

      // There is no package with a empty name (except the default package)
      if (ch == usedSeparator) {
        if (pos >= length) {
          return false;
        }
      }
    }

    return true;
  }
}
