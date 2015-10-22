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
  private static final int NO_START_LINE = 0;

  @Nonnegative
  private static final int NO_END_LINE = 0;

  private static final String NO_FILENAME = null;

  @CheckForNull
  private String currentFileName;

  @Nonnegative
  private int currentLineNumber;

  public SourceInfoWriter(JayceWriter writer) {
    this.writer = writer;
  }

  public void writeDebugBegin(@Nonnull ClassNode cn) throws IOException {
    writeDebugBegin(cn, NO_START_LINE);
  }

  public void writeDebugBegin(@Nonnull ClassNode cn, @Nonnull FieldNode fn)
      throws IOException {
    writeUnknwonDebugBegin();
  }

  public void writeDebugBegin(@Nonnull ClassNode cn, int startLine)
      throws IOException {
    if (cn.sourceFile == null) {
      writeUnknwonDebugBegin();
    } else {
      writeDebugBeginInternal(cn.sourceFile, startLine);
    }
  }

  public void writeUnknwonDebugBegin() throws IOException {
    writeDebugBeginInternal(NO_FILENAME, NO_START_LINE);
  }

  private void writeDebugBeginInternal(@CheckForNull String sourceFile, int startLine)
      throws IOException {
    writeFileNameIfDifferentFromCurrent(sourceFile);
    writeLineIfDifferentFromCurrent(startLine, true);
  }

  public void writeDebugEnd(@Nonnull ClassNode cn)
      throws IOException {
    writeDebugEnd(cn, NO_END_LINE);
  }

  public void writeDebugEnd(@Nonnull ClassNode cn, @Nonnull FieldNode fn)
      throws IOException {
    writeUnknownDebugEnd();
  }

  public void writeDebugEnd(@Nonnull ClassNode cn, int endLine) throws IOException {
    if (cn.sourceFile == null) {
      writeUnknownDebugEnd();
    } else {
      writeLineIfDifferentFromCurrent(endLine, false);
    }
  }

  public void writeUnknownDebugEnd() throws IOException {
    writeLineIfDifferentFromCurrent(NO_END_LINE, false);
  }

  private void writeFileNameIfDifferentFromCurrent(@CheckForNull String fileName)
      throws IOException {
    if (fileName != null && !fileName.equals(currentFileName)) {
      writeCurrentFileName(fileName);
    }
    // Assume that elements with unknown debug infos are in same file.
  }

  private void writeCurrentFileName(@Nonnull String fileName)
      throws IOException {
    writer.writeFileName(fileName);
    currentFileName = fileName;
  }

  private void writeLineIfDifferentFromCurrent(@Nonnegative int lineNumber,
      boolean isStartLine)
      throws IOException {
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
