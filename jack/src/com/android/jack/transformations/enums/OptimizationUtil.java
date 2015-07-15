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
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.JNodeLookup;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.enums.opt.OptimizedSwitchEnumSupport;
import com.android.jack.transformations.request.TransformationRequest;

import javax.annotation.Nonnull;

/**
 * This class provides basic APIs to use during {@link OptimizedSwitchEnumSupport}.
 */
public class OptimizationUtil {
  // prefix for the name of synthetic switch map field
  public static final String ShorterPrefix = "-";

  // prefix for the name of synthetic switch map initializing method
  public static final String LongerPrefix = "-get";

  // suffix for both the synthetic switch map field and initializing method
  public static final String Suffix = "SwitchesValues";

  // the method name of Enum.name()
  public static final String Name = "name";

  // the method name of Object.equals()
  public static final String Equals = "equals";

  // the method name of Enum.ordinal()
  public static final String Ordinal = "ordinal";

  // the method name of Enum.values()
  public static final String Values = "values";

  // the method name of Enum.valueOf()
  public static final String ValueOf = "valueOf";

  // node lookup utility used to lookup a type in current execution
  private final JNodeLookup nodeLookup;

  // field not found exception type (java.lang.NoSuchFieldError)
  private JDefinedClass noSuchFieldErrorType;

  // class not found exception type (java.lang.ClassNotFoundException)
  private JDefinedClass classNotFoundExceptionType;

  // runtime exception type (java.lang.RuntimeException)
  private JDefinedClass runtimeExceptionType;

  // java/lang/Object type (java.lang.Object)
  private JDefinedClass objectType;

  // java/lang/Class type (java.lang.Class)
  private JDefinedClass classType;

  // java/lang/String type (java.lang.String)
  private JDefinedClass stringType;

  // java/lang/Enum type (java.lang.Enum)
  private JDefinedClass enumType;

  // int primitive type
  private JPrimitiveType primitiveIntType;

  // boolean primitive type
  private JPrimitiveType primitiveBooleanType;

  // void type
  private JPrimitiveType voidType;

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
  public JNodeLookup getLookup() {
    if (nodeLookup == null) {
      // lookup is not ready yet
      throw new AssertionError();
    }
    return nodeLookup;
  }

  /**
   * Get the internal data structure representing the type of NoSuchFieldError.
   *
   * @return Type of NoSuchFieldError
   */
  public synchronized JDefinedClass getNoSuchFieldErrorType() {
    if (noSuchFieldErrorType == null) {
      noSuchFieldErrorType = nodeLookup.getClass("Ljava/lang/NoSuchFieldError;");
    }
    return noSuchFieldErrorType;
  }

  /**
   * Get the internal data structure representing the type of ClassNotFoundException.
   *
   * @return Type of ClassNotFoundException
   */
  public synchronized JDefinedClass getClassNotFoundExceptionType() {
    if (classNotFoundExceptionType == null) {
      classNotFoundExceptionType = nodeLookup.getClass("Ljava/lang/ClassNotFoundException;");
    }
    return classNotFoundExceptionType;
  }

  /**
   * Get the internal data structure representing the type of RuntimeException.
   *
   * @return Type of RuntimeException
   */
  public synchronized JDefinedClass getRuntimeExceptionErrorType() {
    if (runtimeExceptionType == null) {
      runtimeExceptionType =  nodeLookup.getClass("Ljava/lang/RuntimeException;");
    }
    return runtimeExceptionType;
  }

  /**
   * Get the internal data structure representing the type of java.lang.Object.
   *
   * @return Type of java.lang.Object
   */
  public synchronized JDefinedClass getObjectType() {
    if (objectType == null) {
      objectType = nodeLookup.getClass("Ljava/lang/Object;");
    }
    return objectType;
  }

  /**
   * Get the internal data structure representing the type of java.lang.Class.
   *
   * @return Type of java.lang.Class
   */
  public synchronized JDefinedClass getClassType() {
    if (classType == null) {
      classType = nodeLookup.getClass("Ljava/lang/Class;");
    }
    return classType;
  }

  /**
   * Get the internal data structure representing the type of java.lang.String.
   *
   * @return Type of java.lang.String
   */
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
  public synchronized JDefinedClass getEnumType() {
    if (enumType == null) {
      enumType = nodeLookup.getClass("Ljava/lang/Enum;");
    }
    return enumType;
  }

