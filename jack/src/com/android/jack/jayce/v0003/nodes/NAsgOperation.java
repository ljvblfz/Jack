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

package com.android.jack.jayce.v0003.nodes;

import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Binary operator expression for {@code assign}.
 */
public class NAsgOperation extends NExpression {

  @Nonnull
  public static final Token TOKEN = Token.ASG_OPERATION;

  @CheckForNull
  public NExpression lhs;

  @CheckForNull
  public NExpression rhs;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JAsgOperation operation = (JAsgOperation) node;
    lhs = (NExpression) loader.load(operation.getLhs());
    rhs = (NExpression) loader.load(operation.getRhs());
    sourceInfo = loader.load(operation.getSourceInfo());
  }

  @Override
  @Nonnull
  public JAsgOperation exportAsJast(@Nonnull ExportSession exportSession)
      throws JMethodLookupException, JTypeLookupException {
    assert sourceInfo != null;
    assert lhs != null;
    assert rhs != null;
    return new JAsgOperation(sourceInfo.exportAsJast(exportSession),
        lhs.exportAsJast(exportSession),
        rhs.exportAsJast(exportSession));
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    lhs = in.readNode(NExpression.class);
    rhs = in.readNode(NExpression.class);
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }

  @Override
  @Nonnull
  public NSourceInfo getSourceInfos() {
    assert sourceInfo != null;
    return sourceInfo;
  }

  @Override
  public void setSourceInfos(@Nonnull NSourceInfo sourceInfo) {
    this.sourceInfo = sourceInfo;
  }
}
