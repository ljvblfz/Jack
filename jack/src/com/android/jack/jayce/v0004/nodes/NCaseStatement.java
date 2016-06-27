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

import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.linker.CatchBlockLinker;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java case statement.
 */
public class NCaseStatement extends NStatement {

  @Nonnull
  public static final Token TOKEN = Token.CASE_STATEMENT;

  @CheckForNull
  public String id;

  @CheckForNull
  public NLiteral expr;

  @Nonnull
  public List<String> catchBlockIds = Collections.emptyList();

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JCaseStatement caseStatement = (JCaseStatement) node;

    id = loader.getCaseSymbols().getId(caseStatement);
    expr = (NLiteral) loader.load(caseStatement.getExpr());
    catchBlockIds = loader.getIds(loader.getCatchBlockSymbols(), caseStatement.getJCatchBlocks());
    sourceInfo = caseStatement.getSourceInfo();
  }

  @Override
  @Nonnull
  public JCaseStatement exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert sourceInfo != null;
    assert id != null;
    JLiteral jExpr = expr != null ? expr.exportAsJast(exportSession) : null;
    JCaseStatement jCase = new JCaseStatement(sourceInfo, jExpr);
    exportSession.getCaseResolver().addTarget(id, jCase);
    for (String catchId : catchBlockIds) {
      exportSession.getCatchBlockResolver().addLink(catchId, new CatchBlockLinker(jCase));
    }
    return jCase;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeId(id);
    out.writeNode(expr);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    id = in.readId();
    expr = in.readNode(NLiteral.class);
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
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
