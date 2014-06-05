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

public abstract class Charset implements java.lang.Comparable<java.nio.charset.Charset> {
  protected Charset(java.lang.String canonicalName, java.lang.String[] aliases) {
    throw new RuntimeException("Stub!");
  }

  public static java.util.SortedMap<java.lang.String, java.nio.charset.Charset> availableCharsets() {
    throw new RuntimeException("Stub!");
  }

  public static java.nio.charset.Charset forName(java.lang.String charsetName) {
    throw new RuntimeException("Stub!");
  }

  public static boolean isSupported(java.lang.String charsetName) {
    throw new RuntimeException("Stub!");
  }

  public abstract boolean contains(java.nio.charset.Charset charset);

  public abstract java.nio.charset.CharsetEncoder newEncoder();

  public abstract java.nio.charset.CharsetDecoder newDecoder();

  public final java.lang.String name() {
    throw new RuntimeException("Stub!");
  }

  public final java.util.Set<java.lang.String> aliases() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String displayName() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String displayName(java.util.Locale l) {
    throw new RuntimeException("Stub!");
  }

  public final boolean isRegistered() {
    throw new RuntimeException("Stub!");
  }

  public boolean canEncode() {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.ByteBuffer encode(java.nio.CharBuffer buffer) {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.ByteBuffer encode(java.lang.String s) {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.CharBuffer decode(java.nio.ByteBuffer buffer) {
    throw new RuntimeException("Stub!");
  }

  public final int compareTo(java.nio.charset.Charset charset) {
    throw new RuntimeException("Stub!");
  }

  public final boolean equals(java.lang.Object obj) {
    throw new RuntimeException("Stub!");
  }

  public final int hashCode() {
    throw new RuntimeException("Stub!");
  }

  public final java.lang.String toString() {
    throw new RuntimeException("Stub!");
  }

  public static java.nio.charset.Charset defaultCharset() {
    throw new RuntimeException("Stub!");
  }
}
