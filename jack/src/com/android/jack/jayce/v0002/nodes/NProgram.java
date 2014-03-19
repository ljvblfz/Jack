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

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JProgram;
import com.android.jack.jayce.v0002.NNode;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Root representing an entire Java program.
 */
public class NProgram extends NNode {

  @Nonnull
  public static final Token TOKEN = Token.PROGRAM;

  @Nonnull
  public List<NDeclaredType> allTypes = Collections.emptyList();

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull  Object node) {
    JProgram jProgram = (JProgram) node;
    assert jProgram.codeGenTypes.isEmpty();
    allTypes = loader.load(NDeclaredType.class, jProgram.getTypesToEmit());
  }

  @Override
  @Nonnull
  public JProgram exportAsJast(@Nonnull ExportSession exportSession) {
    JProgram jProgram = new JProgram();
    for (NDeclaredType declaredType : allTypes) {
      JDefinedClassOrInterface jDeclaredType = declaredType.exportAsJast(exportSession);
      jProgram.addTypeToEmit(jDeclaredType);
    }
    return jProgram;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeNodes(allTypes);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    allTypes = in.readNodes(NDeclaredType.class);
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }

}
