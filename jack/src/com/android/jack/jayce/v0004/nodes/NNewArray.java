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

import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JTypeLookupException;
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
 * New array expression.
 */
public class NNewArray extends NExpression {
  @Nonnull
  public static final Token TOKEN = Token.NEW_ARRAY;

  @CheckForNull
  public String type;

  @Nonnull
  public List<NExpression> dims = Collections.emptyList();

  @Nonnull
  public List<NExpression> initializers = Collections.emptyList();

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JNewArray jNewArray = (JNewArray) node;
    type = ImportHelper.getSignatureName(jNewArray.getArrayType());
    dims = loader.load(NExpression.class, jNewArray.getDims());
    initializers = loader.load(NExpression.class, jNewArray.getInitializers());
    sourceInfo = jNewArray.getSourceInfo();
  }

  @Override
  @Nonnull
  public JNewArray exportAsJast(@Nonnull ExportSession exportSession) throws JMethodLookupException,
      JTypeLookupException {
    assert sourceInfo != null;
    assert type != null;
    JArrayType jType = (JArrayType) exportSession.getLookup().getType(type);
    if (initializers.isEmpty()) {
      List<JExpression> jDims = new ArrayList<JExpression>(dims.size());
      for (NExpression expr : dims) {
        jDims.add(expr.exportAsJast(exportSession));
      }
      return JNewArray.createWithDims(sourceInfo, jType, jDims);
    } else {
      List<JExpression> jInitializers = new ArrayList<JExpression>(initializers.size());
      for (NExpression expr : initializers) {
        jInitializers.add(expr.exportAsJast(exportSession));
      }
      return JNewArray.createWithInits(sourceInfo, jType, jInitializers);
    }
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeId(type);
    out.writeNodes(dims);
    out.writeNodes(initializers);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    type = in.readId();
    dims = in.readNodes(NExpression.class);
    initializers = in.readNodes(NExpression.class);

  }

  public static void skipContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    in.skipId();
    in.skipNodes();
    in.skipNodes();
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
