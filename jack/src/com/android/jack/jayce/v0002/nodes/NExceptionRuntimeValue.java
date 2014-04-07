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

import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
/**
 * Expression representing the runtime value of a catched exception.
 */
public class NExceptionRuntimeValue extends NExpression {

  @Nonnull
  public static final Token TOKEN = Token.EXCEPTION_RUNTIME_VALUE;

  @CheckForNull
  public String catchedType;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JExceptionRuntimeValue jExceptionRuntime = (JExceptionRuntimeValue) node;
    catchedType = ImportHelper.getSignatureName(jExceptionRuntime.getType());
    sourceInfo = loader.load(jExceptionRuntime.getSourceInfo());
  }

  @Override
  @Nonnull
  public JExceptionRuntimeValue exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException {
    assert sourceInfo != null;
    assert catchedType != null;
    return new JExceptionRuntimeValue(
        sourceInfo.exportAsJast(exportSession),
        (JClassOrInterface) exportSession.getLookup().getType(catchedType));
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeId(catchedType);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    catchedType = in.readId();
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