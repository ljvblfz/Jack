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

import com.android.jack.ir.ast.JOrOperation;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Binary operator expression for {@code or}.
 */
public class NOrOperation extends NExpression {

  @Nonnull
  public static final Token TOKEN = Token.OR_OPERATION;

  @CheckForNull
  public NExpression lhs;

  @CheckForNull
  public NExpression rhs;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JOrOperation or = (JOrOperation) node;
    lhs = (NExpression) loader.load(or.getLhs());
    rhs = (NExpression) loader.load(or.getRhs());
    sourceInfo = loader.load(or.getSourceInfo());
  }

  @Override
  @Nonnull
  public JOrOperation exportAsJast(@Nonnull ExportSession exportSession) {
    assert sourceInfo != null;
    assert lhs != null;
    assert rhs != null;
    return new JOrOperation(sourceInfo.exportAsJast(exportSession),
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
