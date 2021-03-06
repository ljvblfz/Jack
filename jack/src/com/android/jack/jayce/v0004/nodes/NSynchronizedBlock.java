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

package com.android.jack.jayce.v0004.nodes;

import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JSynchronizedBlock;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.jayce.linker.CatchBlockLinker;
import com.android.jack.jayce.v0004.io.ExportSession;
import com.android.jack.jayce.v0004.io.ImportHelper;
import com.android.jack.jayce.v0004.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0004.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0004.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A synchronized block.
 */
public class NSynchronizedBlock extends NStatement {

  @Nonnull
  public static final Token TOKEN = Token.SYNCHRONIZED_BLOCK;

  @CheckForNull
  public NExpression lockExpr;

  @CheckForNull
  public NBlock synchronizedBlock;

  @Nonnull
  public List<String> catchBlockIds = Collections.emptyList();

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    JSynchronizedBlock jSynchronizedBlock = (JSynchronizedBlock) node;

    lockExpr = (NExpression) loader.load(jSynchronizedBlock.getLockExpr());
    synchronizedBlock = (NBlock) loader.load(jSynchronizedBlock.getSynchronizedBlock());
    catchBlockIds =
        loader.getIds(loader.getCatchBlockSymbols(), jSynchronizedBlock.getJCatchBlocks());
    sourceInfo = jSynchronizedBlock.getSourceInfo();
  }

  @Override
  @Nonnull
  public JSynchronizedBlock exportAsJast(@Nonnull ExportSession exportSession)
      throws JMethodLookupException, JTypeLookupException {
    assert sourceInfo != null;
    assert lockExpr != null;
    assert synchronizedBlock != null;
    JExpression jLockExpr = lockExpr.exportAsJast(exportSession);
    JBlock jBlock = synchronizedBlock.exportAsJast(exportSession);
    JSynchronizedBlock jSynchronizedBlock = new JSynchronizedBlock(sourceInfo, jLockExpr, jBlock);
    for (String catchId : catchBlockIds) {
      exportSession.getCatchBlockResolver()
          .addLink(catchId, new CatchBlockLinker(jSynchronizedBlock));
    }
    return jSynchronizedBlock;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeNode(lockExpr);
    out.writeNode(synchronizedBlock);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    lockExpr = in.readNode(NExpression.class);
    synchronizedBlock = in.readNode(NBlock.class);
  }

  public static void skipContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    in.readNode(NExpression.class);
    in.readNode(NBlock.class);
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
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
