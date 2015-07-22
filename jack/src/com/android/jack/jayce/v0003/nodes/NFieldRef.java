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

import com.android.jack.ir.ast.FieldKind;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.v0003.NNode;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0003.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java field reference expression.
 */
public class NFieldRef extends NExpression {

  @Nonnull
  public static final Token TOKEN = Token.FIELD_REF;

  @CheckForNull
  public String field;

  @CheckForNull
  public String fieldType;

  @CheckForNull
  public String receiverType;

  @CheckForNull
  public FieldKind kind;

  @CheckForNull
  public NExpression instance;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JFieldRef jFieldRef = (JFieldRef) node;
    field = jFieldRef.getFieldId().getName();
    fieldType = NNode.getFormatter().getName(jFieldRef.getFieldId().getType());
    receiverType = ImportHelper.getSignatureName(jFieldRef.getReceiverType());
    kind = jFieldRef.getFieldId().getKind();
    instance = (NExpression) loader.load(jFieldRef.getInstance());
    sourceInfo = loader.load(jFieldRef.getSourceInfo());
  }

  @Override
  @Nonnull
  public JFieldRef exportAsJast(@Nonnull ExportSession exportSession) throws JTypeLookupException,
      JMethodLookupException {
    assert sourceInfo != null;
    assert receiverType != null;
    assert field != null;
    assert fieldType != null;
    assert kind != null;
    JExpression jInstance = instance != null ? instance.exportAsJast(exportSession) : null;
    JType jReceiverType = exportSession.getLookup().getType(receiverType);
    JType jFieldType = exportSession.getLookup().getType(fieldType);
    return new JFieldRef(sourceInfo.exportAsJast(exportSession), jInstance,
        exportSession.getFieldId((JClassOrInterface) jReceiverType, field, jFieldType, kind),
        (JClassOrInterface) jReceiverType);
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    assert kind != null;
    out.writeId(field);
    out.writeId(fieldType);
    out.writeId(receiverType);
    out.writeFieldRefKindEnum(kind);
    out.writeNode(instance);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    field = in.readId();
    fieldType = in.readId();
    receiverType = in.readId();
    kind = in.readFieldRefKindEnum();
    instance = in.readNode(NExpression.class);
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
