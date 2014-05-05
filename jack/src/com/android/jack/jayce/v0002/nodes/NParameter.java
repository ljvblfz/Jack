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

import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JTypeLookupException;
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
 * Java method parameter definition.
 */
public class NParameter extends NVariable {

  @Nonnull
  public static final Token TOKEN = Token.PARAMETER;

  @CheckForNull
  public String id;

  public int modifiers;

  /** Signature */
  @CheckForNull
  public String type;

  @CheckForNull
  public String name;

  @Nonnull
  public List<NAnnotationLiteral> annotations = Collections.emptyList();

  @Nonnull
  public List<NMarker> markers = Collections.emptyList();

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JParameter jParameter = (JParameter) node;
    id = loader.getParameterSymbols().getId(jParameter);
    modifiers = jParameter.getModifier();
    type = ImportHelper.getSignatureName(jParameter.getType());
    name = jParameter.getName();
    annotations = loader.load(NAnnotationLiteral.class, jParameter.getAnnotations());
    markers = loader.load(NMarker.class, jParameter.getAllMarkers());
    sourceInfo = loader.load(jParameter.getSourceInfo());
  }

  @Override
  @Nonnull
  public JParameter exportAsJast(@Nonnull ExportSession exportSession) throws JTypeLookupException,
      JMethodLookupException {
    assert sourceInfo != null;
    assert type != null;
    assert name != null;
    JParameter jParameter = new JParameter(
        sourceInfo.exportAsJast(),
        name,
        exportSession.getLookup().getType(type),
        modifiers,
        null); /* enclosingMethod */
    assert id != null;
    exportSession.getParameterResolver().addTarget(id, jParameter);
    for (NAnnotationLiteral annotation : annotations) {
      jParameter.addAnnotation(annotation.exportAsJast(exportSession));
    }
    for (NMarker marker : markers) {
      jParameter.addMarker(marker.exportAsJast(exportSession));
    }

    return jParameter;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeId(id);
    out.writeInt(modifiers);
    out.writeId(type);
    out.writeId(name);
    out.writeNodes(annotations);
    out.writeNodes(markers);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    id = in.readId();
    modifiers = in.readInt();
    type = in.readId();
    name = in.readId();
    annotations = in.readNodes(NAnnotationLiteral.class);
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
}
