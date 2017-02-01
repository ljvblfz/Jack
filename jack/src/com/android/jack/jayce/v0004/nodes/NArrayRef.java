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

import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JTypeLookupException;
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
 * Java array reference expression.
 */
public class NArrayRef extends NExpression {

  @Nonnull
  public static final Token TOKEN = Token.ARRAY_REF;

  @CheckForNull
  public NExpression instance;

  @CheckForNull
  public NExpression index;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JArrayRef jArrayRef = (JArrayRef) node;
    instance = (NExpression) loader.load(jArrayRef.getInstance());
    index = (NExpression) loader.load(jArrayRef.getIndexExpr());
    sourceInfo = jArrayRef.getSourceInfo();
  }

  @Override
  @Nonnull
  public JArrayRef exportAsJast(@Nonnull ExportSession exportSession) throws JTypeLookupException,
      JMethodLookupException {
    assert sourceInfo != null;
    assert instance != null;
    assert index != null;
    JExpression jInstance = instance.exportAsJast(exportSession);
    JExpression jIndex = index.exportAsJast(exportSession);
    JArrayRef jArrayRef = new JArrayRef(sourceInfo, jInstance, jIndex);
    return jArrayRef;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeNode(instance);
    out.writeNode(index);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    instance = in.readNode(NExpression.class);
    index = in.readNode(NExpression.class);
  }

  public static void skipContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    in.skipNode();
    in.skipNode();
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
