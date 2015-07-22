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

import com.android.jack.ir.ast.JLabel;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.linker.CatchBlockLinker;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0003.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java statement that has an associated label.
 */
public class NLabeledStatement extends NStatement {

  @Nonnull
  public static final Token TOKEN = Token.LABELED_STATEMENT;

  @CheckForNull
  public String label;

  @CheckForNull
  public String id;

  @CheckForNull
  public NStatement body;

  @Nonnull
  public List<String> catchBlockIds = Collections.emptyList();

  @CheckForNull
  public NSourceInfo sourceInfo;


  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JLabeledStatement jLabeled = (JLabeledStatement) node;
    label = jLabeled.getLabel().getName();
    id = loader.getLabelSymbols().getId(jLabeled);
    body = (NStatement) loader.load(jLabeled.getBody());
    catchBlockIds = loader.getIds(loader.getCatchBlockSymbols(), jLabeled.getJCatchBlocks());
    sourceInfo = loader.load(jLabeled.getSourceInfo());
  }

  @Override
  @Nonnull
  public JLabeledStatement exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert sourceInfo != null;
    assert body != null;
    assert id != null;
    assert label != null;
    SourceInfo jSourceInfo = sourceInfo.exportAsJast(exportSession);
    JLabeledStatement jLabeled = new JLabeledStatement(jSourceInfo,
        new JLabel(jSourceInfo, label), body.exportAsJast(exportSession));
    exportSession.getLabelResolver().addTarget(id, jLabeled);
    for (String catchId : catchBlockIds) {
      exportSession.getCatchBlockResolver().addLink(catchId, new CatchBlockLinker(jLabeled));
    }
    return jLabeled;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeString(label);
    out.writeId(id);
    out.writeNode(body);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    label = in.readString();
    id = in.readId();
    body = in.readNode(NStatement.class);
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
