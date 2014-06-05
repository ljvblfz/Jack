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

package java.nio.charset;

public abstract class CharsetEncoder {
  protected CharsetEncoder(java.nio.charset.Charset cs, float averageBytesPerChar,
      float maxBytesPerChar) {
    throw new RuntimeException("Stub!");
  }

  protected CharsetEncoder(java.nio.charset.Charset cs, float averageBytesPerChar,
      float maxBytesPerChar, byte[] replacement) {
    throw new RuntimeException("Stub!");
  }

  public final float averageBytesPerChar() {
    throw new RuntimeException("Stub!");
  }

  public boolean canEncode(char c) {
    throw new RuntimeException("Stub!");
  }

  public boolean canEncode(java.lang.CharSequence sequence) {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.charset.Charset charset() {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.ByteBuffer encode(java.nio.CharBuffer in)
      throws java.nio.charset.CharacterCodingException {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.charset.CoderResult encode(java.nio.CharBuffer in, java.nio.ByteBuffer out,
      boolean endOfInput) {
    throw new RuntimeException("Stub!");
  }

  protected abstract java.nio.charset.CoderResult encodeLoop(java.nio.CharBuffer in,
      java.nio.ByteBuffer out);

  public final java.nio.charset.CoderResult flush(java.nio.ByteBuffer out) {
    throw new RuntimeException("Stub!");
  }

  protected java.nio.charset.CoderResult implFlush(java.nio.ByteBuffer out) {
    throw new RuntimeException("Stub!");
  }

  protected void implOnMalformedInput(java.nio.charset.CodingErrorAction newAction) {
    throw new RuntimeException("Stub!");
  }

  protected void implOnUnmappableCharacter(java.nio.charset.CodingErrorAction newAction) {
    throw new RuntimeException("Stub!");
  }

  protected void implReplaceWith(byte[] newReplacement) {
    throw new RuntimeException("Stub!");
  }

  protected void implReset() {
    throw new RuntimeException("Stub!");
  }

  public boolean isLegalReplacement(byte[] replacement) {
    throw new RuntimeException("Stub!");
  }

  public java.nio.charset.CodingErrorAction malformedInputAction() {
    throw new RuntimeException("Stub!");
  }

  public final float maxBytesPerChar() {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.charset.CharsetEncoder onMalformedInput(
      java.nio.charset.CodingErrorAction newAction) {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.charset.CharsetEncoder onUnmappableCharacter(
      java.nio.charset.CodingErrorAction newAction) {
    throw new RuntimeException("Stub!");
  }

  public final byte[] replacement() {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.charset.CharsetEncoder replaceWith(byte[] replacement) {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.charset.CharsetEncoder reset() {
    throw new RuntimeException("Stub!");
  }

  public java.nio.charset.CodingErrorAction unmappableCharacterAction() {
    throw new RuntimeException("Stub!");
  }
}
