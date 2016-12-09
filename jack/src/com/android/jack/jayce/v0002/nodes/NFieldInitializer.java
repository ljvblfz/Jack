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

import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Statement setting the initial value of fields.
 */
public class NFieldInitializer extends NStatement {

  @Nonnull
  public static final Token TOKEN = Token.FIELD_INITIALIZER;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public JFieldInitializer exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }

  @Override
  @Nonnull
  public NSourceInfo getSourceInfos() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setSourceInfos(@Nonnull NSourceInfo sourceInfo) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public List<String> getCatchBlockIds() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setCatchBlockIds(@Nonnull List<String> catchBlockIds) {
    throw new UnsupportedOperationException();
  }
}
