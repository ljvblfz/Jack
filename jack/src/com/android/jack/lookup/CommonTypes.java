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

package com.android.jack.lookup;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.formatter.TypeFormatter;

import javax.annotation.Nonnull;

/**
 * This class lists common java types to lookup.
 */
public class CommonTypes {

  private static final String JAVA_LANG_ASSERTION_ERROR_SIGNATURE = "Ljava/lang/AssertionError;";
  private static final String JAVA_UTIL_CONCURRENT_ATOMIC_ATOMICREFERENCEFIELDUPDATER_SIGNATURE =
      "Ljava/util/concurrent/atomic/AtomicReferenceFieldUpdater;";
  private static final String JAVA_UTIL_CONCURRENT_ATOMIC_ATOMICINTEGERFIELDUPDATER_SIGNATURE =
      "Ljava/util/concurrent/atomic/AtomicIntegerFieldUpdater;";
  private static final String JAVA_UTIL_CONCURRENT_ATOMIC_ATOMICLONGFIELDUPDATER_SIGNATURE =
      "Ljava/util/concurrent/atomic/AtomicLongFieldUpdater;";
  private static final String JAVA_LANG_STRING_SIGNATURE = "Ljava/lang/String;";
  private static final String JAVA_LANG_STRING_BUILDER_SIGNATURE = "Ljava/lang/StringBuilder;";
  private static final String JAVA_LANG_CLASS_SIGNATURE = "Ljava/lang/Class;";
  private static final String JAVA_LANG_ENUM_SIGNATURE = "Ljava/lang/Enum;";
  private static final String JAVA_LANG_OBJECT_SIGNATURE = "Ljava/lang/Object;";
  private static final String JAVA_IO_SERIALIZABLE_SIGNATURE = "Ljava/io/Serializable;";
  private static final String JAVA_LANG_CLONEABLE_SIGNATURE = "Ljava/lang/Cloneable;";
  private static final String JAVA_LANG_BOOLEAN_SIGNATURE = "Ljava/lang/Boolean;";
  private static final String JAVA_LANG_BYTE_SIGNATURE = "Ljava/lang/Byte;";
  private static final String JAVA_LANG_CHAR_SIGNATURE = "Ljava/lang/Character;";
  private static final String JAVA_LANG_SHORT_SIGNATURE = "Ljava/lang/Short;";
  private static final String JAVA_LANG_INTEGER_SIGNATURE = "Ljava/lang/Integer;";
  private static final String JAVA_LANG_FLOAT_SIGNATURE = "Ljava/lang/Float;";
  private static final String JAVA_LANG_DOUBLE_SIGNATURE = "Ljava/lang/Double;";
  private static final String JAVA_LANG_LONG_SIGNATURE = "Ljava/lang/Long;";
  private static final String JAVA_LANG_VOID_SIGNATURE = "Ljava/lang/Void;";

  private static final TypeFormatter formatter = Jack.getLookupFormatter();

  /** Common type identifier */
  public enum CommonType {
    ASSERTION_ERROR(JAVA_LANG_ASSERTION_ERROR_SIGNATURE),
    ATOMICREFERENCEFIELDUPDATER(JAVA_UTIL_CONCURRENT_ATOMIC_ATOMICREFERENCEFIELDUPDATER_SIGNATURE),
    ATOMIC_ATOMICINTEGERFIELDUPDATER(
        JAVA_UTIL_CONCURRENT_ATOMIC_ATOMICINTEGERFIELDUPDATER_SIGNATURE),
    ATOMIC_ATOMICLONGFIELDUPDATER(JAVA_UTIL_CONCURRENT_ATOMIC_ATOMICLONGFIELDUPDATER_SIGNATURE),
    STRING(JAVA_LANG_STRING_SIGNATURE),
    STRING_BUILDER(JAVA_LANG_STRING_BUILDER_SIGNATURE),
    CLASS(JAVA_LANG_CLASS_SIGNATURE),
    ENUM(JAVA_LANG_ENUM_SIGNATURE),
    OBJECT(JAVA_LANG_OBJECT_SIGNATURE),
    SERIALIZABLE(JAVA_IO_SERIALIZABLE_SIGNATURE),
    CLONEABLE(JAVA_LANG_CLONEABLE_SIGNATURE),
    BOOLEAN(JAVA_LANG_BOOLEAN_SIGNATURE),
    BYTE(JAVA_LANG_BYTE_SIGNATURE),
    CHAR(JAVA_LANG_CHAR_SIGNATURE),
    SHORT(JAVA_LANG_SHORT_SIGNATURE),
    INTEGER(JAVA_LANG_INTEGER_SIGNATURE),
    FLOAT(JAVA_LANG_FLOAT_SIGNATURE),
    DOUBLE(JAVA_LANG_DOUBLE_SIGNATURE),
    LONG(JAVA_LANG_LONG_SIGNATURE),
    VOID(JAVA_LANG_VOID_SIGNATURE);

    @Nonnull
    private final String signature;

    private CommonType(@Nonnull String signature) {
      this.signature = signature;
    }

    @Nonnull
    String getSignature() {
      return signature;
    }

    @Override
    public String toString() {
      return signature;
    }
  }

  public static final CommonType JAVA_LANG_ASSERTION_ERROR = CommonType.ASSERTION_ERROR;
  public static final CommonType JAVA_UTIL_CONCURRENT_ATOMIC_ATOMICREFERENCEFIELDUPDATER =
      CommonType.ATOMICREFERENCEFIELDUPDATER;
  public static final CommonType JAVA_UTIL_CONCURRENT_ATOMIC_ATOMICINTEGERFIELDUPDATER =
      CommonType.ATOMIC_ATOMICINTEGERFIELDUPDATER;
  public static final CommonType JAVA_UTIL_CONCURRENT_ATOMIC_ATOMICLONGFIELDUPDATER =
      CommonType.ATOMIC_ATOMICLONGFIELDUPDATER;
  public static final CommonType JAVA_LANG_STRING = CommonType.STRING;
  public static final CommonType JAVA_LANG_STRING_BUILDER = CommonType.STRING_BUILDER;
  public static final CommonType JAVA_LANG_CLASS = CommonType.CLASS;
  public static final CommonType JAVA_LANG_ENUM = CommonType.ENUM;
  public static final CommonType JAVA_LANG_OBJECT = CommonType.OBJECT;
  public static final CommonType JAVA_IO_SERIALIZABLE = CommonType.SERIALIZABLE;
  public static final CommonType JAVA_LANG_CLONEABLE = CommonType.CLONEABLE;
  public static final CommonType JAVA_LANG_BOOLEAN = CommonType.BOOLEAN;
  public static final CommonType JAVA_LANG_BYTE = CommonType.BYTE;
  public static final CommonType JAVA_LANG_CHAR = CommonType.CHAR;
  public static final CommonType JAVA_LANG_SHORT = CommonType.SHORT;
  public static final CommonType JAVA_LANG_INTEGER = CommonType.INTEGER;
  public static final CommonType JAVA_LANG_FLOAT = CommonType.FLOAT;
  public static final CommonType JAVA_LANG_DOUBLE = CommonType.DOUBLE;
  public static final CommonType JAVA_LANG_LONG = CommonType.LONG;
  public static final CommonType JAVA_LANG_VOID = CommonType.VOID;

  public static boolean isCommonType(@Nonnull CommonType commonType, @Nonnull JType type) {
    return commonType.getSignature().equals(formatter.getName(type));
  }

}
