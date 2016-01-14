/*
* Copyright (C) 2016 The Android Open Source Project
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

import com.android.jack.digest.OriginDigestMarker;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0003.io.Token;
import com.android.jack.jayce.v0003.util.OriginDigestDescriptorHelper;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link NMarker} holds digest on the origin.
 */
public class NOriginDigest extends NMarker {

  @Nonnull
  public static final Token TOKEN = Token.ORIGIN_DIGEST;

  private int descriptor;
  @CheckForNull
  private String algo;
  @CheckForNull
  private byte[] digest;
  @CheckForNull
  private String emitter;
  private int major;
  private int minor;

  @Override
  public void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node) {
    OriginDigestMarker marker = (OriginDigestMarker) node;
    algo = marker.getAlgo();
    digest = marker.getDigest();
    descriptor = OriginDigestDescriptorHelper.getInt(marker.getDescriptor());
    emitter = marker.getEmitterId();
    major = marker.getMajorCode();
    minor = marker.getMinorCode();
  }

  @Override
  @Nonnull
  public OriginDigestMarker exportAsJast(@Nonnull ExportSession exportSession) {
    assert algo != null;
    assert digest != null;
    assert emitter != null;

    return new OriginDigestMarker(OriginDigestDescriptorHelper.getValue(descriptor), algo, digest,
        emitter, major, minor);
  }

  @Override
  public void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException {
    assert digest != null;

    out.writeInt(descriptor);
    out.writeString(algo);
    out.writeBuffer(digest);
    out.writeString(emitter);
    out.writeInt(major);
    out.writeInt(minor);
  }

  @Override
  public void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException {
    descriptor = in.readInt();
    algo = in.readString();
    digest = in.readBuffer();
    emitter = in.readString();
    major = in.readInt();
    minor = in.readInt();
  }

  @Override
  @Nonnull
  public Token getToken() {
    return TOKEN;
  }
}
