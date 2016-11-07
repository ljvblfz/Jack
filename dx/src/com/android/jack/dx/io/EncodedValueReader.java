/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.jack.dx.io;

import com.android.jack.dx.dex.file.ValueEncoder.ValueType;
import com.android.jack.dx.util.ByteInput;
import com.android.jack.dx.util.Leb128Utils;

/**
 * SAX-style reader for encoded values.
 * TODO(dx team): convert this to a pull-style reader
 */
public class EncodedValueReader {

  protected final ByteInput in;

  public EncodedValueReader(ByteInput in) {
    this.in = in;
  }

  public EncodedValueReader(EncodedValue in) {
    this(in.asByteInput());
  }

  public void readArray() {
    int size = Leb128Utils.readUnsignedLeb128(in);
    visitArray(size);

    for (int i = 0; i < size; i++) {
      readValue();
    }
  }

  public void readAnnotation() {
    int typeIndex = Leb128Utils.readUnsignedLeb128(in);
    int size = Leb128Utils.readUnsignedLeb128(in);
    visitAnnotation(typeIndex, size);

    for (int i = 0; i < size; i++) {
      visitAnnotationName(Leb128Utils.readUnsignedLeb128(in));
      readValue();
    }
  }

  public final void readValue() {
    int argAndType = in.readByte() & 0xff;
    int type = argAndType & 0x1f;
    int arg = (argAndType & 0xe0) >> 5;
    int size = arg + 1;

    switch (ValueType.getValueType(type)) {
      case VALUE_BYTE:
      case VALUE_SHORT:
      case VALUE_CHAR:
      case VALUE_INT:
      case VALUE_LONG:
      case VALUE_FLOAT:
      case VALUE_DOUBLE:
        visitPrimitive(type, arg, size);
        break;
      case VALUE_STRING:
        visitString(readIndex(in, size));
        break;
      case VALUE_TYPE:
        visitType(readIndex(in, size));
        break;
      case VALUE_FIELD:
      case VALUE_ENUM:
        visitField(type, readIndex(in, size));
        break;
      case VALUE_METHOD:
        visitMethod(readIndex(in, size));
        break;
      case VALUE_ARRAY:
        visitArrayValue(argAndType);
        readArray();
        break;
      case VALUE_ANNOTATION:
        visitAnnotationValue(argAndType);
        readAnnotation();
        break;
      case VALUE_NULL:
        visitEncodedNull(argAndType);
        break;
      case VALUE_BOOLEAN:
        visitEncodedBoolean(argAndType);
        break;
      default:
        throw new AssertionError();
    }
  }

  protected void visitArray(int size) {}

  protected void visitAnnotation(int typeIndex, int size) {}

  protected void visitAnnotationName(int nameIndex) {}

  protected void visitPrimitive(int type, int arg, int size) {
    for (int i = 0; i < size; i++) {
      in.readByte();
    }
  }

  protected void visitString(int index) {}

  protected void visitType(int index) {}

  protected void visitField(int type, int index) {}

  protected void visitMethod(int index) {}

  protected void visitArrayValue(int argAndType) {}

  protected void visitAnnotationValue(int argAndType) {}

  protected void visitEncodedBoolean(int argAndType) {}

  protected void visitEncodedNull(int argAndType) {}

  private int readIndex(ByteInput in, int byteCount) {
    int result = 0;
    int shift = 0;
    for (int i = 0; i < byteCount; i++) {
      result += (in.readByte() & 0xff) << shift;
      shift += 8;
    }
    return result;
  }
}
