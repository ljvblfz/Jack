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

package com.android.jack.jayce.v0003.io;

import com.android.jack.util.StringUtils;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

/**
 * This class extends {@code DataInputStream} to add uleb128 support for int and long.
 */
class JayceInputStream implements DataInput {

  @Nonnull
  private final DataInputStream in;

  public JayceInputStream(InputStream in) {
    this.in = new DataInputStream(in);
  }

  @Override
  public void readFully(byte[] b) throws IOException {
    in.readFully(b);
  }

  @Override
  public void readFully(byte[] b, int off, int len) throws IOException {
    in.readFully(b, off, len);
  }

  @Override
  public int skipBytes(int n) throws IOException {
    return in.skipBytes(n);
  }

  @Override
  public boolean readBoolean() throws IOException {
    return in.readBoolean();
  }

  @Override
  public byte readByte() throws IOException {
    return in.readByte();
  }

  @Override
  public int readUnsignedByte() throws IOException {
    return in.readUnsignedByte();
  }

  @Override
  public short readShort() throws IOException {
    return in.readShort();
  }

  @Override
  public int readUnsignedShort() throws IOException {
    return in.readUnsignedShort();
  }

  @Override
  public char readChar() throws IOException {
    return in.readChar();
  }

  @Override
  public int readInt() throws IOException {
    int result = 0;
    int cur;
    int count = 0;
    int signBits = -1;

    do {
        cur = readByte();
        result |= (cur & 0x7f) << (count * 7);
        signBits <<= 7;
        count++;
    } while (((cur & 0x80) == 0x80) && count < 5);

    if ((cur & 0x80) == 0x80) {
        throw new ParseException("invalid LEB128 sequence");
    }

    // Sign extend if appropriate
    if (((signBits >> 1) & result) != 0) {
        result |= signBits;
    }

    return result;
  }

  @Override
  public long readLong() throws IOException {
    long result = 0;
    long cur;
    int count = 0;
    long signBits = -1;

    do {
        cur = readByte();
        result |= (cur & 0x7f) << (count * 7);
        signBits <<= 7;
        count++;
    } while (((cur & 0x80) == 0x80) && count < 10);

    if ((cur & 0x80) == 0x80) {
        throw new ParseException("invalid LEB128 sequence");
    }

    // Sign extend if appropriate
    if (((signBits >> 1) & result) != 0) {
        result |= signBits;
    }

    return result;
  }

  @Override
  public float readFloat() throws IOException {
    return in.readFloat();
  }

  @Override
  public double readDouble() throws IOException {
    return in.readDouble();
  }

  @Deprecated
  @Override
  public String readLine() throws IOException {
    return in.readLine();
  }

  @Override
  public String readUTF() throws IOException {
    int length = readInt();

    if (length == -1) {
      return null;
    }
    byte[] utf8String = new byte[length];
    in.readFully(utf8String);
    try {
      return StringUtils.utf8BytesToString(utf8String);
    } catch (java.text.ParseException e) {
      throw new ParseException(e);
    }
  }

  public byte[] readBuffer() throws IOException {
    int length = readInt();

    if (length == -1) {
      return null;
    }
    byte[] b = new byte[length];
    in.readFully(b);

    return b;
  }
}
