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

package com.android.jack.backend.dex.rop;

import com.android.jack.dx.rop.code.SourcePosition;
import com.android.jack.dx.rop.cst.CstFieldRef;
import com.android.jack.dx.rop.cst.CstMethodRef;
import com.android.jack.dx.rop.cst.CstNat;
import com.android.jack.dx.rop.cst.CstString;
import com.android.jack.dx.rop.cst.CstType;
import com.android.jack.dx.rop.type.StdTypeList;
import com.android.jack.dx.rop.type.Type;
import com.android.jack.dx.rop.type.TypeList;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JNullType;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReferenceType;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.formatter.InternalFormatter;
import com.android.jack.ir.formatter.TypeAndMethodFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.scheduling.feature.SourceVersion8;
import com.android.jack.transformations.lambda.CapturedVariable;
import com.android.jack.transformations.lambda.ForceClosureMarker;
import com.android.jack.transformations.lambda.NeedsLambdaMarker;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Optional;
import com.android.sched.schedulable.ToSupport;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Utilities for dx API uses when building dex structures and rop methods.
 */
@Optional(@ToSupport(feature = SourceVersion8.class,
    add = @Constraint(need = {CapturedVariable.class, NeedsLambdaMarker.class})))
public class RopHelper {

  @Nonnull
  private static TypeAndMethodFormatter formatterWithoutClosure = new RopFormatterWithoutClosure();

  @Nonnull
  private static RopFormatterWithClosure formatterWithClosure = new RopFormatterWithClosure();

  /**
   * Builds a {@code CstMethodRef} from a {@code JMethod}.
   *
   * @param method The {@code JMethod} used to build a {@code CstMethodRef}.
   * @return The built {@code CstMethodRef}.
   */
  @Nonnull
  public static CstMethodRef createMethodRef(@Nonnull JMethod method) {
    return createMethodRef(method.getEnclosingType(), method);
  }

  /**
   * Builds a {@code CstMethodRef} from a {@code JMethod}.
   *
   * @param type The class in which to search the method.
   * @param method The {@code JMethod} used to build a {@code CstMethodRef}.
   * @return The built {@code CstMethodRef}.
   */
  @Nonnull
  public static CstMethodRef createMethodRef(@Nonnull JReferenceType type,
      @Nonnull JMethod method) {
    CstType definingClass = RopHelper.getCstType(type);
    CstNat nat = createSignature(method);
    CstMethodRef methodRef = new CstMethodRef(definingClass, nat);
    return methodRef;
  }

  /**
   * Builds a {@code CstMethodRef} from a {@code JSymbolicCall}.
   *
   * @param methodCall The {@code JSymbolicCall} used to build a {@code CstMethodRef}.
   * @return The built {@code CstMethodRef}.
   */
  @Nonnull
  public static CstMethodRef createMethodRef(@Nonnull JMethodCall methodCall) {
    CstType definingClass = RopHelper.getCstType(methodCall.getReceiverType());
    String signatureWithoutName = getMethodSignatureWithoutName(methodCall);
    CstNat nat =
        new CstNat(new CstString(methodCall.getMethodName()), new CstString(signatureWithoutName));
    CstMethodRef methodRef = new CstMethodRef(definingClass, nat);
    return methodRef;
  }

  /**
   * Builds a {@code CstFieldRef} from a {@code JField}.
   *
   * @param field The {@code JField} used to build a {@code CstFieldRef}.
   * @param receiverType The {@code JDeclaredType} in which the field is accessed
   * @return The built {@code CstFieldRef}.
   */
  @Nonnull
  public static CstFieldRef createFieldRef(@Nonnull JField field,
      @Nonnull JClassOrInterface receiverType) {
    return createFieldRef(field.getId(), receiverType);
  }
  @Nonnull
  public static CstFieldRef createFieldRef(@Nonnull JFieldId field,
      @Nonnull JClassOrInterface receiverType) {
    CstType definingClass = getCstType(receiverType);
    CstNat nat = createSignature(field);
    CstFieldRef fieldRef = new CstFieldRef(definingClass, nat);
    return fieldRef;
  }

  /**
   * Builds a {@code CstString} from a {@code JAbstractStringLiteral}.
   *
   * @param string The {@code JAbstractStringLiteral} used to build a {@code CstString}.
   * @return The built {@code CstString}.
   */
  @Nonnull
  public static CstString createString(@Nonnull JAbstractStringLiteral string) {
    CstString res = new CstString(string.getValue());
    return res;
  }

