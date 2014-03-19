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

package com.android.jack.util;

import com.google.common.io.NullOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * Class that continuously read an {@link InputStream} and optionally could write the input in a
 * {@link OutputStream}.
 */
public class BytesStreamSucker implements Runnable {
  @Nonnull
  private final byte[] buffer = new byte[1024];

  @Nonnull
  private final InputStream is;

  @Nonnull
  private final OutputStream os;

  private final boolean toBeClose;

  public BytesStreamSucker(
      @Nonnull InputStream is, @Nonnull OutputStream os, boolean toBeClose) {
    this.is = is;
    this.os = os;
    this.toBeClose = toBeClose;
  }

  public BytesStreamSucker(@Nonnull InputStream is, @Nonnull OutputStream os) {
    this(is, os, false);
  }

  public BytesStreamSucker(@Nonnull InputStream is) {
    this(is, new NullOutputStream(), false);
  }

  @Override
  public void run() {
    try {
      int bytesRead;
      while ((bytesRead = is.read(buffer)) >= 0) {
        os.write(buffer, 0, bytesRead);
        os.flush();
      }
    } catch (Exception e) {
      // Best effort
    } finally {
      if (toBeClose) {
        try {
          os.close();
        } catch (IOException e) {
          // Best effort
        }
      }
    }
  }
}