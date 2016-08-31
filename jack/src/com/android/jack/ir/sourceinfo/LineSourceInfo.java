/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.sourceinfo;



import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Describes where a SourceInfo's node came from.
 */
public class LineSourceInfo extends SourceInfo {

  @Nonnull
  private final FileSourceInfo fileSourceInfo;
  @Nonnegative
  private final int startLine;
  @Nonnegative
  private final int endLine;

  LineSourceInfo(
      @Nonnull FileSourceInfo location, @Nonnegative int startLine, @Nonnegative int endLine) {
    assert startLine != SourceInfo.UNKNOWN_LINE_NUMBER;
    this.fileSourceInfo = location;
    this.startLine = startLine;
    this.endLine = endLine;
  }

  @Override
  public final boolean equals(@CheckForNull Object o) {
    if (!(o instanceof LineSourceInfo)) {
      return false;
    }
    LineSourceInfo other = (LineSourceInfo) o;
    return startLine == other.startLine && endLine == other.getEndLine()
        && fileSourceInfo.equals(other.fileSourceInfo);
  }

  @Override
  @Nonnull
  public FileSourceInfo getFileSourceInfo() {
    return fileSourceInfo;
  }

  @Override
  @Nonnegative
  public int getStartLine() {
    return startLine;
  }

  @Override
  @Nonnegative
  public int getEndLine() {
    return endLine;
  }

  @Override
  public final int hashCode() {
    return 2 + 13 * fileSourceInfo.hashCode() + 17 * startLine + 19 * endLine;
  }

  @Override
  @Nonnull
  public String toString() {
    return getFileName() + ":" + getStartLine() + '-' + getEndLine();
  }
}