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

package com.android.jill.backend.jayce;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * This class extends {@code DataOutputStream} to add uleb128 support for int and long.
 */
class JayceOutputStream implements DataOutput {

  @Nonnull
  private final DataOutputStream out;

  public JayceOutputStream(@Nonnull OutputStream out) {
    this.out = new DataOutputStream(out);
  }

  /**
   * Converts a string into its Java-style UTF-8 form. Java-style UTF-8
   * differs from normal UTF-8 in the handling of character '\0' and
   * surrogate pairs.
   *
   * @param string {@code non-null;} the string to convert
   * @return {@code non-null;} the UTF-8 bytes for it
   */
  @Nonnull
  private static byte[] stringToUtf8Bytes(@Nonnull String string) {
      int len = string.length();
      byte[] bytes = new byte[len * 3]; // Avoid having to reallocate.
      int outAt = 0;

      for (int i = 0; i < len; i++) {
          char c = string.charAt(i);
          if ((c != 0) && (c < 0x80)) {
              bytes[outAt] = (byte) c;
              outAt++;
          } else if (c < 0x800) {
              bytes[outAt] = (byte) (((c >> 6) & 0x1f) | 0xc0);
              bytes[outAt + 1] = (byte) ((c & 0x3f) | 0x80);
              outAt += 2;
          } else {
              bytes[outAt] = (byte) (((c >> 12) & 0x0f) | 0xe0);
              bytes[outAt + 1] = (byte) (((c >> 6) & 0x3f) | 0x80);
              bytes[outAt + 2] = (byte) ((c & 0x3f) | 0x80);
              outAt += 3;
          }
      }

      byte[] result = new byte[outAt];
      System.arraycopy(bytes, 0, result, 0, outAt);
      return result;
  }

  @Override
  public void write(int b) throws IOException {
    out.write(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    out.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
  }

  @Override
  public void writeBoolean(boolean v) throws IOException {
    out.writeBoolean(v);
  }

  @Override
  public void writeByte(int v) throws IOException {
    out.writeByte(v);
  }

  @Override
  public void writeShort(int v) throws IOException {
    out.writeShort(v);
  }

  @Override
  public void writeChar(int v) throws IOException {
    out.writeChar(v);
  }

  /**
   * Encode an integer value into uleb128 format and write it to the stream.
   */
  @Override
  public void writeInt(int v) throws IOException {
    int remaining = v >> 7;
    boolean hasMore = true;
    int end = ((v & Integer.MIN_VALUE) == 0) ? 0 : -1;

    while (hasMore) {
      hasMore = (remaining != end) || ((remaining & 1) != ((v >> 6) & 1));

      writeByte((byte) ((v & 0x7f) | (hasMore ? 0x80 : 0)));
      v = remaining;
      remaining >>= 7;
    }
  }
  /**
   * Encode a long value into uleb128 format and write it to the stream.
   */
  @Override
  public void writeLong(long v) throws IOException {
    long remaining = v >> 7;
    boolean hasMore = true;
    long end = ((v & Long.MIN_VALUE) == 0) ? 0 : -1;

    while (hasMore) {
      hasMore = (remaining != end) || ((remaining & 1) != ((v >> 6) & 1));

      writeByte((byte) ((v & 0x7f) | (hasMore ? 0x80 : 0)));
      v = remaining;
      remaining >>= 7;
    }
  }

  @Override
  public void writeFloat(float v) throws IOException {
    out.writeFloat(v);
  }

  @Override
  public void writeDouble(double v) throws IOException {
    out.writeDouble(v);
  }

  @Override
  public void writeBytes(String s) throws IOException {
    out.writeBytes(s);
  }

  @Override
  public void writeChars(String s) throws IOException {
    out.writeChars(s);
  }

  @Override
  public void writeUTF(String s) throws IOException {
    if (s == null) {
      writeInt(-1);
    } else {
      byte[] utf8String = stringToUtf8Bytes(s);
      writeInt(utf8String.length);
      write(utf8String);
    }
  }

  public void flush() throws IOException {
    out.flush();
  }

  public void close() throws IOException {
    out.close();
  }
}
