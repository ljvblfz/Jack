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
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JReferenceType;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java instance of expression.
 */
public class NInstanceOf extends NExpression {
  @Nonnull
  public static final Token TOKEN = Token.INSTANCE_OF;

  @CheckForNull
  public NExpression expr;

  @CheckForNull
  public String testType;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JInstanceOf jInstanceOf = (JInstanceOf) node;
    expr = (NExpression) loader.load(jInstanceOf.getExpr());
    testType = ImportHelper.getSignatureName(jInstanceOf.getTestType());
    sourceInfo = loader.load(jInstanceOf.getSourceInfo());
  }

  @Override
  @Nonnull
  public JInstanceOf exportAsJast(@Nonnull ExportSession exportSession) {
    assert sourceInfo != null;
    assert expr != null;
    assert testType != null;
    JExpression jExpr = expr.exportAsJast(exportSession);
    JReferenceType jType = (JReferenceType) exportSession.getLookup().getType(testType);
    SourceInfo jSourceInfo = sourceInfo.exportAsJast();
    JInstanceOf jInstanceOf = new JInstanceOf(jSourceInfo, jType, jExpr);
    return jInstanceOf;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeNode(expr);
    out.writeId(testType);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    expr = in.readNode(NExpression.class);
    testType = in.readId();
    
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
