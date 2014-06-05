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

public abstract class OutputStream implements java.io.Closeable, java.io.Flushable {
  public OutputStream() {
    throw new RuntimeException("Stub!");
  }

  public void close() throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public void flush() throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public void write(byte[] buffer) throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public void write(byte[] buffer, int offset, int count) throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public abstract void write(int oneByte) throws java.io.IOException;
}
