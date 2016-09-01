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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A factory used to create {@link SourceInfo}s.
 */
@Constraint(need = SourceInfoCreation.class)
public class SourceInfoFactory {

  @Nonnull
  private final ConcurrentHashMap<String, FileSourceInfo> canonicalFileSourceInfos =
      new ConcurrentHashMap<String, FileSourceInfo>();

  @Nonnull
  private final ConcurrentHashMap<LineSourceInfo, LineSourceInfo> canonicalLineSourceInfos =
      new ConcurrentHashMap<LineSourceInfo, LineSourceInfo>();

  @Nonnull
  private final ConcurrentHashMap<ColumnSourceInfo, ColumnSourceInfo> canonicalColumnSourceInfos =
      new ConcurrentHashMap<ColumnSourceInfo, ColumnSourceInfo>();

  /**
   * Creates SourceInfo nodes. This factory method will provide
   * canonicalized instances of SourceInfo objects.
   */
  @Nonnull
  public FileSourceInfo create(@Nonnull String fileName) {
    assert fileName != null;
    FileSourceInfo newInstance = canonicalFileSourceInfos.get(fileName);
    if (newInstance == null) {
      newInstance = new FileSourceInfo(fileName);
      FileSourceInfo previousValue = canonicalFileSourceInfos.putIfAbsent(fileName, newInstance);
      if (previousValue != null) {
        newInstance = previousValue;
      }
    }
    return newInstance;
  }

  /**
   * Creates SourceInfo nodes. This factory method will provide
   * canonicalized instances of SourceInfo objects.
   */
  @Nonnull
  public SourceInfo create(@Nonnegative int startCol, @Nonnegative int endCol,
      @Nonnegative int startLine, @Nonnegative int endLine, @Nonnull String fileName) {
    SourceInfo si = create(startLine, endLine, fileName);

    if (startCol == SourceInfo.UNKNOWN_COLUMN_NUMBER || si instanceof FileSourceInfo) {
       assert endCol == SourceInfo.UNKNOWN_COLUMN_NUMBER;
       return si;
    }

    ColumnSourceInfo newInstance = new ColumnSourceInfo((LineSourceInfo) si, startCol, endCol);
    ColumnSourceInfo canonical = canonicalColumnSourceInfos.get(newInstance);

    assert canonical == null || (newInstance != canonical && newInstance.equals(canonical));
    if (canonical != null) {
      return canonical;
    } else {
      ColumnSourceInfo previousValue = canonicalColumnSourceInfos.putIfAbsent(newInstance,
          newInstance);
      if (previousValue != null) {
        newInstance = previousValue;
      }
      return newInstance;
    }
  }

  /**
   * Creates SourceInfo nodes. This factory method will provide
   * canonicalized instances of SourceInfo objects.
   */
  @Nonnull
  public SourceInfo create(@Nonnegative int startLine, @Nonnegative int endLine,
      @Nonnull String fileName) {
    FileSourceInfo fileSourceInfo = create(fileName);

    if (startLine == SourceInfo.UNKNOWN_LINE_NUMBER) {
      assert endLine == SourceInfo.UNKNOWN_LINE_NUMBER;
      return fileSourceInfo;
    }

    return create(startLine, endLine, fileSourceInfo);
  }

  /**
   * Creates SourceInfo nodes with a file name coming from another SourceInfo. This factory method
   * will provide canonicalized instances of SourceInfo objects.
   */
  @Nonnull
  public SourceInfo create(@Nonnegative int startLine, @Nonnegative int endLine,
      @Nonnull SourceInfo originalSourceInfo) {
    if (originalSourceInfo == SourceInfo.UNKNOWN) {
      assert startLine == SourceInfo.UNKNOWN_LINE_NUMBER;
      assert endLine == SourceInfo.UNKNOWN_LINE_NUMBER;
      return SourceInfo.UNKNOWN;
    }
    LineSourceInfo newInstance =
        new LineSourceInfo(originalSourceInfo.getFileSourceInfo(), startLine, endLine);
    LineSourceInfo canonical = canonicalLineSourceInfos.get(newInstance);

    assert canonical == null || (newInstance != canonical && newInstance.equals(canonical));
    if (canonical != null) {
      return canonical;
    } else {
      LineSourceInfo previousValue = canonicalLineSourceInfos.putIfAbsent(newInstance, newInstance);
      if (previousValue != null) {
        newInstance = previousValue;
      }
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
