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

import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.jayce.linker.CatchBlockLinker;
import com.android.jack.jayce.linker.FieldInitializerLinker;
import com.android.jack.jayce.v0002.NNode;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Statement setting the initial value of fields.
 */
public class NFieldInitializer extends NStatement {

  @Nonnull
  public static final Token TOKEN = Token.FIELD_INITIALIZER;

  @CheckForNull
  public NFieldRef fieldRef;

  @CheckForNull
  public NExpression initializer;

  @Nonnull
  public List<String> catchBlockIds = Collections.emptyList();

  @CheckForNull
  public NSourceInfo sourceInfo;


  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JFieldInitializer fieldInit = (JFieldInitializer) node;
    fieldRef = (NFieldRef) loader.load(fieldInit.getFieldRef());
    initializer = (NExpression) loader.load(fieldInit.getInitializer());
    catchBlockIds = loader.getIds(loader.getCatchBlockSymbols(), fieldInit.getJCatchBlocks());
    sourceInfo = loader.load(fieldInit.getSourceInfo());
  }

  @Override
  @Nonnull
  public JFieldInitializer exportAsJast(@Nonnull ExportSession exportSession) {
    assert sourceInfo != null;
    assert fieldRef != null;
    assert initializer != null;
    JFieldInitializer jFieldInitializer =
        new JFieldInitializer(
            sourceInfo.exportAsJast(exportSession),
            fieldRef.exportAsJast(exportSession),
            initializer.exportAsJast(exportSession));
    // field of fieldRef can be external after exportToJast(), so use  field resolver here
    // so the link is done when processing the field.
    JFieldId fieldId = jFieldInitializer.getFieldRef().getFieldId();
    exportSession.getFieldInitializerFieldResolver().addLink(
        NField.getResolverFieldId(fieldId.getName(),
            NNode.getFormatter().getName(fieldId.getType())),
        new FieldInitializerLinker(jFieldInitializer));
    for (String catchId : catchBlockIds) {
      exportSession.getCatchBlockResolver()
          .addLink(catchId, new CatchBlockLinker(jFieldInitializer));
    }
    return jFieldInitializer;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeNode(fieldRef);
    out.writeNode(initializer);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    fieldRef = in.readNode(NFieldRef.class);
    initializer = in.readNode(NExpression.class);
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
  @Nonnull
  public List<String> getCatchBlockIds() {
    return catchBlockIds;
  }

  @Override
  public void setCatchBlockIds(@Nonnull List<String> catchBlockIds) {
    this.catchBlockIds = catchBlockIds;
  }
}
