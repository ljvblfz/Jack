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

package com.android.jack.jayce.v0003.nodes;

import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodIdWithReturnType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.jayce.linker.VariableRefLinker;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0003.io.Token;
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

  boolean captureInstance;

  @Nonnull
  private List<String> capturedVariableIds = new ArrayList<String>();

  @CheckForNull
  private NMethod method;

  @CheckForNull
  private String typeSig;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Nonnull
  private List<String> boundsIds = Collections.emptyList();

  @CheckForNull
  private NMethodIdWithReturnType mthIdToImplement;

  @Nonnull
  private List<NMethodIdWithReturnType> bridges = Collections.emptyList();

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JLambda lambda = (JLambda) node;
    captureInstance = lambda.needToCaptureInstance();
    for (JVariableRef capturedVariableRef : lambda.getCapturedVariables()) {
      capturedVariableIds.add(loader.getVariableSymbols().getId(capturedVariableRef.getTarget()));
    }
    method = (NMethod) loader.load(lambda.getMethod());
    typeSig = ImportHelper.getSignatureName(lambda.getType());
    sourceInfo = loader.load(lambda.getSourceInfo());
    boundsIds = ImportHelper.getSignatureNameList(lambda.getInterfaceBounds());
    mthIdToImplement = (NMethodIdWithReturnType) loader.load(lambda.getMethodIdToImplement());
    bridges = loader.load(NMethodIdWithReturnType.class, lambda.getBridgeMethodIds());
  }

  @Override
  @Nonnull
  public JExpression exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert sourceInfo != null;
    assert capturedVariableIds != null;
    assert method != null;
    assert typeSig != null;
    assert mthIdToImplement != null;

    JMethod lambdaMethod =
        method.exportLambdaMethodAsJast(exportSession);

    List<JInterface> jBounds = new ArrayList<JInterface>(boundsIds.size());
    for (String bound : boundsIds) {
      jBounds.add(exportSession.getLookup().getInterface(bound));
    }

    JMethodIdWithReturnType mthIdToImplements =
        (JMethodIdWithReturnType) mthIdToImplement.exportAsJast(exportSession);

    JLambda lambda = new JLambda(sourceInfo.exportAsJast(exportSession), mthIdToImplements,
        lambdaMethod, exportSession.getLookup().getInterface(typeSig), captureInstance, jBounds);

    for (NMethodIdWithReturnType bridge : bridges) {
      lambda.addBridgeMethodId((JMethodIdWithReturnType) bridge.exportAsJast(exportSession));
    }

    for (String capturedVariableId : capturedVariableIds) {
      exportSession.getVariableResolver().addLink(capturedVariableId,
          new VariableRefLinker(lambda));
    }

    return lambda;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeBoolean(captureInstance);
    out.writeIds(capturedVariableIds);
    out.writeNode(method);
    out.writeId(typeSig);
    out.writeIds(boundsIds);
    out.writeNode(mthIdToImplement);
    out.writeNodes(bridges);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    captureInstance = in.readBoolean();
    capturedVariableIds = in.readIds();
    method = in.readNode(NMethod.class);
    typeSig = in.readId();
    boundsIds = in.readIds();
    mthIdToImplement = in.readNode(NMethodIdWithReturnType.class);
    bridges = in.readNodes(NMethodIdWithReturnType.class);
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

