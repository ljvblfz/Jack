/*
* Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.jayce.v0004.nodes;

import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.v0004.NNode;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.lookup.JMethodLookupException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Base class for all Java expressions.
 */
public abstract class NExpression extends NNode implements HasSourceInfo {

  @CheckForNull
  protected SourceInfo sourceInfo;

  @Override
  @Nonnull
  public SourceInfo getSourceInfos() {
    assert sourceInfo != null;
    return sourceInfo;
  }

  @Override
  public void setSourceInfos(@Nonnull SourceInfo sourceInfo) {
    this.sourceInfo = sourceInfo;
  }

  @Override
  @Nonnull
  public abstract JExpression exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException;
}
