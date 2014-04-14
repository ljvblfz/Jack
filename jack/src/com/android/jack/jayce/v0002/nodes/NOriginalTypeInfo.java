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

import com.android.jack.ir.ast.marker.OriginalTypeInfo;
import com.android.jack.jayce.v0002.io.ExportSession;
import com.android.jack.jayce.v0002.io.ImportHelper;
import com.android.jack.jayce.v0002.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0002.io.Token;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link NMarker} holds generic signature and source name retrieved from ecj.
 */
public class NOriginalTypeInfo extends NMarker {

  @Nonnull
  public static final Token TOKEN = Token.ORIGINAL_TYPE_INFO;

  @CheckForNull
  public String genericSignature;

  @CheckForNull
  public String sourceName;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    OriginalTypeInfo marker = (OriginalTypeInfo) node;
    genericSignature = marker.getGenericSignature();
    sourceName = marker.getSourceName();
  }

  @Override
  @Nonnull
  public OriginalTypeInfo exportAsJast(@Nonnull ExportSession exportSession) {
    OriginalTypeInfo marker = new OriginalTypeInfo();
    if (genericSignature != null) {
      marker.setGenericSignature(genericSignature);
    }
    if (sourceName != null) {
      marker.setSourceName(sourceName);
    }

    return marker;
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    out.writeString(genericSignature);
    out.writeString(sourceName);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    genericSignature = in.readString();
    sourceName = in.readString();
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
