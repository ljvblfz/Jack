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

import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.JayceClassOrInterfaceLoader;
import com.android.jack.jayce.JayceMethodLoader;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * A Java constructor method.
 */
public class NConstructor extends NMethod {

  @SuppressWarnings("hiding")
  @Nonnull
  public static final Token TOKEN = Token.CONSTRUCTOR;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object jElement) {
    JConstructor jConstructor = (JConstructor) jElement;
    setParameters(loader.load(NParameter.class, jConstructor.getParams()));
    modifier = jConstructor.getModifier();
    annotations = loader.load(NAnnotation.class, jConstructor.getAnnotations());
    body = (NAbstractMethodBody) loader.load(jConstructor.getBody());
    markers = loader.load(NMarker.class, jConstructor.getAllMarkers());
    sourceInfo = loader.load(jConstructor.getSourceInfo());
  }

  @Override
  @Nonnull
  public JConstructor exportAsJast(@Nonnull ExportSession exportSession) {
    throw new UnsupportedOperationException();
  }

@Override
  @Nonnull
  public JMethod exportAsJast(@Nonnull ExportSession exportSession,
      @Nonnull JayceClassOrInterfaceLoader enclosingLoader) throws JTypeLookupException,
      JMethodLookupException {
    assert sourceInfo != null;
    assert methodId != null;
    exportSession.getLocalResolver().clear();
    SourceInfo jSourceInfo = sourceInfo.exportAsJast(exportSession);
    JDefinedClass enclosingType = (JDefinedClass) exportSession.getCurrentType();
    assert enclosingType != null;
    JayceMethodLoader methodLoader = new JayceMethodLoader(this, methodId, enclosingLoader);
    JConstructor jConstructor = new JConstructor(jSourceInfo,
        enclosingType, modifier, methodLoader);
    exportSession.setCurrentMethod(jConstructor);
    for (NParameter parameter : getParameters()) {
      JParameter jParam = parameter.exportAsJast(exportSession, methodLoader);
      jConstructor.addParam(jParam);
      JMethodIdWide id = jConstructor.getMethodIdWide();
      id.addParam(jParam.getType());
    }
    for (NMarker marker : markers) {
      jConstructor.addMarker(marker.exportAsJast(exportSession));
    }
    clearBodyResolvers(exportSession);
    return jConstructor;
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    level = in.getNodeLevel();
    setParameters(in.readNodes(NParameter.class));
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
  public String getName() {
    return "<init>";
  }

  @Override
  @Nonnull
  public String getReturnType() {
    return "V";
  }
}
