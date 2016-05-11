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

import com.android.jack.ir.ast.JArrayLiteral;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * {@link NLiteral} representing an array of other {@code NLiteral}.
 */
public class NArrayLiteral extends NLiteral {

  @Nonnull
  public static final Token TOKEN = Token.ARRAY_LITERAL;

  @Nonnull
  public List<NLiteral> values = Collections.emptyList();

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JArrayLiteral jArrayLiteral = (JArrayLiteral) node;
    values = loader.load(NLiteral.class, jArrayLiteral.getValues());
    sourceInfo = loader.load(jArrayLiteral.getSourceInfo());
  }

  @Override
  @Nonnull
  public JArrayLiteral exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert sourceInfo != null;
    List<JLiteral> jValues = new ArrayList<JLiteral>(values.size());
    for (NLiteral value : values) {
      jValues.add(value.exportAsJast(exportSession));
    }
    SourceInfo jSourceInfo = sourceInfo.exportAsJast(exportSession);
    JArrayLiteral jArrayLiteral = new JArrayLiteral(jSourceInfo, jValues);
    return jArrayLiteral;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeNodes(values);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    values = in.readNodes(NLiteral.class);
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
