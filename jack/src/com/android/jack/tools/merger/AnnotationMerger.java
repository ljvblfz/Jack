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

import com.android.jack.dx.dex.file.AnnotationItem;
import com.android.jack.dx.dex.file.ClassDefItem;
import com.android.jack.dx.io.Annotation;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.dx.io.DexBuffer.Section;
import com.android.jack.dx.io.EncodedValueCodec;
import com.android.jack.dx.io.EncodedValueReader;
import com.android.jack.dx.io.FieldId;
import com.android.jack.dx.rop.annotation.AnnotationVisibility;
import com.android.jack.dx.rop.annotation.Annotations;
import com.android.jack.dx.rop.annotation.AnnotationsList;
import com.android.jack.dx.rop.annotation.NameValuePair;
import com.android.jack.dx.rop.cst.Constant;
import com.android.jack.dx.rop.cst.CstAnnotation;
import com.android.jack.dx.rop.cst.CstArray;
import com.android.jack.dx.rop.cst.CstBoolean;
import com.android.jack.dx.rop.cst.CstByte;
import com.android.jack.dx.rop.cst.CstChar;
import com.android.jack.dx.rop.cst.CstDouble;
import com.android.jack.dx.rop.cst.CstEnumRef;
import com.android.jack.dx.rop.cst.CstFieldRef;
import com.android.jack.dx.rop.cst.CstFloat;
import com.android.jack.dx.rop.cst.CstIndexMap;
import com.android.jack.dx.rop.cst.CstInteger;
import com.android.jack.dx.rop.cst.CstKnownNull;
import com.android.jack.dx.rop.cst.CstLong;
import com.android.jack.dx.rop.cst.CstMethodRef;
import com.android.jack.dx.rop.cst.CstNat;
import com.android.jack.dx.rop.cst.CstShort;
import com.android.jack.dx.rop.cst.CstString;
import com.android.jack.dx.rop.cst.CstType;
import com.android.jack.dx.util.ByteInput;
import com.android.jack.dx.util.Leb128Utils;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A tool to merge dex annotations.
 */
public class AnnotationMerger extends MergerTools {

  @CheckForNull
  private CstIndexMap cstIndexMap;

  public void mergeAnnotationDirectory(@Nonnull DexBuffer dex,
      @Nonnegative int annotationDirectoryOffset, @Nonnull ClassDefItem newClassDef,
      @Nonnull CstIndexMap cstIndexMap) {
    this.cstIndexMap = cstIndexMap;
    Section directoryIn = dex.open(annotationDirectoryOffset);

    int classAnnotationSetOffset = directoryIn.readInt();
    if (classAnnotationSetOffset != 0) {
      newClassDef.setClassAnnotations(readAnnotationSet(dex, classAnnotationSetOffset));
    }

    int fieldsSize = directoryIn.readInt();

    int methodsSize = directoryIn.readInt();

    int parameterListSize = directoryIn.readInt();

    for (int i = 0; i < fieldsSize; i++) {
      CstFieldRef cstFieldRef = cstIndexMap.getCstFieldRef(directoryIn.readInt());
      newClassDef.addFieldAnnotations(cstFieldRef, readAnnotationSet(dex, directoryIn.readInt()));
    }

    for (int i = 0; i < methodsSize; i++) {
      CstMethodRef cstMethodRef = cstIndexMap.getCstMethodRef(directoryIn.readInt());
      newClassDef.addMethodAnnotations(cstMethodRef, readAnnotationSet(dex, directoryIn.readInt()));
    }

    for (int i = 0; i < parameterListSize; i++) {
      CstMethodRef cstMethodRef = cstIndexMap.getCstMethodRef(directoryIn.readInt());
      newClassDef.addParameterAnnotations(cstMethodRef,
          readAnnotationSetRefList(dex, directoryIn.readInt()));
    }
  }

  @Nonnull
  private AnnotationsList readAnnotationSetRefList(@Nonnull DexBuffer dex,
      @Nonnegative int annotationSetRefListOffset) {
    Section annotationSetRefListIn = dex.open(annotationSetRefListOffset);
    int parameterCount = annotationSetRefListIn.readInt();
    AnnotationsList parameterAnnotationList = new AnnotationsList(parameterCount);

    for (int paramIdx = 0; paramIdx < parameterCount; paramIdx++) {
      Annotations annotations = readAnnotationSet(dex, annotationSetRefListIn.readInt());
      annotations.setImmutable();
      parameterAnnotationList.set(paramIdx, annotations);
    }

    return parameterAnnotationList;
  }

  @Nonnull
  private Annotations readAnnotationSet(@Nonnull DexBuffer dex,
      @Nonnegative int annotationSetOffset) {
    Section annotationSetIn = dex.open(annotationSetOffset);
    int size = annotationSetIn.readInt();
    Annotations annotations = new Annotations();

    for (int j = 0; j < size; j++) {
      annotations.add(readAnnotationItem(dex, annotationSetIn.readInt()));
    }

    return annotations;
  }

