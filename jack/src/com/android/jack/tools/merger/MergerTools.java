/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.android.jack.tools.merger;

import com.android.jack.dx.io.ClassData.Field;
import com.android.jack.dx.io.ClassData.Method;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.dx.io.EncodedValueCodec;
import com.android.jack.dx.io.EncodedValueReader;
import com.android.jack.dx.io.FieldId;
import com.android.jack.dx.io.MethodId;
import com.android.jack.dx.io.ProtoId;
import com.android.jack.dx.rop.cst.Constant;
import com.android.jack.dx.rop.cst.CstBoolean;
import com.android.jack.dx.rop.cst.CstByte;
import com.android.jack.dx.rop.cst.CstChar;
import com.android.jack.dx.rop.cst.CstDouble;
import com.android.jack.dx.rop.cst.CstEnumRef;
import com.android.jack.dx.rop.cst.CstFieldRef;
import com.android.jack.dx.rop.cst.CstFloat;
import com.android.jack.dx.rop.cst.CstInteger;
import com.android.jack.dx.rop.cst.CstKnownNull;
import com.android.jack.dx.rop.cst.CstLong;
import com.android.jack.dx.rop.cst.CstMethodRef;
import com.android.jack.dx.rop.cst.CstNat;
import com.android.jack.dx.rop.cst.CstShort;
import com.android.jack.dx.rop.cst.CstString;
import com.android.jack.dx.rop.cst.CstType;
import com.android.jack.dx.rop.type.Type;
import com.android.jack.dx.util.ByteInput;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class MergerTools {

  @Nonnull
  protected CstMethodRef getCstMethodRef(@Nonnull DexBuffer dex, @Nonnegative int methodIdx) {
    MethodId methodId = dex.methodIds().get(methodIdx);
    return getCstMethodRef(dex, methodId);
  }

  @Nonnull
  protected CstMethodRef getCstMethodRef(@Nonnull DexBuffer dex, @Nonnull Method method) {
    MethodId methodId = dex.methodIds().get(method.getMethodIndex());
    return getCstMethodRef(dex, methodId);
  }

  @Nonnull
  protected CstMethodRef getCstMethodRef(@Nonnull DexBuffer dex, @Nonnull MethodId methodId) {
    return new CstMethodRef(getCstTypeFromTypeIndex(dex, methodId.getDeclaringClassIndex()),
        getCstNatFromMethodId(dex, methodId));
  }

  protected CstEnumRef getCstEnumRef(DexBuffer dex, int fieldIdx) {
    return new CstEnumRef(getCstNatFromFieldId(dex, dex.fieldIds().get(fieldIdx)));
  }

  @Nonnull
  protected CstFieldRef getCstFieldRef(@Nonnull DexBuffer dex, @Nonnegative int fieldIdx) {
    FieldId fieldId = dex.fieldIds().get(fieldIdx);
    return getCstFieldRef(dex, fieldId);
  }

  @Nonnull
  protected CstFieldRef getCstFieldRef(@Nonnull DexBuffer dex, @Nonnull Field field) {
    FieldId fieldId = dex.fieldIds().get(field.getFieldIndex());
    return getCstFieldRef(dex, fieldId);
  }

  @Nonnull
  protected CstFieldRef getCstFieldRef(@Nonnull DexBuffer dex, @Nonnull FieldId fieldId) {
    return new CstFieldRef(getCstTypeFromTypeIndex(dex, fieldId.getDeclaringClassIndex()),
        getCstNatFromFieldId(dex, fieldId));
  }

  @Nonnull
  protected CstNat getCstNatFromMethodId(@Nonnull DexBuffer dex, @Nonnull MethodId methodId) {
    ProtoId protoId = dex.protoIds().get(methodId.getProtoIndex());
    return new CstNat(getCstStringFromIndex(dex, methodId.getNameIndex()),
        new CstString(getProtoString(protoId, dex)));
  }

  @Nonnull
  protected String getProtoString(@Nonnull ProtoId protoId, @Nonnull DexBuffer dex) {
    return dex.readTypeList(protoId.getParametersOffset()) + dex.typeNames().get(
        protoId.getReturnTypeIndex());
  }

  @Nonnull
  protected CstNat getCstNatFromFieldId(@Nonnull DexBuffer dex, @Nonnull FieldId fieldId) {
    return new CstNat(getCstStringFromIndex(dex, fieldId.getNameIndex()),
        getCstStringFromTypeIndex(dex, fieldId.getTypeIndex()));
  }

  @Nonnull
  protected CstString getCstStringFromIndex(@Nonnull DexBuffer dex, @Nonnegative int stringIdx) {
    String str = dex.strings().get(stringIdx);
    return new CstString(str);
  }

  @Nonnull
  protected CstString getCstStringFromTypeIndex(@Nonnull DexBuffer dex, @Nonnegative int typeIdx) {
    String typeNameDesc = dex.typeNames().get(typeIdx);
    return new CstString(typeNameDesc);
  }

  @Nonnull
  protected Type getTypeFromTypeIndex(@Nonnull DexBuffer dex, @Nonnegative int typeIdx) {
    String typeNameDesc = dex.typeNames().get(typeIdx);
    return Type.intern(typeNameDesc);
  }

  @Nonnull
  protected CstType getCstTypeFromTypeIndex(@Nonnull DexBuffer dex, @Nonnegative int typeIdx) {
    String typeNameDesc = dex.typeNames().get(typeIdx);
    return CstType.intern(Type.intern(typeNameDesc));
  }

  @Nonnull
  protected CstType getCstTypeFromTypeName(@Nonnull String typeNameDesc) {
    return CstType.intern(Type.intern(typeNameDesc));
  }


  protected static final class ConstantValueArrayBuilder extends EncodedValueReader {

    @Nonnegative
    private int cstIndex = 0;

    @CheckForNull
    private Constant[] constantValues;

    @Nonnull
    private final DexBuffer dex;

    public ConstantValueArrayBuilder(@Nonnull DexBuffer dex, @Nonnull ByteInput in) {
      super(in);
      this.dex = dex;
    }

    @Nonnegative
    public int getCstSize() {
      assert constantValues != null;
      return constantValues.length;
    }

    @Nonnull
    public Constant getCstValueAtIdx(@Nonnegative int idx) {
      assert constantValues != null;
      assert constantValues[idx] != null;
      return constantValues[idx];
    }

    @Override
    protected void visitArray(int size) {
      constantValues = new Constant[size];
    }

    @Override
    protected void visitEncodedBoolean(int argAndType) {
      int arg = (argAndType & 0xe0) >> 5;
      addConstant(CstBoolean.make(arg));
    }

    @Override
    protected void visitString(int type, int index) {
      addConstant(new CstString(dex.strings().get(index)));
    }

    @Override
    protected void visitEncodedNull(int argAndType) {
      addConstant(CstKnownNull.THE_ONE);
    }

    @Override
    protected void visitAnnotation(int typeIndex, int size) {
      throw new AssertionError("Unsupported encoded value.");
    }

    @Override
    protected void visitAnnotationName(int nameIndex) {
      throw new AssertionError("Unsupported encoded value.");
    }

    @Override
    protected void visitAnnotationValue(int argAndType) {
      throw new AssertionError("Unsupported encoded value.");
    }

    @Override
    protected void visitArrayValue(int argAndType) {
      throw new AssertionError("Unsupported encoded value.");
    }

    @Override
    protected void visitField(int type, int index) {
      throw new AssertionError("Unsupported encoded value.");
    }


    @Override
    protected void visitMethod(int type, int index) {
      throw new AssertionError("Unsupported encoded value.");
    }

    @Override
    protected void visitType(int type, int index) {
      throw new AssertionError("Unsupported encoded value.");
    }

    @Override
    protected void visitPrimitive(int argAndType, int type, int arg, int size) {
      Constant cst;

      switch (type) {
        case ENCODED_BYTE: {
          cst = CstByte.make((byte) EncodedValueCodec.readSignedInt(in, arg));
          break;
        }
        case ENCODED_CHAR: {
          cst = CstChar.make((char) EncodedValueCodec.readUnsignedInt(in, arg, false));
          break;
        }
        case ENCODED_SHORT: {
          cst = CstShort.make((short) EncodedValueCodec.readSignedInt(in, arg));
          break;
        }
        case ENCODED_INT: {
          cst = CstInteger.make(EncodedValueCodec.readSignedInt(in, arg));
          break;
        }
        case ENCODED_LONG: {
          cst = CstLong.make(EncodedValueCodec.readSignedLong(in, arg));
          break;
        }
        case ENCODED_FLOAT: {
          cst = CstFloat.make(EncodedValueCodec.readUnsignedInt(in, arg, true));
          break;
        }
        case ENCODED_DOUBLE: {
          cst = CstDouble.make(EncodedValueCodec.readUnsignedLong(in, arg, true));
          break;
        }
        default: {
          throw new AssertionError();
        }
      }

      addConstant(cst);
    }

    private void addConstant(@Nonnull Constant cst) {
      assert constantValues != null;
      constantValues[cstIndex++] = cst;
    }
  }
 }
