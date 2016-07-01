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

import com.android.sched.schedulable.Constraint;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A factory used to create {@link SourceInfo}s.
 */
@Constraint(need = SourceInfoCreation.class)
public class SourceInfoFactory {
  @Nonnull
  private final HashMap<String, FileSourceInfo> canonicalFileSourceInfos =
      new HashMap<String, FileSourceInfo>();

  @Nonnull
  private final HashMap<LineSourceInfo, LineSourceInfo> canonicalLineSourceInfos =
      new HashMap<LineSourceInfo, LineSourceInfo>();

  @Nonnull
  private final HashMap<ColumnSourceInfo, ColumnSourceInfo> canonicalColumnSourceInfos =
      new HashMap<ColumnSourceInfo, ColumnSourceInfo>();

  /**
   * Creates SourceInfo nodes. This factory method will provide
   * canonicalized instances of SourceInfo objects.
   */
  @Nonnull
  public synchronized FileSourceInfo create(@Nonnull String fileName) {
    FileSourceInfo newInstance = canonicalFileSourceInfos.get(fileName);
    if (newInstance == null) {
      newInstance = new FileSourceInfo(fileName);
      canonicalFileSourceInfos.put(fileName, newInstance);
    }
    return newInstance;
  }

  /**
   * Creates SourceInfo nodes. This factory method will provide
   * canonicalized instances of SourceInfo objects.
   */
  @Nonnull
  public synchronized SourceInfo create(int startCol, int endCol,
      @Nonnegative int startLine, @Nonnegative int endLine, @Nonnull String fileName) {
    FileSourceInfo fileSourceInfo = create(fileName);
    LineSourceInfo lineSourceOrigin = create(startLine, endLine, fileSourceInfo);
    if (startCol <= 0 && endCol <= 0) {
      return lineSourceOrigin;
    }

    ColumnSourceInfo newInstance = new ColumnSourceInfo(lineSourceOrigin, startCol, endCol);
    ColumnSourceInfo canonical = canonicalColumnSourceInfos.get(newInstance);

    assert canonical == null || (newInstance != canonical && newInstance.equals(canonical));
    if (canonical != null) {
      return canonical;
    } else {
      canonicalColumnSourceInfos.put(newInstance, newInstance);
      return newInstance;
    }
  }

  /**
   * Creates SourceInfo nodes. This factory method will provide
   * canonicalized instances of SourceInfo objects.
   */
  @Nonnull
  public synchronized SourceInfo create(@Nonnegative int startLine, @Nonnegative int endLine,
      @Nonnull String fileName) {
    FileSourceInfo fileSourceInfo = create(fileName);
    return create(startLine, endLine, fileSourceInfo);
  }

  /**
   * Creates SourceInfo nodes. This factory method will provide
   * canonicalized instances of SourceInfo objects.
   */
  @Nonnull
  public synchronized LineSourceInfo create(
      @Nonnegative int startLine, @Nonnegative int endLine, @Nonnull FileSourceInfo fileName) {
    LineSourceInfo newInstance = new LineSourceInfo(fileName, startLine, endLine);
    LineSourceInfo canonical = canonicalLineSourceInfos.get(newInstance);

    assert canonical == null || (newInstance != canonical && newInstance.equals(canonical));
    if (canonical != null) {
      return canonical;
    } else {
      canonicalLineSourceInfos.put(newInstance, newInstance);
      return newInstance;
    }
  }

  @Nonnull
  public Collection<FileSourceInfo> getFileSourceInfos() {
    return canonicalFileSourceInfos.values();
  }

  @Nonnull
  public Set<ColumnSourceInfo> getColumnSourceInfos() {
    return canonicalColumnSourceInfos.keySet();
  }

  @Nonnull
  public Set<LineSourceInfo> getLineSourceInfos() {
    return canonicalLineSourceInfos.keySet();
  }
}
