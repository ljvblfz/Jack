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

import com.android.jack.ir.ast.JAbstractMethodCall;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JPolymorphicMethodCall;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;
import com.android.jack.jayce.v0004.nodes.NMethodCall.ReceiverKind;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java method call expression.
 */
public class NPolymorphicCall extends NExpression {

  @Nonnull
  public static final Token TOKEN = Token.POLYMORPHIC_CALL;

  @CheckForNull
  public NExpression instance;

  @CheckForNull
  public String receiverType;

  @CheckForNull
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
  public String callSiteReturnType;

  @Nonnull
  public List<String> callSiteParameterStrTypes = Collections.emptyList();

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JPolymorphicMethodCall pmc = (JPolymorphicMethodCall) node;
    instance = (NExpression) loader.load(pmc.getInstance());
    receiverType = ImportHelper.getSignatureName(pmc.getReceiverType());
    receiverKind = getReceiverKind(pmc);
    methodName = pmc.getMethodId().getName();
    methodArgsType = ImportHelper.getMethodArgsSignature(pmc.getMethodId());
    methodKind = pmc.getMethodId().getKind();
    returnType = ImportHelper.getSignatureName(pmc.getReturnTypeOfPolymorphicMethod());
    args = loader.load(NExpression.class, pmc.getArgs());
    sourceInfo = pmc.getSourceInfo();
    callSiteReturnType = ImportHelper.getSignatureName(pmc.getType());
    callSiteParameterStrTypes = ImportHelper.getSignatureNameList(pmc.getCallSiteParameterTypes());
  }

  private ReceiverKind getReceiverKind(JAbstractMethodCall jMethodCall) {
    return jMethodCall.getReceiverType() instanceof JClass ? ReceiverKind.CLASS :
      ReceiverKind.INTERFACE;
  }

  @Override
  @Nonnull
  public JPolymorphicMethodCall exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert sourceInfo != null;
    assert receiverType != null;
    assert receiverKind != null;
    assert methodName != null;
    assert methodArgsType != null;
    assert methodKind != null;
    assert returnType != null;
    assert instance != null;
    assert callSiteReturnType != null;

    JClassOrInterface jReceiverType;
    if (receiverKind == ReceiverKind.CLASS) {
      jReceiverType = exportSession.getLookup().getClass(receiverType);
    } else {
      jReceiverType = exportSession.getLookup().getInterface(receiverType);
    }

    JType jReturnType = exportSession.getLookup().getType(returnType);
    JMethodId methodId = jReceiverType.getOrCreateMethodId(methodName,
        exportSession.getTypeListFromSignatureList(methodArgsType), methodKind, jReturnType);

    List<JType> callSiteParameterTypes = new ArrayList<>(callSiteParameterStrTypes.size());
    for (String callsiteParameterType : callSiteParameterStrTypes) {
      callSiteParameterTypes.add(exportSession.getLookup().getType(callsiteParameterType));
    }

    JPolymorphicMethodCall pmc = new JPolymorphicMethodCall(sourceInfo,
        instance.exportAsJast(exportSession), jReceiverType, methodId,
        exportSession.getLookup().getType(callSiteReturnType), callSiteParameterTypes);

    for (NExpression arg : args) {
      pmc.addArg(arg.exportAsJast(exportSession));
    }

    return pmc;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    assert instance != null;
    assert receiverType != null;
    assert receiverKind != null;
    assert methodName != null;
    assert methodKind != null;
    assert returnType != null;
    assert sourceInfo != null;
    out.writeNode(instance);
    out.writeId(receiverType);
    out.writeReceiverKindEnum(receiverKind);
    out.writeId(methodName);
    out.writeIds(methodArgsType);
    out.writeMethodKindEnum(methodKind);
    out.writeId(returnType);
    out.writeNodes(args);
    out.writeId(callSiteReturnType);
    out.writeIds(callSiteParameterStrTypes);
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
    callSiteReturnType = in.readId();
    callSiteParameterStrTypes = in.readIds();
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
