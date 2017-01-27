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

import javax.annotation.Nonnull;

/**
 * Utilities for class names.
 */
public class NamingUtils {

  /**
   * Convert fully qualified name (primitive or reference) to binary name.
   *
   * @param typeName the fully-qualified name to convert
   * @return the binary name of the class
   */
  @Nonnull
  public static String fqNameToBinaryName(@Nonnull String typeName) {
    assert typeName.indexOf('/') < 0;
    return typeName.replace('.', '/');
  }

  @Nonnull
  public static String binaryNameToFqName(@Nonnull String binaryName) {
    assert binaryName.indexOf('.') < 0;
    return binaryName.replace('/', '.');
  }

  /**
   * Converts the fully qualified name to a signature.
   */
  @Nonnull
  public static String fqNameToSignature(@Nonnull String typeName) {
    int arrayPos = typeName.lastIndexOf('[');
    if (arrayPos > 0) {
      assert arrayPos == typeName.length() - 2;
      assert typeName.charAt(arrayPos + 1) == ']';
      return '[' + fqNameToSignature(typeName.substring(0, arrayPos));
    } else if (typeName.equals("boolean")) {
      return "Z";
    } else if (typeName.equals("byte")) {
      return "B";
    } else if (typeName.equals("char")) {
      return "C";
    } else if (typeName.equals("short")) {
      return "S";
    } else if (typeName.equals("int")) {
      return "I";
    } else if (typeName.equals("float")) {
      return "F";
    } else if (typeName.equals("double")) {
      return "D";
    } else if (typeName.equals("long")) {
      return "J";
    } else if (typeName.equals("void")) {
      return "V";
    } else {
      return binaryNameToSignature(fqNameToBinaryName(typeName));
    }
  }

  @Nonnull
  public static String binaryNameToSignature(@Nonnull String binaryName) {
    assert binaryName.indexOf('.') < 0;
    return 'L' + binaryName + ';';
  }

  @Nonnull
  public static String signatureToBinaryName(@Nonnull String signature) {
    assert signature.length() > 2;  // at least one character between 'L' and ';';
    assert signature.charAt(0) == 'L';
    int lastCharPos = signature.length() - 1;
    assert lastCharPos > 1;
    assert signature.charAt(lastCharPos) == ';';
    return signature.substring(1, lastCharPos);
  }
}
