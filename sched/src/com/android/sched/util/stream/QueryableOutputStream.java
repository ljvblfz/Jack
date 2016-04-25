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

package com.android.sched.util.stream;

import java.io.BufferedReader;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * {@link BufferedReader} which supports {@link #isClosed()} method.
 */
public class QueryableOutputStream extends FilterOutputStream implements QueryableStream {
  public QueryableOutputStream(@Nonnull OutputStream out) {
    super(out);
  }

  private boolean closed = false;

  @Override
  public synchronized void close() throws IOException {
    out.close();
    closed = true;
  }

  @Override
  public synchronized boolean isClosed() {
    return closed;
  }

  @Override
  public void write(byte[] b) throws IOException {
    out.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
  }
}
