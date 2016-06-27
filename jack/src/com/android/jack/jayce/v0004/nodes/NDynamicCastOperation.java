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

import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Dynamic cast expression.
 */
public class NDynamicCastOperation extends NExpression {

  @Nonnull
  public static final Token TOKEN = Token.DYNAMIC_CAST_OPERATION;

  @CheckForNull
  public List<String> castTypes;

  @CheckForNull
  public NExpression expr;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JDynamicCastOperation jMultiCastOperation = (JDynamicCastOperation) node;
    castTypes = ImportHelper.getSignatureNameList(jMultiCastOperation.getTypes());
    expr = (NExpression) loader.load(jMultiCastOperation.getExpr());
    sourceInfo = jMultiCastOperation.getSourceInfo();
  }

  @Override
  @Nonnull
  public JDynamicCastOperation exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert sourceInfo != null;
    assert castTypes != null;
    assert expr != null;
    List<JType> jTypes = new ArrayList<JType>(castTypes.size());
    for (String types : castTypes) {
      jTypes.add(exportSession.getLookup().getType(types));
    }
    JExpression jExpr = expr.exportAsJast(exportSession);
    JDynamicCastOperation castOperation = new JDynamicCastOperation(sourceInfo, jExpr, jTypes);
    return castOperation;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    assert castTypes != null;
    out.writeIds(castTypes);
    out.writeNode(expr);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    castTypes = in.readIds();
    expr = in.readNode(NExpression.class);
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
