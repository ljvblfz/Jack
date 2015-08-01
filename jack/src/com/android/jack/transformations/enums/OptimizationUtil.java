/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.transformations.enums;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JNodeLookup;
import com.android.jack.transformations.enums.opt.OptimizedSwitchEnumSupport;
import com.android.jack.util.NamingTools;

import javax.annotation.Nonnull;

/**
 * This class provides basic APIs to use during {@link OptimizedSwitchEnumSupport}.
 */
public class OptimizationUtil {
  // prefix for the name of synthetic switch map field
  private static final char ShorterPrefix = '-';

  // prefix for the name of synthetic switch map initializing method
  private static final String LongerPrefix = "-get";

  // suffix for both the synthetic switch map field and initializing method
  private static final String Suffix = "SwitchesValues";

  // the method name of Enum.ordinal()
  public static final String Ordinal = "ordinal";

  // the method name of Enum.values()
  public static final String Values = "values";

  // node lookup utility used to lookup a type in current execution
  private final JNodeLookup nodeLookup;

  // field not found exception type (java.lang.NoSuchFieldError)
  private JDefinedClass noSuchFieldErrorType;

  // java/lang/Object type (java.lang.Object)
  private JDefinedClass objectType;

  // java/lang/String type (java.lang.String)
  private JDefinedClass stringType;

  // java/lang/Enum type (java.lang.Enum)
  private JDefinedClass enumType;

  // java/lang/Class type
  private JClass javaLangClass;

  /**
   * Constructor.
   * @param lookup The NodeLookup utility
   */
  public OptimizationUtil(@Nonnull JNodeLookup lookup) {
    nodeLookup = lookup;
  }

  /**
   * Get the utility which provides APIs to convert string to JType.
   *
   * @return JNodeLookup
   */
  @Nonnull
  public JNodeLookup getLookup() {
    assert nodeLookup != null;
    return nodeLookup;
  }

  /**
   * Get the internal data structure representing the type of NoSuchFieldError.
   *
   * @return Type of NoSuchFieldError
   */
  @Nonnull
  public synchronized JDefinedClass getNoSuchFieldErrorType() {
    if (noSuchFieldErrorType == null) {
      noSuchFieldErrorType = nodeLookup.getClass("Ljava/lang/NoSuchFieldError;");
    }
    return noSuchFieldErrorType;
  }

  /**
   * Get the internal data structure representing the type of java.lang.Object.
   *
   * @return Type of java.lang.Object
   */
  @Nonnull
  public synchronized JDefinedClass getObjectType() {
    if (objectType == null) {
      objectType = nodeLookup.getClass("Ljava/lang/Object;");
    }
    return objectType;
  }

  /**
   * Get the internal data structure representing the type of java.lang.String.
   *
   * @return Type of java.lang.String
   */
  @Nonnull
  public synchronized JDefinedClass getStringType() {
    if (stringType == null) {
      stringType = nodeLookup.getClass("Ljava/lang/String;");
    }
    return stringType;
  }

  /**
   * Get the internal data structure representing the type of java.lang.Enum.
   *
   * @return Type of java.lang.Enum
   */
  @Nonnull
  public synchronized JDefinedClass getEnumType() {
    if (enumType == null) {
      enumType = nodeLookup.getClass("Ljava/lang/Enum;");
    }
    return enumType;
  }

  /**
   * Get the internal data structure representing the type of java.lang.Class.
   * @return Type of java.lang.Class
   */
  public synchronized JClass getJavaLangClass() {
    if (javaLangClass == null) {
      javaLangClass = Jack.getSession().getPhantomLookup().getClass(
          CommonTypes.JAVA_LANG_CLASS);
    }
    return javaLangClass;
  }

  /**
   * Determine if the given method is synthetic switch map initializer method.
   * @param method The method to check
   *
   * @return true if given method is synthetic initializer
   */
  public boolean isSyntheticSwitchMapInitializer(@Nonnull JMethod method) {
    String methodName = method.getName();
    int modifier = method.getModifier();
    return methodName.startsWith(LongerPrefix) && methodName.endsWith(Suffix)
        && method.getParams().isEmpty() && JModifier.isSynthetic(modifier)
        && JModifier.isPublic(modifier) && JModifier.isStatic(modifier)
        && method.getType().isSameType(JPrimitiveTypeEnum.INT.getType().getArray());
  }

  /**
   * Determine if the given field is synthetic switch map field whose type is int[].
   * @param field The field to check
   *
   * @return true if the given field is synthetic switch map field
   */
  public boolean isSyntheticSwitchMapField(@Nonnull JField field) {
    String fieldName = field.getName();
    int modifier = field.getModifier();
    return fieldName.endsWith(Suffix) && JModifier.isSynthetic(modifier)
        && JModifier.isPrivate(modifier) && JModifier.isStatic(modifier)
        && field.getType().isSameType(JPrimitiveTypeEnum.INT.getType().getArray());
  }

  /**
   * Get the method name initializing switch map, e.g., given A/B/C/Enum1, the method
   * will return the method name -getA-B-C-Enum1SwitchValues.
   * @param enumType The enum the synthetic method is associated with
   *
   * @return The synthetic initializer method name
   */
  @Nonnull
  public static String getSyntheticSwitchMapInitializerName(@Nonnull JDefinedEnum enumType) {
    String enumName = BinaryQualifiedNameFormatter.getFormatter().getName(enumType);
    return NamingTools.getStrictNonSourceConflictingName("get" + enumName + Suffix);
  }

  /**
   * The field name of switch map, e.g., given enum A/B/C/Enum1, the method will
   * return the method name -A-B-C-Enum1SwitchValues.
   * @param enumType The enum to which switch map is associated
   *
   * @return The synthetic field name
   */
  @Nonnull
  public static String getSyntheticSwitchMapFieldName(@Nonnull JDefinedEnum enumType) {
    // full class name including package, e.g., LA/B/EnumSwitchesValues;
    String enumName = BinaryQualifiedNameFormatter.getFormatter().getName(enumType);
    return NamingTools.getStrictNonSourceConflictingName(enumName + Suffix);
  }

  /**
   * Given a synthetic field, this method will return the enum type string related to this
   * field.
   * @param syntheticField the int[] field in synthetic switch map class
   *
   * @return the corresponding enum type
   */
  public static String getEnumNameFromSyntheticField(@Nonnull JField syntheticField) {
    String fieldName = syntheticField.getName().replace(ShorterPrefix, '/');
    return "L" + fieldName.substring(1,
        fieldName.length() - OptimizationUtil.Suffix.length()) + ";";
  }

  /**
   * Set the method body and update its parent in the AST.
   * @param method The owner method of parameter body
   * @param body The method body of synthetic initializer
   *
   * @return Return true if this process is successful, otherwise return false
   */
  public static boolean setMethodBody(@Nonnull JMethod method, @Nonnull JMethodBody body) {
    method.setBody(body);
    body.updateParents(method);
    return true;
  }
}
