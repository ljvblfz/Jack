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

import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JParameterUnresolved;
import com.android.jack.jayce.linker.VariableRefLinker;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java parameter reference.
 */
public class NParameterRef extends NExpression {

  @Nonnull
  public static final Token TOKEN = Token.PARAMETER_REF;

  @CheckForNull
  public String localId;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JParameterRef jRef = (JParameterRef) node;
    localId = loader.getVariableSymbols().getId(jRef.getParameter());
    sourceInfo = jRef.getSourceInfo();
  }

  @Override
  @Nonnull
  public JParameterRef exportAsJast(@Nonnull ExportSession exportSession) {
    assert sourceInfo != null;
    assert localId != null;
    JParameterRef jRef = JParameterUnresolved.INSTANCE.makeRef(sourceInfo);
    exportSession.getVariableResolver().addLink(localId, new VariableRefLinker(jRef));
    return jRef;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeId(localId);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    localId = in.readId();
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
