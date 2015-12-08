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

package com.android.jack.tools.jacoco;

import org.jacoco.core.internal.analysis.MethodCoverageImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

class ProbeDescription {
  int id;

  @CheckForNull
  MethodCoverageImpl method;

  @Nonnull
  final List<Line> lines = new ArrayList<ProbeDescription.Line>();

  private static final int UNKNOWN_LINE = -1;

  class Line {
    public Line(int line, int instructionsCount, int branchesCount) {
      if (line < UNKNOWN_LINE) {
        throw new IllegalArgumentException("negative line");
      }
      if (instructionsCount < 0) {
        throw new IllegalArgumentException("negative instructionsCount");
      }
      if (branchesCount < 0) {
        throw new IllegalArgumentException("negative branchesCount");
      }
      this.line = line;
      this.instructionsCount = instructionsCount;
      this.branchesCount = branchesCount;
    }

    final int line;
    final int instructionsCount;
    final int branchesCount;

    @Override
    public String toString() {
      return "[line " + line + ": i=" + instructionsCount + ", b=" + branchesCount + "]";
    }
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setMethod(@Nonnull MethodCoverageImpl method) {
    this.method = method;
  }

  public void addLine(int line, int instructionsCount, int branchesCount) {
    lines.add(new Line(line, instructionsCount, branchesCount));
  }

  @Override
  public String toString() {
    return "Probe " + id + " lines=" + Arrays.toString(lines.toArray());
  }
}
