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

import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.jayce.linker.CaseStatementLinker;
import com.android.jack.jayce.linker.CatchBlockLinker;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java switch statement.
 */
public class NSwitchStatement extends NStatement {

  @Nonnull
  public static final Token TOKEN = Token.SWITCH_STATEMENT;

  @CheckForNull
  public NExpression expr;

  @Nonnull
  public List<String> cases = Collections.emptyList();

  @CheckForNull
  public NBlock body;

  @Nonnull
  public List<String> catchBlockIds = Collections.emptyList();

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JSwitchStatement switchStatement = (JSwitchStatement) node;

    expr = (NExpression) loader.load(switchStatement.getExpr());
    List<JCaseStatement> fullCaseList = switchStatement.getCases();
    if (switchStatement.getDefaultCase() != null) {
      fullCaseList = new ArrayList<JCaseStatement>(fullCaseList);
      fullCaseList.add(switchStatement.getDefaultCase());
    }
    cases = loader.getIds(loader.getCaseSymbols(), fullCaseList);
    body = (NBlock) loader.load(switchStatement.getBody());
    catchBlockIds = loader.getIds(loader.getCatchBlockSymbols(), switchStatement.getJCatchBlocks());
    sourceInfo = loader.load(switchStatement.getSourceInfo());
  }

  @Override
  @Nonnull
  public JSwitchStatement exportAsJast(@Nonnull ExportSession exportSession) {
    assert sourceInfo != null;
    assert expr != null;
    assert body != null;
    final JSwitchStatement jSwitch = new JSwitchStatement(sourceInfo.exportAsJast(),
        expr.exportAsJast(exportSession),
        body.exportAsJast(exportSession),
        new ArrayList<JCaseStatement>(1),
        null
        );
    for (String caseId : cases) {
      exportSession.getCaseResolver().addLink(caseId, new CaseStatementLinker(jSwitch));
    }
    for (String catchId : catchBlockIds) {
      exportSession.getCatchBlockResolver().addLink(catchId, new CatchBlockLinker(jSwitch));
    }
    return jSwitch;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeNode(expr);
    out.writeIds(cases);
    out.writeNode(body);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    expr = in.readNode(NExpression.class);
    cases = in.readIds();
    body = in.readNode(NBlock.class);
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
