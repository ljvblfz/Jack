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

public abstract class IntBuffer extends java.nio.Buffer implements
    java.lang.Comparable<java.nio.IntBuffer> {
  IntBuffer() {
    throw new RuntimeException("Stub!");
  }

  public static java.nio.IntBuffer allocate(int capacity) {
    throw new RuntimeException("Stub!");
  }

  public static java.nio.IntBuffer wrap(int[] array) {
    throw new RuntimeException("Stub!");
  }

  public static java.nio.IntBuffer wrap(int[] array, int start, int intCount) {
    throw new RuntimeException("Stub!");
  }

  public final int[] array() {
    throw new RuntimeException("Stub!");
  }

  public final int arrayOffset() {
    throw new RuntimeException("Stub!");
  }

  public abstract java.nio.IntBuffer asReadOnlyBuffer();

  public abstract java.nio.IntBuffer compact();

  public int compareTo(java.nio.IntBuffer otherBuffer) {
    throw new RuntimeException("Stub!");
  }

  public abstract java.nio.IntBuffer duplicate();

  public boolean equals(java.lang.Object other) {
    throw new RuntimeException("Stub!");
  }

  public abstract int get();

  public java.nio.IntBuffer get(int[] dst) {
    throw new RuntimeException("Stub!");
  }

  public java.nio.IntBuffer get(int[] dst, int dstOffset, int intCount) {
    throw new RuntimeException("Stub!");
  }

  public abstract int get(int index);

  public final boolean hasArray() {
    throw new RuntimeException("Stub!");
  }

  public int hashCode() {
    throw new RuntimeException("Stub!");
  }

  public abstract boolean isDirect();

  public abstract java.nio.ByteOrder order();

  public abstract java.nio.IntBuffer put(int i);

  public final java.nio.IntBuffer put(int[] src) {
    throw new RuntimeException("Stub!");
  }

  public java.nio.IntBuffer put(int[] src, int srcOffset, int intCount) {
    throw new RuntimeException("Stub!");
  }

  public java.nio.IntBuffer put(java.nio.IntBuffer src) {
    throw new RuntimeException("Stub!");
  }

  public abstract java.nio.IntBuffer put(int index, int i);

  public abstract java.nio.IntBuffer slice();
}
