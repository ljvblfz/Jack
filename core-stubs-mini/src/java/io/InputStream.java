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

public abstract class InputStream implements java.io.Closeable {
  public InputStream() {
    throw new RuntimeException("Stub!");
  }

  public int available() throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public void close() throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public void mark(int readlimit) {
    throw new RuntimeException("Stub!");
  }

  public boolean markSupported() {
    throw new RuntimeException("Stub!");
  }

  public abstract int read() throws java.io.IOException;

  public int read(byte[] buffer) throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public int read(byte[] buffer, int offset, int length) throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public synchronized void reset() throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public long skip(long byteCount) throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }
}
