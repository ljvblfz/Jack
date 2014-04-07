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

import com.android.jack.ir.ast.FieldKind;
import com.android.jack.ir.ast.JEnum;
import com.android.jack.ir.ast.JEnumLiteral;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;
import com.android.jack.lookup.JLookup;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * {@link NLiteral} representing a reference to an {@code Enum} field.
 */
public class NEnumLiteral extends NLiteral {

  @Nonnull
  public static final Token TOKEN = Token.ENUM_LITERAL;

  @CheckForNull
  public String enumFieldDeclaringType;
  @CheckForNull
  public String enumFieldName;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JEnumLiteral jEnumLiteral = (JEnumLiteral) node;
    enumFieldDeclaringType =
        ImportHelper.getSignatureName(jEnumLiteral.getType());
    enumFieldName = jEnumLiteral.getFieldId().getName();
    sourceInfo = loader.load(jEnumLiteral.getSourceInfo());
  }

  @Override
  @Nonnull
  public JEnumLiteral exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException {
    assert sourceInfo != null;
    assert enumFieldDeclaringType != null;
    assert enumFieldName != null;
    SourceInfo jSourceInfo = sourceInfo.exportAsJast(exportSession);
    JLookup lookup = exportSession.getLookup();
    JEnum enumType = lookup.getEnum(enumFieldDeclaringType);
    /* type of the field is enumType, see JLS-8 8.9.2 */
    JFieldId field = exportSession.getFieldId(enumType, enumFieldName, enumType, FieldKind.STATIC);
    JEnumLiteral jEnumLiteral = new JEnumLiteral(jSourceInfo, field);
    return jEnumLiteral;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeId(enumFieldDeclaringType);
    out.writeId(enumFieldName);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    enumFieldDeclaringType = in.readId();
    enumFieldName = in.readId();
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
