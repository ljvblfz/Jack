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

package com.android.jack.jayce.v0002.nodes;

import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.v0002.io.ExportSession;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Tracks file and line information for Jayce nodes.
 */
public class NSourceInfo {

  @CheckForNull
  public String fileName;
  @Nonnegative
  public int startLine;
  @Nonnegative
  public int endLine;

  public int startColumn;

  public int endColumn;

  public static final NSourceInfo UNKNOWN = new NSourceInfo();

  public void importFromJast(@Nonnull SourceInfo sourceInfo) {
    fileName = sourceInfo.getFileName();
    startLine = sourceInfo.getStartLine();
    endLine = sourceInfo.getEndLine();
    startColumn = sourceInfo.getStartColumn();
    endColumn = sourceInfo.getEndColumn();
  }

  @Nonnull
  public SourceInfo exportAsJast(@Nonnull ExportSession exportSession) {
    if (fileName == null && startLine == 0 && endLine == 0 && startColumn == 0 && endColumn == 0) {
      return SourceInfo.UNKNOWN;
    }
    assert fileName != null;
    return exportSession.getSession().getSourceInfoFactory()
        .create(startColumn, endColumn, startLine, endLine, fileName);
  }
}
