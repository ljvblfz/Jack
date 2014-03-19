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

package com.android.jack.jayce.v0002.io;

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

  /**
   * Converts an array of UTF-8 bytes into a string.
   *
   * @param bytes {@code non-null;} the bytes to convert
   * @return {@code non-null;} the converted string
   * @throws ParseException
   */
  @Nonnull
  private static String utf8BytesToString(@Nonnull byte [] bytes) throws ParseException {
      int length = bytes.length;
      char[] chars = new char[length]; // This is sized to avoid a realloc.
      int outAt = 0;

      for (int at = 0; length > 0; /*at*/) {
          int v0 = 0x000000FF & bytes[at];
          char out;
          switch (v0 >> 4) {
              case 0x00: case 0x01: case 0x02: case 0x03:
              case 0x04: case 0x05: case 0x06: case 0x07: {
                  // 0XXXXXXX -- single-byte encoding
                  length--;
                  if (v0 == 0) {
                      // A single zero byte is illegal.
                      throw new ParseException("Invalid string value");
                  }
                  out = (char) v0;
                  at++;
                  break;
              }
              case 0x0c: case 0x0d: {
                  // 110XXXXX -- two-byte encoding
                  length -= 2;
                  if (length < 0) {
                      throw new ParseException("Invalid string value");
                  }
                  int v1 = 0x000000FF & bytes[at + 1];
                  if ((v1 & 0xc0) != 0x80) {
                      throw new ParseException("Invalid string value");
                  }
                  int value = ((v0 & 0x1f) << 6) | (v1 & 0x3f);
                  if ((value != 0) && (value < 0x80)) {
                      /*
                       * This should have been represented with
                       * one-byte encoding.
                       */
                      throw new ParseException("Invalid string value");
                  }
                  out = (char) value;
                  at += 2;
                  break;
              }
              case 0x0e: {
                  // 1110XXXX -- three-byte encoding
                  length -= 3;
                  if (length < 0) {
                      throw new ParseException("Invalid string value");
                  }
                  int v1 = 0x000000FF & bytes[at + 1];
                  if ((v1 & 0xc0) != 0x80) {
                      throw new ParseException("Invalid string value");
                  }
                  int v2 = 0x000000FF & bytes[at + 2];
                  if ((v1 & 0xc0) != 0x80) {
                      throw new ParseException("Invalid string value");
                  }
                  int value = ((v0 & 0x0f) << 12) | ((v1 & 0x3f) << 6) |
                      (v2 & 0x3f);
                  if (value < 0x800) {
                      /*
                       * This should have been represented with one- or
                       * two-byte encoding.
                       */
                      throw new ParseException("Invalid string value");
                  }
                  out = (char) value;
                  at += 3;
                  break;
              }
              default: {
                  // 10XXXXXX, 1111XXXX -- illegal
                  throw new ParseException("Invalid string value");
              }
          }
          chars[outAt] = out;
          outAt++;
      }

      return new String(chars, 0, outAt);
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
    return utf8BytesToString(utf8String);
  }
}
