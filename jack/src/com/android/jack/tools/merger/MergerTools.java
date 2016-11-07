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

import com.android.jack.dx.dex.file.ValueEncoder.ValueType;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.dx.io.EncodedValueCodec;
import com.android.jack.dx.io.EncodedValueReader;
import com.android.jack.dx.rop.cst.Constant;
import com.android.jack.dx.rop.cst.CstBoolean;
import com.android.jack.dx.rop.cst.CstByte;
import com.android.jack.dx.rop.cst.CstChar;
import com.android.jack.dx.rop.cst.CstDouble;
import com.android.jack.dx.rop.cst.CstFloat;
import com.android.jack.dx.rop.cst.CstInteger;
import com.android.jack.dx.rop.cst.CstKnownNull;
import com.android.jack.dx.rop.cst.CstLong;
import com.android.jack.dx.rop.cst.CstShort;
import com.android.jack.dx.rop.cst.CstString;
import com.android.jack.dx.util.ByteInput;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Tools to merge dex structures.
 */
public class MergerTools {

  /**
   * A tool to build {@link Constant} arrays.
   */
  protected static final class ConstantValueArrayBuilder extends EncodedValueReader {

    @Nonnegative
    private int cstIndex = 0;

    @CheckForNull
    private Constant[] constantValues;

    public ConstantValueArrayBuilder(@Nonnull DexBuffer dex, @Nonnull ByteInput in) {
      super(dex, in);
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
    protected void visitString(int index) {
      addConstant(new CstString(dexBuffer.strings().get(index)));
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
    protected void visitMethod(int index) {
      throw new AssertionError("Unsupported encoded value.");
    }

    @Override
    protected void visitType(int index) {
      throw new AssertionError("Unsupported encoded value.");
    }

    @Override
    protected void visitMethodType(int index) {
      throw new AssertionError("Unsupported encoded value.");
    }

    @Override
    protected void visitPrimitive(int type, int arg, int size) {
      addConstant(createConstant(in, type, arg));
    }

    private void addConstant(@Nonnull Constant cst) {
      assert constantValues != null;
      constantValues[cstIndex++] = cst;
    }
  }

  @Nonnull
  public static Constant createConstant(@Nonnull ByteInput in, @Nonnegative int type, int arg)  {
    Constant cst;

    switch (ValueType.getValueType(type)) {
      case VALUE_BYTE: {
        cst = CstByte.make((byte) EncodedValueCodec.readSignedInt(in, arg));
        break;
      }
      case VALUE_CHAR: {
        cst = CstChar.make((char) EncodedValueCodec.readUnsignedInt(in, arg, false));
        break;
      }
      case VALUE_SHORT: {
        cst = CstShort.make((short) EncodedValueCodec.readSignedInt(in, arg));
        break;
      }
      case VALUE_INT: {
        cst = CstInteger.make(EncodedValueCodec.readSignedInt(in, arg));
        break;
      }
      case VALUE_LONG: {
        cst = CstLong.make(EncodedValueCodec.readSignedLong(in, arg));
        break;
      }
      case VALUE_FLOAT: {
        cst = CstFloat.make(EncodedValueCodec.readUnsignedInt(in, arg, true));
        break;
      }
      case VALUE_DOUBLE: {
        cst = CstDouble.make(EncodedValueCodec.readUnsignedLong(in, arg, true));
        break;
      }
      default: {
        throw new AssertionError();
      }
    }

    return cst;
  }
 }
