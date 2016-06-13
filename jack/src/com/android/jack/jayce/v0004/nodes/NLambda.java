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

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdRef;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
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
 * Node representing a lambda expression.
 */
public class NLambda extends NExpression {

  @Nonnull
  public static final Token TOKEN = Token.LAMBDA;

  @Nonnull
  private List<NExpression> capturedVariables = Collections.emptyList();

  @CheckForNull
  public String methodRefType;

  @CheckForNull
  public String methodRefName;

  @Nonnull
  public List<String> methodRefArgsType = Collections.emptyList();

  @CheckForNull
  public MethodKind methodRefKind;

  @CheckForNull
  private String typeSig;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Nonnull
  private List<String> boundsIds = Collections.emptyList();

  @CheckForNull
  private NMethodId mthIdWithErasure;

  @CheckForNull
  private NMethodId mthIdWithoutErasure;

  @CheckForNull
  public String enclosingType;

  @CheckForNull
  public ReceiverKind receiverKind;

  @Nonnull
  private List<NMethodId> bridges = Collections.emptyList();

  @Nonnull
  public List<NMarker> markers = Collections.emptyList();

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JLambda lambda = (JLambda) node;
    capturedVariables = loader.load(NExpression.class, lambda.getCapturedVariables());

    JMethodIdRef methodIdRef = lambda.getMethodIdRef();
    methodRefName = methodIdRef.getMethodId().getMethodIdWide().getName();
    methodRefArgsType =
        ImportHelper.getMethodArgsSignature(methodIdRef.getMethodId().getMethodIdWide());
    methodRefKind = methodIdRef.getMethodId().getMethodIdWide().getKind();
    methodRefType = ImportHelper.getSignatureName(methodIdRef.getMethodId().getType());

    enclosingType = ImportHelper.getSignatureName(methodIdRef.getEnclosingType());
    receiverKind = methodIdRef.getEnclosingType() instanceof JClass ? ReceiverKind.CLASS
        : ReceiverKind.INTERFACE;
    typeSig = ImportHelper.getSignatureName(lambda.getType());
    sourceInfo = loader.load(lambda.getSourceInfo());
    boundsIds = ImportHelper.getSignatureNameList(lambda.getInterfaceBounds());
    mthIdWithErasure = (NMethodId) loader.load(lambda.getMethodIdWithErasure());
    mthIdWithoutErasure = (NMethodId) loader.load(lambda.getMethodIdWithoutErasure());
    bridges = loader.load(NMethodId.class, lambda.getBridgeMethodIds());
    markers = loader.load(NMarker.class, lambda.getAllMarkers());
  }

  @Override
  @Nonnull
  public JExpression exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert sourceInfo != null;
    assert capturedVariables != null;
    assert methodRefName != null;
    assert methodRefKind != null;
    assert methodRefArgsType != null;
    assert typeSig != null;
    assert mthIdWithErasure != null;
    assert mthIdWithoutErasure != null;
    assert receiverKind != null;
    assert enclosingType != null;
    assert methodRefType != null;

    JClassOrInterface jEnclosingType;
    if (receiverKind == ReceiverKind.CLASS) {
      jEnclosingType = exportSession.getLookup().getClass(enclosingType);
    } else {
      jEnclosingType = exportSession.getLookup().getInterface(enclosingType);
    }

    JMethodId methodId = jEnclosingType.getOrCreateMethodId(methodRefName,
        exportSession.getTypeListFromSignatureList(methodRefArgsType), methodRefKind,
        exportSession.getLookup().getType(methodRefType));

    List<JInterface> jBounds = new ArrayList<JInterface>(boundsIds.size());
    for (String bound : boundsIds) {
      jBounds.add(exportSession.getLookup().getInterface(bound));
    }

    JMethodId mthIdToImplements =
        (JMethodId) mthIdWithErasure.exportAsJast(exportSession);

    JMethodId jmthIdToEnforce =
        (JMethodId) mthIdWithoutErasure.exportAsJast(exportSession);

    SourceInfo jSourceInfo = sourceInfo.exportAsJast(exportSession);
    JLambda lambda = new JLambda(jSourceInfo, mthIdToImplements,
        new JMethodIdRef(jSourceInfo, (JDefinedClassOrInterface) jEnclosingType, methodId),
        exportSession.getLookup().getInterface(typeSig), jBounds, jmthIdToEnforce);

    for (NMethodId bridge : bridges) {
      lambda.addBridgeMethodId((JMethodId) bridge.exportAsJast(exportSession));
    }

    for (NExpression capturedVariable : capturedVariables) {
      JExpression jcapturedVariable = capturedVariable.exportAsJast(exportSession);
      lambda.addCapturedVariable(jcapturedVariable);
    }

    for (NMarker marker : markers) {
      lambda.addMarker(marker.exportAsJast(exportSession));
    }

    return lambda;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    assert methodRefKind != null;
    assert receiverKind != null;
    out.writeNodes(capturedVariables);
    out.writeReceiverKindEnum(receiverKind);
    out.writeId(enclosingType);
    out.writeId(methodRefName);
    out.writeIds(methodRefArgsType);
    out.writeMethodKindEnum(methodRefKind);
    out.writeId(methodRefType);
    out.writeId(typeSig);
    out.writeIds(boundsIds);
    out.writeNode(mthIdWithErasure);
    out.writeNode(mthIdWithoutErasure);
    out.writeNodes(bridges);
    out.writeNodes(markers);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    capturedVariables = in.readNodes(NExpression.class);
    receiverKind = in.readReceiverKindEnum();
    enclosingType = in.readId();
    methodRefName = in.readId();
    methodRefArgsType = in.readIds();
    methodRefKind = in.readMethodKindEnum();
    methodRefType = in.readId();
    typeSig = in.readId();
    boundsIds = in.readIds();
    mthIdWithErasure = in.readNode(NMethodId.class);
    mthIdWithoutErasure = in.readNode(NMethodId.class);
    bridges = in.readNodes(NMethodId.class);
    markers = in.readNodes(NMarker.class);
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }

  @Override
  public void setSourceInfos(@Nonnull NSourceInfo sourceInfo) {
    this.sourceInfo = sourceInfo;
  }

  @Override
  @Nonnull
  public NSourceInfo getSourceInfos() {
    assert sourceInfo != null;
    return sourceInfo;
  }
}

