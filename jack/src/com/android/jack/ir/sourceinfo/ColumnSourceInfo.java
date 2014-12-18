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

package com.android.jack.ir.sourceinfo;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

class ColumnSourceInfo extends SourceInfo {

  private final int endCol;
  private final int startCol;

  @Nonnull
  private final LineSourceInfo lineSourceInfo;

  ColumnSourceInfo(@Nonnull LineSourceInfo location, int startCol, int endCol) {
    this.lineSourceInfo = location;
    this.startCol = startCol;
    this.endCol = endCol;
  }

  @Override
  public int getEndColumn() {
    return endCol;
  }

  @Override
  public int getStartColumn() {
    return startCol;
  }

  @Override
  @Nonnegative
  public int getStartLine() {
    return lineSourceInfo.getStartLine();
  }

  @Override
  @Nonnegative
  public int getEndLine() {
    return lineSourceInfo.getEndLine();
  }

  @Override
  @Nonnull
  public FileSourceInfo getFileSourceInfo() {
    return lineSourceInfo.getFileSourceInfo();
  }

  @Override
  public final boolean equals(@CheckForNull Object o) {
    if (!(o instanceof ColumnSourceInfo)) {
      return false;
    }
    ColumnSourceInfo other = (ColumnSourceInfo) o;
    return startCol == other.getStartColumn() && endCol == other.getEndColumn()
        && lineSourceInfo.equals(other.lineSourceInfo);
  }

  @Override
  @Nonnull
  public String toString() {
    return lineSourceInfo.getFileName() + ':' + getStartLine() + '.' + getStartColumn() + '-'
        + getEndLine() + '.' + getEndColumn();
  }

  @Override
  public final int hashCode() {
    return lineSourceInfo.hashCode() + 29 * startCol + 31 * endCol;
  }
}
