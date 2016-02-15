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
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * New instance.
 */
public class NNewInstance extends NMethodCall {

  @SuppressWarnings("hiding")
  @Nonnull
  public static final Token TOKEN = Token.NEW_INSTANCE;

  @Nonnull
  public static final String INIT_NAME = "<init>";

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JNewInstance jNewInstance = (JNewInstance) node;
    receiverType = ImportHelper.getSignatureName(jNewInstance.getReceiverType());
    methodArgsType = ImportHelper.getMethodArgsSignature(jNewInstance.getMethodId());
    args = loader.load(NExpression.class, jNewInstance.getArgs());
    sourceInfo = loader.load(jNewInstance.getSourceInfo());
  }

  @Override
  @Nonnull
  public JNewInstance exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert instance == null;
    assert dispatchKind == null;
    assert methodName == null;
    assert methodKind == null;
    assert sourceInfo != null;
    assert receiverType != null;
    assert methodArgsType != null;
    assert args != null;
    JClassOrInterface jReceiverType = exportSession.getLookup().getClass(receiverType);
    JMethodIdWide methodId = jReceiverType.getOrCreateMethodIdWide(INIT_NAME,
        exportSession.getTypeListFromSignatureList(methodArgsType),
        MethodKind.INSTANCE_NON_VIRTUAL);
    SourceInfo jSourceInfo = sourceInfo.exportAsJast(exportSession);
    JNewInstance jNewInstance = new JNewInstance(jSourceInfo, jReceiverType, methodId);
    for (NExpression arg : args) {
      jNewInstance.addArg(arg.exportAsJast(exportSession));
    }
    return jNewInstance;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    assert instance == null;
    assert methodKind == null;
    out.writeId(receiverType);
    out.writeIds(methodArgsType);
    out.writeNodes(args);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    receiverType = in.readId();
    methodArgsType = in.readIds();
    args = in.readNodes(NExpression.class);
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
