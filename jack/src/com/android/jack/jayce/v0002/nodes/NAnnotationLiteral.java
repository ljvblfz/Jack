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

import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationLiteral;
import com.android.jack.ir.ast.JRetentionPolicy;
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
 * An annotation on an element of a class. Annotations have an
 * associated type and additionally consist of a set of (name, value)
 * pairs, where the names are unique.
 */
public class NAnnotationLiteral extends NLiteral {

  @Nonnull
  public static final Token TOKEN = Token.ANNOTATION_LITERAL;

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
    JAnnotationLiteral jAnnotationLiteral = (JAnnotationLiteral) node;
    retentionPolicy = jAnnotationLiteral.getRetentionPolicy();
    annotationType = ImportHelper.getSignatureName(jAnnotationLiteral.getType());
    elements = loader.load(NNameValuePair.class, jAnnotationLiteral.getNameValuePairs());
    sourceInfo = loader.load(jAnnotationLiteral.getSourceInfo());
  }

  @Override
  @Nonnull
  public JAnnotationLiteral exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert retentionPolicy != null;
    assert sourceInfo != null;
    assert annotationType != null;
    SourceInfo jSourceInfo = sourceInfo.exportAsJast();
    JAnnotation type = exportSession.getLookup().getAnnotation(annotationType);
    JAnnotationLiteral jAnnotationLiteral =
        new JAnnotationLiteral(jSourceInfo, retentionPolicy, type);
    for (NNameValuePair valuePair : elements) {
      jAnnotationLiteral.put(valuePair.exportAsJast(exportSession));
    }
    return jAnnotationLiteral;
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