  @Nonnull
  private com.android.jack.dx.rop.annotation.Annotation readAnnotationItem(@Nonnull DexBuffer dex,
      @Nonnegative int annotationItemOffset) {
    Section annotationItemIn = dex.open(annotationItemOffset);
    Annotation ioAnnotation = annotationItemIn.readAnnotation();
    assert cstIndexMap != null;
    CstType annotationType = cstIndexMap.getCstType(ioAnnotation.getTypeIndex());
    com.android.jack.dx.rop.annotation.Annotation a =
        new com.android.jack.dx.rop.annotation.Annotation(annotationType,
            AnnotationItem.getAnnotationVisibility(ioAnnotation.getVisibility()));

    for (int i = 0; i < ioAnnotation.getValues().length; i++) {
      AnnotationValueReader avr =
          new AnnotationValueReader(dex, ioAnnotation.getValues()[i].asByteInput());
      avr.readValue();
      assert cstIndexMap != null;
      a.add(new NameValuePair(cstIndexMap.getCstString(ioAnnotation.getNames()[i]),
          avr.getCstValue()));
    }

    return (a);
  }

  private final class AnnotationValueReader extends EncodedValueReader {

    @Nonnegative
    private final int cstIndex = 0;

    @CheckForNull
    private Constant constantValue;

    @CheckForNull
    private CstString annotationName;

    @Nonnull
    private final DexBuffer dex;

    public AnnotationValueReader(@Nonnull DexBuffer dex, @Nonnull ByteInput in) {
      super(in);
      this.dex = dex;
    }

    @Nonnull
    public Constant getCstValue() {
      assert constantValue != null;
      return constantValue;
    }

    @Override
    protected void visitEncodedBoolean(int argAndType) {
      int arg = (argAndType & 0xe0) >> 5;
      constantValue = CstBoolean.make(arg);
    }

    @Override
    protected void visitString(int type, int index) {
      assert cstIndexMap != null;
      constantValue = cstIndexMap.getCstString(index);
    }

    @Override
    protected void visitEncodedNull(int argAndType) {
      constantValue = CstKnownNull.THE_ONE;
    }

    @Override
    public final void readAnnotation() {
      int typeIndex = Leb128Utils.readUnsignedLeb128(in);
      int size = Leb128Utils.readUnsignedLeb128(in);
      assert cstIndexMap != null;
      com.android.jack.dx.rop.annotation.Annotation embeddedAnnotation =
          new com.android.jack.dx.rop.annotation.Annotation(
              cstIndexMap.getCstType(typeIndex), AnnotationVisibility.EMBEDDED);


      for (int i = 0; i < size; i++) {
        assert cstIndexMap != null;
        CstString pairName = cstIndexMap.getCstString((Leb128Utils.readUnsignedLeb128(in)));
        readValue();
        embeddedAnnotation.add(new NameValuePair(pairName, constantValue));
        constantValue = null;
      }

      embeddedAnnotation.setImmutable();
      constantValue = new CstAnnotation(embeddedAnnotation);
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
      // Nothing to do.
    }

    @Override
    public void readArray() {
      int size = Leb128Utils.readUnsignedLeb128(in);
      CstArray.List constants = new CstArray.List(size);
      for (int i = 0; i < size; i++) {
        readValue();
        constants.set(i, constantValue);
        constantValue = null;
      }
      constants.setImmutable();
      constantValue = new CstArray(constants);
    }

    @Override
    protected void visitArrayValue(int argAndType) {
      // Nothing to do.
    }

    @Override
    protected void visitField(int type, int index) {
      assert cstIndexMap != null;
      if (type == ENCODED_FIELD) {
        constantValue = cstIndexMap.getCstFieldRef(index);
      } else {
        assert type == ENCODED_ENUM;
        FieldId fieldId = dex.fieldIds().get(index);
        CstNat fieldNat = new CstNat(cstIndexMap.getCstString(fieldId.getNameIndex()),
            new CstString(dex.typeNames().get(fieldId.getTypeIndex())));
        constantValue = new CstEnumRef(fieldNat);
      }
    }


    @Override
    protected void visitMethod(int type, int index) {
      assert cstIndexMap != null;
      constantValue = cstIndexMap.getCstMethodRef(index);
    }

    @Override
    protected void visitType(int type, int index) {
      assert cstIndexMap != null;
      constantValue = cstIndexMap.getCstType(index);
    }

    @Override
    protected void visitPrimitive(int argAndType, int type, int arg, int size) {
      switch (type) {
        case ENCODED_BYTE: {
          constantValue = CstByte.make((byte) EncodedValueCodec.readSignedInt(in, arg));
          break;
        }
        case ENCODED_CHAR: {
          constantValue = CstChar.make((char) EncodedValueCodec.readUnsignedInt(in, arg, false));
          break;
        }
        case ENCODED_SHORT: {
          constantValue = CstShort.make((short) EncodedValueCodec.readSignedInt(in, arg));
          break;
        }
        case ENCODED_INT: {
          constantValue = CstInteger.make(EncodedValueCodec.readSignedInt(in, arg));
          break;
        }
        case ENCODED_LONG: {
          constantValue = CstLong.make(EncodedValueCodec.readSignedLong(in, arg));
          break;
        }
        case ENCODED_FLOAT: {
          constantValue = CstFloat.make(EncodedValueCodec.readUnsignedInt(in, arg, true));
          break;
        }
        case ENCODED_DOUBLE: {
          constantValue = CstDouble.make(EncodedValueCodec.readUnsignedLong(in, arg, true));
          break;
        }
        default: {
          throw new AssertionError();
        }
      }
    }
  }
}
