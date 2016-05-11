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

  /**
   * Return a string representing the strict valid name for generated files and which does not
   * conflict with name coming from Java source files. The difference between this API and
   * {@link #getNonSourceConflictingName} is this API replaces '/' with '-'.
   */
  @Nonnull
  public static String getStrictNonSourceConflictingName(@Nonnull String name) {
    return NON_SOURCE_CONFLICTING_CHAR
        + name.replace(JLookup.PACKAGE_SEPARATOR, NON_SOURCE_CONFLICTING_CHAR);
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

  /**
   * Return the binary name of the given source symbol name. The symbol can be a package or a type.
   * For example, calling that method with "foo.bar.MyClass" will return "foo/bar/MyClass".
   *
   * @param srcSymbolName the source name of a symbol (package, type, ...)
   * @return the corresponding binary name
   */
  @Nonnull
  public static String getBinaryName(@Nonnull String srcSymbolName) {
    return srcSymbolName.replace(PACKAGE_SOURCE_SEPARATOR, JLookup.PACKAGE_SEPARATOR);
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

  public static boolean isTypeIdentifier(@Nonnull String name) {
    int nameLength = name.length();
    if (nameLength == 0) {
      return false;
    }
    for (int i = 1; i < nameLength; i++) {
      char curChar = name.charAt(i);
      if (curChar == '.' || curChar == ';' || curChar == '[' || curChar == '/') {
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
    return isClassOrPackageName(name.toCharArray(), 0, name.length(), JLookup.PACKAGE_SEPARATOR,
        PACKAGE_SOURCE_SEPARATOR);
  }

  public static boolean isPackageSourceName(@Nonnull String name) {
    return isClassOrPackageName(name.toCharArray(), 0, name.length(), PACKAGE_SOURCE_SEPARATOR,
        JLookup.PACKAGE_SEPARATOR);
  }

  public static boolean isClassSourceName(@Nonnull String name) {
    return isClassOrPackageName(name.toCharArray(), 0, name.length(), PACKAGE_SOURCE_SEPARATOR,
        JLookup.PACKAGE_SEPARATOR);
  }

  public static boolean isFullMethodName(@Nonnull String fullName) {
    int seperatorIdx = fullName.indexOf('#');
    char[] buffer = fullName.toCharArray();
    if (seperatorIdx == -1) {
      return false;
    }
    if (!isClassOrPackageName(
        buffer, 0, seperatorIdx, PACKAGE_SOURCE_SEPARATOR, JLookup.PACKAGE_SEPARATOR)) {
      return false;
    }
    return isValidJavaIdentifier(buffer, seperatorIdx + 1, buffer.length);
  }

  public static boolean isValidJavaIdentifier(
      @Nonnull char[] buffer, @Nonnegative int pos, @Nonnegative int length) {
    if (pos == length) {
      return false;
    }
    if (!Character.isJavaIdentifierStart(buffer[pos])) {
      return false;
    }
    for (int i = pos + 1; i < length; i++) {
      if (!Character.isJavaIdentifierPart(buffer[i])) {
        return false;
      }
    }
    return true;
  }

  private static boolean isClassOrPackageName(
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
