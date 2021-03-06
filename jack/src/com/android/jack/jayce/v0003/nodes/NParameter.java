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

import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.JayceMethodLoader;
import com.android.jack.jayce.JayceParameterLoader;
import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.ParameterNode;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.Token;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Java method parameter definition.
 */
public class NParameter extends NVariable implements ParameterNode {

  @Nonnull
  public static final Token TOKEN = Token.PARAMETER;

  protected static final int INDEX_UNKNOWN = -1;

  @CheckForNull
  public String id;

  public int modifiers;

  /** Signature */
  @CheckForNull
  public String type;

  @CheckForNull
  public String name;

  @Nonnull
  public List<NAnnotation> annotations = Collections.emptyList();

  @Nonnull
  public List<NMarker> markers = Collections.emptyList();

  @CheckForNull
  public NSourceInfo sourceInfo;

  protected int parameterNodeIndex = INDEX_UNKNOWN;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JParameter jParameter = (JParameter) node;
    id = loader.getVariableSymbols().getId(jParameter);
    modifiers = jParameter.getModifier();
    type = ImportHelper.getSignatureName(jParameter.getType());
    name = jParameter.getName();
    annotations = loader.load(NAnnotation.class, jParameter.getAnnotations());
    markers = loader.load(NMarker.class, jParameter.getAllMarkers());
    sourceInfo = loader.load(jParameter.getSourceInfo());
  }

  @Override
  @Nonnull
  public JParameter exportAsJast(@Nonnull ExportSession exportSession) throws JTypeLookupException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  public JParameter exportAsJast(@Nonnull ExportSession exportSession,
      @Nonnull JayceMethodLoader enclosingMethodLoader) throws JTypeLookupException {
    assert sourceInfo != null;
    assert type != null;
    assert name != null;
    JParameter jParameter = new JParameter(
        sourceInfo.exportAsJast(exportSession),
        name,
        exportSession.getLookup().getType(type),
        modifiers,
        exportSession.getCurrentMethod(),
        new JayceParameterLoader(this, parameterNodeIndex, enclosingMethodLoader));

    manageSynthetic(jParameter);

    assert id != null;
    exportSession.getVariableResolver().addTarget(id, jParameter);
    for (NMarker marker : markers) {
      jParameter.addMarker(marker.exportAsJast(exportSession));
    }

    return jParameter;
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    id = in.readId();
    modifiers = in.readInt();
    type = in.readId();
    name = in.readId();
    annotations = in.readNodes(NAnnotation.class);
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

  public void setIndex(@Nonnegative int index) {
    parameterNodeIndex = index;
  }

  @Override
  public void loadAnnotations(@Nonnull JParameter loading, @Nonnull JayceParameterLoader loader) {
    if (!annotations.isEmpty()) {
      ExportSession exportSession = new ExportSession(loader.getSession(), NodeLevel.STRUCTURE);
      for (NAnnotation annotation : annotations) {
        JAnnotation annote = annotation.exportAsJast(exportSession);
        loading.addAnnotation(annote);
        annote.updateParents(loading);
      }
    }
  }

}
