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

package com.android.jack.jayce.v0004.io;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

class Tokenizer {

  @Nonnull
  private final JayceInputStream in;

  @CheckForNull
  private Token currentTokenId;

  private boolean prefetched;

  @Nonnull
  private static final Token [] tokenMap = Token.values();

  public Tokenizer(@Nonnull InputStream in) {
    this.in = new JayceInputStream(in);
  }

  private void readNextToken() throws IOException {
    int token = in.readUnsignedByte();
    try {
      currentTokenId = tokenMap[token];
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new ParseException("Unknown token: " + token);
    }
  }

  @Nonnull
  public Token next() throws IOException {
    if (prefetched) {
      prefetched = false;
    } else {
      readNextToken();
    }
    assert currentTokenId != null;
    return currentTokenId;
  }

  public Token peekNext() throws IOException {
    if (!prefetched) {
      readNextToken();
      prefetched = true;
    }
    return currentTokenId;
  }

  public int readInt() throws IOException {
    currentTokenId = Token.NUMBER_VALUE;
    return in.readInt();
  }

  public byte readByte() throws IOException {
    currentTokenId = Token.NUMBER_VALUE;
    return in.readByte();
  }

  public boolean readBoolean() throws IOException {
    return in.readBoolean();
  }

  public long readLong() throws IOException {
    currentTokenId = Token.NUMBER_VALUE;
    return in.readLong();
  }

  public short readShort() throws IOException {
    currentTokenId = Token.NUMBER_VALUE;
    return in.readShort();
  }

  public char readChar() throws IOException {
    currentTokenId = Token.NUMBER_VALUE;
    return in.readChar();
  }

  public float readFloat() throws IOException {
    currentTokenId = Token.NUMBER_VALUE;
    return Float.intBitsToFloat(readInt());
  }

  public double readDouble() throws IOException {
    currentTokenId = Token.NUMBER_VALUE;
    return Double.longBitsToDouble(readLong());
  }

  @CheckForNull
  public String readString() throws IOException {
    return in.readUTF();
  }

  @CheckForNull
  public byte[] readBuffer() throws IOException {
    return in.readBuffer();
  }

  private void readToken(@Nonnull Token expected) throws IOException {
    next();
    if (currentTokenId != expected) {
      throw new ParseException("Unexpected token " + String.valueOf(currentTokenId)
          + " while token " + expected.toString() + " was expected");
    }
  }

  public void readOpen() {
  }

  public void readClose() throws IOException {
    readToken(Token.RPARENTHESIS);
  }

  /**
   * Reads the character that precedes the file name if present,
   * does nothing otherwise.
   * @return the character has been found and consumed.
   * @throws IOException
   */
  public boolean readOpenFileName() throws IOException {
    if (peekNext() == Token.SHARP) {
      readToken(Token.SHARP);
      return true;
    }
    return false;
  }

  public void readCloseFileName() {
  }

  /**
   * Reads the character that precedes the current line info if present,
   * does nothing otherwise.
   * @return the character has been found and consumed.
   * @throws IOException
   */
  public boolean readOpenLineInfo() throws IOException {
    if (peekNext() == Token.LBRACKET) {
      readToken(Token.LBRACKET);
      return true;
    }
    return false;
  }

  public void readCloseLineInfo() {
  }

  /**
   * Reads the token that precedes a catch block id if present,
   * does nothing otherwise.
   * @return the token has been found and consumed.
   * @throws IOException
   */
  public boolean readOpenCatchBlockIdAdd() throws IOException {
    if (peekNext() == Token.LCURLY_ADD) {
      readToken(Token.LCURLY_ADD);
      return true;
    }
    return false;
  }

  /**
   * Reads the token that precedes a catch block id if present,
   * does nothing otherwise.
   * @return the token has been found and consumed.
   * @throws IOException
   */
  public boolean readOpenCatchBlockIdRemove() throws IOException {
    if (peekNext() == Token.LCURLY_REMOVE) {
      readToken(Token.LCURLY_REMOVE);
      return true;
    }
    return false;
  }


  public void readCloseCatchBlockId() {
  }
}
