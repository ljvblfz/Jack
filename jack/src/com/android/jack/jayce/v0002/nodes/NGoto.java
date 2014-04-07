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

import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JLabeledStatementUnresolved;
import com.android.jack.jayce.linker.CatchBlockLinker;
import com.android.jack.jayce.linker.GotoLinker;
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
 * Goto statement.
 */
public class NGoto extends NStatement {

  @Nonnull
  public static final Token TOKEN = Token.GOTO;

  @CheckForNull
  public String target;

  @Nonnull
  public List<String> catchBlockIds = Collections.emptyList();

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JGoto jGoto = (JGoto) node;
    target = loader.getLabelSymbols().getId(jGoto.getTargetBlock());
    catchBlockIds = loader.getIds(loader.getCatchBlockSymbols(), jGoto.getJCatchBlocks());
    sourceInfo = loader.load(jGoto.getSourceInfo());
  }

  @Override
  @Nonnull
  public JGoto exportAsJast(@Nonnull ExportSession exportSession) {
    assert sourceInfo != null;
    assert target != null;
    JGoto jGoto =
        new JGoto(sourceInfo.exportAsJast(exportSession), JLabeledStatementUnresolved.INSTANCE);
    exportSession.getLabelResolver().addLink(target, new GotoLinker(jGoto));
    for (String catchId : catchBlockIds) {
      exportSession.getCatchBlockResolver().addLink(catchId, new CatchBlockLinker(jGoto));
    }
    return jGoto;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeId(target);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    target = in.readId();
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
