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

import com.android.sched.util.location.ColumnAndLineLocation;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.HasLocation;
import com.android.sched.util.location.Location;

import javax.annotation.Nonnull;

/**
 * Tracks file and line information for AST nodes.
 */
public abstract class SourceInfo implements HasLocation {

  public static final int UNKNOWN_LINE_NUMBER = 0;

  public static final int UNKNOWN_COLUMN_NUMBER = 0;

  @Nonnull
  public static final SourceInfo UNKNOWN = new UnknownSourceInfo();

  @Nonnull
  public String getFileName() {
    return getFileSourceInfo().getFileName();
  }

  @Nonnull
  public abstract FileSourceInfo getFileSourceInfo();

  public int getStartLine() {
    return UNKNOWN_LINE_NUMBER;
  }

  public int getEndLine() {
    return UNKNOWN_LINE_NUMBER;
  }

  public int getStartColumn() {
    return UNKNOWN_COLUMN_NUMBER;
  }

  public int getEndColumn() {
    return UNKNOWN_COLUMN_NUMBER;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    Location location;
    FileLocation fileLocation = new FileLocation(getFileName());
    if (getStartLine() != UNKNOWN_LINE_NUMBER) {
      int endLine = getEndLine();
      if (endLine == UNKNOWN_LINE_NUMBER) {
        endLine = ColumnAndLineLocation.UNKNOWN;
      }

      int startColumn = getStartColumn();
      if (startColumn == UNKNOWN_LINE_NUMBER) {
        startColumn = ColumnAndLineLocation.UNKNOWN;
      }

      int endColumn = getEndColumn();
      if (endColumn == UNKNOWN_LINE_NUMBER) {
        endColumn = ColumnAndLineLocation.UNKNOWN;
      }

      location =
          new ColumnAndLineLocation(fileLocation, getStartLine(), endLine, startColumn, endColumn);
    } else {
      location = fileLocation;
    }
    return location;
  }
}
