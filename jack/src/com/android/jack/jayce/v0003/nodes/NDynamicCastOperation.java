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

package com.android.jack.jayce.v0003.nodes;

import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0003.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Dynamic cast expression.
 */
public class NDynamicCastOperation extends NExpression {
  @Nonnull
  public static final Token TOKEN = Token.DYNAMIC_CAST_OPERATION;

  @CheckForNull
  public String castType;

  @CheckForNull
  public NExpression expr;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JDynamicCastOperation jDynamicCastOperation = (JDynamicCastOperation) node;
    castType = ImportHelper.getSignatureName(jDynamicCastOperation.getCastType());
    expr = (NExpression) loader.load(jDynamicCastOperation.getExpr());
    sourceInfo = loader.load(jDynamicCastOperation.getSourceInfo());
  }

  @Override
  @Nonnull
  public JDynamicCastOperation exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert sourceInfo != null;
    assert castType != null;
    assert expr != null;
    JType jType = exportSession.getLookup().getType(castType);
    JExpression jExpr = expr.exportAsJast(exportSession);
    SourceInfo jSourceInfo = sourceInfo.exportAsJast(exportSession);
    JDynamicCastOperation jDynamicCastOperation =
        new JDynamicCastOperation(jSourceInfo, jType, jExpr);
    return jDynamicCastOperation;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeId(castType);
    out.writeNode(expr);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    castType = in.readId();
    expr = in.readNode(NExpression.class);

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
