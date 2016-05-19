/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.sched.util.stream;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * Class that continuously read an {@link InputStream} and optionally could write the input in a
 * {@link OutputStream}.
 */
public class ByteStreamSucker {

  private static final int BUFFER_SIZE = 4096;

  @Nonnull
  private final byte[] buffer = new byte[BUFFER_SIZE];

  @Nonnull
  private final InputStream is;

  @Nonnull
  private final OutputStream os;

  public ByteStreamSucker(@Nonnull InputStream is, @Nonnull OutputStream os) {
    this.is = is;
    this.os = os;
  }

  public ByteStreamSucker(@Nonnull InputStream is) {
    this(is, ByteStreams.nullOutputStream());
  }

  public void suck() throws IOException {
    int bytesRead;
    while ((bytesRead = is.read(buffer)) >= 0) {
      os.write(buffer, 0, bytesRead);
      os.flush();
    }
  }
}