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
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.lang.reflect.Array;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
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

  protected void writeValue(Object value, @CheckForNull ClassNode classNode,
      @Nonnegative int currentLine)  throws IOException{
    if (value == null) {
      writer.writeNull();
    } else if (value instanceof Boolean) {
      writeValue(((Boolean) value).booleanValue(), classNode, currentLine);
    } else if (value instanceof Integer) {
      writeValue(((Integer) value).intValue(), classNode, currentLine);
    } else if (value instanceof Long){
      writeValue(((Long) value).longValue(), classNode, currentLine);
    } else if (value instanceof Float) {
      writeValue(((Float) value).floatValue(), classNode, currentLine);
    } else if (value instanceof Double) {
      writeValue(((Double) value).doubleValue(), classNode, currentLine);
    } else if (value instanceof String) {
      writeValue((String) value, classNode, currentLine);
    } else if (value instanceof Type) {
      writeValue((Type) value, classNode, currentLine);
    } else {
      throw new JillException("Unsupported object value.");
    }
  }

  protected void writeValue(Object value)  throws IOException{
    writeValue(value, /* classNode= */ null, SourceInfoWriter.NO_LINE);
  }

  protected void writeValue(@Nonnull String value) throws IOException {
    writeValue(value, /* classNode= */ null, SourceInfoWriter.NO_LINE);
  }

  protected void writeValue(@Nonnull String value, @CheckForNull ClassNode classNode,
      @Nonnegative int currentLine) throws IOException {
    writeDebugBegin(classNode, currentLine);
    writer.writeKeyword(Token.STRING_LITERAL);
    writer.writeOpen();
    writer.writeString(value);
    writeDebugEnd(classNode, currentLine);
    writer.writeClose();
  }

  protected void writeValue(boolean value, @CheckForNull ClassNode classNode,
      @Nonnegative int currenLine) throws IOException {
    writeDebugBegin(classNode, currenLine);
    writer.writeKeyword(Token.BOOLEAN_LITERAL);
    writer.writeOpen();
    writer.writeBoolean(value);
    writeDebugEnd(classNode, currenLine);
    writer.writeClose();
  }

  protected void writeValue(boolean value) throws IOException {
    writeValue(value, /* classNode= */ null, SourceInfoWriter.NO_LINE);
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

  protected void writeValue(int value, @CheckForNull ClassNode classNode,
      @Nonnegative int currentLine) throws IOException {
    writeDebugBegin(classNode, currentLine);
    writer.writeKeyword(Token.INT_LITERAL);
    writer.writeOpen();
    writer.writeInt(value);
    writeDebugEnd(classNode, currentLine);
    writer.writeClose();
  }

  protected void writeValue(int value) throws IOException {
    writeValue(value, /* classNode= */ null, SourceInfoWriter.NO_LINE);
  }

  protected void writeValue(float value) throws IOException {
    writeValue(value, /* classNode= */ null, SourceInfoWriter.NO_LINE);
  }

  protected void writeValue(float value, @CheckForNull ClassNode classNode,
      @Nonnegative int currentLine) throws IOException {
    writeDebugBegin(classNode, currentLine);
    writer.writeKeyword(Token.FLOAT_LITERAL);
    writer.writeOpen();
    writer.writeFloat(value);
    writeDebugEnd(classNode, currentLine);
    writer.writeClose();
  }

  protected void writeValue(double value) throws IOException {
    writeValue(value, /* classNode= */ null, SourceInfoWriter.NO_LINE);
  }

  protected void writeValue(double value, @CheckForNull ClassNode classNode,
      @Nonnegative int currentLine) throws IOException {
    writeDebugBegin(classNode, currentLine);
    writer.writeKeyword(Token.DOUBLE_LITERAL);
    writer.writeOpen();
    writer.writeDouble(value);
    writeDebugEnd(classNode, currentLine);
    writer.writeClose();
  }

  protected void writeValue(long value) throws IOException {
    writeValue(value, /* classNode= */ null, SourceInfoWriter.NO_LINE);
  }

  protected void writeValue(long value, @CheckForNull ClassNode classNode,
      @Nonnegative int currentLine) throws IOException {
    writeDebugBegin(classNode, currentLine);
    writer.writeKeyword(Token.LONG_LITERAL);
    writer.writeOpen();
    writer.writeLong(value);
    writeDebugEnd(classNode, currentLine);
    writer.writeClose();
  }

  protected void writeValue() throws IOException {
    writeValue(/* classNode= */ null, SourceInfoWriter.NO_LINE);
  }

  protected void writeValue(@CheckForNull ClassNode classNode,
      @Nonnegative int currentLine) throws IOException {
    writeDebugBegin(classNode, currentLine);
    writer.writeKeyword(Token.NULL_LITERAL);
    writer.writeOpen();
    writeDebugEnd(classNode, currentLine);
    writer.writeClose();
  }

  protected void writeValue(@Nonnull Type value) throws IOException {
    writeValue(value, /* classNode= */ null, SourceInfoWriter.NO_LINE);
  }

  protected void writeValue(@Nonnull Type value, @CheckForNull ClassNode classNode,
      @Nonnegative int currentLine) throws IOException {
    writeDebugBegin(classNode, currentLine);
    writer.writeKeyword(Token.CLASS_LITERAL);
    writer.writeOpen();
    writer.writeId(value.getDescriptor());
    writeDebugEnd(classNode, currentLine);
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

  protected void writeDebugBegin(@CheckForNull ClassNode classNode, @Nonnegative int currentLine)
      throws IOException {
    if (classNode != null && currentLine != SourceInfoWriter.NO_LINE) {
      sourceInfoWriter.writeDebugBegin(classNode, currentLine);
    } else {
      sourceInfoWriter.writeUnknwonDebugBegin();
    }
  }

  protected void writeDebugEnd(@CheckForNull ClassNode classNode, @Nonnegative int currentLine)
      throws IOException {
    if (classNode != null && currentLine != SourceInfoWriter.NO_LINE) {
      sourceInfoWriter.writeDebugEnd(classNode, currentLine + 1);
    } else {
      sourceInfoWriter.writeUnknownDebugEnd();
    }
  }
}
