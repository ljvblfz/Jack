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
import com.android.jack.dx.dex.file.ValueEncoder.ValueType;
import com.android.jack.dx.io.Annotation;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.dx.io.DexBuffer.Section;
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
import com.android.jack.dx.rop.cst.CstEnumRef;
import com.android.jack.dx.rop.cst.CstFieldRef;
import com.android.jack.dx.rop.cst.CstIndexMap;
import com.android.jack.dx.rop.cst.CstKnownNull;
import com.android.jack.dx.rop.cst.CstMethodRef;
import com.android.jack.dx.rop.cst.CstString;
import com.android.jack.dx.rop.type.Type;
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
    Type annotationType = cstIndexMap.getType(ioAnnotation.getTypeIndex());
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

    @CheckForNull
    private Constant constantValue;

    public AnnotationValueReader(@Nonnull DexBuffer dex, @Nonnull ByteInput in) {
      super(dex, in);
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
    protected void visitString(int index) {
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
              cstIndexMap.getType(typeIndex), AnnotationVisibility.EMBEDDED);


      for (int i = 0; i < size; i++) {
        assert cstIndexMap != null;
        CstString pairName = cstIndexMap.getCstString((Leb128Utils.readUnsignedLeb128(in)));
        readValue();
        assert constantValue != null;
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
    protected void visitMethodType(int index) {
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
      if (type == ValueType.VALUE_FIELD.getValue()) {
        constantValue = cstIndexMap.getCstFieldRef(index);
      } else {
        assert type == ValueType.VALUE_ENUM.getValue();
        FieldId fieldId = dexBuffer.fieldIds().get(index);
        constantValue = new CstEnumRef(cstIndexMap.getCstString(fieldId.getNameIndex()),
            cstIndexMap.getType(fieldId.getTypeIndex()));
      }
    }


    @Override
    protected void visitMethod(int index) {
      assert cstIndexMap != null;
      constantValue = cstIndexMap.getCstMethodRef(index);
    }

    @Override
    protected void visitType(int index) {
      assert cstIndexMap != null;
      constantValue = cstIndexMap.getType(index);
    }

    @Override
    protected void visitPrimitive(int type, int arg, int size) {
      constantValue = MergerTools.createConstant(in, type, arg);
    }
  }
}
