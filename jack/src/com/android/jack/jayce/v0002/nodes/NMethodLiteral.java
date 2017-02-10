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

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodLiteral;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Method literal.
 */
public class NMethodLiteral extends NLiteral {

  @Nonnull
  public static final Token TOKEN = Token.METHOD_LITERAL;

  @CheckForNull
  public String method;

  @CheckForNull
  public String methodEnclosingType;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JMethodLiteral jMethodLiteral = (JMethodLiteral) node;
    method = ImportHelper.getMethodSignature(jMethodLiteral.getMethod());
    methodEnclosingType =
        ImportHelper.getSignatureName(jMethodLiteral.getMethod().getEnclosingType());
    sourceInfo = loader.load(jMethodLiteral.getSourceInfo());
  }

  @Override
  @Nonnull
  public JMethodLiteral exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert sourceInfo != null;
    assert methodEnclosingType != null;
    assert method != null;
    JDefinedClassOrInterface jEnclosingType =
        (JDefinedClassOrInterface) exportSession.getLookup().getType(methodEnclosingType);
    JMethod jMethod = exportSession.getDeclaredMethod(jEnclosingType, method);
    SourceInfo jSourceInfo = sourceInfo.exportAsJast(exportSession);
    JMethodLiteral jMethodLiteral = new JMethodLiteral(jMethod, jSourceInfo);
    return jMethodLiteral;
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    method = in.readId();
    methodEnclosingType = in.readId();
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