  /**
   * Builds a {@code CstString} from a {@code String}.
   *
   * @param string The {@code String} used to build a {@code CstString}.
   * @return The built {@code CstString}.
   */
  @Nonnull
  public static CstString createString(@Nonnull String string) {
    CstString res = new CstString(string);
    return res;
  }

  @Nonnull
  public static String getMethodSignatureWithoutName(@Nonnull JMethodCall call) {
    StringBuilder sb = new StringBuilder();
    sb.append('(');

    for (JType p : call.getMethodId().getParamTypes()) {
      sb.append(formatterWithClosure.getName(p, /*isForceClosure=*/ false));
    }

    sb.append(')');
    sb.append(formatterWithClosure.getName(call.getType(), /*isForceClosure=*/ false));

    return sb.toString();

  }

  /**
   * Builds a {@code SourcePosition} for a {@code JNode}.
   *
   * @param stmt The statement used to extract source position information.
   * @return The built {@code SourcePosition}.
   */
  @Nonnull
  public static SourcePosition getSourcePosition(@Nonnull JNode stmt) {
    if (stmt.getSourceInfo() != SourceInfo.UNKNOWN) {
      return (new SourcePosition(new CstString(stmt.getSourceInfo().getFileName()), -1,
          stmt.getSourceInfo().getStartLine()));
    } else {
      return (new SourcePosition(null, -1, -1));
    }

  }

  /**
   * Converts a {@code JType} into a {@code Type} of dx.
   *
   * @param type The {@code JType} to convert.
   * @return The dx type representing the {@code JType}.
   */
  @Nonnull
  public static Type convertTypeToDx(@Nonnull JType type) {
    return convertTypeToDx(type, /*isForceClosure=*/ false);
  }

  @Nonnull
  public static Type convertTypeToDx(@Nonnull JType type, boolean isForcedClosure) {
    if (JNullType.isNullType(type)) {
      return Type.KNOWN_NULL;
    }

    if (type instanceof JPrimitiveType) {
      JPrimitiveType jPrimitiveType = (JPrimitiveType) type;
      JPrimitiveTypeEnum primitiveType = jPrimitiveType.getPrimitiveTypeEnum();
      switch (primitiveType) {
        case BOOLEAN:
          return Type.BOOLEAN;
        case BYTE:
          return Type.BYTE;
        case CHAR:
          return Type.CHAR;
        case DOUBLE:
          return Type.DOUBLE;
        case FLOAT:
          return Type.FLOAT;
        case INT:
          return Type.INT;
        case LONG:
          return Type.LONG;
        case SHORT:
          return Type.SHORT;
        case VOID:
          return Type.VOID;
      }
      throw new AssertionError(jPrimitiveType.toSource() + " not supported.");
    } else {
      return Type.intern(formatterWithClosure.getName(type, isForcedClosure));
    }
  }

    @Nonnull
    public static Type convertTypeToDxWithoutClosure(@Nonnull JType type) {
      if (JNullType.isNullType(type)) {
        return Type.KNOWN_NULL;
      }

      if (type instanceof JPrimitiveType) {
        JPrimitiveType jPrimitiveType = (JPrimitiveType) type;
        JPrimitiveTypeEnum primitiveType = jPrimitiveType.getPrimitiveTypeEnum();
        switch (primitiveType) {
          case BOOLEAN:
            return Type.BOOLEAN;
          case BYTE:
            return Type.BYTE;
          case CHAR:
            return Type.CHAR;
          case DOUBLE:
            return Type.DOUBLE;
          case FLOAT:
            return Type.FLOAT;
          case INT:
            return Type.INT;
          case LONG:
            return Type.LONG;
          case SHORT:
            return Type.SHORT;
          case VOID:
            return Type.VOID;
        }
        throw new AssertionError(jPrimitiveType.toSource() + " not supported.");
      } else {
        return Type.intern(formatterWithoutClosure.getName(type));
       }
     }

  /**
   * Builds a constant name and type ({@code CstNat}) from a {@code JMethod}
   *
   * @param method The {@code JMethod} used to build a {@code CstNat}.
   * @return The built {@code CstNat}.
   */
  @Nonnull
  private static CstNat createSignature(@Nonnull JMethod method) {
    CstString name = new CstString(method.getName());
    CstString descriptor = new CstString(formatterWithClosure.getName(method));
    CstNat signature = new CstNat(name, descriptor);
    return signature;
  }

