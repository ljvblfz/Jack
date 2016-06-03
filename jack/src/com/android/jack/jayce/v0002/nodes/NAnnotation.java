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
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.sourceinfo.SourceInfo;
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
 * NNode of a {@link JAnnotation}, it is an instance of {@link JAnnotationType} and consist of a
 * reference to an annotation type and zero or more element-value pairs, each of which associates a
 * value with a different element of the annotation type.
 */
public class NAnnotation extends NLiteral {

  @Nonnull
  public static final Token TOKEN = Token.ANNOTATION;

  @CheckForNull
  public JRetentionPolicy retentionPolicy;

  @CheckForNull
  public String annotationType;

  @Nonnull
  public List<NNameValuePair> elements = Collections.emptyList();

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JAnnotation jAnnotation = (JAnnotation) node;
    retentionPolicy = jAnnotation.getRetentionPolicy();
    annotationType = ImportHelper.getSignatureName(jAnnotation.getType());
    elements = loader.load(NNameValuePair.class, jAnnotation.getNameValuePairs());
    sourceInfo = loader.load(jAnnotation.getSourceInfo());
  }

  @Override
  @Nonnull
  public JAnnotation exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert retentionPolicy != null;
    assert sourceInfo != null;
    assert annotationType != null;
    SourceInfo jSourceInfo = sourceInfo.exportAsJast(exportSession);
    JAnnotationType type = exportSession.getLookup().getAnnotationType(annotationType);
    JAnnotation jAnnotation = new JAnnotation(jSourceInfo, retentionPolicy, type);
    for (NNameValuePair valuePair : elements) {
      jAnnotation.put(valuePair.exportAsJast(exportSession, type));
    }
    return jAnnotation;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    assert retentionPolicy != null;
    out.writeRetentionPolicyEnum(retentionPolicy);
    out.writeId(annotationType);
    out.writeNodes(elements);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    retentionPolicy = in.readRetentionPolicyEnum();
    annotationType = in.readId();
    elements = in.readNodes(NNameValuePair.class);
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
