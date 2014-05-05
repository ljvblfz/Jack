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

import com.android.jack.ir.ast.JAssertStatement;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.linker.CatchBlockLinker;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java assert statement.
 */
public class NAssertStatement extends NStatement {

  @Nonnull
  public static final Token TOKEN = Token.ASSERT_STATEMENT;

  @CheckForNull
  public NExpression testExpression;

  @CheckForNull
  public NExpression arg;

  @Nonnull
  public List<String> catchBlockIds = Collections.emptyList();

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JAssertStatement jAssertStatement = (JAssertStatement) node;
    testExpression = (NExpression) loader.load(jAssertStatement.getTestExpr());
    arg = (NExpression) loader.load(jAssertStatement.getArg());
    catchBlockIds = loader.getIds(loader.getCatchBlockSymbols(),
        jAssertStatement.getJCatchBlocks());
    sourceInfo = loader.load(jAssertStatement.getSourceInfo());
  }

  @Override
  @Nonnull
  public JAssertStatement exportAsJast(@Nonnull ExportSession exportSession)
      throws JMethodLookupException, JTypeLookupException {
    assert testExpression != null;
    assert sourceInfo != null;
    JExpression jArg = null;
    if (arg != null) {
      jArg = arg.exportAsJast(exportSession);
    }
    JAssertStatement jAssertStatement = new JAssertStatement(
        sourceInfo.exportAsJast(), testExpression.exportAsJast(exportSession), jArg);
    for (String catchId : catchBlockIds) {
      exportSession.getCatchBlockResolver().addLink(catchId,
          new CatchBlockLinker(jAssertStatement));
    }
    return jAssertStatement;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeNode(testExpression);
    out.writeNode(arg);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    testExpression = in.readNode(NExpression.class);
    arg = in.readNode(NExpression.class);
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
