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

import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JTypeLookupException;
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
 * Represents the body of a method.
 */
public class NMethodBody extends NAbstractMethodBody {

  @Nonnull
  public static final Token TOKEN = Token.METHOD_BODY;

  @Nonnull
  private List<NLocal> locals = Collections.emptyList();
  @CheckForNull
  private NBlock block;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object jElement) {
    JMethodBody jMethodBody = (JMethodBody) jElement;
    locals = loader.load(NLocal.class, jMethodBody.getLocals());
    block = (NBlock) loader.load(jMethodBody.getBlock());
    sourceInfo = jMethodBody.getSourceInfo();
  }

  @Override
  @Nonnull
  public JMethodBody exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException {
    assert block != null;
    assert sourceInfo != null;
    JMethodBody jMethodBody = new JMethodBody(sourceInfo, block.exportAsJast(exportSession));
    for (NLocal local : locals) {
      JLocal jLocal = local.exportAsJast(exportSession);
      jLocal.setEnclosingMethodBody(jMethodBody);
      jMethodBody.addLocal(jLocal);
    }
    return jMethodBody;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeNodes(locals);
    out.writeNode(block);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    locals = in.readNodes(NLocal.class);
    block = in.readNode(NBlock.class);
  }

  public static void skipContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    in.skipNodes();
    in.skipNode();
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
