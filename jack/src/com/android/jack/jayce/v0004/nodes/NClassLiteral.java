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

import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java class literal expression.
 */
public class NClassLiteral extends NLiteral {

  @Nonnull
  public static final Token TOKEN = Token.CLASS_LITERAL;

  @Nonnull
  private static final String JAVA_LANG_CLASS = "Ljava/lang/Class;";

  @CheckForNull
  public String refType;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JClassLiteral jClassLiteral = (JClassLiteral) node;
    refType = ImportHelper.getSignatureName(jClassLiteral.getRefType());
    sourceInfo = jClassLiteral.getSourceInfo();
  }

  @Override
  @Nonnull
  public JClassLiteral exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException {
    assert sourceInfo != null;
    assert refType != null;
    return new JClassLiteral(sourceInfo, exportSession.getLookup().getType(refType));
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeId(refType);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    refType = in.readId();

  }

  public static void skipContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    in.skipId();
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
