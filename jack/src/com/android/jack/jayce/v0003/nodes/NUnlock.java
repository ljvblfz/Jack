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

import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.JUnlock;
import com.android.jack.jayce.linker.CatchBlockLinker;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 *  Release the monitor(lock) for the expression {@code lockExpr}.
 */
public class NUnlock extends NStatement {

  @Nonnull
  public static final Token TOKEN = Token.UNLOCK;


  @CheckForNull
  public NExpression lockExpr;

  @Nonnull
  public List<String> catchBlockIds = Collections.emptyList();

  @CheckForNull
  public NSourceInfo sourceInfo;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JUnlock statement = (JUnlock) node;

    lockExpr = (NExpression) loader.load(statement.getLockExpr());
    catchBlockIds = loader.getIds(loader.getCatchBlockSymbols(), statement.getJCatchBlocks());
    sourceInfo = loader.load(statement.getSourceInfo());
  }

  @Override
  @Nonnull
  public JUnlock exportAsJast(@Nonnull ExportSession exportSession) throws JMethodLookupException,
      JTypeLookupException {
    assert sourceInfo != null;
    assert lockExpr != null;
    JUnlock jStatement = new JUnlock(sourceInfo.exportAsJast(exportSession),
        lockExpr.exportAsJast(exportSession));
    for (String catchId : catchBlockIds) {
      exportSession.getCatchBlockResolver().addLink(catchId, new CatchBlockLinker(jStatement));
    }
    return jStatement;
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    lockExpr = in.readNode(NExpression.class);
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
