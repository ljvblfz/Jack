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

import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JLocalUnresolved;
import com.android.jack.jayce.linker.VariableRefLinker;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0003.io.Token;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java local variable reference.
 */
public class NLocalRef extends NExpression {

  @Nonnull
  public static final Token TOKEN = Token.LOCAL_REF;

  @CheckForNull
  public String localId;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JLocalRef jLocalRef = (JLocalRef) node;
    localId = loader.getVariableSymbols().getId(jLocalRef.getLocal());
    sourceInfo = loader.load(jLocalRef.getSourceInfo());
  }

  @Override
  @Nonnull
  public JLocalRef exportAsJast(@Nonnull ExportSession exportSession) {
    assert sourceInfo != null;
    assert localId != null;
    JLocalRef jLocalRef = JLocalUnresolved.INSTANCE.makeRef(sourceInfo.exportAsJast(exportSession));
    exportSession.getVariableResolver().addLink(localId, new VariableRefLinker(jLocalRef));

    return jLocalRef;
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
