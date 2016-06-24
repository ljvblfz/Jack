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

import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JEnumField;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.JayceClassOrInterfaceLoader;
import com.android.jack.jayce.JayceFieldLoader;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Java field definition.
 */
public class NEnumField extends NField {

  @SuppressWarnings("hiding")
  @Nonnull
  public static final Token TOKEN = Token.ENUM_FIELD;

  public int ordinal = JEnumField.ORDINAL_UNKNOWN;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JEnumField jEnumField = (JEnumField) node;
    modifiers = jEnumField.getModifier();
    type = ImportHelper.getSignatureName(jEnumField.getType());
    name = jEnumField.getName();
    initialValue = (NLiteral) loader.load(jEnumField.getInitialValue());
    ordinal = jEnumField.ordinal();
    annotations = loader.load(NAnnotation.class, jEnumField.getAnnotations());
    markers = loader.load(NMarker.class, jEnumField.getAllMarkers());
    sourceInfo = loader.load(jEnumField.getSourceInfo());
  }

  @Override
  @Nonnull
  public JEnumField exportAsJast(@Nonnull ExportSession exportSession,
      @Nonnull JayceClassOrInterfaceLoader enclosingLoader) throws JTypeLookupException,
      JMethodLookupException {
    assert sourceInfo != null;
    assert name != null;
    assert type != null;
    JDefinedEnum enclosingType = (JDefinedEnum) exportSession.getCurrentType();
    assert enclosingType != null;
    JEnumField jField = new JEnumField(
        sourceInfo.exportAsJast(exportSession),
        name,
        ordinal,
        enclosingType,
        (JDefinedClass) exportSession.getLookup().getType(type),
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
    out.writeInt(ordinal);
    out.writeNodes(annotations);
    out.writeNodes(markers);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    modifiers = in.readInt();
    type = in.readId();
    name = in.readId();
    initialValue = in.readNode(NLiteral.class);
    ordinal = in.readInt();
    annotations = in.readNodes(NAnnotation.class);
    markers = in.readNodes(NMarker.class);

  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
