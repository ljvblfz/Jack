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

import com.android.jack.ir.ast.JAbstractMethodBody;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.JayceClassOrInterfaceLoader;
import com.android.jack.jayce.JayceMethodLoader;
import com.android.jack.jayce.MethodNode;
import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.ParameterNode;
import com.android.jack.jayce.v0002.NNode;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A Java method representation.
 */
public class NMethod extends NNode implements HasSourceInfo, MethodNode {

  @Nonnull
  public static final Token TOKEN = Token.METHOD;

  @CheckForNull
  protected String name;
  @CheckForNull
  protected String returnType;
  @Nonnull
  private List<NParameter> parameters = Collections.emptyList();
  @CheckForNull
  public MethodKind methodKind;

  public int modifier;
  @Nonnull
  public List<NAnnotation> annotations = Collections.emptyList();
  @CheckForNull
  public NAbstractMethodBody body;
  @Nonnull
  public List<NMarker> markers = Collections.emptyList();
  @CheckForNull
  public NSourceInfo sourceInfo;

  @CheckForNull
  protected NodeLevel level;
  @CheckForNull
  protected String methodId;

  @Override
  @Nonnull
  public NodeLevel getLevel() {
    assert level != null;
    return level;
  }

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object jElement) {
    JMethod jMethod = (JMethod) jElement;
    name = jMethod.getName();
    returnType = ImportHelper.getSignatureName(jMethod.getType());
    setParameters(loader.load(NParameter.class, jMethod.getParams()));
    methodKind = jMethod.getMethodIdWide().getKind();
    modifier = jMethod.getModifier();
    annotations = loader.load(NAnnotation.class, jMethod.getAnnotations());
    body = (NAbstractMethodBody) loader.load(jMethod.getBody());
    markers = loader.load(NMarker.class, jMethod.getAllMarkers());
    sourceInfo = loader.load(jMethod.getSourceInfo());
  }

  @Override
  @Nonnull
  public JMethod exportAsJast(@Nonnull ExportSession exportSession) {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  public JMethod exportAsJast(@Nonnull ExportSession exportSession,
      @Nonnull JayceClassOrInterfaceLoader enclosingLoader) throws JTypeLookupException,
      JMethodLookupException {
    assert name != null;
    assert returnType != null;
    assert methodKind != null;
    assert sourceInfo != null;
    assert methodId != null;
    SourceInfo info = sourceInfo.exportAsJast(exportSession);
    JDefinedClassOrInterface enclosingType = exportSession.getCurrentType();
    assert enclosingType != null;
    JMethodIdWide id = new JMethodIdWide(name, methodKind);
    JType returnJType = exportSession.getLookup().getType(returnType);
    JayceMethodLoader methodLoader = new JayceMethodLoader(this, methodId, enclosingLoader);
    JMethod jMethod = new JMethod(
        info, new JMethodId(id, returnJType), enclosingType,
        modifier, methodLoader);
    exportSession.setCurrentMethod(jMethod);
    for (NParameter parameter : getParameters()) {
      JParameter jParam = parameter.exportAsJast(exportSession, methodLoader);
      jMethod.addParam(jParam);
      id.addParam(jParam.getType());
    }
    for (NMarker marker : markers) {
      jMethod.addMarker(marker.exportAsJast(exportSession));
    }
    clearBodyResolvers(exportSession);
    return jMethod;
  }

  @CheckForNull
  @Override
  public JAbstractMethodBody loadBody(@Nonnull JMethod method, @Nonnull JayceMethodLoader loader)
      throws JTypeLookupException, JMethodLookupException {
    if (body != null) {
      ExportSession exportSession = new ExportSession(loader.getSession(), NodeLevel.FULL);
      exportSession.setCurrentMethod(method);
      exportSession.setCurrentType(method.getEnclosingType());

      Iterator<JParameter> iter = method.getParams().iterator();
      for (NParameter parameter : getParameters()) {
        assert parameter.id != null;
        exportSession.getParameterResolver().addTarget(parameter.id, iter.next());
      }

      JAbstractMethodBody jBody = body.exportAsJast(exportSession);
      method.setBody(jBody);
      clearBodyResolvers(exportSession);
      return jBody;
    }
    return null;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    assert methodKind != null;
    out.writeId(name);
    out.writeId(returnType);
    out.writeNodes(getParameters());
    out.writeMethodKindEnum(methodKind);
    out.writeInt(modifier);
    out.writeNodes(annotations);
    out.writeNode(body);
    out.writeNodes(markers);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    level = in.getNodeLevel();
    name = in.readId();
    returnType = in.readId();
    setParameters(in.readNodes(NParameter.class));
    methodKind = in.readMethodKindEnum();
    modifier = in.readInt();
    annotations = in.readNodes(NAnnotation.class);
    body = in.readNode(NAbstractMethodBody.class);
    markers = in.readNodes(NMarker.class);
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

  public void setId(@Nonnull String id) {
    this.methodId = id;
  }

  protected static void clearBodyResolvers(ExportSession exportSession) {
    exportSession.getLocalResolver().clear();
    exportSession.getCaseResolver().clear();
    exportSession.getCatchBlockResolver().clear();
    exportSession.getLabelResolver().clear();
    exportSession.getParameterResolver().clear();
  }

  @Override
  public void loadAnnotations(@Nonnull JMethod loading, @Nonnull JayceMethodLoader loader) {
    if (!annotations.isEmpty()) {
      ExportSession exportSession = new ExportSession(loader.getSession(), NodeLevel.STRUCTURE);
      for (NAnnotation annotation : annotations) {
        JAnnotation annote = annotation.exportAsJast(exportSession);
        loading.addAnnotation(annote);
        annote.updateParents(loading);
      }
    }
  }

  @Nonnull
  public List<NParameter> getParameters() {
    return parameters;
  }

  public void setParameters(@Nonnull List<NParameter> parameters) {
    this.parameters = parameters;
    int parameterIndex = 0;
    for (NParameter nParameter : parameters) {
      nParameter.setIndex(parameterIndex);
      parameterIndex++;
    }
  }

  @Override
  @Nonnull
  public ParameterNode getParameterNode(@Nonnegative int parameterNodeIndex) {
    return getParameters().get(parameterNodeIndex);
  }

  @Nonnull
  public String getName() {
    assert name != null;
    return name;
  }

  @Nonnull
  public String getReturnType() {
    assert returnType != null;
    return returnType;
  }
}
