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

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodCall.DispatchKind;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java method call expression.
 */
public class NMethodCall extends NExpression {

  /**
   * Method call receiver kind.
   */
  public static enum ReceiverKind {
    CLASS,
    INTERFACE;
  }

  @Nonnull
  public static final Token TOKEN = Token.METHOD_CALL;

  @CheckForNull
  public NExpression instance;

  @CheckForNull
  public String receiverType;

  public ReceiverKind receiverKind;

  @CheckForNull
  public String methodName;

  @Nonnull
  public List<String> methodArgsType = Collections.emptyList();

  @CheckForNull
  public MethodKind methodKind;

  @CheckForNull
  public String returnType;

  @Nonnull
  public List<NExpression> args = Collections.emptyList();

  @CheckForNull
  public JMethodCall.DispatchKind dispatchKind;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JMethodCall jMethodCall = (JMethodCall) node;
    instance = (NExpression) loader.load(jMethodCall.getInstance());
    receiverType = ImportHelper.getSignatureName(jMethodCall.getReceiverType());
    receiverKind = getReceiverKind(jMethodCall);
    methodName = jMethodCall.getMethodIdWide().getName();
    methodArgsType = ImportHelper.getMethodArgsSignature(jMethodCall.getMethodIdWide());
    methodKind = jMethodCall.getMethodIdWide().getKind();
    returnType = ImportHelper.getSignatureName(jMethodCall.getType());
    args = loader.load(NExpression.class, jMethodCall.getArgs());
    dispatchKind = jMethodCall.getDispatchKind();
    sourceInfo = loader.load(jMethodCall.getSourceInfo());
  }

  private ReceiverKind getReceiverKind(JMethodCall jMethodCall) {
    return jMethodCall.getReceiverType() instanceof JClass ? ReceiverKind.CLASS :
      ReceiverKind.INTERFACE;
  }

  @Override
  @Nonnull
  public JMethodCall exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert sourceInfo != null;
    assert receiverType != null;
    assert receiverKind != null;
    assert methodName != null;
    assert methodArgsType != null;
    assert methodKind != null;
    assert returnType != null;
    assert dispatchKind != null;
    JExpression jInstance = instance != null ? instance.exportAsJast(exportSession) : null;
    JClassOrInterface jReceiverType;
    if (receiverKind == ReceiverKind.CLASS) {
      jReceiverType = exportSession.getLookup().getClass(receiverType);
    } else {
      jReceiverType = exportSession.getLookup().getInterface(receiverType);
    }
    JType jReturnType = exportSession.getLookup().getType(returnType);
    JMethodId methodId = jReceiverType.getOrCreateMethodId(methodName,
        exportSession.getTypeListFromSignatureList(methodArgsType), methodKind, jReturnType);
    SourceInfo jSourceInfo = sourceInfo.exportAsJast(exportSession);
    JMethodCall jMethodCall = new JMethodCall(jSourceInfo, jInstance, jReceiverType, methodId,
        dispatchKind == DispatchKind.VIRTUAL /* isVirtualDispatch */);
    for (NExpression arg : args) {
      jMethodCall.addArg(arg.exportAsJast(exportSession));
    }
    return jMethodCall;
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    instance = in.readNode(NExpression.class);
    receiverType = in.readId();
    receiverKind = in.readReceiverKindEnum();
    methodName = in.readId();
    methodArgsType = in.readIds();
    methodKind = in.readMethodKindEnum();
    returnType = in.readId();
    args = in.readNodes(NExpression.class);
    dispatchKind = in.readDispatchKindEnum();
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
