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

package com.android.jack.ir.formatter;

import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JNullType;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JType;

import javax.annotation.Nonnull;

/**
 * Provides formatted Strings for types as binary qualified names.
 * The format is for instance "package1/package2/Classname".
 */
public class BinaryQualifiedNameFormatter extends CharSeparatedPackageFormatter
    implements TypeFormatter, PackageFormatter {

  @Nonnull
  private static final BinaryQualifiedNameFormatter formatter = new BinaryQualifiedNameFormatter();

  protected BinaryQualifiedNameFormatter() {
  }

  @Override
  protected char getPackageSeparator() {
    return '/';
  }

  @Nonnull
  public static BinaryQualifiedNameFormatter getFormatter() {
    return formatter;
  }

  @Override
  @Nonnull
  public String getName(@Nonnull JType type) {
    if (type instanceof JClassOrInterface) {
      return getClassOrInterfaceName((JClassOrInterface) type);
    } else if (type instanceof JArrayType) {
      return getName(((JArrayType) type).getElementType()) + "[]";
    } else if (type instanceof JNullType) {
      return "null";
    } else if (type instanceof JPrimitiveType) {
      switch (((JPrimitiveType) type).getPrimitiveTypeEnum()) {
        case BOOLEAN:
          return "boolean";
        case BYTE:
          return "byte";
        case CHAR:
          return "char";
        case DOUBLE:
          return "double";
        case FLOAT:
          return "float";
        case INT:
          return "int";
        case LONG:
          return "long";
        case SHORT:
          return "short";
        case VOID:
          return "void";
        default:
          throw new AssertionError();
      }
    } else {
      throw new AssertionError();
    }
  }

  @Nonnull
  private String getClassOrInterfaceName(@Nonnull JClassOrInterface type) {
    JPackage enclosingPackage = type.getEnclosingPackage();
    assert enclosingPackage != null;
    return getName(enclosingPackage, type.getName());
  }

  @Override
  @Nonnull
  public String getName(@Nonnull JPackage pack) {
    return getNameInternal(pack).toString();
  }

  @Override
  @Nonnull
  public String getName(
      @Nonnull JPackage enclosingPackage, @Nonnull String classOrInterfaceSimpleName) {
    StringBuilder sb;
    if (!enclosingPackage.isDefaultPackage()) {
      sb = getNameInternal(enclosingPackage);
      sb.append(getPackageSeparator());
    } else {
      sb = new StringBuilder();
    }
    sb.append(classOrInterfaceSimpleName);
    return sb.toString();
  }
}
