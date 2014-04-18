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

import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Conditional expression.
 */
public class NConditionalExpression extends NExpression {

  @Nonnull
  public static final Token TOKEN = Token.CONDITIONAL_EXPRESSION;

  @CheckForNull
  public NExpression ifTest;

  @CheckForNull
  public NExpression thenExpr;

  @CheckForNull
  public NExpression elseExpr;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JConditionalExpression jConditionalExpression = (JConditionalExpression) node;
    ifTest = (NExpression) loader.load(jConditionalExpression.getIfTest());
    thenExpr = (NExpression) loader.load(jConditionalExpression.getThenExpr());
    elseExpr = (NExpression) loader.load(jConditionalExpression.getElseExpr());
    sourceInfo = loader.load(jConditionalExpression.getSourceInfo());
  }

  @Override
  @Nonnull
  public JConditionalExpression exportAsJast(@Nonnull ExportSession exportSession) {
    assert sourceInfo != null;
    assert ifTest != null;
    assert thenExpr != null;
    assert elseExpr != null;
    JExpression jIf = ifTest.exportAsJast(exportSession);
    JExpression jThen = thenExpr.exportAsJast(exportSession);
    JExpression jElse = elseExpr.exportAsJast(exportSession);
    SourceInfo jSourceInfo = sourceInfo.exportAsJast();
    JConditionalExpression jConditionalExpression =
        new JConditionalExpression(jSourceInfo, jIf, jThen, jElse);
    return jConditionalExpression;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeNode(ifTest);
    out.writeNode(thenExpr);
    out.writeNode(elseExpr);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    ifTest = in.readNode(NExpression.class);
    thenExpr = in.readNode(NExpression.class);
    elseExpr = in.readNode(NExpression.class);

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
