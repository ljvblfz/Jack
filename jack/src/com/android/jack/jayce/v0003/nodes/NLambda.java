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

import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.jayce.JayceClassOrInterfaceLoader;
import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.linker.VariableRefLinker;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0003.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;
import java.util.ArrayList;
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

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JLambda lambda = (JLambda) node;
    captureInstance = lambda.needToCaptureInstance();
    for (JVariable capturedVariable : lambda.getCapturedVariables()) {
      capturedVariableIds.add(loader.getVariableSymbols().getId(capturedVariable));
    }
    method = (NMethod) loader.load(lambda.getMethod());
    typeSig = ImportHelper.getSignatureName(lambda.getType());
    sourceInfo = loader.load(lambda.getSourceInfo());
  }

  @Override
  @Nonnull
  public JExpression exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert sourceInfo != null;
    assert capturedVariableIds != null;
    assert method != null;
    assert typeSig != null;

    ExportSession exportSessionForLambdaMethStructure = new ExportSession(exportSession.getLookup(),
        exportSession.getSession(), NodeLevel.STRUCTURE);
    exportSessionForLambdaMethStructure.setCurrentType(exportSession.getCurrentType());

    JMethod lambdaMethod =
        method.exportLambdaMethodAsJast(exportSessionForLambdaMethStructure,
            (JayceClassOrInterfaceLoader) exportSessionForLambdaMethStructure.getCurrentType()
                .getLoader());

    ExportSession exportSessionForLambdaMethod = new ExportSession(exportSession);
    exportSessionForLambdaMethod.setCurrentType(exportSession.getCurrentType());

    lambdaMethod.setThis(exportSession.getCurrentMethod().getThis());

    method.loadBody(lambdaMethod, exportSessionForLambdaMethod);

    lambdaMethod.setThis(null);

    // Lambda method is already loaded
    lambdaMethod.removeLoader();

    JLambda lambda = new JLambda(sourceInfo.exportAsJast(exportSession), lambdaMethod,
        (JDefinedInterface) exportSession.getLookup().getInterface(typeSig), captureInstance);

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
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    captureInstance = in.readBoolean();
    capturedVariableIds = in.readIds();
    method = in.readNode(NMethod.class);
    typeSig = in.readId();
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

