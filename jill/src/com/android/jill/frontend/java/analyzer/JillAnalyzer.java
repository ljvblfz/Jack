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

package com.android.jill.frontend.java.analyzer;

import com.google.common.base.Strings;

import com.android.jill.JillException;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

import javax.annotation.Nonnull;

/**
 * Jill analyzer computes types by separating arrays and references, instead of using a single
 * "object reference" type. Nevertheless it does not compute always the exact type of object
 * reference but uses {@Code java.lang.Object} when exact type computation requires to load
 * classes.
 */
public class JillAnalyzer extends BasicInterpreter {

  @Override
  public BasicValue newValue(Type type) {
    if (type == null) {
      return BasicValue.UNINITIALIZED_VALUE;
    }
    switch (type.getSort()) {
      case Type.VOID:
        return null;
      case Type.BOOLEAN:
      case Type.CHAR:
      case Type.BYTE:
      case Type.SHORT:
      case Type.INT:
        return BasicValue.INT_VALUE;
      case Type.FLOAT:
        return BasicValue.FLOAT_VALUE;
      case Type.LONG:
        return BasicValue.LONG_VALUE;
      case Type.DOUBLE:
        return BasicValue.DOUBLE_VALUE;
      case Type.ARRAY:
      case Type.OBJECT:
        return new BasicValue(type);
      default:
        throw new Error("Internal error");
    }
  }


  @Override
  public BasicValue binaryOperation(AbstractInsnNode insn, BasicValue value1, BasicValue value2)
      throws AnalyzerException {
    if (insn.getOpcode() == AALOAD) {
      // value1 means array reference.
      Type arrayType = value1.getType();
      String arrayTypeDesc = arrayType.getDescriptor();
      if (arrayType.getSort() == Type.ARRAY) {
        return newValue(Type.getType(arrayTypeDesc.substring(1)));
      } else if ("Lnull;".equals(arrayTypeDesc)) {
        return value1;
      }

      throw new JillException("Fails to retrieve array element value");
    }
    return super.binaryOperation(insn, value1, value2);
  }

  @Override
  public BasicValue merge(final BasicValue v, final BasicValue w) {
    assert v != null;
    assert w != null;

    if (v.equals(w) || v == BasicValue.UNINITIALIZED_VALUE) {
      return v;
    } else if (w == BasicValue.UNINITIALIZED_VALUE) {
      return BasicValue.UNINITIALIZED_VALUE;
    } else {
      Type type1 = v.getType();
      Type type2 = w.getType();

      if ((type1.getSort() == Type.ARRAY && type2.getSort() == Type.OBJECT
          && !isKnownNull(type2))
          || (type1.getSort() == Type.OBJECT && !isKnownNull(type1) &&
          type2.getSort() == Type.ARRAY)) {
        return BasicValue.REFERENCE_VALUE;
      } else if ((type1.getSort() == Type.ARRAY || isKnownNull(type1))
          && (type2.getSort() == Type.ARRAY || isKnownNull(type2))) {
        if (isKnownNull(type1)) {
          return w;
        } else if (isKnownNull(type2)) {
          return v;
        } else {
          BasicValue componentUnion =
              merge(new BasicValue(type1.getElementType()), new BasicValue(type2.getElementType()));
          if (componentUnion == BasicValue.UNINITIALIZED_VALUE) {
            return BasicValue.REFERENCE_VALUE;
          }
          return new BasicValue(Type.getType(
              Strings.repeat("[", Math.min(type1.getDimensions(), type2.getDimensions()))
              + componentUnion.getType().getDescriptor()));
        }
      } else if (type1.getSort() == Type.OBJECT && type2.getSort() == Type.OBJECT) {
        if (isKnownNull(type1)) {
          return w;
        } else if (isKnownNull(type2)) {
          return v;
        } else {
          return BasicValue.REFERENCE_VALUE;
        }
      } else if (isIntLike(type1) && isIntLike(type2)) {
        return BasicValue.INT_VALUE;
      } else {
        return BasicValue.UNINITIALIZED_VALUE;
      }
    }
  }

  private boolean isIntLike(@Nonnull Type t) {
    return t == Type.BOOLEAN_TYPE || t == Type.BYTE_TYPE || t == Type.CHAR_TYPE
        || t == Type.SHORT_TYPE || t == Type.INT_TYPE;
  }

  private boolean isKnownNull(@Nonnull Type t) {
    return ("Lnull;".equals(t.getDescriptor()));
  }
}
