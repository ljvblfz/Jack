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

import com.android.jack.ir.ast.JPrefixNegOperation;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Binary operator expression for {@code prefix neg}.
 */
public class NPrefixNegOperation extends NExpression {

  @Nonnull
  public static final Token TOKEN = Token.PREFIX_NEG_OPERATION;

  @CheckForNull
  public NExpression arg;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JPrefixNegOperation prefixNeg = (JPrefixNegOperation) node;
    arg = (NExpression) loader.load(prefixNeg.getArg());
    sourceInfo = loader.load(prefixNeg.getSourceInfo());
  }

  @Override
  @Nonnull
  public JPrefixNegOperation exportAsJast(@Nonnull ExportSession exportSession)
      throws JMethodLookupException, JTypeLookupException {
    assert sourceInfo != null;
    assert arg != null;
    return new JPrefixNegOperation(sourceInfo.exportAsJast(exportSession),
        arg.exportAsJast(exportSession));
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    arg = in.readNode(NExpression.class);

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
