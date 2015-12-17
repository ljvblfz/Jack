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

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.marker.ThrownExceptionMarker;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0003.io.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This {@link NMarker} holds the thrown exception names.
 */
public class NThrownExceptionMarker extends NMarker {

  @Nonnull
  public static final Token TOKEN = Token.THROWN_EXCEPTION;

  @Nonnull
  public List<String> thrownExceptions = Collections.emptyList();

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    ThrownExceptionMarker marker = (ThrownExceptionMarker) node;
    thrownExceptions = ImportHelper.getSignatureNameList(marker.getThrownExceptions());
  }

  @Override
  @Nonnull
  public ThrownExceptionMarker exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException {
    List<JClass> jThrownExceptions = new ArrayList<JClass>();
    for (String exceptionName : thrownExceptions) {
      jThrownExceptions.add(exportSession.getLookup().getClass(exceptionName));
    }
    return new ThrownExceptionMarker(jThrownExceptions);
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeIds(thrownExceptions);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    thrownExceptions = in.readIds();
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
