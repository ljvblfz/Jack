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

package com.android.jack.transformations.enums.opt;

import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.util.NamingTools;

import javax.annotation.Nonnull;

/**
 * This class provides basic APIs for switch enum optimization.
 */
public class OptimizationUtil {
  // prefix for the name of synthetic switch map field. The switch map field
  // name follows the style: -$PACKAGE$-$SUBPACKAGE4-$SUBSUBPACKAGE$SwitchesValues
  private static final char ShorterPrefix = '-';

  // prefix for the name of synthetic switch map initializing method. The
  // switch map initializer name follows the style:
  // static int[] -get$PACKAGE$-$SUBPACKAGE4-$SUBSUBPACKAGE$SwitchesValues()
  @Nonnull
  private static final String LongerPrefix = "-get";

  // suffix for both the synthetic switch map field and initializing method
  @Nonnull
  private static final String Suffix = "SwitchesValues";

  public OptimizationUtil() {}

  /**
   * Determine if the given method is synthetic switch map initializer method.
   * @param method the input method to check
   *
   * @return true if given method is synthetic initializer. The synthetic
   * switch map initializer should match the signature static int[] -getXYZSwitchesValues
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
   * @param field The input field to check
   *
   * @return true if the given field is synthetic switch map field. The synthetic
   * switch map field should match the signature static int[] -XYZSwitchesValues
   */
  public boolean isSyntheticSwitchMapField(@Nonnull JField field) {
    String fieldName = field.getName();
    int modifier = field.getModifier();
    return fieldName.endsWith(Suffix) && JModifier.isSynthetic(modifier)
        && JModifier.isPrivate(modifier) && JModifier.isStatic(modifier)
        && field.getType().isSameType(JPrimitiveTypeEnum.INT.getType().getArray());
  }

  /**
   * Get the switch map initializer related to the given enum type, e.g., given
   * A/B/C/Enum1, the method will return the method name -getA-B-C-Enum1SwitchValues.
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
   * The field name of switch map related to the given enum type, e.g., given
   * enum A/B/C/Enum1, the method will return the method name -A-B-C-Enum1SwitchValues.
   * @param enumType The enum to which switch map is associated
   *
   * @return The synthetic field name
   */
  @Nonnull
  public static String getSyntheticSwitchMapFieldName(@Nonnull JDefinedEnum enumType) {
    String enumName = BinaryQualifiedNameFormatter.getFormatter().getName(enumType);
    return NamingTools.getStrictNonSourceConflictingName(enumName + Suffix);
  }

  /**
   * Given a synthetic field, this method will return the enum type string related to this
   * field.
   * @param syntheticField the switch map initializer int[] field
   *
   * @return the related enum type
   */
  @Nonnull
  public static String getEnumNameFromSyntheticField(@Nonnull JField syntheticField) {
    String fieldName = syntheticField.getName().replace(ShorterPrefix, '/');
    return "L" + fieldName.substring(1, fieldName.length() - Suffix.length()) + ";";
  }
}
