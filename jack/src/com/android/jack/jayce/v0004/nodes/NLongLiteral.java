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

import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Java long literal expression.
 */
public class NLongLiteral extends NLiteral {

  @Nonnull
  public static final Token TOKEN = Token.LONG_LITERAL;

  public long value;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JLongLiteral jLongLiteral = (JLongLiteral) node;
    value = jLongLiteral.getValue();
    sourceInfo = jLongLiteral.getSourceInfo();
  }

  @Override
  @Nonnull
  public JLongLiteral exportAsJast(@Nonnull ExportSession exportSession) {
    assert sourceInfo != null;
    JLongLiteral jLongLiteral = new JLongLiteral(sourceInfo, value);
    return jLongLiteral;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeLong(value);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    value = in.readLong();

  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
