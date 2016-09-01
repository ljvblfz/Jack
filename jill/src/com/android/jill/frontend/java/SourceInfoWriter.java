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

import com.android.jill.backend.jayce.JayceWriter;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This writer handles source info writing context.
 */
public class SourceInfoWriter {

  @Nonnull
  protected final JayceWriter writer;

  /* debug infos */
  @Nonnegative
  public static final int NO_LINE = 0;

  private static final String NO_FILENAME = null;

  @CheckForNull
  private String currentFileName;

  @Nonnegative
  private int currentLineNumber;

  public SourceInfoWriter(JayceWriter writer) {
    this.writer = writer;
  }

  public void writeDebugBegin(@Nonnull ClassNode cn) throws IOException {
    writeDebugBegin(cn, NO_LINE);
  }

  public void writeDebugBegin(@Nonnull ClassNode cn, @Nonnull FieldNode fn) throws IOException {
    writeUnknwonDebugBegin();
  }

  public void writeDebugBegin(@Nonnull ClassNode cn, int startLine) throws IOException {
    if (cn.sourceFile == null) {
      writeUnknwonDebugBegin();
    } else {
      writeFileNameIfDifferentFromCurrent(cn.sourceFile);
      writeLineIfDifferentFromCurrent(startLine);
    }
  }

  public void writeUnknwonDebugBegin() throws IOException {
    writeUnknowDebug();
  }

  public void writeDebugEnd(@Nonnull ClassNode cn)
      throws IOException {
    writeDebugEnd(cn, NO_LINE);
  }

  public void writeDebugEnd(@Nonnull ClassNode cn, @Nonnull FieldNode fn)
      throws IOException {
    writeUnknownDebugEnd();
  }

  public void writeDebugEnd(@Nonnull ClassNode cn, int endLine) throws IOException {
    if (cn.sourceFile == null) {
      writeUnknwonDebugBegin();
    } else {
      writeFileNameIfDifferentFromCurrent(cn.sourceFile);
      writeLineIfDifferentFromCurrent(endLine);
    }
  }

  public void writeUnknownDebugEnd() throws IOException {
    writeUnknowDebug();
  }

  private void writeUnknowDebug()  throws IOException {
    if (currentFileName != null) {
      writeCurrentFileName(null);
      currentLineNumber = 0;
    }
  }

  private void writeFileNameIfDifferentFromCurrent(@Nonnull String fileName)
      throws IOException {
    if (!fileName.equals(currentFileName)) {
      writeCurrentFileName(fileName);
    }
  }

  private void writeCurrentFileName(@CheckForNull String fileName)
      throws IOException {
    writer.writeFileName(fileName);
    currentFileName = fileName;
  }

  private void writeLineIfDifferentFromCurrent(@Nonnegative int lineNumber) throws IOException {
    assert currentFileName != NO_FILENAME || lineNumber == NO_LINE;
    if (lineNumber != currentLineNumber) {
      writeCurrentLine(lineNumber);
    }
  }

  private void writeCurrentLine(@Nonnegative int lineNumber)
      throws IOException {
    writer.writeCurrentLineInfo(lineNumber);
    currentLineNumber = lineNumber;
  }
}
