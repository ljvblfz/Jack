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

package com.android.jill.frontend.java;

import com.android.jill.JillException;
import com.android.jill.backend.jayce.JayceWriter;
import com.android.jill.backend.jayce.Token;

import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.reflect.Array;

import javax.annotation.Nonnull;

/**
 * {@code JillWriter}s are helper class build around a Jayce writer to realize
 * Jayce file output.
 */
public abstract class JillWriter {

  @Nonnull
  protected final JayceWriter writer;

  @Nonnull
  protected final SourceInfoWriter sourceInfoWriter;

  public JillWriter(@Nonnull JayceWriter writer,
      @Nonnull SourceInfoWriter sourceInfoWriter) {
    this.writer = writer;
    this.sourceInfoWriter = sourceInfoWriter;
  }

  protected void writeValue(Object value)  throws IOException{
    if (value == null) {
      writer.writeNull();
    } else if (value instanceof Boolean) {
      writeValue(((Boolean) value).booleanValue());
    } else if (value instanceof Integer) {
      writeValue(((Integer) value).intValue());
    } else if (value instanceof Long){
      writeValue(((Long) value).longValue());
    } else if (value instanceof Float) {
      writeValue(((Float) value).floatValue());
    } else if (value instanceof Double) {
      writeValue(((Double) value).doubleValue());
    } else if (value instanceof String) {
      writeValue((String) value);
    } else if (value instanceof Type) {
      writeValue((Type) value);
    } else {
      throw new JillException("Unsupported object value.");
    }
  }

  protected void writeValue(@Nonnull String value) throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.STRING_LITERAL);
    writer.writeOpen();
    writer.writeString(value);
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }

  protected void writeValue(boolean value) throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.BOOLEAN_LITERAL);
    writer.writeOpen();
    writer.writeBoolean(value);
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }

  protected void writeValue(byte value) throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.BYTE_LITERAL);
    writer.writeOpen();
    writer.writeByte(value);
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }

  protected void writeValue(char value) throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.CHAR_LITERAL);
    writer.writeOpen();
    writer.writeChar(value);
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }

  protected void writeValue(short value) throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.SHORT_LITERAL);
    writer.writeOpen();
    writer.writeShort(value);
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }

  protected void writeValue(int value) throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.INT_LITERAL);
    writer.writeOpen();
    writer.writeInt(value);
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }

  protected void writeValue(float value) throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.FLOAT_LITERAL);
    writer.writeOpen();
    writer.writeFloat(value);
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }

  protected void writeValue(double value) throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.DOUBLE_LITERAL);
    writer.writeOpen();
    writer.writeDouble(value);
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }

  protected void writeValue(long value) throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.LONG_LITERAL);
    writer.writeOpen();
    writer.writeLong(value);
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }

  protected void writeValue() throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.NULL_LITERAL);
    writer.writeOpen();
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }

  protected void writeValue(@Nonnull Type value) throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.CLASS_LITERAL);
    writer.writeOpen();
    writer.writeId(value.getDescriptor());
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }

  protected void writeValue(@Nonnull Object[] value) throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.ARRAY_LITERAL);
    writer.writeOpen();
    writer.writeOpenNodeList();
    for (int j = 0; j < value.length; ++j) {
      writeValue(value[j]);
    }
    writer.writeCloseNodeList();
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }

  @Nonnull
  protected Object[] convertPrimitiveArrayToObject(@Nonnull Object array) {
    assert array.getClass().isArray();
    assert array.getClass().getComponentType().isPrimitive();

    int length = Array.getLength(array);
    Object objectArray[] = new Object[length];

    for (int i = 0; i < length; i++) {
      objectArray[i] = Array.get(array, i);
    }

    return objectArray;
  }
}
