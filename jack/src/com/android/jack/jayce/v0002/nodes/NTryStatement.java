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

import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java try statement.
 */
public class NTryStatement extends NStatement {

  @Nonnull
  public static final Token TOKEN = Token.TRY_STATEMENT;

  @Nonnull
  public List<NStatement> resourcesDeclaration = Collections.emptyList();

  @CheckForNull
  public NBlock tryBlock;

  @Nonnull
  public List<NCatchBlock> catchBlocks = Collections.emptyList();

  @CheckForNull
  public NBlock finallyBlock;

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JTryStatement jTryStatement = (JTryStatement) node;
    resourcesDeclaration = loader.load(NStatement.class, jTryStatement.getResourcesDeclarations());
    tryBlock = (NBlock) loader.load(jTryStatement.getTryBlock());
    catchBlocks = loader.load(NCatchBlock.class, jTryStatement.getCatchBlocks());
    finallyBlock = (NBlock) loader.load(jTryStatement.getFinallyBlock());
    sourceInfo = loader.load(jTryStatement.getSourceInfo());
  }

  @Override
  @Nonnull
  public JTryStatement exportAsJast(@Nonnull ExportSession exportSession) {
    assert tryBlock != null;
    assert sourceInfo != null;
    List<JCatchBlock> jCatchBlocks = new ArrayList<JCatchBlock>(catchBlocks.size());
    for (NCatchBlock catchBlock : catchBlocks) {
      jCatchBlocks.add(catchBlock.exportAsJast(exportSession));
    }
    JBlock jFinallyBlock = finallyBlock != null ? finallyBlock.exportAsJast(exportSession) : null;

    List<JStatement> jResourcesDeclaration = new ArrayList<JStatement>(resourcesDeclaration.size());
    for (NStatement stmt : resourcesDeclaration) {
      jResourcesDeclaration.add(stmt.exportAsJast(exportSession));
    }

    JTryStatement jTryStatement = new JTryStatement(sourceInfo.exportAsJast(exportSession),
        jResourcesDeclaration,
        tryBlock.exportAsJast(exportSession),
        jCatchBlocks,
        jFinallyBlock);

    return jTryStatement;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeNodes(resourcesDeclaration);
    out.writeNode(tryBlock);
    out.writeNodes(catchBlocks);
    out.writeNode(finallyBlock);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    resourcesDeclaration = in.readNodes(NStatement.class);
    tryBlock = in.readNode(NBlock.class);
    catchBlocks = in.readNodes(NCatchBlock.class);
    finallyBlock = in.readNode(NBlock.class);

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
    return Collections.emptyList();
  }

  @Override
  public void setCatchBlockIds(@Nonnull List<String> catchBlockIds) {
  }
}
