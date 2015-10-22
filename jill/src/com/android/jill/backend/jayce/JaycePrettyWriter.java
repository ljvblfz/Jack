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

package com.android.jill.backend.jayce;

import com.google.common.base.Strings;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Jayce pretty writer.
 */
public class JaycePrettyWriter extends JayceWriter {

  @Nonnull
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  @Nonnull
  private static final String INDENT_GRANULARITY = "  ";

  @Nonnegative
  private int indent = 0;

  public JaycePrettyWriter(@Nonnull OutputStream out) {
    super(out);
  }

  @Override
  public void writeKeyword(@Nonnull Token token) throws IOException {
    writeIdent();
    super.writeKeyword(token);
    writeLn();
  }

  @Override
  public void writeOpen() throws IOException {
    writeIdent();
    super.writeOpen();
    writeLn();
    indentIn();
  }

  @Override
  public void writeClose() throws IOException {
    indentOut();
    writeIdent();
    super.writeClose();
    writeLn();
  }

  @Override
  public void writeString(@CheckForNull String string) throws IOException {
    writeIdent();
    super.writeString(string);
    writeLn();
  }

  @Override
  public void writeNull() throws IOException {
    writeIdent();
    super.writeNull();
  }

  @Override
  public void writeFileName(@CheckForNull String fileName) throws IOException {
    writeIdent();
    super.writeFileName(fileName);
    writeLn();
  }

  @Override
  public void writeCurrentLineInfo(int lineNumber)
      throws IOException {
    writeIdent();
    super.writeCurrentLineInfo(lineNumber);
    writeLn();
  }

  @Override
  protected void writeSpace() throws IOException {
    writeLn();
  }

  @Override
  public void writeInt(int value) throws IOException {
    writeIdent();
    super.writeInt(value);
  }

  @Override
  public void writeByte(byte value) throws IOException {
    writeIdent();
    super.writeByte(value);
  }

  @Override
  public void writeChar(char value) throws IOException {
    writeIdent();
    super.writeChar(value);
  }

  @Override
  public void writeShort(short value) throws IOException {
    writeIdent();
    super.writeShort(value);
  }

  @Override
  public void writeLong(long value) throws IOException {
    writeIdent();
    super.writeLong(value);
  }

  @Override
  public void writeFloat(float value) throws IOException {
    writeIdent();
    super.writeFloat(value);
  }

  @Override
  public void writeDouble(double value) throws IOException {
    writeIdent();
    super.writeDouble(value);
  }

  private void writeIdent() throws IOException {
    writers.peek().writeChars(Strings.repeat(INDENT_GRANULARITY, indent));
  }

  private void writeLn() throws IOException {
    writers.peek().writeChars(LINE_SEPARATOR);
  }

  private void indentIn() {
    indent++;
  }

  private void indentOut() {
    indent--;
  }
}
