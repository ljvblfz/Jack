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

import com.android.jack.ir.ast.JLtOperation;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Binary operator expression for {@code lt}.
 */
public class NLtOperation extends NExpression {

  @Nonnull
  public static final Token TOKEN = Token.LT_OPERATION;

  @CheckForNull
  public NExpression lhs;

  @CheckForNull
  public NExpression rhs;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JLtOperation operation = (JLtOperation) node;
    lhs = (NExpression) loader.load(operation.getLhs());
    rhs = (NExpression) loader.load(operation.getRhs());
    sourceInfo = loader.load(operation.getSourceInfo());
  }

  @Override
  @Nonnull
  public JLtOperation exportAsJast(@Nonnull ExportSession exportSession) {
    assert sourceInfo != null;
    assert lhs != null;
    assert rhs != null;
    return new JLtOperation(sourceInfo.exportAsJast(exportSession),
        lhs.exportAsJast(exportSession),
        rhs.exportAsJast(exportSession));
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeNode(lhs);
    out.writeNode(rhs);
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
