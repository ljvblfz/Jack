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

package com.android.jack.ir.naming;

import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.ir.formatter.BinarySignatureFormatter;
import com.android.jack.ir.formatter.SourceFormatter;
import com.android.jack.ir.formatter.TypeFormatter;

import javax.annotation.Nonnull;

/**
 * An {@link AbstractName} referencing a type. This implementation is not thread-safe.
 * If multiple threads modify the referenced type, it must be synchronized externally.
 */
public class TypeName extends AbstractName {

  @Nonnull
  private static final TypeFormatter binaryQnFormatter =
      BinaryQualifiedNameFormatter.getFormatter();
  @Nonnull
  private static final TypeFormatter binarySignatureFormatter =
      BinarySignatureFormatter.getFormatter();
  @Nonnull
  private static final TypeFormatter sourceQnFormatter = SourceFormatter.getFormatter();

  /**
   * kind of literal type representation.
   */
  public enum Kind {
    BINARY_SIGNATURE,   // means Ljava/lang/Object;
    SRC_SIGNATURE,      // means Ljava.lang.Object;
    BINARY_QN,          // means java/lang/Object
    SRC_QN,             // means java.lang.Object
    SIMPLE_NAME         // means d when type name is a/b/c/e$1d or a/b/c/e$1d or a.b.c$1d
                        // or d when type name is a/b/c/e$d or a/b/c/e$d or a.b.c.d or a.b.c$d
  }

  @Nonnull
  private final JType type;

  @Nonnull
  private final Kind kind;

  public TypeName(@Nonnull Kind kind, @Nonnull JType type) {
    assert kind != Kind.SIMPLE_NAME || type instanceof JClassOrInterface;
    this.kind = kind;
    this.type = type;
  }

  @Nonnull
  private static String getValue(@Nonnull JType type, @Nonnull Kind kind) {
    if (type instanceof JArrayType) {
      switch (kind) {
        case BINARY_SIGNATURE:
        case SRC_SIGNATURE:
          return "[" + getValue(((JArrayType) type).getElementType(), kind);
        case BINARY_QN:
        case SRC_QN:
          return getValue(((JArrayType) type).getElementType(), kind) + "[]";
        default: {
          throw new AssertionError();
        }
      }
    } else {
      switch (kind) {
        case BINARY_SIGNATURE:
          return binarySignatureFormatter.getName(type);
        case SRC_SIGNATURE:
          return binarySignatureFormatter.getName(type).replace('/', '.');
        case BINARY_QN:
          assert type instanceof JClassOrInterface;
          return binaryQnFormatter.getName(type);
        case SRC_QN:
          assert type instanceof JClassOrInterface;
          return sourceQnFormatter.getName(type);
        case SIMPLE_NAME:
          return getSimpleName((JDefinedClassOrInterface) type);
        default: {
          throw new AssertionError();
        }
      }
    }
  }

  @Override
  @Nonnull
  public String toString() {
    return getValue(type, kind);
  }

  @Nonnull
  public JType getReferencedType() {
    return type;
  }

  @Nonnull
  public static String getSimpleName(@Nonnull JDefinedClassOrInterface type) {
    String simpleName = type.getName();
    JClassOrInterface enclosingType = type.getEnclosingType();

    if (enclosingType != null && simpleName.startsWith(enclosingType.getName() + '$')) {
      // Remove enclosing type name from simpleName
      int simpleNameBeginIndex = enclosingType.getName().length() + 1;

      // Simple name of Foo$1Bar is Bar, it happens when there is another class named Foo$Bar
      while (simpleNameBeginIndex < simpleName.length()
          && Character.isDigit(simpleName.charAt(simpleNameBeginIndex))) {
        simpleNameBeginIndex++;
      }

      simpleName = simpleName.substring(simpleNameBeginIndex);
    }

    return simpleName;
  }
}
