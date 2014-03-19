/*
 * Copyright (C) 2013 The Android Open Source Project
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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Implements a parser for type signatures.
 * <pre>
 *
 * FieldTypeSignature ::=
 *     ClassTypeSignature | ArrayTypeSignature | TypeVariableSignature.
 * ArrayTypeSignature ::= "[" TypSignature.
 *
 * ClassTypeSignature ::=
 *     "L" {Ident "/"}* Ident ";".
 *
 *
 * TypSignature ::= FieldTypeSignature | BaseType.
 * BaseType ::= "B" | "C" | "D" | "F" | "I" | "J" | "S" | "Z".
 *
 * ReturnType ::= TypSignature | VoidDescriptor.
 * VoidDescriptor ::= "V".
 * </pre>
 */
public class SignatureParser {

  private SignatureParser() {

  }

  public static boolean isTypeSignature(@Nonnull String signature) {
    int length = signature.length();
    if (length == 0) {
      return false;
    }

    char[] buffer = signature.toCharArray();
    // [V is not valid.
    if (buffer[0] == 'V') {
      return length == 1;
    }

    return isTypeSignatureWithoutVoid(buffer, 0, length);
  }

  private static boolean isTypeSignatureWithoutVoid(
      @Nonnull char[] buffer, @Nonnegative int pos, @Nonnegative int length) {
    if (pos >= length) {
      return false;
    }
    switch (buffer[pos++]) {
      case 'B':
      case 'C':
      case 'D':
      case 'F':
      case 'I':
      case 'J':
      case 'S':
      case 'Z':
        return pos == length;
      case 'L':
        return isClassSignature(buffer, pos, length);
      case '[':
        return isTypeSignatureWithoutVoid(buffer, pos, length);
      default:
        return false;
    }
  }

  public static boolean isClassSignature(@Nonnull String signature) {
    char[] buffer = signature.toCharArray();
    int length = buffer.length;
    if (length < 3 || buffer[0] != 'L') {
      return false;
    }

    return isClassSignature(buffer, 1, length);
  }

  private static boolean isClassSignature(
      @Nonnull char[] buffer, @Nonnegative int pos, @Nonnegative int length) {
    // The beginning "L" character must already have been parsed.
    char ch;
    do {
      // First letter of an Ident (an Ident can't be empty)
      if (pos >= length) {
        return false;
      }

      ch = buffer[pos++];
      if (isInvalidChar(ch) || ch == '/' || ch == ';') {
        return false;
      }

      // Next letters of an Ident
      do {
        if (pos >= length) {
          return false;
        }

        ch = buffer[pos++];
        if (isInvalidChar(ch)) {
          return false;
        }
      } while (ch != '/' && ch != ';');

    } while (ch != ';');

    return pos == length;
  }

  private static boolean isInvalidChar(char ch) {
    switch (ch) {
      case '.':
      case '[':
        return true;
    }
    return false;
  }
}

