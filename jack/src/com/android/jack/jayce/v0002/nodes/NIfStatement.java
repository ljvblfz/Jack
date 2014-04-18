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

import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.jayce.linker.CatchBlockLinker;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java if statement.
 */
public class NIfStatement extends NStatement {

  @Nonnull
  public static final Token TOKEN = Token.IF_STATEMENT;

  @CheckForNull
  public NExpression ifExpression;

  @CheckForNull
  public NStatement thenStatement;

  @CheckForNull
  public NStatement elseStatement;

  @Nonnull
  public List<String> catchBlockIds = Collections.emptyList();

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JIfStatement jIfStatement = (JIfStatement) node;
    ifExpression = (NExpression) loader.load(jIfStatement.getIfExpr());
    thenStatement = (NStatement) loader.load(jIfStatement.getThenStmt());
    elseStatement = (NStatement) loader.load(jIfStatement.getElseStmt());
    catchBlockIds = loader.getIds(loader.getCatchBlockSymbols(), jIfStatement.getJCatchBlocks());
    sourceInfo = loader.load(jIfStatement.getSourceInfo());
  }

  @Override
  @Nonnull
  public JIfStatement exportAsJast(@Nonnull ExportSession exportSession) {
    assert ifExpression != null;
    assert thenStatement != null;
    assert sourceInfo != null;
    JStatement jElseStatement = null;
    if (elseStatement != null) {
      jElseStatement = elseStatement.exportAsJast(exportSession);
    }
    JIfStatement jIfStatement = new JIfStatement(
        sourceInfo.exportAsJast(), ifExpression.exportAsJast(exportSession),
        thenStatement.exportAsJast(exportSession), jElseStatement);
    for (String catchId : catchBlockIds) {
      exportSession.getCatchBlockResolver().addLink(catchId, new CatchBlockLinker(jIfStatement));
    }
    return jIfStatement;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeNode(ifExpression);
    out.writeNode(thenStatement);
    out.writeNode(elseStatement);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    ifExpression = in.readNode(NExpression.class);
    thenStatement = in.readNode(NStatement.class);
    elseStatement = in.readNode(NStatement.class);
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

  @Override
  @Nonnull
  public List<String> getCatchBlockIds() {
    return catchBlockIds;
  }

  @Override
  public void setCatchBlockIds(@Nonnull List<String> catchBlockIds) {
    this.catchBlockIds = catchBlockIds;
  }
}
