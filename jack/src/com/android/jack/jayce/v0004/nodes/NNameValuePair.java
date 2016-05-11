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

import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.v0004.NNode;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A (name, value) pair. These are used as the contents of an annotation.
 */
public class NNameValuePair extends NNode implements HasSourceInfo {

  @Nonnull
  public static final Token TOKEN = Token.NAME_VALUE_PAIR;

  @CheckForNull
  public String name;

  @CheckForNull
  public NLiteral value;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JNameValuePair jNameValuePair = (JNameValuePair) node;
    name = jNameValuePair.getName();
    value = (NLiteral) loader.load(jNameValuePair.getValue());
    assert value != null;
    sourceInfo = loader.load(jNameValuePair.getSourceInfo());
  }

  @Override
  @Nonnull
  public JNameValuePair exportAsJast(@Nonnull ExportSession exportSession)
      throws JMethodLookupException, JTypeLookupException {
    assert sourceInfo != null;
    assert value != null;
    assert name != null;
    JLiteral jValue = value.exportAsJast(exportSession);
    SourceInfo jSourceInfo = sourceInfo.exportAsJast(exportSession);
    JMethodIdWide methodId = new JMethodIdWide(name, MethodKind.INSTANCE_VIRTUAL);
    JNameValuePair jNameValuePair = new JNameValuePair(jSourceInfo, methodId, jValue);
    return jNameValuePair;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeString(name);
    out.writeNode(value);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    name = in.readString();
    value = in.readNode(NLiteral.class);

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
