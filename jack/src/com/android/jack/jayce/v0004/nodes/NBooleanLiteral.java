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

import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Java boolean literal expression.
 */
public class NBooleanLiteral extends NLiteral {

  @Nonnull
  public static final Token TOKEN = Token.BOOLEAN_LITERAL;

  public boolean value;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JBooleanLiteral jBooleanLiteral = (JBooleanLiteral) node;
    value = jBooleanLiteral.getValue();
    sourceInfo = jBooleanLiteral.getSourceInfo();
  }

  @Override
  @Nonnull
  public JBooleanLiteral exportAsJast(@Nonnull ExportSession exportSession) {
    assert sourceInfo != null;
    JBooleanLiteral jBooleanLiteral = new JBooleanLiteral(sourceInfo, value);
    return jBooleanLiteral;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeBoolean(value);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    value = in.readBoolean();

  }

  public static void skipContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    in.skipBoolean();
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
