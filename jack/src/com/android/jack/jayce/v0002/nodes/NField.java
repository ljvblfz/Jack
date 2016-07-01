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

import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.FieldNode;
import com.android.jack.jayce.JayceClassOrInterfaceLoader;
import com.android.jack.jayce.JayceFieldLoader;
import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.v0002.NNode;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java field definition.
 */
public class NField extends NNode implements HasSourceInfo, FieldNode {

  @Nonnull
  public static final Token TOKEN = Token.FIELD;

  protected static final int INDEX_UNKNOWN = -1;

  public int modifiers;

  @CheckForNull
  public String type;

  @CheckForNull
  public String name;

  @CheckForNull
  public NLiteral initialValue;

  @Nonnull
  public List<NAnnotation> annotations = Collections.emptyList();

  @Nonnull
  public List<NMarker> markers = Collections.emptyList();

  @CheckForNull
  public NSourceInfo sourceInfo;

  protected int fieldNodeIndex = INDEX_UNKNOWN;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JField jField = (JField) node;
    modifiers = jField.getModifier();
    type = ImportHelper.getSignatureName(jField.getType());
    name = jField.getName();
    initialValue = (NLiteral) loader.load(jField.getInitialValue());
    annotations = loader.load(NAnnotation.class, jField.getAnnotations());
    markers = loader.load(NMarker.class, jField.getAllMarkers());
    sourceInfo = loader.load(jField.getSourceInfo());
  }

  @Override
  @Nonnull
  public JField exportAsJast(@Nonnull ExportSession exportSession) throws JTypeLookupException,
      JMethodLookupException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  public JField exportAsJast(@Nonnull ExportSession exportSession,
      @Nonnull JayceClassOrInterfaceLoader enclosingLoader) throws JTypeLookupException,
      JMethodLookupException {
    assert sourceInfo != null;
    assert name != null;
    assert type != null;
    JDefinedClassOrInterface enclosingType = exportSession.getCurrentType();
    assert enclosingType != null;
    JField jField = new JField(
        sourceInfo.exportAsJast(exportSession),
        name,
        enclosingType,
        exportSession.getLookup().getType(type),
        modifiers,
        new JayceFieldLoader(this, fieldNodeIndex, enclosingLoader));

    assert name != null;
    assert type != null;
    exportSession.getFieldInitializerFieldResolver().addTarget(getResolverFieldId(name, type),
        jField);
    if (initialValue != null) {
      jField.setInitialValue(initialValue.exportAsJast(exportSession));
    }
    for (NMarker marker : markers) {
      jField.addMarker(marker.exportAsJast(exportSession));
    }
    return jField;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeInt(modifiers);
    out.writeId(type);
    out.writeId(name);
    out.writeNode(initialValue);
    out.writeNodes(annotations);
    out.writeNodes(markers);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    modifiers = in.readInt();
    type = in.readId();
    name = in.readId();
    initialValue = in.readNode(NLiteral.class);
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

  @Override
  public void setIndex(int index) {
    fieldNodeIndex = index;
  }

  @Nonnull
  static String getResolverFieldId(@Nonnull String name, @Nonnull String type) {
    return name + "-" + type;
  }

  @Override
  public void loadAnnotations(@Nonnull JField loading, @Nonnull JayceFieldLoader loader) {
    if (!annotations.isEmpty()) {
      ExportSession exportSession =
          new ExportSession(loader.getSession(), NodeLevel.STRUCTURE);
      for (NAnnotation annotation : annotations) {
        JAnnotation annote = annotation.exportAsJast(exportSession);
        loading.addAnnotation(annote);
        annote.updateParents(loading);
      }
    }
  }

}
