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

import com.android.jack.ir.ast.JAnnotationMethod;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.JayceClassOrInterfaceLoader;
import com.android.jack.jayce.JayceMethodLoader;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Method of an annotation type.
 */
public class NAnnotationMethod extends NMethod {

  @SuppressWarnings("hiding")
  @Nonnull
  public static final Token TOKEN = Token.ANNOTATION_METHOD;

  @CheckForNull
  public NLiteral defaultValue;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JAnnotationMethod jAnnotationMethod = (JAnnotationMethod) node;
    assert jAnnotationMethod.getBody() == null;
    assert jAnnotationMethod.getParams().isEmpty();
    name = jAnnotationMethod.getName();
    returnType = ImportHelper.getSignatureName(jAnnotationMethod.getType());
    modifier = jAnnotationMethod.getModifier();
    annotations = loader.load(NAnnotation.class, jAnnotationMethod.getAnnotations());
    defaultValue = (NLiteral) loader.load(jAnnotationMethod.getDefaultValue());
    markers = loader.load(NMarker.class, jAnnotationMethod.getAllMarkers());
    sourceInfo = loader.load(jAnnotationMethod.getSourceInfo());
  }

  @Override
  @Nonnull
  public JAnnotationMethod exportAsJast(@Nonnull ExportSession exportSession) {
      throw new UnsupportedOperationException();
    }

  @Override
    @Nonnull
  public JMethod exportAsJast(@Nonnull ExportSession exportSession,
      @Nonnull JayceClassOrInterfaceLoader enclosingLoader) throws JTypeLookupException,
      JMethodLookupException {
    assert name != null;
    assert returnType != null;
    assert sourceInfo != null;
    assert body == null;
    assert methodId != null;
    SourceInfo info = sourceInfo.exportAsJast(exportSession);
    JDefinedClassOrInterface enclosingType = exportSession.getCurrentType();
    assert enclosingType != null;
    JType returnJType = exportSession.getLookup().getType(returnType);
    JAnnotationMethod jAnnotationMethod = new JAnnotationMethod(
        info,
        new JMethodId(new JMethodIdWide(name, MethodKind.INSTANCE_VIRTUAL), returnJType),
        enclosingType,
        modifier,
        new JayceMethodLoader(this, methodId, enclosingLoader));
    exportSession.setCurrentMethod(jAnnotationMethod);
    if (defaultValue != null) {
      jAnnotationMethod.setDefaultValue(defaultValue.exportAsJast(exportSession));
    }
    for (NMarker marker : markers) {
      jAnnotationMethod.addMarker(marker.exportAsJast(exportSession));
    }
    return jAnnotationMethod;
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    level = in.getNodeLevel();
    name = in.readId();
    returnType = in.readId();
    modifier = in.readInt();
    annotations = in.readNodes(NAnnotation.class);
    defaultValue = in.readNode(NLiteral.class);
    markers = in.readNodes(NMarker.class);
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }

}
