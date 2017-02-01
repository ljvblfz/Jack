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

import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java literal expression that evaluates to a string.
 */
public class NStringLiteral extends NLiteral {

  @Nonnull
  public static final Token TOKEN = Token.STRING_LITERAL;

  @CheckForNull
  public String value;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JAbstractStringLiteral jStringLiteral = (JAbstractStringLiteral) node;
    value = jStringLiteral.getValue();
    sourceInfo = jStringLiteral.getSourceInfo();
  }

  @Override
  @Nonnull
  public JAbstractStringLiteral exportAsJast(@Nonnull ExportSession exportSession) {
    assert sourceInfo != null;
    assert value != null;
    JAbstractStringLiteral jStringLiteral = new JStringLiteral(sourceInfo, value);
    return jStringLiteral;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeString(value);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    value = in.readString();

  }

  public static void skipContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    in.skipString();
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
