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

package java.nio;

public abstract class ShortBuffer extends java.nio.Buffer implements
    java.lang.Comparable<java.nio.ShortBuffer> {
  ShortBuffer() {
    throw new RuntimeException("Stub!");
  }

  public static java.nio.ShortBuffer allocate(int capacity) {
    throw new RuntimeException("Stub!");
  }

  public static java.nio.ShortBuffer wrap(short[] array) {
    throw new RuntimeException("Stub!");
  }

  public static java.nio.ShortBuffer wrap(short[] array, int start, int shortCount) {
    throw new RuntimeException("Stub!");
  }

  public final short[] array() {
    throw new RuntimeException("Stub!");
  }

  public final int arrayOffset() {
    throw new RuntimeException("Stub!");
  }

  public abstract java.nio.ShortBuffer asReadOnlyBuffer();

  public abstract java.nio.ShortBuffer compact();

  public int compareTo(java.nio.ShortBuffer otherBuffer) {
    throw new RuntimeException("Stub!");
  }

  public abstract java.nio.ShortBuffer duplicate();

  public boolean equals(java.lang.Object other) {
    throw new RuntimeException("Stub!");
  }

  public abstract short get();

  public java.nio.ShortBuffer get(short[] dst) {
    throw new RuntimeException("Stub!");
  }

  public java.nio.ShortBuffer get(short[] dst, int dstOffset, int shortCount) {
    throw new RuntimeException("Stub!");
  }

  public abstract short get(int index);

  public final boolean hasArray() {
    throw new RuntimeException("Stub!");
  }

  public int hashCode() {
    throw new RuntimeException("Stub!");
  }

  public abstract boolean isDirect();

  public abstract java.nio.ByteOrder order();

  public abstract java.nio.ShortBuffer put(short s);

  public final java.nio.ShortBuffer put(short[] src) {
    throw new RuntimeException("Stub!");
  }

  public java.nio.ShortBuffer put(short[] src, int srcOffset, int shortCount) {
    throw new RuntimeException("Stub!");
  }

  public java.nio.ShortBuffer put(java.nio.ShortBuffer src) {
    throw new RuntimeException("Stub!");
  }

  public abstract java.nio.ShortBuffer put(int index, short s);

  public abstract java.nio.ShortBuffer slice();
}
