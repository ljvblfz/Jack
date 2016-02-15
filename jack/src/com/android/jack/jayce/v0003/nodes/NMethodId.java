/*
 * Copyright (C) 2016 The Android Open Source Project
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

import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.jayce.v0003.NNode;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0003.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Node representing a method id with a return type.
 */
public class NMethodId extends NNode {
  @Nonnull
  public static final Token TOKEN = Token.METHODID_WITH_RETURN_TYPE;

  @CheckForNull String name;

  @CheckForNull String returnTypeSig;

  @Nonnull
  private List<String> paramTypeSigs = Collections.emptyList();

  @CheckForNull
  private MethodKind methodKind;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JMethodId mthIdToImplement = (JMethodId) node;
    name = mthIdToImplement.getMethodIdWide().getName();
    returnTypeSig = ImportHelper.getSignatureName(mthIdToImplement.getType());
    paramTypeSigs =
        ImportHelper.getSignatureNameList(mthIdToImplement.getMethodIdWide().getParamTypes());
    methodKind = mthIdToImplement.getMethodIdWide().getKind();
  }

  @Override
  @Nonnull
  public Object exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert name != null;
    assert returnTypeSig != null;
    assert methodKind != null;
    JType returnType = exportSession.getLookup().getType(returnTypeSig);
    JMethodIdWide mthId = new JMethodIdWide(name, methodKind);
    for (String paramSig : paramTypeSigs) {
      mthId.addParam(exportSession.getLookup().getType(paramSig));
    }
    return new JMethodId(mthId, returnType);
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    assert methodKind != null;
    out.writeId(name);
    out.writeMethodKindEnum(methodKind);
    out.writeId(returnTypeSig);
    out.writeIds(paramTypeSigs);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    name = in.readId();
    methodKind = in.readMethodKindEnum();
    returnTypeSig = in.readId();
    paramTypeSigs = in.readIds();
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}