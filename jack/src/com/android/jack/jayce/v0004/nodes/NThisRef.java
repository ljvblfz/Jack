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

import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java method this expression.
 */
public class NThisRef extends NExpression {
  @Nonnull
  public static final Token TOKEN = Token.THIS_REF;

  @CheckForNull
  public String type;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JThisRef jThisRef = (JThisRef) node;
    type = ImportHelper.getSignatureName(jThisRef.getType());
    sourceInfo = jThisRef.getSourceInfo();
  }

  @Override
  @Nonnull
  public JThisRef exportAsJast(@Nonnull ExportSession exportSession) {
    assert sourceInfo != null;
    assert type != null;
    JThis jThis = exportSession.getCurrentMethod().getThis();
    assert jThis != null;
    JThisRef jThisRef = jThis.makeRef(sourceInfo);
    return jThisRef;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeId(type);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    type = in.readId();

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
