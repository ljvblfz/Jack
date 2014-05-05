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

import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JForStatement;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.linker.CatchBlockLinker;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java for statement.
 */
public class NForStatement extends NStatement {

  @Nonnull
  public static final Token TOKEN = Token.FOR_STATEMENT;

  @CheckForNull
  public List<NStatement> initializers = Collections.emptyList();

  @CheckForNull
  public NExpression testExpression;

  @CheckForNull
  public List<NExpressionStatement> increments = Collections.emptyList();

  @CheckForNull
  public NStatement body;

  @Nonnull
  public List<String> catchBlockIds = Collections.emptyList();

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JForStatement jForStatement = (JForStatement) node;
    initializers = loader.load(NStatement.class, jForStatement.getInitializers());
    testExpression = (NExpression) loader.load(jForStatement.getTestExpr());
    increments = loader.load(NExpressionStatement.class, jForStatement.getIncrements());
    body = (NStatement) loader.load(jForStatement.getBody());
    catchBlockIds = loader.getIds(loader.getCatchBlockSymbols(), jForStatement.getJCatchBlocks());
    sourceInfo = loader.load(jForStatement.getSourceInfo());
  }

  @Override
  @Nonnull
  public JForStatement exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert sourceInfo != null;
    assert initializers != null;
    assert increments != null;
    JExpression jTestExpression =
        testExpression != null ? testExpression.exportAsJast(exportSession) : null;
    JStatement jBody = body != null ? body.exportAsJast(exportSession) : null;
    List<JStatement> jInitializers = new ArrayList<JStatement>(initializers.size());
    for (NStatement initializer : initializers) {
      jInitializers.add(initializer.exportAsJast(exportSession));
    }
    List<JExpressionStatement> jIncrements = new ArrayList<JExpressionStatement>(increments.size());
    for (NExpressionStatement increment : increments) {
      jIncrements.add(increment.exportAsJast(exportSession));
    }
    JForStatement jForStatement = new JForStatement(
      sourceInfo.exportAsJast(), jInitializers, jTestExpression, jIncrements, jBody);
    for (String catchId : catchBlockIds) {
      exportSession.getCatchBlockResolver().addLink(catchId, new CatchBlockLinker(jForStatement));
    }
    return jForStatement;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    assert initializers != null;
    assert increments != null;

    out.writeNodes(initializers);
    out.writeNode(testExpression);
    out.writeNodes(increments);
    out.writeNode(body);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    initializers = in.readNodes(NStatement.class);
    testExpression = in.readNode(NExpression.class);
    increments = in.readNodes(NExpressionStatement.class);
    body = in.readNode(NStatement.class);
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
