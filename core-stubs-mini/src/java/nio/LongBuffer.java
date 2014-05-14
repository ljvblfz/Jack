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

public abstract class LongBuffer extends java.nio.Buffer implements
    java.lang.Comparable<java.nio.LongBuffer> {
  LongBuffer() {
    throw new RuntimeException("Stub!");
  }

  public static java.nio.LongBuffer allocate(int capacity) {
    throw new RuntimeException("Stub!");
  }

  public static java.nio.LongBuffer wrap(long[] array) {
    throw new RuntimeException("Stub!");
  }

  public static java.nio.LongBuffer wrap(long[] array, int start, int longCount) {
    throw new RuntimeException("Stub!");
  }

  public final long[] array() {
    throw new RuntimeException("Stub!");
  }

  public final int arrayOffset() {
    throw new RuntimeException("Stub!");
  }

  public abstract java.nio.LongBuffer asReadOnlyBuffer();

  public abstract java.nio.LongBuffer compact();

  public int compareTo(java.nio.LongBuffer otherBuffer) {
    throw new RuntimeException("Stub!");
  }

  public abstract java.nio.LongBuffer duplicate();

  public boolean equals(java.lang.Object other) {
    throw new RuntimeException("Stub!");
  }

  public abstract long get();

  public java.nio.LongBuffer get(long[] dst) {
    throw new RuntimeException("Stub!");
  }

  public java.nio.LongBuffer get(long[] dst, int dstOffset, int longCount) {
    throw new RuntimeException("Stub!");
  }

  public abstract long get(int index);

  public final boolean hasArray() {
    throw new RuntimeException("Stub!");
  }

  public int hashCode() {
    throw new RuntimeException("Stub!");
  }

  public abstract boolean isDirect();

  public abstract java.nio.ByteOrder order();

  public abstract java.nio.LongBuffer put(long l);

  public final java.nio.LongBuffer put(long[] src) {
    throw new RuntimeException("Stub!");
  }

  public java.nio.LongBuffer put(long[] src, int srcOffset, int longCount) {
    throw new RuntimeException("Stub!");
  }

  public java.nio.LongBuffer put(java.nio.LongBuffer src) {
    throw new RuntimeException("Stub!");
  }

  public abstract java.nio.LongBuffer put(int index, long l);

  public abstract java.nio.LongBuffer slice();
}
