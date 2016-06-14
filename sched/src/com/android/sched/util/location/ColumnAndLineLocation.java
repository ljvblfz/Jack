/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.sched.util.location;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;


/**
 * Class describing line and column info in another location.
 */
public class ColumnAndLineLocation implements Location {

  public static final int UNKNOWN = -1;

  private final int startLine;

  private int endLine = UNKNOWN;

  private int startColumn = UNKNOWN;

  private int endColumn = UNKNOWN;

  @Nonnull
  private final Location parentLocation;

  public ColumnAndLineLocation(@Nonnull Location parentLocation, int line) {
    assert line != UNKNOWN;
    this.parentLocation = parentLocation;
    this.startLine = line;
  }

  public ColumnAndLineLocation(@Nonnull Location parentLocation, int startLine, int endLine) {
    assert startLine != UNKNOWN;
    if (endLine == startLine) {
      endLine = UNKNOWN;
    }
    this.parentLocation = parentLocation;
    this.startLine = startLine;
    this.endLine = endLine;
  }

  public ColumnAndLineLocation(@Nonnull Location parentLocation, int startLine, int endLine,
      int startColumn, int endColumn) {
    assert startLine != UNKNOWN;
    assert !(startColumn == UNKNOWN && endColumn != UNKNOWN);
    if (endLine == startLine) {
      endLine = UNKNOWN;
    }
    if (endColumn == startColumn) {
      endColumn = UNKNOWN;
    }
    this.parentLocation = parentLocation;
    this.startLine = startLine;
    this.endLine = endLine;
    this.startColumn = startColumn;
    this.endColumn = endColumn;
  }

  @Override
  @Nonnull
  public String getDescription() {
    assert hasStartLine();
    StringBuilder sb = new StringBuilder();

    if (!parentLocation.getDescription().isEmpty()) {
      sb.append(parentLocation.getDescription()).append(", ");
    }

    if (!hasEndLine()) {
      sb.append("line ").append(startLine);
    } else {
      sb.append("from line ").append(startLine).append(" to ").append(endLine);
    }

    if (hasStartColumn()) {
      if (!hasEndColumn()) {
        sb.append(", column ").append(startColumn);
      } else {
        sb.append(", from column ").append(startColumn).append(" to ").append(endColumn);
      }
    }

    return sb.toString();
  }

  @Nonnull
  public Location getParentLocation() {
    return parentLocation;
  }

  @Nonnegative
  public int getStartLine() {
    assert hasStartLine();
    return startLine;
  }

  @Nonnegative
  public int getEndLine() {
    assert hasEndLine();
    return endLine;
  }

  @Nonnegative
  public int getStartColumn() {
    assert hasStartColumn();
    return startColumn;
  }

  @Nonnegative
  public int getEndColumn() {
    assert hasEndColumn();
    return endColumn;
  }

  public boolean hasStartLine() {
    return startLine != UNKNOWN;
  }

  public boolean hasEndLine() {
    return endLine != UNKNOWN;
  }

  public boolean hasStartColumn() {
    return startColumn != UNKNOWN;
  }

  public boolean hasEndColumn() {
    return endColumn != UNKNOWN;
  }

  @Override
  public final boolean equals(Object obj) {
    return obj instanceof ColumnAndLineLocation
        && ((ColumnAndLineLocation) obj).startLine == startLine
        && ((ColumnAndLineLocation) obj).endLine == endLine
        && ((ColumnAndLineLocation) obj).startColumn == startColumn
        && ((ColumnAndLineLocation) obj).endColumn == endColumn
        && ((ColumnAndLineLocation) obj).parentLocation.equals(parentLocation);
  }

  @Override
  public final int hashCode() {
    return (startLine * 13) ^ (endLine * 27) ^ (startColumn * 29) ^ (endColumn * 31)
        ^ parentLocation.hashCode();
  }
}
