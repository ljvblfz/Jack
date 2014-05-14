/*
* Copyright (C) 2014 The Android Open Source Project
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

package java.io;

public abstract class Reader implements java.lang.Readable, java.io.Closeable {
  protected Reader() {
    throw new RuntimeException("Stub!");
  }

  protected Reader(java.lang.Object lock) {
    throw new RuntimeException("Stub!");
  }

  public abstract void close() throws java.io.IOException;

  public void mark(int readLimit) throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public boolean markSupported() {
    throw new RuntimeException("Stub!");
  }

  public int read() throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public int read(char[] buf) throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public abstract int read(char[] buf, int offset, int count) throws java.io.IOException;

  public boolean ready() throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public void reset() throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public long skip(long charCount) throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public int read(java.nio.CharBuffer target) throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  protected java.lang.Object lock;
}
