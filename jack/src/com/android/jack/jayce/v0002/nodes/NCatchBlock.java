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

import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.linker.CatchBlockLinker;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 *  A list of statements representing a catch block.
 */
public class NCatchBlock extends NStatement {

  @Nonnull
  public static final Token TOKEN = Token.CATCH_BLOCK;

  @CheckForNull
  public String id;

  @Nonnull
  protected List<NStatement> statements = Collections.emptyList();

  @Nonnull
  public List<String> catchBlockIds = Collections.emptyList();

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Nonnull
  public List<String> catchTypes = Collections.emptyList();

  @CheckForNull
  public NLocal catchVar;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JCatchBlock catchBlock = (JCatchBlock) node;

    id = loader.getCatchBlockSymbols().getId(catchBlock);
    catchTypes = ImportHelper.getSignatureNameList(catchBlock.getCatchTypes());
    catchVar = (NLocal) loader.load(catchBlock.getCatchVar());
    statements = loader.load(NStatement.class, catchBlock.getStatements());
    catchBlockIds = loader.getIds(loader.getCatchBlockSymbols(), catchBlock.getJCatchBlocks());
    sourceInfo = loader.load(catchBlock.getSourceInfo());
  }

  @Override
  @Nonnull
  public JCatchBlock exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert sourceInfo != null;
    List<JClass> jCatchTypes = new ArrayList<JClass>(catchTypes.size());
    for (String catchedType : catchTypes) {
      jCatchTypes.add(exportSession.getLookup().getClass(catchedType));
    }

    assert catchVar != null;
    JLocal jCatchVar = catchVar.exportAsJast(exportSession);
    final JCatchBlock jCatchBlock =
        new JCatchBlock(sourceInfo.exportAsJast(exportSession), jCatchTypes, jCatchVar);

    for (NStatement nStatement : statements) {
      jCatchBlock.addStmt(nStatement.exportAsJast(exportSession));
    }
    // FINDBUGS
    String id = this.id;
    assert id != null;
    exportSession.getCatchBlockResolver().addTarget(id, jCatchBlock);
    for (String catchId : catchBlockIds) {
      exportSession.getCatchBlockResolver().addLink(catchId, new CatchBlockLinker(jCatchBlock));
    }
    return jCatchBlock;
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    id = in.readId();
    catchTypes = in.readIds();
    catchVar = in.readNode(NLocal.class);
    statements = in.readNodes(NStatement.class);
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