  /**
   * Get the internal data structure representing the type of primitive int.
   *
   * @return Type of primitive int
   */
  public synchronized JPrimitiveType getPrimitiveIntType() {
    if (primitiveIntType == null) {
      primitiveIntType = JPrimitiveTypeEnum.INT.getType();
    }
    return primitiveIntType;
  }

  /**
   * Get the internal data structure representing the type of primitive boolean.
   *
   * @return Type of primitive boolean
   */
  public synchronized JPrimitiveType getPrimitiveBooleanType() {
    if (primitiveBooleanType == null) {
      primitiveBooleanType = JPrimitiveTypeEnum.BOOLEAN.getType();
    }
    return primitiveBooleanType;
  }

  /**
   * Get the internal data structure representing the type of void.
   *
   * @return Type of void
   */
  public synchronized JPrimitiveType getPrimitiveVoidType() {
    if (voidType == null) {
      voidType = JPrimitiveTypeEnum.VOID.getType();
    }
    return voidType;
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
        && method.getType().isSameType(getPrimitiveIntType().getArray());
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
        && field.getType().isSameType(getPrimitiveIntType().getArray());
  }

  /**
   * Get the method name initializing switch map, e.g., given A/B/C/Enum1, the method
   * will return the method name -getA_B_C_Enum1SwitchValues.
   * @param enumType The enum the synthetic method is associated with
   *
   * @return The synthetic initializer method name
   */
  public static String getSyntheticSwitchMapInitializerName(JDefinedEnum enumType) {
    String enumName = Jack.getLookupFormatter().getName(enumType);
    // remove the first char 'L' and last one ';'
    enumName = enumName.substring(1, enumName.length() - 1).replace('/', '_');
    return LongerPrefix + enumName + Suffix;
  }

  /**
   * The field name of switch map, e.g., given enum A/B/C/Enum1, the method will
   * return the method name -A_B_C_Enum1SwitchValues.
   * @param enumType The enum to which switch map is associated
   *
   * @return The synthetic field name
   */
  public static String getSyntheticSwitchMapFieldName(JDefinedEnum enumType) {
    // full class name including package, e.g., LA/B/EnumSwitchesValues;
    String fullEnumName = Jack.getLookupFormatter().getName(enumType);
    fullEnumName = fullEnumName.substring(1, fullEnumName.length() - 1);
    // get the field name e.g., -A_B_EnumSwitchesValues
    String fieldName = OptimizationUtil.ShorterPrefix + fullEnumName.replace('/', '_')
        + OptimizationUtil.Suffix;
    return fieldName;
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

  /**
   * Always create new local with specified type, and add it into local set of body.
   * @param localCreator The local creator
   * @param transformRequest Transformation request used to add/delete/modify code
   * @param localType The type local is attached to
   *
   * @return The newly created local
   */
  public static JLocal newLocal(@Nonnull LocalVarCreator localCreator,
      @Nonnull TransformationRequest transformRequest, @Nonnull JType localType) {
    return localCreator.createTempLocal(localType, SourceInfo.UNKNOWN, transformRequest);
  }

  /**
   * Get the left hand side local variable of given statement. Throw exception if it
   * doesn't have.
   * @param stmt The target statement
   *
   * @return The lhs variable of given stmt
   * @throws RunTimeException if current statement doesn't have lhs local variable
   */
  public static JLocal getLhs(@Nonnull JStatement stmt) {
    if (!(stmt instanceof JExpressionStatement)) {
      throw new AssertionError("Cannot get the lhs local from non-expression stmt: " + stmt);
    }
    JExpression expr = ((JExpressionStatement) stmt).getExpr();
    if (!(expr instanceof JAsgOperation)) {
      throw new AssertionError("Cannot get the lhs local from non-assignment expr: " + expr);
    }
    JExpression lhsExpr = ((JAsgOperation) expr).getLhs();
    if (!(lhsExpr instanceof JLocalRef)) {
      throw new AssertionError("Cannot get the lhs local from expr whose lhs is not local: "
          + expr);
    }
    return ((JLocalRef) lhsExpr).getLocal();
  }
}
