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
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JNullType;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JType;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Provides formatted Strings for types and methods as binary signatures.
 * For types, the format is for instance "Lpackage1/package2/Classname;" or "[I".
 * For methods, the format is for instance "methodname(ILpackage1/package2/Classname;)B".
 */
public class BinarySignatureFormatter extends CharSeparatedPackageFormatter
    implements TypeAndMethodFormatter {

  private static final char PACKAGE_SEPARATOR = '/';

  @Nonnull
  private static final BinarySignatureFormatter formatter = new BinarySignatureFormatter();

  protected BinarySignatureFormatter() {
  }

  @Nonnull
  public static BinarySignatureFormatter getFormatter() {
    return formatter;
  }

  @Override
  @Nonnull
  public String getName(@Nonnull JType type) {
    if (type instanceof JClassOrInterface) {
      return getClassOrInterfaceName((JClassOrInterface) type);
    } else if (type instanceof JArrayType) {
      return "[" + getName(((JArrayType) type).getElementType());
    } else if (type instanceof JNullType) {
      return "N";
    } else if (type instanceof JPrimitiveType) {
      switch (((JPrimitiveType) type).getPrimitiveTypeEnum()) {
        case BOOLEAN:
          return "Z";
        case BYTE:
          return "B";
        case CHAR:
          return "C";
        case DOUBLE:
          return "D";
        case FLOAT:
          return "F";
        case INT:
          return "I";
        case LONG:
          return "J";
        case SHORT:
          return "S";
        case VOID:
          return "V";
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
  public String getName(
      @Nonnull JPackage enclosingPackage, @Nonnull String classOrInterfaceSimpleName) {
    StringBuilder sb = new StringBuilder("L");
    if (!enclosingPackage.isDefaultPackage()) {
      sb.append(getNameInternal(enclosingPackage));
      sb.append(PACKAGE_SEPARATOR);
    }
    sb.append(classOrInterfaceSimpleName).append(";");
    return sb.toString();
  }

  @Override
  @Nonnull
  public String getName(@Nonnull JMethod method) {
    StringBuilder sb = new StringBuilder();
    sb.append(method.getName());
    sb.append('(');

    for (JParameter p : method.getParams()) {
      sb.append(getName(p.getType()));
    }

    sb.append(')');
    sb.append(getName(method.getType()));

    return sb.toString();
  }

  @Override
  @Nonnull
  public String getNameWithoutReturnType(@Nonnull JMethodId methodId) {
    return getNameWithoutReturnType(methodId.getName(), methodId.getParamTypes());
  }

  @Override
  @Nonnull
  public String getName(@Nonnull String methodName, @Nonnull List<? extends JType> argumentTypes,
      @Nonnull JType returnType) {
    return getNameWithoutReturnType(methodName, argumentTypes) + getName(returnType);
  }

  @Override
  @Nonnull
  public String getNameWithoutReturnType(
      @Nonnull String methodName, @Nonnull List<? extends JType> argumentTypes) {
    StringBuilder sb = new StringBuilder();
    sb.append(methodName);
    sb.append('(');

    for (JType argumentType : argumentTypes) {
      sb.append(getName(argumentType));
    }

    sb.append(')');

    return sb.toString();
  }

  @Override
  protected char getPackageSeparator() {
    return PACKAGE_SEPARATOR;
  }

}
