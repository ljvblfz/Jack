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

import com.android.jack.ir.ast.JDoStatement;
import com.android.jack.ir.ast.JStatement;
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
 * Java do statement.
 */
public class NDoStatement extends NStatement {

  @Nonnull
  public static final Token TOKEN = Token.DO_STATEMENT;

  @CheckForNull
  public NExpression testExpression;

  @CheckForNull
  public NStatement body;

  @Nonnull
  public List<String> catchBlockIds = Collections.emptyList();

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JDoStatement jDoStatement = (JDoStatement) node;
    testExpression = (NExpression) loader.load(jDoStatement.getTestExpr());
    body = (NStatement) loader.load(jDoStatement.getBody());
    catchBlockIds = loader.getIds(loader.getCatchBlockSymbols(), jDoStatement.getJCatchBlocks());
    sourceInfo = jDoStatement.getSourceInfo();
  }

  @Override
  @Nonnull
  public JDoStatement exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert testExpression != null;
    assert sourceInfo != null;
    JStatement jBody = body != null ? body.exportAsJast(exportSession) : null;
    JDoStatement jDoStatement =
        new JDoStatement(sourceInfo, testExpression.exportAsJast(exportSession), jBody);
    for (String catchId : catchBlockIds) {
      exportSession.getCatchBlockResolver().addLink(catchId, new CatchBlockLinker(jDoStatement));
    }
    return jDoStatement;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeNode(testExpression);
    out.writeNode(body);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    testExpression = in.readNode(NExpression.class);
    body = in.readNode(NStatement.class);
  }

  public static void skipContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    in.skipNode();
    in.skipNode();
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
