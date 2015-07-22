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

import com.android.jack.ir.ast.marker.ThisRefTypeInfo;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0003.io.Token;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link NMarker} holds generic signature and source name retrieved from ecj.
 */
public class NThisRefTypeInfo extends NMarker {

  @Nonnull
  public static final Token TOKEN = Token.THIS_REF_TYPE_INFO;

  @CheckForNull
  public String genericSignature;


  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    ThisRefTypeInfo marker = (ThisRefTypeInfo) node;
    genericSignature = marker.getGenericSignature();
  }

  @Override
  @Nonnull
  public ThisRefTypeInfo exportAsJast(@Nonnull ExportSession exportSession) {
    assert genericSignature != null;
    ThisRefTypeInfo marker = new ThisRefTypeInfo(genericSignature);

    return marker;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeString(genericSignature);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    genericSignature = in.readString();
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
