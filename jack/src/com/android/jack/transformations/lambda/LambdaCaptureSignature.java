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

package com.android.jack.transformations.lambda;

import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.util.NamingTools;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents lambda capture signature, which is a tuple of (z, b, s, i, j, f, d, c, r)
 * specifying the number of captured variables of different primitive types (z = boolean,
 * b = byte, ..., c = char) or reference types (r).
 */
final class LambdaCaptureSignature {
  @Nonnegative
  private static final int R_INDEX = JPrimitiveType.JPrimitiveTypeEnum.VOID.ordinal();

  /**
   * The array contains values of 'z'..'c' from the tuple indexed by
   * JPrimitiveTypeEnum ordinal. It also includes the element for VOID type
   * which is not part of the tuple, and thus is being used for storing
   * 'r' element from the tuple.
   */
  @Nonnull
  private final int[] tuple;

  private LambdaCaptureSignature(@Nonnull int[] tuple) {
    this.tuple = tuple;
  }

  /** Create a capture signature for a given lambda */
  @Nonnull
  static LambdaCaptureSignature forLambda(@Nonnull JLambda lambda) {
    int[] tuple = new int[JPrimitiveType.JPrimitiveTypeEnum.values().length];

    // Compute counts for all primitive and reference types
    for (JExpression capture : lambda.getCapturedVariables()) {
      JType fieldType = capture.getType();
      if (fieldType instanceof JPrimitiveType) {
        int index = ((JPrimitiveType) fieldType).getPrimitiveTypeEnum().ordinal();
        tuple[index]++;
      } else {
        tuple[R_INDEX]++;
      }
    }

    return new LambdaCaptureSignature(tuple);
  }

  /**
   * Creates mapping of the indices of the original lambda captures into
   * indices of the correspondent capture fields (and constructor parameters)
   * of the signature, and therefore lambda group class.
   *
   * For example, if the captures [v0, v1, v2] of the original lambda map into
   * fields [#2, #0, #1] of the capture signature, the result will be: [1, 2, 0].
   */
  @Nonnull
  int[] createMapping(@Nonnull JLambda lambda) {
    // Starting indices of the capture field groups by type
    int length = tuple.length;
    int[] indices = new int[length];
    for (int i = 1; i < length; i++) {
      indices[i] = indices[i - 1] + tuple[i - 1];
    }

    List<JExpression> vars = lambda.getCapturedVariables();
    assert vars.size() == (indices[length - 1] + tuple[length - 1]);

    int[] result = new int[vars.size()];

    for (int k = 0; k < vars.size(); k++) {
      JType fieldType = vars.get(k).getType();
      if (fieldType instanceof JPrimitiveType) {
        int index = ((JPrimitiveType) fieldType).getPrimitiveTypeEnum().ordinal();
        result[k] = indices[index]++;
      } else {
        result[k] = indices[R_INDEX]++;
      }
    }

    return result;
  }

  /** Create fields for this capture signature */
  @Nonnull
  List<JField> createFields(@Nonnull JDefinedClass clazz, @Nonnull JType objType) {
    List<JField> fields = new ArrayList<>();
    JPrimitiveType.JPrimitiveTypeEnum[] primitiveTypes =
        JPrimitiveType.JPrimitiveTypeEnum.values();
    for (int i = 0; i < tuple.length; i++) {
      JType fieldType = i == R_INDEX ? objType : primitiveTypes[i].getType();
      addFields(tuple[i], clazz, fieldType, fields);
    }
    return fields;
  }

  private void addFields(@Nonnegative int count,
      @Nonnull JDefinedClass clazz, @Nonnull JType type, @Nonnull List<JField> fields) {
    for (int i = 0; i < count; i++) {
      fields.add(new JField(SourceInfo.UNKNOWN,
          NamingTools.getNonSourceConflictingName("$f" + fields.size()),
          clazz, type, JModifier.PRIVATE | JModifier.FINAL | JModifier.SYNTHETIC));
    }
  }

  /** A short id that can be used to uniquely identify and sort capture signatures */
  @Nonnull
  String getUniqueId() {
    StringBuilder builder = new StringBuilder();
    for (int element : tuple) {
      builder.append(Integer.toString(element, 16)).append(';');
    }
    return builder.toString();
  }
}
