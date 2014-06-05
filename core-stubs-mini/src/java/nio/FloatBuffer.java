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

public abstract class FloatBuffer extends java.nio.Buffer implements
    java.lang.Comparable<java.nio.FloatBuffer> {
  FloatBuffer() {
    throw new RuntimeException("Stub!");
  }

  public static java.nio.FloatBuffer allocate(int capacity) {
    throw new RuntimeException("Stub!");
  }

  public static java.nio.FloatBuffer wrap(float[] array) {
    throw new RuntimeException("Stub!");
  }

  public static java.nio.FloatBuffer wrap(float[] array, int start, int floatCount) {
    throw new RuntimeException("Stub!");
  }

  public final float[] array() {
    throw new RuntimeException("Stub!");
  }

  public final int arrayOffset() {
    throw new RuntimeException("Stub!");
  }

  public abstract java.nio.FloatBuffer asReadOnlyBuffer();

  public abstract java.nio.FloatBuffer compact();

  public int compareTo(java.nio.FloatBuffer otherBuffer) {
    throw new RuntimeException("Stub!");
  }

  public abstract java.nio.FloatBuffer duplicate();

  public boolean equals(java.lang.Object other) {
    throw new RuntimeException("Stub!");
  }

  public abstract float get();

  public java.nio.FloatBuffer get(float[] dst) {
    throw new RuntimeException("Stub!");
  }

  public java.nio.FloatBuffer get(float[] dst, int dstOffset, int floatCount) {
    throw new RuntimeException("Stub!");
  }

  public abstract float get(int index);

  public final boolean hasArray() {
    throw new RuntimeException("Stub!");
  }

  public int hashCode() {
    throw new RuntimeException("Stub!");
  }

  public abstract boolean isDirect();

  public abstract java.nio.ByteOrder order();

  public abstract java.nio.FloatBuffer put(float f);

  public final java.nio.FloatBuffer put(float[] src) {
    throw new RuntimeException("Stub!");
  }

  public java.nio.FloatBuffer put(float[] src, int srcOffset, int floatCount) {
    throw new RuntimeException("Stub!");
  }

  public java.nio.FloatBuffer put(java.nio.FloatBuffer src) {
    throw new RuntimeException("Stub!");
  }

  public abstract java.nio.FloatBuffer put(int index, float f);

  public abstract java.nio.FloatBuffer slice();
}