  /**
   * Builds a constant name and type ({@code CstNat}) from a {@code JField}
   *
   * @param field The {@code JField} used to build a {@code CstNat}.
   * @return The built {@code CstNat}.
   */
  @Nonnull
  public static CstNat createSignature(@Nonnull JField field) {
    return createSignature(field.getId());
  }

  @Nonnull
  public static CstNat createSignature(@Nonnull JFieldId field) {
    String fieldName = field.getName();
    String fieldSignature =
        formatterWithClosure.getName(field.getType(), /* isForceClosure= */ false);
    // STOPSHIP: Do not generate closure for field until field lambda opcode are supported by the
    // runtime
    if (fieldSignature.startsWith("\\")) {
      fieldSignature = "L" + fieldSignature.substring(1);
    }
    CstString name = new CstString(fieldName);
    CstString descriptor = new CstString(fieldSignature);
    CstNat signature = new CstNat(name, descriptor);
    return signature;
  }

  /**
   * Return true if type are compatible, for example as source and destination of a {@code mov}
   * instruction.
   */
  public static boolean areTypeCompatible(@Nonnull Type type1, @Nonnull Type type2) {
    return (type1.getBasicFrameType() == type2.getBasicFrameType());
  }

  /**
   * Converts a list of {@code JType} to a non-null {@code TypeList}.
   *
   * @param types a non-null {@code List} of {@code JType}s.
   * @return a non-null {@code TypeList} of types.
   * @throws NullPointerException if {@code types} is null
   */
  @Nonnull
  public static TypeList createTypeList(@Nonnull List<? extends JType> types) {
    StdTypeList typesList = StdTypeList.EMPTY;
    final int elementsCount = types.size();
    if (elementsCount > 0) {
      typesList = new StdTypeList(elementsCount);
      for (int i = 0; i < elementsCount; ++i) {
        JType type = types.get(i);
        typesList.set(i, convertTypeToDx(type));
      }
    }
    return typesList;
  }

  /**
   * Converts a {@code JType} to a {@code CstType}.
   *
   * @param type a non-null {@code JType}.
   * @throws NullPointerException if given type is null.
   */
  @Nonnull
  public static CstType getCstType(@Nonnull JType type) {
    Type ropType = convertTypeToDx(type);
    CstType cstType = CstType.intern(ropType);
    return cstType;
  }

  private static class RopFormatterWithoutClosure extends InternalFormatter {

    /**
     * Gets method signature without method's name
     */
    @Override
    @Nonnull
    public String getName(@Nonnull JMethod method) {
      StringBuilder sb = new StringBuilder();
      sb.append('(');

      for (JParameter p : method.getParams()) {
        // STOPSHIP: Generate captured variables as parameters needs a runtime with the new
        // create-lambda opcode
        if (p.getMarker(CapturedVariable.class) != null) {
          continue;
        }
        sb.append(getName(p.getType()));
      }

      sb.append(')');
      sb.append(getName(method.getType()));

      return sb.toString();
    }
  }

  private static class RopFormatterWithClosure extends RopFormatterWithoutClosure {

    @Nonnull
    public String getName(@Nonnull JType type, boolean isForcedClosure) {
      if (type instanceof JClassOrInterface) {
        if (type instanceof JDefinedInterface
            && (((JDefinedInterface) type).getMarker(NeedsLambdaMarker.class) != null
                || isForcedClosure)) {
          JPackage enclosingPackage = ((JClassOrInterface) type).getEnclosingPackage();
          assert enclosingPackage != null;
          StringBuilder sb = new StringBuilder("\\");
          if (!enclosingPackage.isDefaultPackage()) {
            sb.append(getNameInternal(enclosingPackage));
            sb.append(getPackageSeparator());
          }
          sb.append(type.getName()).append(";");
          return sb.toString();
        }
      }

      return super.getName(type);
     }

    /**
     * Gets method signature without method's name
     */
    @Override
    @Nonnull
    public String getName(@Nonnull JMethod method) {
      StringBuilder sb = new StringBuilder();
      sb.append('(');

      for (JParameter p : method.getParams()) {
        // STOPSHIP: Generate captured variables as parameters needs a runtime with the new
        // create-lambda opcode
        if (p.getMarker(CapturedVariable.class) != null) {
          continue;
        }
        sb.append(getName(p.getType(), p.getMarker(ForceClosureMarker.class) != null));
      }

      sb.append(')');
      sb.append(getName(method.getType(), /*isForceClosure*/ false));

      return sb.toString();
    }
  }

}
