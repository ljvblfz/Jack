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

import com.android.jack.ir.ast.JNativeMethodBody;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Represents the body of a native method.
 */
public class NNativeMethodBody extends NAbstractMethodBody {

  @Nonnull
  public static final Token TOKEN = Token.NATIVE_METHOD_BODY;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JNativeMethodBody jNativeMethodBody = (JNativeMethodBody) node;
    sourceInfo = loader.load(jNativeMethodBody.getSourceInfo());
  }

  @Override
  @Nonnull
  public JNativeMethodBody exportAsJast(@Nonnull ExportSession exportSession) {
    assert sourceInfo != null;
    SourceInfo jSourceInfo = sourceInfo.exportAsJast(exportSession);
    JNativeMethodBody jNativeMethodBody = new JNativeMethodBody(jSourceInfo);
    return jNativeMethodBody;
  }

  @SuppressWarnings("unused")
  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {

  }

  @SuppressWarnings("unused")
  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {

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
